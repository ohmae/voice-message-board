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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.viewModels
import androidx.core.database.getStringOrNull
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mm2d.android.vmb.constant.Constants
import net.mm2d.android.vmb.customtabs.CustomTabsHelperHolder
import net.mm2d.android.vmb.font.FontUtils
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.ui.settings.SettingsScreen
import net.mm2d.android.vmb.ui.settings.SettingsViewModel
import net.mm2d.android.vmb.ui.theme.AppTheme
import net.mm2d.android.vmb.util.Toaster
import net.mm2d.android.vmb.util.registerForActivityResultWrapper
import java.io.File

class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels()
    private val fontChooserLauncher =
        registerForActivityResultWrapper(GetContent(), "*/*", ::onSelectFont)

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                SettingsScreen(
                    onBackClick = { finish() },
                    onSelectFontClick = { fontChooserLauncher.launch() },
                    onOpenUrl = { url -> openUrl(url) },
                    onOpenLicense = { LicenseActivity.start(this) },
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun onSelectFont(
        uri: Uri?,
    ) {
        uri ?: return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                val (path, name) = withContext(Dispatchers.IO) {
                    prepareFontFile(this@SettingsActivity, uri)
                }
                val settings = Settings.get()
                settings.fontPath = path
                settings.fontName = name
                viewModel.updateFontName(name)
                if (path.isEmpty()) {
                    Toaster.show(this@SettingsActivity, R.string.toast_not_a_valid_font)
                } else {
                    recreate()
                }
            }
        }
    }

    private fun prepareFontFile(
        context: Context,
        uri: Uri,
    ): Pair<String, String> {
        val name: String = context.contentResolver
            .query(uri, null, null, null, null)
            ?.use {
                if (it.moveToFirst()) {
                    it.getStringOrNull(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                } else {
                    null
                }
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

    private fun openUrl(
        url: String,
    ) {
        CustomTabsHelperHolder.openUrl(this, url)
    }

    override fun onResume() {
        super.onResume()
        CustomTabsHelperHolder.mayLaunchUrl(
            listOf(
                Constants.PRIVACY_POLICY_URL,
                Constants.SOURCE_CODE_URL,
            ),
        )
    }
}
