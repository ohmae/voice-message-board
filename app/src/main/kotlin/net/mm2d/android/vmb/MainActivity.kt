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
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.app.ShareCompat
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
import net.mm2d.android.vmb.dialog.EditStringDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.dialog.SelectThemeDialog
import net.mm2d.android.vmb.font.FontUtils
import net.mm2d.android.vmb.history.HistoryDelegate
import net.mm2d.android.vmb.recognize.VoiceInputDelegate
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.settings.SettingsData
import net.mm2d.android.vmb.theme.ThemeDelegate
import net.mm2d.android.vmb.ui.main.MainScreen
import net.mm2d.android.vmb.ui.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var settings: Settings

    private var currentSettingsData by mutableStateOf(SettingsData())

    private lateinit var themeDelegate: ThemeDelegate
    private lateinit var historyDelegate: HistoryDelegate
    private lateinit var voiceInputDelegate: VoiceInputDelegate
    private var fontSizeMin: Float = 0.0f
    private var fontSizeMax: Float = 0.0f
    private var fontSize by mutableFloatStateOf(0.0f)
    private var displayedText by mutableStateOf("")
    private var typeface by mutableStateOf(android.graphics.Typeface.DEFAULT)

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        fontSizeMin = resources.getDimension(R.dimen.font_size_min)
        fontSizeMax = resources.getDimension(R.dimen.font_size_max)
        themeDelegate = ThemeDelegate(this, settings)
        historyDelegate = HistoryDelegate(this, settings)
        voiceInputDelegate = VoiceInputDelegate(this, ::setText)
        restoreInstanceState(savedInstanceState)
        setContent {
            AppTheme {
                MainScreen(
                    text = displayedText,
                    fontSizePx = fontSize,
                    typeface = typeface,
                    backgroundColor = Color(currentSettingsData.backgroundColor),
                    foregroundColor = Color(currentSettingsData.foregroundColor),
                    showHistory = historyDelegate.exist(),
                    onTap = voiceInputDelegate::start,
                    onScale = ::scaleFont,
                    onEditClick = ::startEdit,
                    onHistoryClick = historyDelegate::showSelectDialog,
                    onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
                    onThemeClick = themeDelegate::showDialog,
                    onClearHistoryClick = historyDelegate::showClearDialog,
                    onShareClick = ::shareText,
                )
            }
        }
        checkUpdate()
        EditStringDialog.registerListener(this, REQUEST_EDIT) {
            setText(it)
        }
        SelectThemeDialog.registerListener(this, REQUEST_THEME) { theme ->
            lifecycleScope.launch {
                themeDelegate.select(theme)
            }
        }
        SelectStringDialog.registerListener(this, REQUEST_SELECT) {
            setText(it)
            if (currentSettingsData.shouldShowEditorAfterSelect) {
                startEdit()
            }
        }
        RecognizerDialog.registerListener(this, REQUEST_RECOGNIZE) {
            voiceInputDelegate.onRecognize(it)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settings.settingsFlow.collect { data ->
                    currentSettingsData = data
                    historyDelegate.updateHistory(data)
                    voiceInputDelegate.updateSettings(data)
                    typeface = FontUtils.getFont(this@MainActivity, data) {
                        lifecycleScope.launch {
                            settings.resetFont()
                        }
                    }
                    requestedOrientation = data.screenOrientation
                }
            }
        }
    }

    private fun restoreInstanceState(
        savedInstanceState: Bundle?,
    ) {
        if (savedInstanceState == null) {
            // 画面幅に初期文字列が収まる大きさに調整
            val width = resources.displayMetrics.widthPixels
            val initialText = getString(R.string.initial_string)
            fontSize = if (initialText[0] <= '\u007e') {
                width.toFloat() / initialText.length * 2
            } else {
                width.toFloat() / initialText.length
            }
            displayedText = initialText
        } else {
            // テキストとフォントサイズを復元
            fontSize = savedInstanceState.getFloat(TAG_FONT_SIZE)
            displayedText = savedInstanceState.getString(TAG_TEXT).orEmpty()
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle,
    ) {
        super.onSaveInstanceState(outState)
        // テキストとフォントサイズを保存
        outState.putFloat(TAG_FONT_SIZE, fontSize)
        outState.putString(TAG_TEXT, displayedText)
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

    private fun startEdit() {
        EditStringDialog.show(this, REQUEST_EDIT, displayedText)
    }

    private fun setText(
        string: String,
    ) {
        displayedText = string
        lifecycleScope.launch {
            historyDelegate.put(string)
        }
    }

    private fun shareText() {
        ShareCompat.IntentBuilder(this)
            .setText(displayedText)
            .setType("text/plain")
            .startChooser()
    }

    private fun scaleFont(
        scaleFactor: Float,
    ) {
        fontSize = (fontSize * scaleFactor).coerceIn(fontSizeMin, fontSizeMax)
    }

    companion object {
        private const val REQUEST_PREFIX = "MainActivity:"
        const val REQUEST_EDIT = REQUEST_PREFIX + "REQUEST_EDIT"
        const val REQUEST_RECOGNIZE = REQUEST_PREFIX + "REQUEST_RECOGNIZE"
        const val REQUEST_SELECT = REQUEST_PREFIX + "REQUEST_SELECT"
        const val REQUEST_THEME = REQUEST_PREFIX + "REQUEST_THEME"
        private const val TAG_FONT_SIZE = "TAG_FONT_SIZE"
        private const val TAG_TEXT = "TAG_TEXT"
        private const val DAYS_FOR_UPDATE: Int = 2
    }
}
