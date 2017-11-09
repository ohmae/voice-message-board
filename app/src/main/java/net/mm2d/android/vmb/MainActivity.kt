/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import java.util.*

/**
 * 起動後から表示されるActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainActivity : AppCompatActivity(),
        SelectThemeDialog.SelectThemeListener,
        SelectStringDialog.SelectStringListener,
        EditStringDialog.ConfirmStringListener {
    private val themes: ArrayList<Theme> = ArrayList()

    /**
     * DefaultSharedPreferencesを返す。
     *
     * @return DefaultSharedPreferences
     */
    private val defaultSharedPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(this)

    /**
     * MainFragmentを返す。
     *
     * @return MainFragment
     */
    private val mainFragment: MainFragment?
        get() {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment)
            return fragment as? MainFragment
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = null
        initPreferences()
        makeThemes()
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = getOrientation()
    }

    private fun getOrientation(): Int {
        val value = defaultSharedPreferences.getString(Settings.SCREEN_ORIENTATION.name, null)
        if (value != null) {
            try {
                return Integer.parseInt(value)
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    /**
     * テーマの選択肢を作成。
     */
    private fun makeThemes() {
        themes.add(Theme(getString(R.string.theme_white_black), Color.WHITE, Color.BLACK))
        themes.add(Theme(getString(R.string.theme_black_white), Color.BLACK, Color.WHITE))
        themes.add(Theme(getString(R.string.theme_black_yellow), Color.BLACK, Color.YELLOW))
        themes.add(Theme(getString(R.string.theme_black_green), Color.BLACK, Color.GREEN))
    }

    /**
     * アプリ設定の初期化。
     */
    private fun initPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_settings ->
                startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_theme ->
                showThemeDialog()
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
        // 設定を保存する。
        defaultSharedPreferences.edit()
                .putInt(Settings.KEY_BACKGROUND.name, theme.backgroundColor)
                .putInt(Settings.KEY_FOREGROUND.name, theme.foregroundColor)
                .apply()
        mainFragment?.applyTheme()
    }

    override fun onSelectString(string: String) {
        mainFragment?.setText(string)
        // 継続して
        if (defaultSharedPreferences.getBoolean(Settings.LIST_EDIT.name, false)) {
            mainFragment?.startEdit()
        }
    }

    override fun onConfirmString(string: String) {
        mainFragment?.setText(string)
    }
}
