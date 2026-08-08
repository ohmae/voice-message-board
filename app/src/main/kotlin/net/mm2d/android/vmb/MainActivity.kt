/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.app.ShareCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import net.mm2d.android.vmb.dialog.EditStringDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.dialog.SelectThemeDialog
import net.mm2d.android.vmb.font.FontUtils
import net.mm2d.android.vmb.history.HistoryDelegate
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
    private lateinit var historyDelegate: HistoryDelegate
    private lateinit var voiceInputDelegate: VoiceInputDelegate
    private var fontSizeMin: Float = 0.0f
    private var fontSizeMax: Float = 0.0f
    private var typeface by mutableStateOf(android.graphics.Typeface.DEFAULT)

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        fontSizeMin = resources.getDimension(R.dimen.font_size_min)
        fontSizeMax = resources.getDimension(R.dimen.font_size_max)
        themeDelegate = ThemeDelegate(this, settings)
        historyDelegate = HistoryDelegate(this)
        voiceInputDelegate = VoiceInputDelegate(this) {
            viewModel.onEvent(UiEvent.UpdateText(it))
        }
        viewModel.onEvent(
            UiEvent.Initialize(
                initialText = getString(R.string.initial_string),
                initialFontSizePx = initialFontSize(),
            ),
        )
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AppTheme {
                MainScreen(
                    text = uiState.text,
                    fontSizePx = uiState.fontSizePx,
                    typeface = typeface,
                    backgroundColor = Color(uiState.settingsData.backgroundColor),
                    foregroundColor = Color(uiState.settingsData.foregroundColor),
                    showHistory = uiState.showHistory,
                    onTap = { viewModel.onEvent(UiEvent.TapText) },
                    onScale = { viewModel.onEvent(UiEvent.ScaleFont(it, fontSizeMin, fontSizeMax)) },
                    onEditClick = { viewModel.onEvent(UiEvent.ClickEdit) },
                    onHistoryClick = { viewModel.onEvent(UiEvent.ClickHistory) },
                    onSettingsClick = { viewModel.onEvent(UiEvent.ClickSettings) },
                    onThemeClick = { viewModel.onEvent(UiEvent.ClickTheme) },
                    onClearHistoryClick = { viewModel.onEvent(UiEvent.ClickClearHistory) },
                    onShareClick = { viewModel.onEvent(UiEvent.ClickShare) },
                )
            }
        }
        checkUpdate()
        EditStringDialog.registerListener(this, REQUEST_EDIT) {
            viewModel.onEvent(UiEvent.UpdateText(it))
        }
        SelectThemeDialog.registerListener(this, REQUEST_THEME) { theme ->
            lifecycleScope.launch {
                themeDelegate.select(theme)
            }
        }
        SelectStringDialog.registerListener(this, REQUEST_SELECT) {
            viewModel.onEvent(UiEvent.SelectText(it))
        }
        RecognizerDialog.registerListener(this, REQUEST_RECOGNIZE) {
            voiceInputDelegate.onRecognize(it)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        val settingsData = uiState.settingsData
                        historyDelegate.updateHistory(settingsData.history)
                        voiceInputDelegate.updateSettings(settingsData)
                        typeface = FontUtils.getFont(this@MainActivity, settingsData) {
                            lifecycleScope.launch {
                                settings.resetFont()
                            }
                        }
                        requestedOrientation = settingsData.screenOrientation
                    }
                }
                launch {
                    viewModel.uiEffect.collect(::handleEffect)
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

    private fun handleEffect(
        effect: UiEffect,
    ) {
        when (effect) {
            UiEffect.StartVoiceInput -> voiceInputDelegate.start()

            is UiEffect.ShowEditDialog -> EditStringDialog.show(this, REQUEST_EDIT, effect.text)

            UiEffect.ShowHistoryDialog -> historyDelegate.showSelectDialog()

            UiEffect.OpenSettings -> startActivity(Intent(this, SettingsActivity::class.java))

            UiEffect.ShowThemeDialog -> themeDelegate.showDialog()

            UiEffect.ShowClearHistoryDialog -> {
                historyDelegate.showClearDialog {
                    viewModel.onEvent(UiEvent.ClearHistory)
                }
            }

            is UiEffect.ShareText -> shareText(effect.text)
        }
    }

    private fun shareText(
        text: String,
    ) {
        ShareCompat.IntentBuilder(this)
            .setText(text)
            .setType("text/plain")
            .startChooser()
    }

    companion object {
        private const val REQUEST_PREFIX = "MainActivity:"
        const val REQUEST_EDIT = REQUEST_PREFIX + "REQUEST_EDIT"
        const val REQUEST_RECOGNIZE = REQUEST_PREFIX + "REQUEST_RECOGNIZE"
        const val REQUEST_SELECT = REQUEST_PREFIX + "REQUEST_SELECT"
        const val REQUEST_THEME = REQUEST_PREFIX + "REQUEST_THEME"
        private const val DAYS_FOR_UPDATE: Int = 2
    }
}
