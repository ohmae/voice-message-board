/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.main

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.settings.SettingsData
import net.mm2d.android.vmb.util.stateWhileSubscribedIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val settings: Settings,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val fontSizeMin = context.resources.getDimension(R.dimen.font_size_min)
    private val fontSizeMax = context.resources.getDimension(R.dimen.font_size_max)

    data class UiState(
        val text: String = "",
        val fontSizePx: Float = 0f,
        val settingsData: SettingsData = SettingsData(),
    ) {
        val showHistory: Boolean
            get() = settingsData.history.isNotEmpty()

        fun scaleFont(
            scaleFactor: Float,
            min: Float,
            max: Float,
        ): UiState = copy(fontSizePx = (fontSizePx * scaleFactor).coerceIn(min, max))
    }

    sealed interface UiEvent {
        data class Initialize(
            val initialText: String,
            val initialFontSizePx: Float,
        ) : UiEvent

        data object TapText : UiEvent
        data class ScaleFont(
            val scaleFactor: Float,
        ) : UiEvent

        data object ClickEdit : UiEvent
        data object ClickHistory : UiEvent
        data object ClickSettings : UiEvent
        data object ClickTheme : UiEvent
        data object ClickClearHistory : UiEvent
        data object ClickShare : UiEvent
        data class UpdateText(
            val text: String,
        ) : UiEvent

        data class SelectText(
            val text: String,
        ) : UiEvent

        data object ClearHistory : UiEvent
    }

    sealed interface UiEffect {
        data object StartVoiceInput : UiEffect
        data class ShowEditDialog(
            val text: String,
        ) : UiEffect

        data object ShowHistoryDialog : UiEffect
        data object OpenSettings : UiEffect
        data object ShowThemeDialog : UiEffect
        data object ShowClearHistoryDialog : UiEffect
        data class ShareText(
            val text: String,
        ) : UiEffect
    }

    private val text: StateFlow<String> = savedStateHandle.getStateFlow(KEY_TEXT, "")
    private val fontSizePx: StateFlow<Float> = savedStateHandle.getStateFlow(KEY_FONT_SIZE, 0f)

    val uiState: StateFlow<UiState> = combine(
        settings.settingsFlow,
        text,
        fontSizePx,
    ) { settingsData, text, fontSizePx ->
        UiState(
            text = text,
            fontSizePx = fontSizePx,
            settingsData = settingsData,
        )
    }.stateWhileSubscribedIn(viewModelScope, UiState())

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(
        event: UiEvent,
    ) {
        when (event) {
            is UiEvent.Initialize -> initialize(event)

            UiEvent.TapText -> sendEffect(UiEffect.StartVoiceInput)

            is UiEvent.ScaleFont ->
                savedStateHandle[KEY_FONT_SIZE] = uiState.value
                    .scaleFont(event.scaleFactor, fontSizeMin, fontSizeMax)
                    .fontSizePx

            UiEvent.ClickEdit -> sendEffect(UiEffect.ShowEditDialog(text.value))

            UiEvent.ClickHistory -> sendEffect(UiEffect.ShowHistoryDialog)

            UiEvent.ClickSettings -> sendEffect(UiEffect.OpenSettings)

            UiEvent.ClickTheme -> sendEffect(UiEffect.ShowThemeDialog)

            UiEvent.ClickClearHistory -> sendEffect(UiEffect.ShowClearHistoryDialog)

            UiEvent.ClickShare -> sendEffect(UiEffect.ShareText(text.value))

            is UiEvent.UpdateText -> updateText(event.text)

            is UiEvent.SelectText -> selectText(event.text)

            UiEvent.ClearHistory -> clearHistory()
        }
    }

    private fun initialize(
        event: UiEvent.Initialize,
    ) {
        if (!savedStateHandle.contains(KEY_TEXT)) {
            savedStateHandle[KEY_TEXT] = event.initialText
        }
        if (!savedStateHandle.contains(KEY_FONT_SIZE)) {
            savedStateHandle[KEY_FONT_SIZE] = event.initialFontSizePx
        }
    }

    private fun updateText(
        value: String,
    ) {
        savedStateHandle[KEY_TEXT] = value
        viewModelScope.launch {
            addToHistory(value, settings.settingsFlow.first())
        }
    }

    private fun selectText(
        value: String,
    ) {
        savedStateHandle[KEY_TEXT] = value
        viewModelScope.launch {
            val settingsData = settings.settingsFlow.first()
            addToHistory(value, settingsData)
            if (settingsData.shouldShowEditorAfterSelect) {
                sendEffect(UiEffect.ShowEditDialog(value))
            }
        }
    }

    private suspend fun addToHistory(
        value: String,
        settingsData: SettingsData,
    ) {
        val history = settingsData.history.toMutableList()
        history.remove(value)
        history.add(0, value)
        settings.updateHistory(history.take(MAX_HISTORY).toSet())
    }

    private fun clearHistory() {
        viewModelScope.launch {
            settings.updateHistory(emptySet())
        }
    }

    private fun sendEffect(
        effect: UiEffect,
    ) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    companion object {
        private const val KEY_TEXT = "text"
        private const val KEY_FONT_SIZE = "fontSize"
        private const val MAX_HISTORY = 30
    }
}
