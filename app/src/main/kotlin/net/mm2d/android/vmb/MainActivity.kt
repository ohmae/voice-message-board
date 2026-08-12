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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.ui.main.MainScreen
import net.mm2d.android.vmb.ui.main.MainViewModel
import net.mm2d.android.vmb.ui.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var settings: Settings

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(
            initialText = getString(R.string.initial_string),
            initialFontSizeDp = initialFontSize(),
        )
        setContent {
            AppTheme {
                MainScreen()
            }
        }
        checkUpdate()

        viewModel.uiState
            .flowWithLifecycle(lifecycle)
            .onEach { uiState ->
                requestedOrientation = uiState.screenOrientation
                WindowInsetsControllerCompat(window, window.decorView)
                    .isAppearanceLightStatusBars = uiState.backgroundColor.luminance() > 0.5f
            }
            .launchIn(lifecycleScope)
    }

    private fun initialFontSize(): Dp {
        // 画面幅に初期文字列が収まる大きさに調整
        val density = resources.displayMetrics.density
        val width = resources.displayMetrics.widthPixels
        val initialText = getString(R.string.initial_string)
        return if (initialText[0] <= '\u007e') {
            (width.toFloat() / initialText.length * 2) / density
        } else {
            (width.toFloat() / initialText.length) / density
        }.dp
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

    companion object {
        private const val DAYS_FOR_UPDATE: Int = 2
    }
}
