/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * SharedPreferencesへのアクセスをカプセル化するクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class SettingsStorage(context: Context) {
    @Suppress("ReplaceGetOrSet")
    private val preferences: SharedPreferences = PreferencesHolder.get(context)

    /**
     * 書き込まれている内容を消去する。
     */
    @Suppress("unused")
    fun clear() {
        preferences.edit()
            .clear()
            .apply()
    }

    /**
     * Keyの設定値が含まれるか否かを返す。
     *
     * @param key Key
     * @return keyが含まれている場合true
     */
    fun contains(key: Key): Boolean =
        preferences.contains(key.name)

    /**
     * boolean値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeBoolean(key: Key, value: Boolean) {
        if (!key.isBooleanKey()) {
            throw IllegalArgumentException(key.name + " is not key for Boolean")
        }
        preferences.edit()
            .putBoolean(key.name, value)
            .apply()
    }

    /**
     * boolean値を読み出す。
     *
     * @param key Key
     * @return 読み出したboolean値
     */
    fun readBoolean(key: Key): Boolean {
        if (!key.isBooleanKey()) {
            throw IllegalArgumentException(key.name + " is not key for Boolean")
        }
        return preferences.getBoolean(key.name, key.getDefaultBooleanToRead())
    }

    /**
     * int値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeInt(key: Key, value: Int) {
        if (!key.isIntKey()) {
            throw IllegalArgumentException(key.name + " is not key for Int")
        }
        preferences.edit()
            .putInt(key.name, value)
            .apply()
    }

    /**
     * int値を読み出す。
     *
     * @param key Key
     * @return 読み出したint値
     */
    fun readInt(key: Key): Int {
        if (!key.isIntKey()) {
            throw IllegalArgumentException(key.name + " is not key for Int")
        }
        return preferences.getInt(key.name, key.getDefaultIntToRead())
    }

    /**
     * long値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun writeLong(key: Key, value: Long) {
        if (!key.isLongKey()) {
            throw IllegalArgumentException(key.name + " is not key for Long")
        }
        preferences.edit()
            .putLong(key.name, value)
            .apply()
    }

    /**
     * long値を読み出す。
     *
     * @param key Key
     * @return 読み出したlong値
     */
    @Suppress("unused")
    fun readLong(key: Key): Long {
        if (!key.isLongKey()) {
            throw IllegalArgumentException(key.name + " is not key for Long")
        }
        return preferences.getLong(key.name, key.getDefaultLongToRead())
    }

    /**
     * String値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeString(key: Key, value: String) {
        if (!key.isStringKey()) {
            throw IllegalArgumentException(key.name + " is not key for String")
        }
        preferences.edit()
            .putString(key.name, value)
            .apply()
    }

    /**
     * String値を読み出す。
     *
     * @param key Key
     * @return 読み出したString値
     */
    fun readString(key: Key): String {
        if (!key.isStringKey()) {
            throw IllegalArgumentException(key.name + " is not key for String")
        }
        return preferences.getString(key.name, null) ?: key.getDefaultStringToRead()
    }

    /**
     * StringSet値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeStringSet(key: Key, value: Set<String>) {
        if (!key.isStringSetKey()) {
            throw IllegalArgumentException(key.name + " is not key for Set<String>")
        }
        preferences.edit()
            .putStringSet(key.name, value)
            .apply()
    }

    /**
     * StringSet値を読み出す。
     *
     * @param key Key
     * @return 読み出したString値
     */
    fun readStringSet(key: Key): Set<String> {
        if (!key.isStringSetKey()) {
            throw IllegalArgumentException(key.name + " is not key for Set<String>")
        }
        return preferences.getStringSet(key.name, null) ?: key.getDefaultStringSetToRead()
    }

    /**
     * デフォルト値を書き込む。
     *
     * @param key       Key
     * @param overwrite true:値を上書きする、false:値がない場合のみ書き込む
     */
    fun writeDefault(key: Key, overwrite: Boolean) {
        if (!key.isReadWriteKey()) {
            return
        }
        if (!overwrite && contains(key)) {
            return
        }
        when {
            key.isBooleanKey() ->
                writeBoolean(key, key.getDefaultBooleanToWrite())
            key.isIntKey() ->
                writeInt(key, key.getDefaultIntToWrite())
            key.isLongKey() ->
                writeLong(key, key.getDefaultLongToWrite())
            key.isStringKey() ->
                writeString(key, key.getDefaultStringToWrite())
            key.isStringSetKey() ->
                writeStringSet(key, key.getDefaultStringSetToWrite())
        }
    }

    private object PreferencesHolder {
        private var sharedPreferences: SharedPreferences? = null

        @Synchronized
        internal operator fun get(context: Context): SharedPreferences {
            if (sharedPreferences == null) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            }
            return sharedPreferences as SharedPreferences
        }
    }
}
