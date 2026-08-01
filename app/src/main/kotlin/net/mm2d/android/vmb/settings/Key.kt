/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import net.mm2d.android.vmb.BuildConfig
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

interface Key {
    enum class Main : Key {
        DATA_VERSION_INT,
        APP_VERSION_AT_INSTALL_INT,
        APP_VERSION_AT_LAST_LAUNCHED_INT,

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

enum class DataStoreFile {
    MAIN,
    ;

    fun fileName(): String = BuildConfig.APPLICATION_ID + "." + name.lowercase()
}

fun preferences(
    file: DataStoreFile,
    produceMigrations: (Context) -> List<DataMigration<Preferences>> = { emptyList() },
): ReadOnlyProperty<Context, DataStore<Preferences>> =
    preferencesDataStore(
        name = file.fileName(),
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceMigrations = produceMigrations,
    )

fun Preferences.edit(
    editor: (preferences: MutablePreferences) -> Unit,
): Preferences = toMutablePreferences().also(editor).toPreferences()

fun <K> K.intKey(): Preferences.Key<Int>
    where K : Enum<*>,
          K : Key {
    if (BuildConfig.DEBUG) {
        checkSuffix(Int::class)
    }
    return intPreferencesKey(name)
}

fun <K> K.stringKey(): Preferences.Key<String>
    where K : Enum<*>,
          K : Key {
    if (BuildConfig.DEBUG) {
        checkSuffix(String::class)
    }
    return stringPreferencesKey(name)
}

fun <K> K.booleanKey(): Preferences.Key<Boolean>
    where K : Enum<*>,
          K : Key {
    if (BuildConfig.DEBUG) {
        checkSuffix(Boolean::class)
    }
    return booleanPreferencesKey(name)
}

fun <K> K.setKey(): Preferences.Key<Set<String>>
    where K : Enum<*>,
          K : Key {
    if (BuildConfig.DEBUG) {
        checkSuffix(Set::class)
    }
    return stringSetPreferencesKey(name)
}

private const val SUFFIX_BOOLEAN = "_BOOLEAN"
private const val SUFFIX_INT = "_INT"
private const val SUFFIX_STRING = "_STRING"
private const val SUFFIX_SET = "_SET"

internal fun Enum<*>.checkSuffix(
    value: KClass<*>,
) {
    if (!BuildConfig.DEBUG) return
    when (value) {
        Boolean::class -> require(name.endsWith(SUFFIX_BOOLEAN)) {
            "$this is used for Boolean, suffix \"$SUFFIX_BOOLEAN\" is required."
        }

        Int::class -> require(name.endsWith(SUFFIX_INT)) {
            "$this is used for Int, suffix \"$SUFFIX_INT\" is required."
        }

        String::class -> require(name.endsWith(SUFFIX_STRING)) {
            "$this is used for String, suffix \"$SUFFIX_STRING\" is required."
        }

        Set::class -> require(name.endsWith(SUFFIX_SET)) {
            "$this is used for Set<String>, suffix \"$SUFFIX_SET\" is required."
        }
    }
}
