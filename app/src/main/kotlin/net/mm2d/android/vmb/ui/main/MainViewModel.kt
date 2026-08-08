/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.main

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.font.FontUtils
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.settings.SettingsData
import net.mm2d.android.vmb.util.stateWhileSubscribedIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: Settings,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val fontSizeMin = context.resources.getDimension(R.dimen.font_size_min)
    private val fontSizeMax = context.resources.getDimension(R.dimen.font_size_max)

    data class UiState(
        val text: String = "",
        val fontSizePx: Float = 0f,
        val backgroundColor: Color = Color.White,
        val foregroundColor: Color = Color.Black,
        val history: Set<String> = emptySet(),
        val fontFamily: FontFamily = FontFamily(Typeface.DEFAULT),
        val screenOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
        val shouldUseSpeechRecognizer: Boolean = false,
        val shouldShowCandidateList: Boolean = false,
    ) {
        val showHistory: Boolean
            get() = history.isNotEmpty()
    }

    sealed interface DialogUiState {
        data object None : DialogUiState

        data class HistorySelect(
            val history: List<String>,
        ) : DialogUiState

        data object HistoryClear : DialogUiState
    }

    sealed interface UiEvent {
        data object TapText : UiEvent
        data class ScaleFont(
            val scaleFactor: Float,
        ) : UiEvent

        data object ClickEdit : UiEvent
        data object ClickHistory : UiEvent
        data object ClickSettings : UiEvent
        data object ClickTheme : UiEvent
        data object ClickClearHistory : UiEvent
        data object DismissDialog : UiEvent
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

        data object OpenSettings : UiEffect
        data object ShowThemeDialog : UiEffect
        data class ShareText(
            val text: String,
        ) : UiEffect
    }

    private val text: StateFlow<String> = savedStateHandle.getStateFlow(KEY_TEXT, "")
    private val fontSizePx: StateFlow<Float> = savedStateHandle.getStateFlow(KEY_FONT_SIZE, 0f)
    private val settingsData: StateFlow<SettingsData> = settings.settingsFlow
        .stateWhileSubscribedIn(viewModelScope, SettingsData())
    private val fontFamily: StateFlow<FontFamily> = settingsData
        .map { it.fontPathToUse }
        .distinctUntilChanged()
        .map(::createFontFamily)
        .stateWhileSubscribedIn(viewModelScope, FontFamily(Typeface.DEFAULT))

    val uiState: StateFlow<UiState> = combine(
        settingsData,
        text,
        fontSizePx,
        fontFamily,
    ) { settingsData, text, fontSizePx, fontFamily ->
        UiState(
            text = text,
            fontSizePx = fontSizePx,
            backgroundColor = Color(settingsData.backgroundColor),
            foregroundColor = Color(settingsData.foregroundColor),
            history = settingsData.history,
            fontFamily = fontFamily,
            screenOrientation = settingsData.screenOrientation,
            shouldUseSpeechRecognizer = settingsData.shouldUseSpeechRecognizer,
            shouldShowCandidateList = settingsData.shouldShowCandidateList,
        )
    }.stateWhileSubscribedIn(viewModelScope, UiState())

    private val _dialogUiState = MutableStateFlow<DialogUiState>(DialogUiState.None)
    val dialogUiState: StateFlow<DialogUiState> = _dialogUiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(
        event: UiEvent,
    ) {
        when (event) {
            UiEvent.TapText -> sendEffect(UiEffect.StartVoiceInput)

            is UiEvent.ScaleFont -> updateFontSize(event.scaleFactor)

            UiEvent.ClickEdit -> sendEffect(UiEffect.ShowEditDialog(text.value))

            UiEvent.ClickHistory -> {
                _dialogUiState.value = DialogUiState.HistorySelect(uiState.value.history.toList())
            }

            UiEvent.ClickSettings -> sendEffect(UiEffect.OpenSettings)

            UiEvent.ClickTheme -> sendEffect(UiEffect.ShowThemeDialog)

            UiEvent.ClickClearHistory -> _dialogUiState.value = DialogUiState.HistoryClear

            UiEvent.DismissDialog -> _dialogUiState.value = DialogUiState.None

            UiEvent.ClickShare -> sendEffect(UiEffect.ShareText(text.value))

            is UiEvent.UpdateText -> updateText(event.text)

            is UiEvent.SelectText -> {
                _dialogUiState.value = DialogUiState.None
                selectText(event.text)
            }

            UiEvent.ClearHistory -> {
                _dialogUiState.value = DialogUiState.None
                clearHistory()
            }
        }
    }

    fun initialize(
        initialText: String,
        initialFontSizePx: Float,
    ) {
        if (savedStateHandle.get<Boolean>(KEY_INITIALIZED) == true) {
            return
        }
        savedStateHandle[KEY_INITIALIZED] = true
        savedStateHandle[KEY_TEXT] = initialText
        savedStateHandle[KEY_FONT_SIZE] = initialFontSizePx
    }

    private fun updateFontSize(
        scaleFactor: Float,
    ) {
        val fontSizePx = uiState.value.fontSizePx
        savedStateHandle[KEY_FONT_SIZE] = (fontSizePx * scaleFactor).coerceIn(fontSizeMin, fontSizeMax)
    }

    private fun createFontFamily(
        fontPath: String,
    ): FontFamily {
        val typeface = FontUtils.getFont(
            context = context,
            fontPathToUse = fontPath,
            onInvalidFont = {
                viewModelScope.launch {
                    settings.resetFont()
                }
            },
        )
        return FontFamily(typeface)
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
        private const val KEY_INITIALIZED = "initialized"
        private const val KEY_TEXT = "text"
        private const val KEY_FONT_SIZE = "fontSize"
        private const val MAX_HISTORY = 30
    }
}
