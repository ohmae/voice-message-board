/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.font

import android.graphics.Typeface
import android.widget.TextView
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

    fun setFont(
        textView: TextView,
        settingsData: SettingsData,
        onInvalidFont: () -> Unit = {},
    ) {
        if (settingsData.fontPathToUse.isEmpty()) {
            textView.typeface = Typeface.DEFAULT
            return
        }
        runCatching { Typeface.createFromFile(settingsData.fontPath) }.getOrNull()
            ?.let {
                textView.setTypeface(it, Typeface.NORMAL)
                return
            }
        onInvalidFont()
        textView.typeface = Typeface.DEFAULT
        Toaster.show(textView.context, R.string.toast_failed_to_load_font)
    }
}
