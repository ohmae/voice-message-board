/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.pm.ActivityInfo
import android.graphics.Color
import java.lang.IllegalArgumentException
import java.util.*

/**
 * 設定値。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
enum class Key {
    PLAY_STORE,
    PRIVACY_POLICY,
    COPYRIGHT,
    VERSION_NUMBER,
    SOURCE_CODE,
    LICENSE,

    SETTINGS_VERSION(
            -1
    ),

    KEY_BACKGROUND(
            Color.WHITE
    ),
    KEY_FOREGROUND(
            Color.BLACK
    ),
    HISTORY(
            Collections.emptySet<String>()
    ),
    SPEECH_RECOGNIZER(
            true
    ),
    CANDIDATE_LIST(
            false
    ),
    LIST_EDIT(
            false
    ),
    LONG_TAP_EDIT(
            false
    ),
    SCREEN_ORIENTATION(
            Integer.toString(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    ),
    USE_FONT(
            false
    ),
    FONT_PATH(
            ""
    ),
    ;

    private enum class Type {
        BOOLEAN,
        INT,
        LONG,
        STRING,
        STRING_SET,
    }

    private val type: Type?
    private val defaultValue: Any?

    constructor() {
        type = null
        defaultValue = null
    }

    constructor(value: Any) {
        type = when (value) {
            is Boolean -> Type.BOOLEAN
            is Int -> Type.INT
            is Long -> Type.LONG
            is String -> Type.STRING
            is Set<*> -> Type.STRING_SET
            else -> throw IllegalArgumentException()
        }
        defaultValue = value
    }

    internal fun isReadWriteKey(): Boolean {
        return type != null
    }

    internal fun isBooleanKey(): Boolean {
        return type === Type.BOOLEAN
    }

    internal fun isIntKey(): Boolean {
        return type === Type.INT
    }

    internal fun isLongKey(): Boolean {
        return type === Type.LONG
    }

    internal fun isStringKey(): Boolean {
        return type === Type.STRING
    }

    internal fun isStringSetKey(): Boolean {
        return type === Type.STRING_SET
    }

    internal fun getDefaultBoolean(): Boolean {
        return defaultValue as Boolean
    }

    internal fun getDefaultInt(): Int {
        return defaultValue as Int
    }

    internal fun getDefaultLong(): Long {
        return defaultValue as Long
    }

    internal fun getDefaultString(): String {
        if (defaultValue == null) {
            throw NullPointerException("Default value is not set")
        }
        return defaultValue as String
    }

    @Suppress("UNCHECKED_CAST")
    internal fun getDefaultStringSet(): Set<String> {
        if (defaultValue == null) {
            throw NullPointerException("Default value is not set")
        }
        return defaultValue as Set<String>
    }
}
