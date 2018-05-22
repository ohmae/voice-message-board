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
import android.support.annotation.ColorInt
import android.text.TextUtils
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.vmb.BuildConfig
import net.mm2d.log.Log
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Settings private constructor(private val storage: SettingsStorage) {
    var backgroundColor: Int
        @ColorInt
        get() = storage.readInt(Key.KEY_BACKGROUND, Color.WHITE)
        set(@ColorInt color) = storage.writeInt(Key.KEY_BACKGROUND, color)

    var foregroundColor: Int
        @ColorInt
        get() = storage.readInt(Key.KEY_FOREGROUND, Color.BLACK)
        set(@ColorInt color) = storage.writeInt(Key.KEY_FOREGROUND, color)

    val screenOrientation: Int
        get() {
            val value = storage.readString(Key.SCREEN_ORIENTATION, "")
            if (!TextUtils.isEmpty(value)) {
                try {
                    return Integer.parseInt(value)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

    var useFont: Boolean
        get() = storage.readBoolean(Key.USE_FONT, false)
        set(value) = storage.writeBoolean(Key.USE_FONT, value)

    var fontPath: String
        get() = storage.readString(Key.FONT_PATH, "")!!
        set(value) = storage.writeString(Key.FONT_PATH, value)

    val fontPathToUse: String
        get() = if (useFont) fontPath else ""

    fun shouldUseSpeechRecognizer(): Boolean =
            storage.readBoolean(Key.SPEECH_RECOGNIZER, true)

    fun shouldShowCandidateList(): Boolean =
            storage.readBoolean(Key.CANDIDATE_LIST, false)

    fun shouldShowEditorWhenLongTap(): Boolean =
            storage.readBoolean(Key.LONG_TAP_EDIT, false)

    fun shouldShowEditorAfterSelect(): Boolean =
            storage.readBoolean(Key.LIST_EDIT, false)

    var history: Set<String>
        get() = storage.readStringSet(Key.HISTORY, null) ?: Collections.emptySet()
        set(history) = storage.writeStringSet(Key.HISTORY, history)

    companion object {
        private var settings: Settings? = null
        private val lock: Lock = ReentrantLock()
        private val condition: Condition = lock.newCondition()!!

        /**
         * Settingsのインスタンスを返す。
         *
         * 初期化が完了していなければブロックされる。
         */
        fun get(): Settings {
            lock.withLock {
                while (settings == null) {
                    if (BuildConfig.DEBUG) {
                        Log.e("!!!!!!!!!! BLOCK !!!!!!!!!!")
                    }
                    condition.await()
                }
                return settings as Settings
            }
        }

        /**
         * アプリ起動時に一度だけコールされ、初期化を行う。
         *
         * @param context コンテキスト
         */
        fun initialize(context: Context) {
            Completable.fromAction {
                val storage = SettingsStorage(context)
                try {
                    Maintainer.maintain(storage)
                } finally {
                    lock.withLock {
                        settings = Settings(storage)
                        condition.signalAll()
                    }
                }
            }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }
    }
}
