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
import java.util.*

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
        get() = mStorage.readInt(Key.KEY_FOREGROUND, Color.BLACK)
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

    var useFont: Boolean
        get() = mStorage.readBoolean(Key.USE_FONT, false)
        set(value) = mStorage.writeBoolean(Key.USE_FONT, value)

    var fontPath: String
        get() = mStorage.readString(Key.FONT_PATH, "")!!
        set(value) = mStorage.writeString(Key.FONT_PATH, value)

    val fontPathToUse: String
        get() = if (useFont) fontPath else ""

    fun shouldUseSpeechRecognizer(): Boolean =
            mStorage.readBoolean(Key.SPEECH_RECOGNIZER, true)

    fun shouldShowCandidateList(): Boolean =
            mStorage.readBoolean(Key.CANDIDATE_LIST, false)

    fun shouldShowEditorWhenLongTap(): Boolean =
            mStorage.readBoolean(Key.LONG_TAP_EDIT, false)

    fun shouldShowEditorAfterSelect(): Boolean =
            mStorage.readBoolean(Key.LIST_EDIT, false)

    var history: Set<String>
        get() = mStorage.readStringSet(Key.HISTORY, null) ?: Collections.emptySet()
        set(history) = mStorage.writeStringSet(Key.HISTORY, history)

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
