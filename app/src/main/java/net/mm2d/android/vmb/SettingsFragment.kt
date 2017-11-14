/*
 * Copyright(C) 2017 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SettingsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        bindPreference(findPreference(Settings.SCREEN_ORIENTATION))
        findPreference(Settings.PLAY_STORE).setOnPreferenceClickListener {
            openUrl(MARKET_URL)
        }
        findPreference(Settings.PRIVACY_POLICY).setOnPreferenceClickListener {
            openUrl(PRIVACY_POLICY_URL)
        }
        findPreference(Settings.SOURCE_CODE).setOnPreferenceClickListener {
            openUrl(SOURCE_CODE_URL)
        }
        findPreference(Settings.LICENSE).setOnPreferenceClickListener {
            val intent = Intent(activity, LicenseActivity::class.java)
            startActivity(intent)
            true
        }
        findPreference(Settings.VERSION_NUMBER).summary = BuildConfig.VERSION_NAME
    }

    private fun findPreference(settings: Settings): Preference {
        return super.findPreference(settings.name)
    }

    /**
     * 設定結果を反映させるListenerとの接続。
     *
     * @param preference Preference
     */
    private fun bindPreference(preference: Preference) {
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
    }
}