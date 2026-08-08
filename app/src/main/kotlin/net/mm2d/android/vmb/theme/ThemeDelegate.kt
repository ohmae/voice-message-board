/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.theme

import android.graphics.Color
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.MainActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.SelectThemeDialog
import net.mm2d.android.vmb.settings.Settings

class ThemeDelegate(
    private val activity: FragmentActivity,
    private val settings: Settings,
) {
    private val themes = arrayListOf(
        Theme(activity.getString(R.string.theme_white_black), Color.WHITE, Color.BLACK),
        Theme(activity.getString(R.string.theme_black_white), Color.BLACK, Color.WHITE),
        Theme(activity.getString(R.string.theme_black_yellow), Color.BLACK, Color.YELLOW),
        Theme(activity.getString(R.string.theme_black_green), Color.BLACK, Color.GREEN),
    )

    suspend fun select(
        theme: Theme,
    ) {
        settings.updateTheme(theme.backgroundColor, theme.foregroundColor)
    }

    fun showDialog() {
        SelectThemeDialog.show(activity, MainActivity.REQUEST_THEME, themes)
    }
}
