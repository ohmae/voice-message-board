/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.RecognizerIntent
import android.support.annotation.ColorInt
import android.support.v4.app.Fragment
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import android.widget.Toast

/**
 * テキストを大きく表示する画面。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
class MainFragment : Fragment() {
    private var fontSizeMin: Float = 0.0f
    private var fontSizeMax: Float = 0.0f
    private var fontSize: Float = 0.0f
    private lateinit var rootView: View
    private lateinit var textView: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var gridDrawable: GridDrawable
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleDetector: ScaleGestureDetector

    /**
     * DefaultSharedPreferencesを返す。
     *
     * @return DefaultSharedPreferences
     */
    private val defaultSharedPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(activity)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        view.findViewById<View>(R.id.fab).setOnClickListener { _ -> startEdit() }
        toolbar = view.findViewById(R.id.toolbar)
        textView = view.findViewById(R.id.textView)
        rootView = view.findViewById(R.id.root)
        rootView.setOnClickListener { v -> startVoiceInput() }
        rootView.setOnLongClickListener { v ->
            startEdit()
            true
        }
        rootView.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            scaleDetector.onTouchEvent(event)
            true
        }
        gridDrawable = GridDrawable(activity)
        scaleDetector = ScaleGestureDetector(activity, ScaleListener())
        gestureDetector = GestureDetector(activity, GestureListener())
        fontSizeMin = resources.getDimension(R.dimen.font_size_min)
        fontSizeMax = resources.getDimension(R.dimen.font_size_max)
        applyTheme()
        onRestoreInstanceState(savedInstanceState)
        return view
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
            if (initialText[0] <= '\u007e') {
                // 半角
                fontSize = width.toFloat() / initialText.length * 2
            } else {
                // 全角
                fontSize = width.toFloat() / initialText.length
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

    /**
     * 音声入力開始。
     */
    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        try {
            startActivityForResult(intent, REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_can_not_use_voice_input, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("TAG", "onActivityResult")
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // 音声入力の結果を反映
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (results.size > 1 && defaultSharedPreferences.getBoolean(Settings.CANDIDATE_LIST.name, false)) {
                SelectStringDialog.newInstance(results).show(fragmentManager, "")
            } else {
                setText(results[0])
            }
        }
    }

    /**
     * テキストの編集を開始する。
     */
    fun startEdit() {
        val string = textView.text.toString()
        EditStringDialog.newInstance(string).show(fragmentManager, "")
    }

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
    }

    /**
     * Preferenceを読みだして、テーマを設定する。
     */
    fun applyTheme() {
        val pref = defaultSharedPreferences
        val bg = pref.getInt(Settings.KEY_BACKGROUND.name, Color.WHITE)
        val fg = pref.getInt(Settings.KEY_FOREGROUND.name, Color.BLACK)
        setTheme(bg, fg)
    }

    /**
     * テーマ設定。
     *
     * 背景色と文字色を設定するのみ。
     *
     * @param background 背景色
     * @param foreground 文字色
     */
    private fun setTheme(
            background: Int,
            foreground: Int) {
        gridDrawable.setColor(background)
        ViewCompat.setBackground(rootView, gridDrawable)
        rootView.invalidate()
        textView.setTextColor(foreground)
        val icon = toolbar.overflowIcon
        if (icon != null) {
            DrawableCompat.setTint(DrawableCompat.wrap(icon), getIconColor(background))
        }
    }

    @ColorInt
    private fun getIconColor(@ColorInt background: Int): Int {
        return if (getBrightness(background) < 128) {
            Color.WHITE
        } else Color.BLACK
    }

    private fun getBrightness(@ColorInt color: Int): Int {
        return getBrightness(Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun getBrightness(r: Int, g: Int, b: Int): Int {
        return clamp((r * 0.299 + g * 0.587 + b * 0.114 + 0.5).toInt(), 0, 255)
    }

    /**
     * タッチイベントをClickとLongClickに振り分ける。
     *
     * 直接OnClickを使うとピンチ時に反応するため、
     * GestureDetectorを利用する。
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            rootView.performClick()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            if (defaultSharedPreferences.getBoolean(Settings.LONG_TAP_EDIT.name, false)) {
                rootView.performLongClick()
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

    /**
     * min以下はmin、max以上はmaxに飽和させる
     *
     * @param value 値
     * @param min   最小値
     * @param max   最大値
     * @return 飽和させた値
     */
    private fun clamp(value: Int, min: Int, max: Int): Int {
        return Math.min(Math.max(value, min), max)
    }

    /**
     * min以下はmin、max以上はmaxに飽和させる
     *
     * @param value 値
     * @param min   最小値
     * @param max   最大値
     * @return 飽和させた値
     */
    private fun clamp(value: Float, min: Float, max: Float): Float {
        return Math.min(Math.max(value, min), max)
    }

    companion object {
        private val TAG_FONT_SIZE = "TAG_FONT_SIZE"
        private val TAG_TEXT = "TAG_TEXT"
        private val REQUEST_CODE = 1
    }
}
