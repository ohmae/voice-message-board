/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.main

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MainViewModelTest {
    @Test
    fun `scale font size clamps the value to the configured bounds`() {
        val uiState = MainViewModel.UiState(fontSizePx = 24f)

        assertThat(uiState.scaleFont(2f, 12f, 32f).fontSizePx).isEqualTo(32f)
        assertThat(uiState.scaleFont(0.1f, 12f, 32f).fontSizePx).isEqualTo(12f)
    }
}
