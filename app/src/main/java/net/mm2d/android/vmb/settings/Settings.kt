/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.annotation.ColorInt

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Settings private constructor(private val storage: SettingsStorage) {
    var backgroundColor: Int
        @ColorInt
        get() = storage.readInt(Key.KEY_BACKGROUND)
        set(@ColorInt color) = storage.writeInt(Key.KEY_BACKGROUND, color)

    var foregroundColor: Int
        @ColorInt
        get() = storage.readInt(Key.KEY_FOREGROUND)
        set(@ColorInt color) = storage.writeInt(Key.KEY_FOREGROUND, color)

    val screenOrientation: Int
        get() {
            val value = storage.readString(Key.SCREEN_ORIENTATION)
            if (value.isNotEmpty()) {
                try {
                    return Integer.parseInt(value)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

    var useFont: Boolean
        get() = storage.readBoolean(Key.USE_FONT)
        set(value) = storage.writeBoolean(Key.USE_FONT, value)

    var fontPath: String
        get() = storage.readString(Key.FONT_PATH)
        set(value) = storage.writeString(Key.FONT_PATH, value)

    val fontPathToUse: String
        get() = if (useFont) fontPath else ""

    fun shouldUseSpeechRecognizer(): Boolean =
        storage.readBoolean(Key.SPEECH_RECOGNIZER)

    fun shouldShowCandidateList(): Boolean =
        storage.readBoolean(Key.CANDIDATE_LIST)

    fun shouldShowEditorWhenLongTap(): Boolean =
        storage.readBoolean(Key.LONG_TAP_EDIT)

    fun shouldShowEditorAfterSelect(): Boolean =
        storage.readBoolean(Key.LIST_EDIT)

    var history: Set<String>
        get() = storage.readStringSet(Key.HISTORY)
        set(history) = storage.writeStringSet(Key.HISTORY, history)

    companion object {
        private lateinit var settings: Settings

        fun get(): Settings = settings

        fun initialize(context: Context) {
            val storage = SettingsStorage(context)
            Maintainer.maintain(storage)
            settings = Settings(storage)
        }
    }
}
