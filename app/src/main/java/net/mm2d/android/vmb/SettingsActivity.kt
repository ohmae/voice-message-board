/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.preference.*
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.Preference.OnPreferenceClickListener

/**
 * 設定画面。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupSimplePreferencesScreen()
    }

    @Suppress("DEPRECATION")
    private fun setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return
        }
        addPreferencesFromResource(R.xml.pref_general)
        val fakeHeader = PreferenceCategory(this)
        fakeHeader.setTitle(R.string.pref_header_information)
        preferenceScreen.addPreference(fakeHeader)
        addPreferencesFromResource(R.xml.pref_information)
        bindPreference(findPreference(Settings.SCREEN_ORIENTATION.name))
        findPreference(Settings.PLAY_STORE.name).onPreferenceClickListener = sPlayStoreClickListener
        findPreference(Settings.PRIVACY_POLICY.name).onPreferenceClickListener = sPrivacyPolicyClickListener
        findPreference(Settings.VERSION_NUMBER.name).summary = BuildConfig.VERSION_NAME
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return true
    }

    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this) && !isSimplePreferences(this)
    }

    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target)
        }
    }

    /**
     * 一般設定のFragment。
     */
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            bindPreference(findPreference(Settings.SCREEN_ORIENTATION.name))
        }
    }

    /**
     * 情報表示のFragment。
     */
    class InformationPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_information)
            findPreference(Settings.PLAY_STORE.name).onPreferenceClickListener = sPlayStoreClickListener
            findPreference(Settings.PRIVACY_POLICY.name).onPreferenceClickListener = sPrivacyPolicyClickListener
            findPreference(Settings.VERSION_NUMBER.name).summary = BuildConfig.VERSION_NAME
        }
    }

    companion object {
        private val ALWAYS_SIMPLE_PREFS = false

        /**
         * 画面サイズからXLARGE以上かを判定
         *
         * @param context コンテキスト
         * @return XLARGE以上ならtrue
         */
        private fun isXLargeTablet(context: Context): Boolean {
            val config = context.resources.configuration
            val layout = config.screenLayout
            val size = layout and Configuration.SCREENLAYOUT_SIZE_MASK
            return size >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }

        /**
         * マルチペイン判定
         *
         * @param context コンテキスト
         * @return マルチペインにしない場合true
         */
        private fun isSimplePreferences(context: Context): Boolean {
            return ALWAYS_SIMPLE_PREFS || !isXLargeTablet(context)
        }

        /**
         * 設定変更を検出してSummaryに反映するリスナー。
         */
        private val sSummaryListener = OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(stringValue)
                preference.summary = if (index >= 0) preference.entries[index] else null
            } else {
                preference.summary = stringValue
            }
            true
        }

        /**
         * Playストアへ飛ばすOnPreferenceClickListener。
         */
        private val sPlayStoreClickListener = OnPreferenceClickListener { preference ->
            val uri = Uri.parse("market://details?id=net.mm2d.android.vmb")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                preference.context.startActivity(intent)
            } catch (ignored: ActivityNotFoundException) {
            }
            true
        }

        /**
         * プライバシーポリシーへ飛ばすOnPreferenceClickListener。
         */
        private val sPrivacyPolicyClickListener = OnPreferenceClickListener { preference ->
            val uri = Uri.parse("https://github.com/ohmae/VoiceMessageBoard/blob/develop/PRIVACY-POLICY.md")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                preference.context.startActivity(intent)
            } catch (ignored: ActivityNotFoundException) {
            }
            true
        }

        /**
         * 設定結果を反映させるListenerとの接続。
         *
         * @param preference Preference
         */
        private fun bindPreference(preference: Preference) {
            preference.onPreferenceChangeListener = sSummaryListener
            val sp = PreferenceManager.getDefaultSharedPreferences(preference.context)
            val value = sp.getString(preference.key, "")
            sSummaryListener.onPreferenceChange(preference, value)
        }
    }
}
