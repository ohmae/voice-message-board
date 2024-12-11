/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object Toaster {
    fun show(
        context: Context?,
        @StringRes text: Int,
    ) {
        context ?: return
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}
