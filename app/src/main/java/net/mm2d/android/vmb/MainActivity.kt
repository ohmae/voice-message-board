/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import net.mm2d.android.vmb.databinding.ActivityMainBinding
import net.mm2d.android.vmb.dialog.EditStringDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.dialog.SelectThemeDialog
import net.mm2d.android.vmb.font.FontUtils
import net.mm2d.android.vmb.history.HistoryDelegate
import net.mm2d.android.vmb.recognize.VoiceInputDelegate
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.theme.ThemeDelegate
import net.mm2d.android.vmb.util.ViewUtils

class MainActivity : AppCompatActivity() {
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
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(left = systemBars.left, right = systemBars.right)
            binding.toolbar.updateLayoutParams<FrameLayout.LayoutParams> { topMargin = systemBars.top }
            binding.fabLayout.updateLayoutParams<FrameLayout.LayoutParams> { bottomMargin = systemBars.bottom }
            insets
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        scaleDetector = ScaleGestureDetector(this, ScaleListener())
        gestureDetector = GestureDetector(this, GestureListener())
        fontSizeMin = resources.getDimension(R.dimen.font_size_min)
        fontSizeMax = resources.getDimension(R.dimen.font_size_max)
        binding.editFab.setOnClickListener { startEdit() }
        val listener = { _: View, event: MotionEvent ->
            gestureDetector.onTouchEvent(event)
            scaleDetector.onTouchEvent(event)
            false
        }
        binding.scrollView.setOnTouchListener(listener)
        binding.textView.setOnTouchListener(listener)
        themeDelegate = ThemeDelegate(this, binding.scrollView, binding.textView, binding.toolbar.overflowIcon)
        themeDelegate.apply()
        historyDelegate = HistoryDelegate(this, binding.historyFab)
        voiceInputDelegate = VoiceInputDelegate(this, ::setText)
        restoreInstanceState(savedInstanceState)
        ViewUtils.execOnLayout(binding.scrollView) {
            updatePadding()
        }
        checkUpdate()
        EditStringDialog.registerListener(this, REQUEST_EDIT) {
            setText(it)
        }
        SelectThemeDialog.registerListener(this, REQUEST_THEME) {
            themeDelegate.select(it)
        }
        SelectStringDialog.registerListener(this, REQUEST_SELECT) {
            setText(it)
            if (settings.shouldShowEditorAfterSelect()) {
                startEdit()
            }
        }
        RecognizerDialog.registerListener(this, REQUEST_RECOGNIZE) {
            voiceInputDelegate.onRecognize(it)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            // 画面幅に初期文字列が収まる大きさに調整
            val width = resources.displayMetrics.widthPixels
            val initialText = binding.textView.text.toString()
            fontSize = if (initialText[0] <= '\u007e') {
                width.toFloat() / initialText.length * 2
            } else {
                width.toFloat() / initialText.length
            }
            binding.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        } else {
            // テキストとフォントサイズを復元
            fontSize = savedInstanceState.getFloat(TAG_FONT_SIZE)
            val text = savedInstanceState.getString(TAG_TEXT)
            binding.textView.text = text
            binding.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // テキストとフォントサイズを保存
        outState.putFloat(TAG_FONT_SIZE, fontSize)
        outState.putString(TAG_TEXT, binding.textView.text.toString())
    }

    override fun onStart() {
        super.onStart()
        FontUtils.setFont(binding.textView, settings)
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = settings.screenOrientation
        updatePadding()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ViewUtils.execOnLayout(binding.scrollView) {
            updatePadding()
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

    private fun startEdit() {
        val string = binding.textView.text.toString()
        EditStringDialog.show(this, REQUEST_EDIT, string)
    }

    private fun setText(string: String) {
        binding.textView.text = string
        historyDelegate.put(string)
        binding.scrollView.scrollTo(0, 0)
        updatePadding()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        showHistoryMenu = menu.findItem(R.id.action_show_history)
        clearHistoryMenu = menu.findItem(R.id.action_clear_history)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
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
                ShareCompat.IntentBuilder(this)
                    .setText(binding.textView.text)
                    .setType("text/plain")
                    .startChooser()

            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

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

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            fontSize = (fontSize * detector.scaleFactor).coerceIn(fontSizeMin, fontSizeMax)
            binding.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            updatePadding()
            return true
        }
    }

    private fun updatePadding() {
        binding.scrollView.post {
            val diff = binding.scrollView.height - binding.textView.height
            binding.scrollView.updatePadding(top = if (diff > 0) diff / 2 else 0)
        }
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
