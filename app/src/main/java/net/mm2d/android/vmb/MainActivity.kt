/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import net.mm2d.android.vmb.R.*
import net.mm2d.android.vmb.data.Theme
import net.mm2d.android.vmb.dialog.EditStringDialog.ConfirmStringListener
import net.mm2d.android.vmb.dialog.SelectStringDialog.SelectStringListener
import net.mm2d.android.vmb.dialog.SelectThemeDialog
import net.mm2d.android.vmb.dialog.SelectThemeDialog.SelectThemeListener
import net.mm2d.android.vmb.settings.Settings
import java.util.*

/**
 * 起動後から表示されるActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainActivity : AppCompatActivity(),
        SelectThemeListener, SelectStringListener, ConfirmStringListener {
    private val themes: ArrayList<Theme> = ArrayList()
    private val settings by lazy {
        Settings(this)
    }

    /**
     * MainFragmentを返す。
     *
     * @return MainFragment
     */
    private val mainFragment: MainFragment?
        get() {
            val fragment = supportFragmentManager.findFragmentById(id.fragment)
            return fragment as? MainFragment
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        setSupportActionBar(findViewById(id.toolbar))
        supportActionBar?.title = null
        initPreferences()
        makeThemes()
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = settings.screenOrientation
    }

    /**
     * テーマの選択肢を作成。
     */
    private fun makeThemes() {
        themes.add(Theme(getString(string.theme_white_black), Color.WHITE, Color.BLACK))
        themes.add(Theme(getString(string.theme_black_white), Color.BLACK, Color.WHITE))
        themes.add(Theme(getString(string.theme_black_yellow), Color.BLACK, Color.YELLOW))
        themes.add(Theme(getString(string.theme_black_green), Color.BLACK, Color.GREEN))
    }

    /**
     * アプリ設定の初期化。
     */
    private fun initPreferences() {
        PreferenceManager.setDefaultValues(this, xml.preferences, true)
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
        if (mainFragment?.hasHistory() == true) {
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
                mainFragment?.showHistoryDialog()
            R.id.action_clear_history ->
                AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_clear_history)
                        .setMessage(R.string.dialog_message_clear_history)
                        .setPositiveButton(R.string.ok) { _, _ -> mainFragment?.clearHistory() }
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
        mainFragment?.applyTheme()
    }

    override fun onSelectString(string: String) {
        mainFragment?.setText(string)
        if (settings.shouldShowEditorAfterSelect()) {
            mainFragment?.startEdit()
        }
    }

    override fun onConfirmString(string: String) {
        mainFragment?.setText(string)
    }
}
