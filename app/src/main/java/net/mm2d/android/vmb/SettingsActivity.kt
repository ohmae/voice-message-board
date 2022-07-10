/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mm2d.android.vmb.constant.Constants
import net.mm2d.android.vmb.customtabs.CustomTabsHelperHolder
import net.mm2d.android.vmb.font.FontUtils
import net.mm2d.android.vmb.settings.Key.Main
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.util.Toaster
import net.mm2d.android.vmb.util.registerForActivityResultWrapper
import java.io.File

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, SettingsFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class SettingsFragment : PreferenceFragmentCompat() {
    private val settings by lazy {
        Settings.get()
    }
    private lateinit var fontPathPreference: Preference
    private val fontChooserLauncher =
        registerForActivityResultWrapper(GetContent(), "*/*", ::onSelectFont)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = settings.preferenceDataSource
        addPreferencesFromResource(R.xml.preferences)
        bindPreference(Main.SCREEN_ORIENTATION_STRING)
        findPreference(Main.PLAY_STORE_SCREEN)?.setOnPreferenceClickListener {
            openUrl(Constants.MARKET_URL)
        }
        findPreference(Main.PRIVACY_POLICY_SCREEN)?.setOnPreferenceClickListener {
            openUrl(Constants.PRIVACY_POLICY_URL)
        }
        findPreference(Main.SOURCE_CODE_SCREEN)?.setOnPreferenceClickListener {
            openUrl(Constants.SOURCE_CODE_URL)
        }
        findPreference(Main.LICENSE_SCREEN)?.setOnPreferenceClickListener {
            LicenseActivity.start(requireContext())
            true
        }
        findPreference(Main.VERSION_NUMBER_SCREEN)?.summary = BuildConfig.VERSION_NAME
        setUpFontSetting()
    }

    private fun setUpFontSetting() {
        fontPathPreference = findPreference(Main.FONT_PATH_STRING)!!
        fontPathPreference.setOnPreferenceClickListener {
            fontChooserLauncher.launch()
            true
        }
        setFontName()
    }

    private fun setFontName() {
        val name = settings.fontName
        if (name.isEmpty()) fontPathPreference.setSummary(R.string.pref_description_font_path)
        else fontPathPreference.summary = name
    }

    private fun onSelectFont(uri: Uri?) {
        uri ?: return
        val context = requireContext()
        lifecycleScope.launchWhenCreated {
            val (path, name) = withContext(Dispatchers.IO) {
                prepareFontFile(context, uri)
            }
            settings.fontPath = path
            settings.fontName = name
            setFontName()
            if (path.isEmpty()) {
                Toaster.show(context, R.string.toast_not_a_valid_font)
            }
        }
    }

    private fun prepareFontFile(context: Context, uri: Uri): Pair<String, String> {
        val name: String = context.contentResolver
            .query(uri, null, null, null, null)
            ?.use {
                if (it.moveToFirst()) {
                    it.getStringOrNull(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                } else null
            } ?: return "" to ""

        val stream = context.contentResolver.openInputStream(uri) ?: return "" to ""
        val data = stream.use { it.readBytes() }
        val file = File(context.filesDir, "font").also {
            if (it.exists()) it.delete()
        }
        file.writeBytes(data)
        return if (FontUtils.isValidFontFile(file)) {
            file.absolutePath to name
        } else {
            file.delete()
            "" to ""
        }
    }

    private fun findPreference(key: Main): Preference? = super.findPreference(key.name)

    private fun bindPreference(key: Main) {
        val preference = findPreference(key) ?: return
        preference.setOnPreferenceChangeListener(this::bindPreference)
        val value = preferenceManager.preferenceDataStore?.getString(key.name, "") ?: ""
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
        CustomTabsHelperHolder.openUrl(requireContext(), url)
        return true
    }

    override fun onResume() {
        super.onResume()
        CustomTabsHelperHolder.mayLaunchUrl(
            listOf(
                Constants.PRIVACY_POLICY_URL,
                Constants.SOURCE_CODE_URL
            )
        )
    }
}
