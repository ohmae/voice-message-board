/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.os.Parcel
import android.os.Parcelable

/**
 * テーマを表現するクラス。
 *
 * Parcelableを実装し、引数として渡せるようにしている。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Theme : Parcelable {
    /**
     * 名前を返す。
     *
     * @return 名前
     */
    val name: String
    /**
     * 背景色を返す。
     *
     * @return 背景色
     */
    val backgroundColor: Int
    /**
     * 文字色を返す。
     *
     * @return 文字色
     */
    val foregroundColor: Int

    /**
     * インスタンス作成。
     *
     * @param name            名前
     * @param backgroundColor 背景色
     * @param foregroundColor 文字色
     */
    constructor(name: String, backgroundColor: Int, foregroundColor: Int) {
        this.name = name
        this.backgroundColor = backgroundColor
        this.foregroundColor = foregroundColor
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeInt(backgroundColor)
        dest.writeInt(foregroundColor)
    }

    private constructor(source: Parcel) {
        name = source.readString()
        backgroundColor = source.readInt()
        foregroundColor = source.readInt()
    }

    companion object {

        /**
         * Parcelable用CREATOR
         */
        @JvmField
        val CREATOR: Parcelable.Creator<Theme> = object : Parcelable.Creator<Theme> {
            override fun createFromParcel(source: Parcel): Theme {
                return Theme(source)
            }

            override fun newArray(size: Int): Array<Theme?> {
                return arrayOfNulls(size)
            }
        }
    }
}
