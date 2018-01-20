/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.Context
import android.preference.PreferenceManager
import net.mm2d.android.vmb.R

/**
 * 設定値のメンテナー。
 *
 *
 * アプリ設定のバージョンを付与し、
 * 元に設定値のマイグレーション処理や初期値の書き込みを行う。
 *
 *
 * すでに使用しなくなった設定値にアクセスするため、
 * `@Deprecated`指定をしたOldKeysに唯一アクセスしてもよいクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object Maintainer {
    /**
     * SharedPreferencesのバージョン。
     *
     * <table>
     * <tr><th>SETTINGS_VERSION</th><th>VersionName</th></tr>
     * <tr><td>0</td><td>1.2.4-</td></tr>
     * </table>
     */
    private const val SETTINGS_VERSION = 0

    /**
     * 起動時に一度だけ呼び出され、SharedPreferencesのメンテナンスを行う。
     *
     * @param context Context
     * @param storage SettingsStorage
     */
    fun maintain(context: Context, storage: SettingsStorage) {
        val currentVersion = getSettingsVersion(storage)
        if (currentVersion == SETTINGS_VERSION) {
            return
        }
        storage.writeInt(Key.SETTINGS_VERSION, SETTINGS_VERSION)
        PreferenceManager.setDefaultValues(context, R.xml.preferences, true)
    }

    /**
     * SharedPreferencesのバージョンを取得する。
     *
     * @param storage SettingsStorage
     * @return バージョン
     */
    private fun getSettingsVersion(storage: SettingsStorage): Int =
            storage.readInt(Key.SETTINGS_VERSION, -1)
}
