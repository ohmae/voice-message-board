/**
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 */

package net.mm2d.android.vmb;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * テーマを表現するクラス。
 *
 * Parcelableを実装し、引数として渡せるようにしている。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public class Theme implements Parcelable {
    private final String mName;
    private final int mBackground;
    private final int mForeground;

    /**
     * インスタンス作成。
     *
     * @param name 名前
     * @param background 背景色
     * @param foreground 文字色
     */
    public Theme(String name, int background, int foreground) {
        mName = name;
        mBackground = background;
        mForeground = foreground;
    }

    /**
     * 名前を返す。
     *
     * @return 名前
     */
    public String getName() {
        return mName;
    }

    /**
     * 背景色を返す。
     *
     * @return 背景色
     */
    public int getBackground() {
        return mBackground;
    }

    /**
     * 文字色を返す。
     *
     * @return 文字色
     */
    public int getForeground() {
        return mForeground;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mBackground);
        dest.writeInt(mForeground);
    }

    private Theme(Parcel source) {
        mName = source.readString();
        mBackground = source.readInt();
        mForeground = source.readInt();
    }

    public static final Parcelable.Creator<Theme> CREATOR = new Parcelable.Creator<Theme>() {
        @Override
        public Theme createFromParcel(Parcel source) {
            return new Theme(source);
        }

        @Override
        public Theme[] newArray(int size) {
            return new Theme[size];
        }
    };
}
