/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.mm2d.android.vmb.BuildConfig
import java.io.File
import java.io.IOException

private const val VERSION = 1
private val DATA_VERSION =
    Key.Main.DATA_VERSION_INT.intKey()
private val APP_VERSION_AT_INSTALL =
    Key.Main.APP_VERSION_AT_INSTALL_INT.intKey()
private val APP_VERSION_AT_LAST_LAUNCHED =
    Key.Main.APP_VERSION_AT_LAST_LAUNCHED_INT.intKey()
private val BACKGROUND =
    Key.Main.BACKGROUND_INT.intKey()
private val FOREGROUND =
    Key.Main.FOREGROUND_INT.intKey()
private val SHOULD_USE_SPEECH_RECOGNIZER =
    Key.Main.SHOULD_USE_SPEECH_RECOGNIZER_BOOLEAN.booleanKey()
private val SHOULD_SHOW_CANDIDATE_LIST =
    Key.Main.SHOULD_SHOW_CANDIDATE_LIST_BOOLEAN.booleanKey()
private val SHOULD_SHOW_EDITOR =
    Key.Main.SHOULD_SHOW_EDITOR_BOOLEAN.booleanKey()
private val SHOULD_SHOW_EDITOR_WHEN_LONG_TAP =
    Key.Main.SHOULD_SHOW_EDITOR_WHEN_LONG_TAP_BOOLEAN.booleanKey()
private val HISTORY =
    Key.Main.HISTORY_SET.setKey()
private val SCREEN_ORIENTATION =
    Key.Main.SCREEN_ORIENTATION_STRING.stringKey()
private val USE_FONT =
    Key.Main.USE_FONT_BOOLEAN.booleanKey()
private val FONT_PATH =
    Key.Main.FONT_PATH_STRING.stringKey()
private val FONT_NAME =
    Key.Main.FONT_NAME_STRING.stringKey()

class Settings private constructor(
    context: Context,
) {
    private val Context.dataStore: DataStore<Preferences> by preferences(
        file = DataStoreFile.MAIN,
        produceMigrations = { context ->
            listOf(
                PreferencesMigration(context),
                WriteFirstValue(),
            )
        },
    )

    private val dataStore: DataStore<Preferences> = context.dataStore
    val settingsFlow: Flow<SettingsData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SettingsData(
                backgroundColor = preferences[BACKGROUND]
                    ?: SettingsData.DEFAULT.backgroundColor,
                foregroundColor = preferences[FOREGROUND]
                    ?: SettingsData.DEFAULT.foregroundColor,
                screenOrientationString = preferences[SCREEN_ORIENTATION]
                    ?: SettingsData.DEFAULT.screenOrientationString,
                useFont = preferences[USE_FONT]
                    ?: SettingsData.DEFAULT.useFont,
                fontPath = preferences[FONT_PATH]
                    ?: SettingsData.DEFAULT.fontPath,
                fontName = preferences[FONT_NAME]
                    ?: SettingsData.DEFAULT.fontName,
                shouldUseSpeechRecognizer = preferences[SHOULD_USE_SPEECH_RECOGNIZER]
                    ?: SettingsData.DEFAULT.shouldUseSpeechRecognizer,
                shouldShowCandidateList = preferences[SHOULD_SHOW_CANDIDATE_LIST]
                    ?: SettingsData.DEFAULT.shouldShowCandidateList,
                shouldShowEditorAfterSelect = preferences[SHOULD_SHOW_EDITOR]
                    ?: SettingsData.DEFAULT.shouldShowEditorAfterSelect,
                shouldShowEditorWhenLongTap = preferences[SHOULD_SHOW_EDITOR_WHEN_LONG_TAP]
                    ?: SettingsData.DEFAULT.shouldShowEditorWhenLongTap,
                history = preferences[HISTORY]
                    ?: SettingsData.DEFAULT.history,
            )
        }

    suspend fun updateTheme(
        backgroundColor: Int,
        foregroundColor: Int,
    ) {
        dataStore.edit {
            it[BACKGROUND] = backgroundColor
            it[FOREGROUND] = foregroundColor
        }
    }

    suspend fun updateScreenOrientation(
        value: String,
    ) {
        dataStore.edit {
            it[SCREEN_ORIENTATION] = value
        }
    }

    suspend fun updateUseFont(
        value: Boolean,
    ) {
        dataStore.edit {
            it[USE_FONT] = value
        }
    }

    suspend fun updateFontInfo(
        path: String,
        name: String,
    ) {
        dataStore.edit {
            it[FONT_PATH] = path
            it[FONT_NAME] = name
        }
    }

    suspend fun resetFont() {
        dataStore.edit {
            it[USE_FONT] = false
            it[FONT_PATH] = ""
            it[FONT_NAME] = ""
        }
    }

    suspend fun updateShouldUseSpeechRecognizer(
        value: Boolean,
    ) {
        dataStore.edit {
            it[SHOULD_USE_SPEECH_RECOGNIZER] = value
        }
    }

    suspend fun updateShouldShowCandidateList(
        value: Boolean,
    ) {
        dataStore.edit {
            it[SHOULD_SHOW_CANDIDATE_LIST] = value
        }
    }

    suspend fun updateShouldShowEditorAfterSelect(
        value: Boolean,
    ) {
        dataStore.edit {
            it[SHOULD_SHOW_EDITOR] = value
        }
    }

    suspend fun updateShouldShowEditorWhenLongTap(
        value: Boolean,
    ) {
        dataStore.edit {
            it[SHOULD_SHOW_EDITOR_WHEN_LONG_TAP] = value
        }
    }

    suspend fun updateHistory(
        history: Set<String>,
    ) {
        dataStore.edit {
            it[HISTORY] = history
        }
    }

    suspend fun updateAppVersionAtLastLaunched() {
        dataStore.edit {
            it[APP_VERSION_AT_LAST_LAUNCHED] = BuildConfig.VERSION_CODE
        }
    }

    companion object {
        private lateinit var instance: Settings

        fun initialize(
            context: Context,
        ) {
            instance = Settings(context.applicationContext)
            MainScope().launch {
                instance.updateAppVersionAtLastLaunched()
            }
        }

        fun get(): Settings = instance
    }
}

private class PreferencesMigration(
    private val context: Context,
) : DataMigration<Preferences> {
    private val name = "${BuildConfig.APPLICATION_ID}.Main"
    private val dir = File(context.dataDir, "shared_prefs")
    private val file = File(dir, "$name.xml")

    override suspend fun shouldMigrate(
        currentData: Preferences,
    ): Boolean = file.exists()

    override suspend fun migrate(
        currentData: Preferences,
    ): Preferences =
        currentData.edit {
            val sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
            it[DATA_VERSION] = VERSION
            it[APP_VERSION_AT_INSTALL] = sharedPreferences
                .getInt(Key.Main.APP_VERSION_AT_INSTALL_INT.name, BuildConfig.VERSION_CODE)
            it[APP_VERSION_AT_LAST_LAUNCHED] = BuildConfig.VERSION_CODE
            it[BACKGROUND] = sharedPreferences
                .getInt(Key.Main.BACKGROUND_INT.name, Color.WHITE)
            it[FOREGROUND] = sharedPreferences
                .getInt(Key.Main.FOREGROUND_INT.name, Color.BLACK)
            it[SHOULD_USE_SPEECH_RECOGNIZER] = sharedPreferences
                .getBoolean(Key.Main.SHOULD_USE_SPEECH_RECOGNIZER_BOOLEAN.name, true)
            it[SHOULD_SHOW_CANDIDATE_LIST] = sharedPreferences
                .getBoolean(Key.Main.SHOULD_SHOW_CANDIDATE_LIST_BOOLEAN.name, false)
            it[SHOULD_SHOW_EDITOR] = sharedPreferences
                .getBoolean(Key.Main.SHOULD_SHOW_EDITOR_BOOLEAN.name, false)
            it[SHOULD_SHOW_EDITOR_WHEN_LONG_TAP] = sharedPreferences
                .getBoolean(Key.Main.SHOULD_SHOW_EDITOR_WHEN_LONG_TAP_BOOLEAN.name, true)
            it[HISTORY] = sharedPreferences
                .getStringSet(Key.Main.HISTORY_SET.name, emptySet()) ?: emptySet()
            it[SCREEN_ORIENTATION] = sharedPreferences
                .getString(Key.Main.SCREEN_ORIENTATION_STRING.name, "")
                ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED.toString()
            it[USE_FONT] = sharedPreferences
                .getBoolean(Key.Main.USE_FONT_BOOLEAN.name, false)
            it[FONT_PATH] = sharedPreferences
                .getString(Key.Main.FONT_PATH_STRING.name, "").orEmpty()
            it[FONT_NAME] = sharedPreferences
                .getString(Key.Main.FONT_NAME_STRING.name, "").orEmpty()
        }

    override suspend fun cleanUp() {
        context.deleteSharedPreferences(name)
    }
}

private class WriteFirstValue : DataMigration<Preferences> {
    override suspend fun shouldMigrate(
        currentData: Preferences,
    ): Boolean = currentData[DATA_VERSION] != VERSION

    override suspend fun migrate(
        currentData: Preferences,
    ): Preferences =
        currentData.edit {
            it[DATA_VERSION] = VERSION
            it[APP_VERSION_AT_INSTALL] = BuildConfig.VERSION_CODE
            it[APP_VERSION_AT_LAST_LAUNCHED] = BuildConfig.VERSION_CODE
        }

    override suspend fun cleanUp() = Unit
}
