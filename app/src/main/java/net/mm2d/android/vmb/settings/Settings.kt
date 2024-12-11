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
import androidx.preference.PreferenceDataStore
import net.mm2d.android.vmb.settings.Key.Main

class Settings private constructor(
    private val preferences: Preferences<Main>,
) {
    val preferenceDataSource: PreferenceDataStore
        get() = preferences.dataStore

    var backgroundColor: Int
        @ColorInt
        get() = preferences.readInt(Main.BACKGROUND_INT, 0)
        set(@ColorInt color) = preferences.writeInt(Main.BACKGROUND_INT, color)

    var foregroundColor: Int
        @ColorInt
        get() = preferences.readInt(Main.FOREGROUND_INT, 0)
        set(@ColorInt color) = preferences.writeInt(Main.FOREGROUND_INT, color)

    val screenOrientation: Int
        get() = preferences.readString(Main.SCREEN_ORIENTATION_STRING, "").toIntOrNull()
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

    fun shouldUseSpeechRecognizer(): Boolean = preferences.readBoolean(Main.SHOULD_USE_SPEECH_RECOGNIZER_BOOLEAN, false)

    fun shouldShowCandidateList(): Boolean = preferences.readBoolean(Main.SHOULD_SHOW_CANDIDATE_LIST_BOOLEAN, false)

    fun shouldShowEditorWhenLongTap(): Boolean =
        preferences.readBoolean(Main.SHOULD_SHOW_EDITOR_WHEN_LONG_TAP_BOOLEAN, false)

    fun shouldShowEditorAfterSelect(): Boolean = preferences.readBoolean(Main.SHOULD_SHOW_EDITOR_BOOLEAN, false)

    var history: Set<String>
        get() = preferences.readStringSet(Main.HISTORY_SET, emptySet())
        set(history) = preferences.writeStringSet(Main.HISTORY_SET, history)

    companion object {
        private lateinit var settings: Settings

        fun initialize(
            context: Context,
        ) {
            Preferences(context, Main::class).also {
                Maintainer.maintain(context, it)
                settings = Settings(it)
            }
        }

        fun get(): Settings = settings
    }
}
