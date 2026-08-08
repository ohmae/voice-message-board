/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.font

import android.content.Context
import android.graphics.Typeface
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.settings.SettingsData
import net.mm2d.android.vmb.util.Toaster
import java.io.File

object FontUtils {
    fun isValidFontFile(
        file: File,
    ): Boolean =
        runCatching {
            Typeface.createFromFile(file) != Typeface.DEFAULT
        }.getOrNull() ?: false

    fun getFont(
        context: Context,
        settingsData: SettingsData,
        onInvalidFont: () -> Unit = {},
    ): Typeface {
        if (settingsData.fontPathToUse.isEmpty()) {
            return Typeface.DEFAULT
        }
        runCatching { Typeface.createFromFile(settingsData.fontPath) }.getOrNull()
            ?.let {
                return it
            }
        onInvalidFont()
        Toaster.show(context, R.string.toast_failed_to_load_font)
        return Typeface.DEFAULT
    }
}
