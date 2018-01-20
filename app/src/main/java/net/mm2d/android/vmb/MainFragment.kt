/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.annotation.ColorInt
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.math.MathUtils.clamp
import android.support.v4.view.ViewCompat
import android.util.TypedValue
import android.view.*
import kotlinx.android.synthetic.main.fragment_main.*
import net.mm2d.android.vmb.R.layout
import net.mm2d.android.vmb.dialog.EditStringDialog
import net.mm2d.android.vmb.dialog.PermissionDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog.RecognizeListener
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.drawable.GridDrawable
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.util.Toaster
import java.util.*

/**
 * テキストを大きく表示する画面。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainFragment : Fragment(), RecognizeListener {
    private lateinit var settings: Settings
    private lateinit var history: LinkedList<String>
    private lateinit var gridDrawable: GridDrawable
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleDetector: ScaleGestureDetector
    private var fontSizeMin: Float = 0.0f
    private var fontSizeMax: Float = 0.0f
    private var fontSize: Float = 0.0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ctx = context!!
        settings = Settings(ctx)
        gridDrawable = GridDrawable(ctx)
        scaleDetector = ScaleGestureDetector(ctx, ScaleListener())
        gestureDetector = GestureDetector(ctx, GestureListener())
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
        onRestoreInstanceState(savedInstanceState)
    }

    /**
     * Bundleがあればそこから、なければ初期値をViewに設定する。
     *
     * @param savedInstanceState State
     */
    private fun onRestoreInstanceState(savedInstanceState: Bundle?) {
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

    private fun startVoiceInput() {
        if (settings.shouldUseSpeechRecognizer()) {
            startRecognizerDialogWithPermission()
        } else {
            startRecognizerActivity()
        }
    }

    private fun startRecognizerDialogWithPermission() {
        val ctx = context ?: return
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
            return
        }
        startRecognizerDialog()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val act = activity ?: return
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(act, Manifest.permission.RECORD_AUDIO)) {
            Toaster.show(context, R.string.toast_should_allow_permission)
        } else {
            fragmentManager?.let {
                PermissionDialog.newInstance()
                        .showAllowingStateLoss(it, "")
            }
        }
    }

    private fun startRecognizerDialog() {
        fragmentManager?.let {
            val dialog = RecognizerDialog.newInstance()
            dialog.setTargetFragment(this, 0)
            dialog.showAllowingStateLoss(it, "")
        }
    }

    private fun startRecognizerActivity() {
        val ctx = context ?: return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.packageName)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.recognizer_title))
        }
        try {
            startActivityForResult(intent, RECOGNIZER_REQUEST_CODE)
        } catch (_: ActivityNotFoundException) {
            Toaster.show(context, R.string.toast_can_not_use_voice_input)
        }
    }

    override fun onRecognize(results: ArrayList<String>) {
        if (activity == null || results.isEmpty()) {
            return
        }
        if (results.size > 1 && settings.shouldShowCandidateList()) {
            SelectStringDialog.newInstance(R.string.dialog_title_select, results)
                    .show(fragmentManager, "")
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
    fun startEdit() {
        val string = textView.text.toString()
        EditStringDialog.newInstance(string)
                .show(fragmentManager, "")
    }

    fun showHistoryDialog() {
        if (history.isEmpty()) {
            return
        }
        SelectStringDialog.newInstance(R.string.dialog_title_history, ArrayList(history))
                .show(fragmentManager, "")
    }

    fun clearHistory() {
        history.clear()
        settings.history = HashSet(history)
        historyFab.hide()
    }

    fun hasHistory(): Boolean = !history.isEmpty()

    /**
     * 文字列を設定する。
     *
     * Activityからも設定できるようにpublic
     * onSaveInstanceStateでここで設定した文字列は保持される。
     *
     * @param string 表示する文字列
     */
    fun setText(string: String) {
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
    fun applyTheme() {
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

    @ColorInt
    private fun getIconColor(@ColorInt background: Int): Int =
            if (getBrightness(background) < 128) Color.WHITE else Color.BLACK

    private fun getBrightness(@ColorInt color: Int): Int =
            getBrightness(Color.red(color), Color.green(color), Color.blue(color))

    private fun getBrightness(r: Int, g: Int, b: Int): Int =
            clamp((r * 0.299 + g * 0.587 + b * 0.114 + 0.5).toInt(), 0, 255)

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
            fontSize = clamp(fontSize * factor, fontSizeMin, fontSizeMax)
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
    }
}
