/*
 * Copyright (c) 2017 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import net.mm2d.android.vmb.font.FontFileChooserActivity
import net.mm2d.android.vmb.settings.Key
import net.mm2d.android.vmb.settings.Settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SettingsFragment : PreferenceFragment() {
    private lateinit var settings: Settings
    private lateinit var fontPathPreference: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(activity)
        addPreferencesFromResource(R.xml.preferences)
        bindPreference(findPreference(Key.SCREEN_ORIENTATION))
        findPreference(Key.PLAY_STORE)?.setOnPreferenceClickListener {
            openUrl(MARKET_URL)
        }
        findPreference(Key.PRIVACY_POLICY)?.setOnPreferenceClickListener {
            openUrl(PRIVACY_POLICY_URL)
        }
        findPreference(Key.SOURCE_CODE)?.setOnPreferenceClickListener {
            openUrl(SOURCE_CODE_URL)
        }
        findPreference(Key.LICENSE)?.setOnPreferenceClickListener {
            val intent = Intent(activity, LicenseActivity::class.java)
            startActivity(intent)
            true
        }
        findPreference(Key.VERSION_NUMBER)?.summary = BuildConfig.VERSION_NAME
        setUpFontSetting()
    }

    private fun setUpFontSetting() {
        fontPathPreference = findPreference(Key.FONT_PATH)!!
        fontPathPreference.setOnPreferenceClickListener {
            startFontChooser()
            true
        }
        setFontPath()
    }

    private fun setFontPath() {
        val path = settings.fontPath
        if (path.isEmpty()) fontPathPreference.setSummary(R.string.pref_description_font_path)
        else fontPathPreference.summary = path
    }

    private fun startFontChooser() {
        val intent = FontFileChooserActivity.makeIntent(activity, settings.fontPath)
        startActivityForResult(intent, FONT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != FONT_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }
        settings.fontPath = data?.data?.path ?: ""
        setFontPath()
    }

    private fun findPreference(key: Key): Preference? = super.findPreference(key.name)

    /**
     * 設定結果を反映させるListenerとの接続。
     *
     * @param preference Preference
     */
    private fun bindPreference(preference: Preference?) {
        preference ?: return
        preference.setOnPreferenceChangeListener(this::bindPreference)
        val sp = PreferenceManager.getDefaultSharedPreferences(preference.context)
        val value = sp.getString(preference.key, "")
        bindPreference(preference, value)
    }

    private fun bindPreference(preference: Preference, value: Any): Boolean {
        val stringValue = value.toString()
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            preference.summary = if (index >= 0) preference.entries[index] else null
        } else {
            preference.summary = stringValue
        }
        return true
    }

    private fun openUrl(url: String): Boolean {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
            return true
        } catch (ignored: ActivityNotFoundException) {
        }
        return false
    }

    companion object {
        private const val MARKET_URL = "market://details?id=net.mm2d.android.vmb"
        private const val SOURCE_CODE_URL = "https://github.com/ohmae/VoiceMessageBoard"
        private const val PRIVACY_POLICY_URL = "https://github.com/ohmae/VoiceMessageBoard/blob/develop/PRIVACY-POLICY.md"
        private const val FONT_REQUEST_CODE = 1
    }
}