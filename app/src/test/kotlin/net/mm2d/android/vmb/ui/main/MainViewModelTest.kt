/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.main

import com.google.common.truth.Truth.assertThat
import net.mm2d.android.vmb.settings.SettingsData
import org.junit.Test

class MainViewModelTest {
    @Test
    fun `ui state shows history when settings contain entries`() {
        val uiState = MainViewModel.UiState(
            settingsData = SettingsData(history = setOf("Recognized text")),
        )

        assertThat(uiState.showHistory).isTrue()
    }
}
