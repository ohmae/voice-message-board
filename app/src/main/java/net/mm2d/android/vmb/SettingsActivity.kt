/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import net.mm2d.android.vmb.customtabs.CustomTabsHelperHolder
import net.mm2d.android.vmb.font.FontUtils
import net.mm2d.android.vmb.settings.Key
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.util.Toaster
import java.io.File

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

class SettingsFragment : PreferenceFragmentCompat() {
    private val settings by lazy {
        Settings.get()
    }
    private lateinit var fontPathPreference: Preference
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
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
        findPreference(Key.LICENSE)?.setOnPreferenceClickListener(fun(_: Preference): Boolean {
            LicenseActivity.start(context ?: return true)
            return true
        })
        findPreference(Key.VERSION_NUMBER)?.summary = BuildConfig.VERSION_NAME
        setUpFontSetting()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
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
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).also {
            it.addCategory(Intent.CATEGORY_OPENABLE)
            it.type = "font/*"
        }
        startActivityForResult(intent, FONT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != FONT_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }
        val uri = data?.data ?: return
        val context = requireContext()
        scope.launch {
            val path = copyToLocal(context, uri)
            withContext(Dispatchers.Main) {
                settings.fontPath = path
                setFontPath()
                if (path.isEmpty()) {
                    Toaster.show(context, R.string.toast_failed_to_load_font)
                }
            }
        }
    }

    private fun copyToLocal(context: Context, uri: Uri): String {
        val name: String = context.contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            } else null
        } ?: return ""

        val stream = context.contentResolver.openInputStream(uri) ?: return ""
        val data = stream.use { it.readBytes() }
        val file = File(context.filesDir, "font").also {
            if (it.exists()) it.delete()
        }
        file.writeBytes(data)
        return if (FontUtils.isValidFontFile(file)) {
            file.absolutePath
        } else {
            file.delete()
            ""
        }
    }

    private fun findPreference(key: Key): Preference? = super.findPreference(key.name)

    private fun bindPreference(preference: Preference?) {
        preference ?: return
        preference.setOnPreferenceChangeListener(this::bindPreference)
        val sp = PreferenceManager.getDefaultSharedPreferences(preference.context)
        val value = sp.getString(preference.key, "") ?: ""
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
        val ctx = context ?: return false
        CustomTabsHelperHolder.openUrl(ctx, url)
        return true
    }

    override fun onResume() {
        super.onResume()
        CustomTabsHelperHolder.mayLaunchUrl(
            listOf(
                PRIVACY_POLICY_URL,
                SOURCE_CODE_URL
            )
        )
    }

    companion object {
        private const val MARKET_URL = "market://details?id=net.mm2d.android.vmb"
        private const val SOURCE_CODE_URL = "https://github.com/ohmae/voice-message-board"
        private const val PRIVACY_POLICY_URL =
            "https://github.com/ohmae/voice-message-board/blob/develop/PRIVACY-POLICY.md"
        private const val FONT_REQUEST_CODE = 1
    }
}
