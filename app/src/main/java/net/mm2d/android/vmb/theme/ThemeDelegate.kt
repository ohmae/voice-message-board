/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.theme

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.SelectThemeDialog
import net.mm2d.android.vmb.drawable.GridDrawable
import net.mm2d.android.vmb.settings.Settings
import java.util.*

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */

class ThemeDelegate(
    private val activity: FragmentActivity,
    private val root: View,
    private val textView: TextView,
    private val icon: Drawable?
) {
    private val themes = ArrayList(
        listOf(
            Theme(activity.getString(R.string.theme_white_black), Color.WHITE, Color.BLACK),
            Theme(activity.getString(R.string.theme_black_white), Color.BLACK, Color.WHITE),
            Theme(activity.getString(R.string.theme_black_yellow), Color.BLACK, Color.YELLOW),
            Theme(activity.getString(R.string.theme_black_green), Color.BLACK, Color.GREEN)
        )
    )
    private val settings = Settings.get()
    private val gridDrawable = GridDrawable(activity)

    /**
     * Preferenceを読みだして、テーマを設定する。
     */
    fun apply() {
        apply(settings.backgroundColor, settings.foregroundColor)
    }

    /**
     * テーマ設定。
     *
     * 背景色と文字色を設定するのみ。
     *
     * @param background 背景色
     * @param foreground 文字色
     */
    private fun apply(background: Int, foreground: Int) {
        gridDrawable.setColor(background)
        ViewCompat.setBackground(root, gridDrawable)
        root.invalidate()
        textView.setTextColor(foreground)
        icon?.let {
            DrawableCompat.setTint(DrawableCompat.wrap(it), getIconColor(background))
        }
    }

    fun select(theme: Theme) {
        settings.backgroundColor = theme.backgroundColor
        settings.foregroundColor = theme.foregroundColor
        apply()
    }

    /**
     * テーマ設定のダイアログを起動。
     */
    fun showDialog() {
        SelectThemeDialog.show(activity, themes)
    }

    companion object {
        @ColorInt
        private fun getIconColor(@ColorInt background: Int): Int =
            if (getBrightness(background) < 128) Color.WHITE else Color.BLACK

        private fun getBrightness(@ColorInt color: Int): Int =
            getBrightness(Color.red(color), Color.green(color), Color.blue(color))

        private fun getBrightness(r: Int, g: Int, b: Int): Int =
            MathUtils.clamp((r * 0.299 + g * 0.587 + b * 0.114 + 0.5).toInt(), 0, 255)
    }
}
