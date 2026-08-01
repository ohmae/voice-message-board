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
import net.mm2d.android.vmb.settings.Key.Main

class Settings private constructor(
    private val preferences: Preferences<Main>,
) {
    var backgroundColor: Int
        @ColorInt
        get() = preferences.readInt(Main.BACKGROUND_INT, 0)
        set(@ColorInt color) = preferences.writeInt(Main.BACKGROUND_INT, color)

    var foregroundColor: Int
        @ColorInt
        get() = preferences.readInt(Main.FOREGROUND_INT, 0)
        set(@ColorInt color) = preferences.writeInt(Main.FOREGROUND_INT, color)

    var screenOrientationString: String
        get() = preferences.readString(Main.SCREEN_ORIENTATION_STRING, "-1")
        set(value) = preferences.writeString(Main.SCREEN_ORIENTATION_STRING, value)

    val screenOrientation: Int
        get() = screenOrientationString.toIntOrNull()
            ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    var useFont: Boolean
        get() = preferences.readBoolean(Main.USE_FONT_BOOLEAN, false)
        set(value) = preferences.writeBoolean(Main.USE_FONT_BOOLEAN, value)

    var fontPath: String
        get() = preferences.readString(Main.FONT_PATH_STRING, "")
        set(value) = preferences.writeString(Main.FONT_PATH_STRING, value)

    var fontName: String
        get() = preferences.readString(Main.FONT_NAME_STRING, "")
        set(value) = preferences.writeString(Main.FONT_NAME_STRING, value)

    val fontPathToUse: String
        get() = if (useFont) fontPath else ""

    var shouldUseSpeechRecognizer: Boolean
        get() = preferences.readBoolean(Main.SHOULD_USE_SPEECH_RECOGNIZER_BOOLEAN, true)
        set(value) = preferences.writeBoolean(Main.SHOULD_USE_SPEECH_RECOGNIZER_BOOLEAN, value)

    fun shouldUseSpeechRecognizer(): Boolean = shouldUseSpeechRecognizer

    var shouldShowCandidateList: Boolean
        get() = preferences.readBoolean(Main.SHOULD_SHOW_CANDIDATE_LIST_BOOLEAN, false)
        set(value) = preferences.writeBoolean(Main.SHOULD_SHOW_CANDIDATE_LIST_BOOLEAN, value)

    fun shouldShowCandidateList(): Boolean = shouldShowCandidateList

    var shouldShowEditorWhenLongTap: Boolean
        get() = preferences.readBoolean(Main.SHOULD_SHOW_EDITOR_WHEN_LONG_TAP_BOOLEAN, true)
        set(value) = preferences.writeBoolean(Main.SHOULD_SHOW_EDITOR_WHEN_LONG_TAP_BOOLEAN, value)

    fun shouldShowEditorWhenLongTap(): Boolean = shouldShowEditorWhenLongTap

    var shouldShowEditorAfterSelect: Boolean
        get() = preferences.readBoolean(Main.SHOULD_SHOW_EDITOR_BOOLEAN, false)
        set(value) = preferences.writeBoolean(Main.SHOULD_SHOW_EDITOR_BOOLEAN, value)

    fun shouldShowEditorAfterSelect(): Boolean = shouldShowEditorAfterSelect

    var history: Set<String>
        get() = preferences.readStringSet(Main.HISTORY_SET, emptySet())
        set(history) = preferences.writeStringSet(Main.HISTORY_SET, history)

    companion object {
        private lateinit var settings: Settings

        fun initialize(
            context: Context,
        ) {
            Preferences(context, Main::class).also {
                Maintainer.maintain(it)
                settings = Settings(it)
            }
        }

        fun get(): Settings = settings
    }
}
