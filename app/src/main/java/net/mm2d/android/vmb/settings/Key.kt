/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.pm.ActivityInfo
import android.graphics.Color
import java.util.*

/**
 * 設定値。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
enum class Key(
        private val type: Class<*>? = null,
        private val defaultValue: Any? = null) {
    PLAY_STORE,
    PRIVACY_POLICY,
    COPYRIGHT,
    VERSION_NUMBER,
    SOURCE_CODE,
    LICENSE,

    SETTINGS_VERSION(
            Integer::class.java, -1
    ),

    KEY_BACKGROUND(
            Integer::class.java, Color.WHITE
    ),
    KEY_FOREGROUND(
            Integer::class.java, Color.BLACK
    ),
    HISTORY(
            Set::class.java, Collections.EMPTY_SET
    ),
    SPEECH_RECOGNIZER(
            java.lang.Boolean::class.java, true
    ),
    CANDIDATE_LIST(
            java.lang.Boolean::class.java, false
    ),
    LIST_EDIT(
            java.lang.Boolean::class.java, false
    ),
    LONG_TAP_EDIT(
            java.lang.Boolean::class.java, false
    ),
    SCREEN_ORIENTATION(
            String::class.java, Integer.toString(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    ),
    USE_FONT(
            java.lang.Boolean::class.java, false
    ),
    FONT_PATH(
            String::class.java, ""
    ),
    ;

    init {
        if (type?.isInstance(defaultValue) == false) {
            throw IllegalArgumentException(this.name + " " + type.toString() + " " + defaultValue)
        }
    }

    internal fun isReadWriteKey(): Boolean {
        return type != null
    }

    internal fun isBooleanKey(): Boolean {
        return type === java.lang.Boolean::class.java
    }

    internal fun isIntKey(): Boolean {
        return type === Integer::class.java
    }

    internal fun isLongKey(): Boolean {
        return type === java.lang.Long::class.java
    }

    internal fun isStringKey(): Boolean {
        return type === String::class.java
    }

    internal fun isStringSetKey(): Boolean {
        return type === Set::class.java
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
