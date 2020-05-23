/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.Context
import net.mm2d.android.vmb.BuildConfig
import kotlin.reflect.KClass

class Preferences<K>(
    context: Context,
    kClass: KClass<K>
) where K : Enum<*>,
        K : Key {
    val dataStore: SharedPreferenceDataStore =
        SharedPreferenceDataStore(
            context.getSharedPreferences(BuildConfig.APPLICATION_ID + "." + kClass.simpleName, Context.MODE_PRIVATE)
        )

    operator fun contains(key: K): Boolean =
        dataStore.contains(key.name)

    fun readBoolean(key: K, default: Boolean): Boolean {
        key.checkSuffix(default)
        return dataStore.getBoolean(key.name, default)
    }

    fun writeBoolean(key: K, value: Boolean) {
        key.checkSuffix(value)
        dataStore.putBoolean(key.name, value)
    }

    fun readInt(key: K, default: Int): Int {
        key.checkSuffix(default)
        return dataStore.getInt(key.name, default)
    }

    fun writeInt(key: K, value: Int) {
        key.checkSuffix(value)
        dataStore.putInt(key.name, value)
    }

    @Suppress("unused")
    fun readLong(key: K, default: Long): Long {
        key.checkSuffix(default)
        return dataStore.getLong(key.name, default)
    }

    @Suppress("unused")
    fun writeLong(key: K, value: Long) {
        key.checkSuffix(value)
        dataStore.putLong(key.name, value)
    }

    fun readString(key: K, default: String): String {
        key.checkSuffix(default)
        return dataStore.getString(key.name, default)!!
    }

    fun writeString(key: K, value: String) {
        key.checkSuffix(value)
        dataStore.putString(key.name, value)
    }

    fun readStringSet(key: K, default: Set<String>): Set<String> {
        key.checkSuffix(default)
        return dataStore.getStringSet(key.name, default)!!
    }

    fun writeStringSet(key: K, value: Set<String>) {
        key.checkSuffix(value)
        dataStore.putStringSet(key.name, value)
    }
}
