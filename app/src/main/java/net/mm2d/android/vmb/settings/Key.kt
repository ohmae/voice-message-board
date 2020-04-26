/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import net.mm2d.android.vmb.BuildConfig

interface Key {
    enum class Main : Key {
        PREFERENCES_VERSION_INT,
        APP_VERSION_AT_INSTALL_INT,
        APP_VERSION_AT_LAST_LAUNCHED_INT,

        VERSION_NUMBER_SCREEN,
        PLAY_STORE_SCREEN,
        PRIVACY_POLICY_SCREEN,
        SOURCE_CODE_SCREEN,
        LICENSE_SCREEN,
        COPYRIGHT_SCREEN,

        BACKGROUND_INT,
        FOREGROUND_INT,
        HISTORY_SET,
        SHOULD_USE_SPEECH_RECOGNIZER_BOOLEAN,
        SHOULD_SHOW_CANDIDATE_LIST_BOOLEAN,
        SHOULD_SHOW_EDITOR_BOOLEAN,
        SHOULD_SHOW_EDITOR_WHEN_LONG_TAP_BOOLEAN,
        SCREEN_ORIENTATION_STRING,
        USE_FONT_BOOLEAN,
        FONT_PATH_STRING,
        FONT_NAME_STRING,
    }
}

private const val SUFFIX_BOOLEAN = "_BOOLEAN"
private const val SUFFIX_INT = "_INT"
private const val SUFFIX_LONG = "_LONG"
private const val SUFFIX_FLOAT = "_FLOAT"
private const val SUFFIX_STRING = "_STRING"
private const val SUFFIX_SET = "_SET"
private const val SUFFIX_SCREEN = "_SCREEN"

private val SUFFIXES =
    listOf(
        SUFFIX_BOOLEAN,
        SUFFIX_INT,
        SUFFIX_LONG,
        SUFFIX_FLOAT,
        SUFFIX_STRING,
        SUFFIX_SET,
        SUFFIX_SCREEN
    )

internal fun Array<out Enum<*>>.checkSuffix() {
    if (!BuildConfig.DEBUG) return
    forEach { key ->
        require(SUFFIXES.any { key.name.endsWith(it) }) { "$key has no type suffix." }
    }
}

internal fun Enum<*>.checkSuffix(value: Any) {
    if (!BuildConfig.DEBUG) return
    when (value) {
        is Boolean -> require(name.endsWith(SUFFIX_BOOLEAN)) {
            "$this is used for Boolean, suffix \"$SUFFIX_BOOLEAN\" is required."
        }
        is Int -> require(name.endsWith(SUFFIX_INT)) {
            "$this is used for Int, suffix \"$SUFFIX_INT\" is required."
        }
        is Long -> require(name.endsWith(SUFFIX_LONG)) {
            "$this is used for Long, suffix \"$SUFFIX_LONG\" is required."
        }
        is Float -> require(name.endsWith(SUFFIX_FLOAT)) {
            "$this is used for Float, suffix \"$SUFFIX_FLOAT\" is required."
        }
        is String -> require(name.endsWith(SUFFIX_STRING)) {
            "$this is used for String, suffix \"$SUFFIX_STRING\" is required."
        }
    }
}
