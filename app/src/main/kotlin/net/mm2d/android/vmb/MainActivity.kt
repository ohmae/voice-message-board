/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.SelectThemeDialog
import net.mm2d.android.vmb.recognize.VoiceInputDelegate
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.theme.ThemeDelegate
import net.mm2d.android.vmb.ui.main.MainScreen
import net.mm2d.android.vmb.ui.main.MainViewModel
import net.mm2d.android.vmb.ui.main.MainViewModel.UiEffect
import net.mm2d.android.vmb.ui.main.MainViewModel.UiEvent
import net.mm2d.android.vmb.ui.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var settings: Settings

    private val viewModel: MainViewModel by viewModels()

    private lateinit var themeDelegate: ThemeDelegate
    private lateinit var voiceInputDelegate: VoiceInputDelegate

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        themeDelegate = ThemeDelegate(this, settings)
        voiceInputDelegate = VoiceInputDelegate(this) {
            viewModel.onEvent(UiEvent.UpdateText(it))
        }
        viewModel.initialize(
            initialText = getString(R.string.initial_string),
            initialFontSizePx = initialFontSize(),
        )
        setContent {
            AppTheme {
                MainScreen(
                    onActivityEffect = ::handleActivityEffect,
                )
            }
        }
        checkUpdate()
        SelectThemeDialog.registerListener(this, REQUEST_THEME) { theme ->
            lifecycleScope.launch {
                themeDelegate.select(theme)
            }
        }
        RecognizerDialog.registerListener(this, REQUEST_RECOGNIZE) {
            voiceInputDelegate.onRecognize(it)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        voiceInputDelegate.updateSettings(
                            uiState.shouldUseSpeechRecognizer,
                            uiState.shouldShowCandidateList,
                        )
                        requestedOrientation = uiState.screenOrientation
                        WindowInsetsControllerCompat(window, window.decorView)
                            .isAppearanceLightStatusBars = uiState.backgroundColor.luminance() > 0.5f
                    }
                }
            }
        }
    }

    private fun initialFontSize(): Float {
        // 画面幅に初期文字列が収まる大きさに調整
        val width = resources.displayMetrics.widthPixels
        val initialText = getString(R.string.initial_string)
        return if (initialText[0] <= '\u007e') {
            width.toFloat() / initialText.length * 2
        } else {
            width.toFloat() / initialText.length
        }
    }

    private fun checkUpdate() {
        val manager = AppUpdateManagerFactory.create(applicationContext)
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.clientVersionStalenessDays.let { it != null && it >= DAYS_FOR_UPDATE } &&
                info.isImmediateUpdateAllowed
            ) {
                val options = AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                manager.startUpdateFlow(info, this, options)
            }
        }
    }

    private fun handleActivityEffect(
        effect: UiEffect,
    ) {
        when (effect) {
            UiEffect.StartVoiceInput -> voiceInputDelegate.start()
            UiEffect.OpenSettings -> Unit
            UiEffect.ShowThemeDialog -> themeDelegate.showDialog()
            is UiEffect.ShareText -> Unit
        }
    }

    companion object {
        private const val REQUEST_PREFIX = "MainActivity:"
        const val REQUEST_RECOGNIZE = REQUEST_PREFIX + "REQUEST_RECOGNIZE"
        const val REQUEST_SELECT = REQUEST_PREFIX + "REQUEST_SELECT"
        const val REQUEST_THEME = REQUEST_PREFIX + "REQUEST_THEME"
        private const val DAYS_FOR_UPDATE: Int = 2
    }
}
