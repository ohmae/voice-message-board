/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * テーマを表現するクラス。
 *
 * Parcelableを実装し、引数として渡せるようにしている。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public class Theme implements Parcelable {
    @NonNull
    private final String mName;
    private final int mBackgroundColor;
    private final int mForegroundColor;

    /**
     * インスタンス作成。
     *
     * @param name            名前
     * @param backgroundColor 背景色
     * @param foregroundColor 文字色
     */
    public Theme(
            @NonNull final String name,
            final int backgroundColor,
            final int foregroundColor) {
        mName = name;
        mBackgroundColor = backgroundColor;
        mForegroundColor = foregroundColor;
    }

    /**
     * 名前を返す。
     *
     * @return 名前
     */
    @NonNull
    public String getName() {
        return mName;
    }

    /**
     * 背景色を返す。
     *
     * @return 背景色
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * 文字色を返す。
     *
     * @return 文字色
     */
    public int getForegroundColor() {
        return mForegroundColor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(
            @NonNull final Parcel dest,
            final int flags) {
        dest.writeString(mName);
        dest.writeInt(mBackgroundColor);
        dest.writeInt(mForegroundColor);
    }

    private Theme(@NonNull final Parcel source) {
        mName = source.readString();
        mBackgroundColor = source.readInt();
        mForegroundColor = source.readInt();
    }

    /**
     * Parcelable用CREATOR
     */
    public static final Parcelable.Creator<Theme> CREATOR = new Parcelable.Creator<Theme>() {
        @Override
        @NonNull
        public Theme createFromParcel(@NonNull final Parcel source) {
            return new Theme(source);
        }

        @Override
        @NonNull
        public Theme[] newArray(final int size) {
            return new Theme[size];
        }
    };
}
