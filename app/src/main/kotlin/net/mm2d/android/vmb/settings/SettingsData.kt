/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.settings

import android.content.pm.ActivityInfo
import android.graphics.Color

data class SettingsData(
    val backgroundColor: Int = Color.WHITE,
    val foregroundColor: Int = Color.BLACK,
    val screenOrientationString: String = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED.toString(),
    val useFont: Boolean = false,
    val fontPath: String = "",
    val fontName: String = "",
    val shouldUseSpeechRecognizer: Boolean = true,
    val shouldShowCandidateList: Boolean = false,
    val shouldShowEditorAfterSelect: Boolean = false,
    val shouldShowEditorWhenLongTap: Boolean = true,
    val history: Set<String> = emptySet(),
) {
    val screenOrientation: Int
        get() = screenOrientationString.toIntOrNull()
            ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    val fontPathToUse: String
        get() = if (useFont) fontPath else ""

    companion object {
        val DEFAULT = SettingsData()
    }
}
