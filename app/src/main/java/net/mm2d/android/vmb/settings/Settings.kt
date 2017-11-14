/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.support.annotation.ColorInt
import android.text.TextUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Settings(context: Context) {

    private val mStorage: SettingsStorage = SettingsStorage(context)

    var backgroundColor: Int
        @ColorInt
        get() = mStorage.readInt(Key.KEY_BACKGROUND, Color.WHITE)
        set(@ColorInt color) = mStorage.writeInt(Key.KEY_BACKGROUND, color)

    var foregroundColor: Int
        @ColorInt
        get() = mStorage.readInt(Key.KEY_FOREGROUND, Color.WHITE)
        set(@ColorInt color) = mStorage.writeInt(Key.KEY_FOREGROUND, color)

    val screenOrientation: Int
        get() {
            val value = mStorage.readString(Key.SCREEN_ORIENTATION, "")
            if (!TextUtils.isEmpty(value)) {
                try {
                    return Integer.parseInt(value)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

    fun shouldUseSpeechRecognizer(): Boolean {
        return mStorage.readBoolean(Key.SPEECH_RECOGNIZER, true)
    }

    fun shouldShowCandidateList(): Boolean {
        return mStorage.readBoolean(Key.CANDIDATE_LIST, false)
    }

    fun shouldShowEditorWhenLongTap(): Boolean {
        return mStorage.readBoolean(Key.LONG_TAP_EDIT, false)
    }

    fun shouldShowEditorAfterSelect(): Boolean {
        return mStorage.readBoolean(Key.LIST_EDIT, false)
    }

    companion object {
        /**
         * アプリ起動時に一度だけコールされ、初期化を行う。
         *
         * @param context コンテキスト
         */
        fun initialize(context: Context) {
            SettingsStorage.initialize(context)
        }
    }
}
