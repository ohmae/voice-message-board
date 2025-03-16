/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.theme

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Theme(
    val name: String,
    val backgroundColor: Int,
    val foregroundColor: Int,
) : Parcelable
