/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.theme

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

/**
 * テーマを表現するクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 *
 * @constructor
 *
 * @param name            名前
 * @param backgroundColor 背景色
 * @param foregroundColor 文字色
 */
data class Theme(
    val name: String,
    val backgroundColor: Int,
    val foregroundColor: Int
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(backgroundColor)
        parcel.writeInt(foregroundColor)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Creator<Theme> {
        override fun createFromParcel(parcel: Parcel): Theme = create(parcel)
        override fun newArray(size: Int): Array<Theme?> = arrayOfNulls(size)
        private fun create(parcel: Parcel): Theme =
            Theme(parcel.readString() ?: "", parcel.readInt(), parcel.readInt())
    }
}
