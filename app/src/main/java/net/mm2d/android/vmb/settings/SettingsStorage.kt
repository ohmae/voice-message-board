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
    private val preferences: SharedPreferences = PreferencesHolder.get(context)

    /**
     * 書き込まれている内容を消去する。
     */
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
        preferences.edit()
                .putBoolean(key.name, value)
                .apply()
    }

    /**
     * boolean値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したboolean値
     */
    fun readBoolean(key: Key, defaultValue: Boolean): Boolean =
            preferences.getBoolean(key.name, defaultValue)

    /**
     * int値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeInt(key: Key, value: Int) {
        preferences.edit()
                .putInt(key.name, value)
                .apply()
    }

    /**
     * int値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したint値
     */
    fun readInt(key: Key, defaultValue: Int): Int =
            preferences.getInt(key.name, defaultValue)

    /**
     * long値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeLong(key: Key, value: Long) {
        preferences.edit()
                .putLong(key.name, value)
                .apply()
    }

    /**
     * long値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したlong値
     */
    fun readLong(key: Key, defaultValue: Long): Long =
            preferences.getLong(key.name, defaultValue)

    /**
     * String値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeString(key: Key, value: String) {
        preferences.edit()
                .putString(key.name, value)
                .apply()
    }

    /**
     * String値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したString値
     */
    fun readString(key: Key, defaultValue: String?): String? =
            preferences.getString(key.name, defaultValue)

    /**
     * StringSet値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeStringSet(key: Key, value: Set<String>) {
        preferences.edit()
                .putStringSet(key.name, value)
                .apply()
    }

    /**
     * StringSet値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したString値
     */
    fun readStringSet(key: Key, defaultValue: Set<String>?): Set<String>? =
            preferences.getStringSet(key.name, defaultValue)

    private object PreferencesHolder {
        private var sharedPreferences: SharedPreferences? = null

        @Synchronized internal operator fun get(context: Context): SharedPreferences {
            if (sharedPreferences == null) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            }
            return sharedPreferences as SharedPreferences
        }
    }

    companion object {
        /**
         * SharedPreferencesのインスタンスを作成し初期化する。
         *
         * @param context コンテキスト
         */
        @JvmStatic
        fun initialize(context: Context) {
            Maintainer.maintain(context, SettingsStorage(context))
        }
    }
}
