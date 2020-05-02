/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.view.updatePadding
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.requestAppUpdateInfo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.mm2d.android.vmb.dialog.EditStringDialog
import net.mm2d.android.vmb.dialog.EditStringDialog.ConfirmStringListener
import net.mm2d.android.vmb.dialog.RecognizerDialog.RecognizeListener
import net.mm2d.android.vmb.dialog.SelectStringDialog.SelectStringListener
import net.mm2d.android.vmb.dialog.SelectThemeDialog.SelectThemeListener
import net.mm2d.android.vmb.font.FontUtils
import net.mm2d.android.vmb.history.HistoryDelegate
import net.mm2d.android.vmb.recognize.VoiceInputDelegate
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.theme.Theme
import net.mm2d.android.vmb.theme.ThemeDelegate
import net.mm2d.android.vmb.util.ViewUtils
import java.util.*

/**
 * 起動後から表示されるActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainActivity : AppCompatActivity(),
    SelectThemeListener, SelectStringListener, ConfirmStringListener, RecognizeListener {
    private val settings by lazy {
        Settings.get()
    }
    private lateinit var themeDelegate: ThemeDelegate
    private lateinit var historyDelegate: HistoryDelegate
    private lateinit var voiceInputDelegate: VoiceInputDelegate
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var showHistoryMenu: MenuItem
    private lateinit var clearHistoryMenu: MenuItem
    private var fontSizeMin: Float = 0.0f
    private var fontSizeMax: Float = 0.0f
    private var fontSize: Float = 0.0f
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
        scaleDetector = ScaleGestureDetector(this, ScaleListener())
        gestureDetector = GestureDetector(this, GestureListener())
        fontSizeMin = resources.getDimension(R.dimen.font_size_min)
        fontSizeMax = resources.getDimension(R.dimen.font_size_max)
        editFab.setOnClickListener { startEdit() }
        val listener = { _: View, event: MotionEvent ->
            gestureDetector.onTouchEvent(event)
            scaleDetector.onTouchEvent(event)
            false
        }
        scrollView.setOnTouchListener(listener)
        textView.setOnTouchListener(listener)
        themeDelegate = ThemeDelegate(this, scrollView, textView, toolbar?.overflowIcon)
        themeDelegate.apply()
        historyDelegate = HistoryDelegate(this, historyFab)
        voiceInputDelegate =
            VoiceInputDelegate(this, RECOGNIZER_REQUEST_CODE, PERMISSION_REQUEST_CODE) {
                setText(it)
            }
        restoreInstanceState(savedInstanceState)
        ViewUtils.execOnLayout(scrollView) {
            updatePadding()
        }
        checkUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    /**
     * Bundleがあればそこから、なければ初期値をViewに設定する。
     *
     * @param savedInstanceState State
     */
    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            // 画面幅に初期文字列が収まる大きさに調整
            val width = resources.displayMetrics.widthPixels
            val initialText = textView.text.toString()
            fontSize = if (initialText[0] <= '\u007e') {
                width.toFloat() / initialText.length * 2
            } else {
                width.toFloat() / initialText.length
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        } else {
            // テキストとフォントサイズを復元
            fontSize = savedInstanceState.getFloat(TAG_FONT_SIZE)
            val text = savedInstanceState.getString(TAG_TEXT)
            textView.text = text
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // テキストとフォントサイズを保存
        outState.putFloat(TAG_FONT_SIZE, fontSize)
        outState.putString(TAG_TEXT, textView.text.toString())
    }

    override fun onStart() {
        super.onStart()
        FontUtils.setFont(textView, settings)
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = settings.screenOrientation
        updatePadding()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ViewUtils.execOnLayout(scrollView) {
            updatePadding()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        voiceInputDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onRecognize(results: ArrayList<String>) {
        voiceInputDelegate.onRecognize(results)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        voiceInputDelegate.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkUpdate() {
        scope.launch {
            val manager = AppUpdateManagerFactory.create(applicationContext)
            val info = manager.requestAppUpdateInfo()
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && info.isImmediateUpdateAllowed) {
                val options = AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                manager.startUpdateFlow(info, this@MainActivity, options)
            }
        }
    }

    /**
     * テキストの編集を開始する。
     */
    private fun startEdit() {
        val string = textView.text.toString()
        EditStringDialog.show(this, string)
    }

    /**
     * 文字列を設定する。
     *
     * Activityからも設定できるようにpublic
     * onSaveInstanceStateでここで設定した文字列は保持される。
     *
     * @param string 表示する文字列
     */
    private fun setText(string: String) {
        textView.text = string
        historyDelegate.put(string)
        scrollView.scrollTo(0, 0)
        updatePadding()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        showHistoryMenu = menu.findItem(R.id.action_show_history)
        clearHistoryMenu = menu.findItem(R.id.action_clear_history)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (historyDelegate.exist()) {
            showHistoryMenu.isEnabled = true
            clearHistoryMenu.isEnabled = true
        } else {
            showHistoryMenu.isEnabled = false
            clearHistoryMenu.isEnabled = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings ->
                startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_theme ->
                themeDelegate.showDialog()
            R.id.action_show_history ->
                historyDelegate.showSelectDialog()
            R.id.action_clear_history ->
                historyDelegate.showClearDialog()
            R.id.action_share ->
                ShareCompat.IntentBuilder.from(this)
                    .setText(textView.text)
                    .setType("text/plain")
                    .startChooser()
            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onSelectTheme(theme: Theme) {
        themeDelegate.select(theme)
    }

    override fun onSelectString(string: String) {
        setText(string)
        if (settings.shouldShowEditorAfterSelect()) {
            startEdit()
        }
    }

    override fun onConfirmString(string: String) {
        setText(string)
    }

    /**
     * タッチイベントをClickとLongClickに振り分ける。
     *
     * 直接OnClickを使うとピンチ時に反応するため、
     * GestureDetectorを利用する。
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            voiceInputDelegate.start()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            if (settings.shouldShowEditorWhenLongTap()) {
                startEdit()
            }
        }
    }

    /**
     * ピンチ操作でフォントサイズを調整する。
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor
            fontSize = (fontSize * factor).coerceIn(fontSizeMin, fontSizeMax)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            updatePadding()
            return true
        }
    }

    private fun updatePadding() {
        scrollView.post {
            val diff = scrollView.height - textView.height
            scrollView.updatePadding(top = if (diff > 0) diff / 2 else 0)
        }
    }

    companion object {
        private const val TAG_FONT_SIZE = "TAG_FONT_SIZE"
        private const val TAG_TEXT = "TAG_TEXT"
        private const val RECOGNIZER_REQUEST_CODE = 1
        private const val PERMISSION_REQUEST_CODE = 2
    }
}
