/*
 * Copyright (c) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.RecognizerIntent
import android.support.annotation.ColorInt
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.math.MathUtils
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.*
import kotlinx.android.synthetic.main.activity_main.*
import net.mm2d.android.vmb.data.Theme
import net.mm2d.android.vmb.dialog.*
import net.mm2d.android.vmb.dialog.EditStringDialog.ConfirmStringListener
import net.mm2d.android.vmb.dialog.RecognizerDialog.RecognizeListener
import net.mm2d.android.vmb.dialog.SelectStringDialog.SelectStringListener
import net.mm2d.android.vmb.dialog.SelectThemeDialog.SelectThemeListener
import net.mm2d.android.vmb.drawable.GridDrawable
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.util.Toaster
import java.util.*

/**
 * 起動後から表示されるActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainActivity : AppCompatActivity(),
        SelectThemeListener, SelectStringListener, ConfirmStringListener, RecognizeListener {
    private val themes by lazy {
        ArrayList<Theme>().apply {
            add(Theme(getString(R.string.theme_white_black), Color.WHITE, Color.BLACK))
            add(Theme(getString(R.string.theme_black_white), Color.BLACK, Color.WHITE))
            add(Theme(getString(R.string.theme_black_yellow), Color.BLACK, Color.YELLOW))
            add(Theme(getString(R.string.theme_black_green), Color.BLACK, Color.GREEN))
        }
    }
    private val settings by lazy {
        Settings(this)
    }
    private lateinit var history: LinkedList<String>
    private lateinit var gridDrawable: GridDrawable
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleDetector: ScaleGestureDetector
    private var fontSizeMin: Float = 0.0f
    private var fontSizeMax: Float = 0.0f
    private var fontSize: Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
        initPreferences()
        gridDrawable = GridDrawable(this)
        scaleDetector = ScaleGestureDetector(this, ScaleListener())
        gestureDetector = GestureDetector(this, GestureListener())
        fontSizeMin = resources.getDimension(R.dimen.font_size_min)
        fontSizeMax = resources.getDimension(R.dimen.font_size_max)
        editFab.setOnClickListener { startEdit() }
        root.apply {
            setOnClickListener { startVoiceInput() }
            setOnLongClickListener {
                startEdit()
                true
            }
            setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                scaleDetector.onTouchEvent(event)
                true
            }
        }
        historyFab.setOnClickListener { showHistoryDialog() }
        applyTheme()
        history = LinkedList(settings.history)
        if (history.isEmpty()) {
            historyFab.hide()
        }
        restoreInstanceState(savedInstanceState)
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
        setFont()
    }

    private fun setFont() {
        if (!settings.useFont || settings.fontPath.isEmpty()) {
            textView.typeface = Typeface.DEFAULT
            return
        }
        try {
            val typeFace = Typeface.createFromFile(settings.fontPath)
            if (typeFace != null) {
                textView.setTypeface(typeFace, Typeface.NORMAL)
                return
            }
        } catch (e: Exception) {
        }
        settings.useFont = false
        settings.fontPath = ""
        textView.typeface = Typeface.DEFAULT
        Toaster.show(this, R.string.toast_failed_to_load_font)
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = settings.screenOrientation
    }

    private fun startVoiceInput() {
        if (settings.shouldUseSpeechRecognizer()) {
            startRecognizerDialogWithPermission()
        } else {
            startRecognizerActivity()
        }
    }

    private fun startRecognizerDialogWithPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
            return
        }
        startRecognizerDialog()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != PERMISSION_REQUEST_CODE || permissions.isEmpty()) {
            return
        }
        val index = permissions.indexOf(Manifest.permission.RECORD_AUDIO)
        if (index < 0 || index > grantResults.size) {
            return
        }
        if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
            startRecognizerDialog()
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            Toaster.show(this, R.string.toast_should_allow_microphone_permission)
        } else {
            supportFragmentManager?.let {
                PermissionDialog.newInstance(R.string.dialog_microphone_permission_message)
                        .showAllowingStateLoss(it, "")
            }
        }
    }

    private fun startRecognizerDialog() {
        supportFragmentManager?.let {
            RecognizerDialog.newInstance()
                    .showAllowingStateLoss(it, "")
        }
    }

    private fun startRecognizerActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.recognizer_title))
        }
        try {
            startActivityForResult(intent, RECOGNIZER_REQUEST_CODE)
        } catch (_: ActivityNotFoundException) {
            Toaster.show(this, R.string.toast_can_not_use_voice_input)
        }
    }

    override fun onRecognize(results: ArrayList<String>) {
        if (results.isEmpty()) {
            return
        }
        if (results.size > 1 && settings.shouldShowCandidateList()) {
            supportFragmentManager?.let {
                SelectStringDialog.newInstance(R.string.dialog_title_select, results)
                        .showAllowingStateLoss(it, "")
            }
        } else {
            setText(results[0])
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != RECOGNIZER_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }
        // 音声入力の結果を反映
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
        onRecognize(results)
    }

    /**
     * テキストの編集を開始する。
     */
    private fun startEdit() {
        val string = textView.text.toString()
        supportFragmentManager?.let {
            EditStringDialog.newInstance(string)
                    .showAllowingStateLoss(it, "")
        }
    }

    private fun showHistoryDialog() {
        if (history.isEmpty()) {
            return
        }
        supportFragmentManager?.let {
            SelectStringDialog.newInstance(R.string.dialog_title_history, ArrayList(history))
                    .showAllowingStateLoss(it, "")
        }
    }

    private fun clearHistory() {
        history.clear()
        settings.history = HashSet(history)
        historyFab.hide()
    }

    private fun hasHistory(): Boolean = !history.isEmpty()

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
        history.remove(string)
        history.addFirst(string)
        while (history.size > MAX_HISTORY) {
            history.removeLast()
        }
        settings.history = HashSet(history)
        historyFab.show()
    }

    /**
     * Preferenceを読みだして、テーマを設定する。
     */
    private fun applyTheme() {
        setTheme(settings.backgroundColor, settings.foregroundColor)
    }

    /**
     * テーマ設定。
     *
     * 背景色と文字色を設定するのみ。
     *
     * @param background 背景色
     * @param foreground 文字色
     */
    private fun setTheme(background: Int, foreground: Int) {
        gridDrawable.setColor(background)
        ViewCompat.setBackground(root, gridDrawable)
        root.invalidate()
        textView.setTextColor(foreground)
        val icon = toolbar.overflowIcon ?: return
        DrawableCompat.setTint(DrawableCompat.wrap(icon), getIconColor(background))
    }

    /**
     * アプリ設定の初期化。
     */
    private fun initPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)
    }

    private lateinit var showHistoryMenu: MenuItem
    private lateinit var clearHistoryMenu: MenuItem

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        showHistoryMenu = menu.findItem(R.id.action_show_history)
        clearHistoryMenu = menu.findItem(R.id.action_clear_history)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (hasHistory()) {
            showHistoryMenu.isEnabled = true
            clearHistoryMenu.isEnabled = true
        } else {
            showHistoryMenu.isEnabled = false
            clearHistoryMenu.isEnabled = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_settings ->
                startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_theme ->
                showThemeDialog()
            R.id.action_show_history ->
                showHistoryDialog()
            R.id.action_clear_history ->
                AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_clear_history)
                        .setMessage(R.string.dialog_message_clear_history)
                        .setPositiveButton(R.string.ok) { _, _ -> clearHistory() }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * テーマ設定のダイアログを起動。
     */
    private fun showThemeDialog() {
        SelectThemeDialog.newInstance(themes)
                .show(supportFragmentManager, "")
    }

    override fun onSelectTheme(theme: Theme) {
        settings.backgroundColor = theme.backgroundColor
        settings.foregroundColor = theme.foregroundColor
        applyTheme()
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
            root.performClick()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            if (settings.shouldShowEditorWhenLongTap()) {
                root.performLongClick()
            }
        }
    }

    /**
     * ピンチ操作でフォントサイズを調整する。
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor
            fontSize = MathUtils.clamp(fontSize * factor, fontSizeMin, fontSizeMax)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            return true
        }
    }

    companion object {
        private const val MAX_HISTORY = 30
        private const val TAG_FONT_SIZE = "TAG_FONT_SIZE"
        private const val TAG_TEXT = "TAG_TEXT"
        private const val RECOGNIZER_REQUEST_CODE = 1
        private const val PERMISSION_REQUEST_CODE = 2
        @ColorInt
        private fun getIconColor(@ColorInt background: Int): Int =
                if (getBrightness(background) < 128) Color.WHITE else Color.BLACK

        private fun getBrightness(@ColorInt color: Int): Int =
                getBrightness(Color.red(color), Color.green(color), Color.blue(color))

        private fun getBrightness(r: Int, g: Int, b: Int): Int =
                MathUtils.clamp((r * 0.299 + g * 0.587 + b * 0.114 + 0.5).toInt(), 0, 255)

    }
}
