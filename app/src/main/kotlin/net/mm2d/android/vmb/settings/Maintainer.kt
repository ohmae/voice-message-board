/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.pm.ActivityInfo
import android.graphics.Color
import net.mm2d.android.vmb.BuildConfig
import net.mm2d.android.vmb.settings.Key.Main

internal object Maintainer {
    // 0 : 1.2.4-
    // 1 : 1.7.0-
    private const val SETTINGS_VERSION = 1

    fun maintain(
        preferences: Preferences<Main>,
    ) {
        Main.entries.toTypedArray().checkSuffix()
        if (preferences.readInt(Main.APP_VERSION_AT_LAST_LAUNCHED_INT, 0) != BuildConfig.VERSION_CODE) {
            preferences.writeInt(Main.APP_VERSION_AT_LAST_LAUNCHED_INT, BuildConfig.VERSION_CODE)
        }
        if (preferences.readInt(Main.PREFERENCES_VERSION_INT, 0) == SETTINGS_VERSION) {
            return
        }
        preferences.writeInt(Main.PREFERENCES_VERSION_INT, SETTINGS_VERSION)
        if (!preferences.contains(Main.APP_VERSION_AT_INSTALL_INT)) {
            preferences.writeInt(Main.APP_VERSION_AT_INSTALL_INT, BuildConfig.VERSION_CODE)
        }
        writeDefaultValue(preferences)
    }

    private fun writeDefaultValue(
        preferences: Preferences<Main>,
    ) {
        preferences.writeInt(Main.BACKGROUND_INT, Color.WHITE)
        preferences.writeInt(Main.FOREGROUND_INT, Color.BLACK)
        preferences.writeStringSet(Main.HISTORY_SET, emptySet())
        preferences.writeBoolean(Main.SHOULD_USE_SPEECH_RECOGNIZER_BOOLEAN, true)
        preferences.writeBoolean(Main.SHOULD_SHOW_CANDIDATE_LIST_BOOLEAN, false)
        preferences.writeBoolean(Main.SHOULD_SHOW_EDITOR_BOOLEAN, false)
        preferences.writeBoolean(Main.SHOULD_SHOW_EDITOR_WHEN_LONG_TAP_BOOLEAN, false)
        preferences.writeString(Main.SCREEN_ORIENTATION_STRING, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED.toString())
        preferences.writeBoolean(Main.USE_FONT_BOOLEAN, false)
        preferences.writeString(Main.FONT_PATH_STRING, "")
        preferences.writeString(Main.FONT_NAME_STRING, "")
    }
}
