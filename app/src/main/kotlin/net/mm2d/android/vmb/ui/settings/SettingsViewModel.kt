/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.mm2d.android.vmb.settings.Settings

class SettingsViewModel(
    private val settings: Settings = Settings.get(),
) : ViewModel() {

    data class UiState(
        val screenOrientation: String = "",
        val shouldUseSpeechRecognizer: Boolean = true,
        val shouldShowCandidateList: Boolean = false,
        val shouldShowEditorAfterSelect: Boolean = false,
        val shouldShowEditorWhenLongTap: Boolean = true,
        val useFont: Boolean = false,
        val fontName: String = "",
    )

    sealed interface DialogUiState {
        data object None : DialogUiState
        data class Orientation(
            val screenOrientation: String,
        ) : DialogUiState
    }

    sealed interface UiEvent {
        data object OnBackClick : UiEvent
        data object OnSelectOrientationClick : UiEvent
        data object OnDismissOrientationDialog : UiEvent
        data class OnSelectScreenOrientation(
            val value: String,
        ) : UiEvent

        data class OnChangeSpeechRecognizer(
            val checked: Boolean,
        ) : UiEvent

        data class OnChangeCandidateList(
            val checked: Boolean,
        ) : UiEvent

        data class OnChangeEditorAfterSelect(
            val checked: Boolean,
        ) : UiEvent

        data class OnChangeEditorWhenLongTap(
            val checked: Boolean,
        ) : UiEvent

        data class OnChangeUseFont(
            val checked: Boolean,
        ) : UiEvent

        data object OnSelectFontClick : UiEvent
        data class OnOpenUrl(
            val url: String,
        ) : UiEvent

        data object OnOpenLicense : UiEvent
    }

    sealed interface UiEffect {
        data object NavigateBack : UiEffect
        data object LaunchFontChooser : UiEffect
        data class OpenUrl(
            val url: String,
        ) : UiEffect

        data object NavigateToLicense : UiEffect
    }

    private val _uiState = MutableStateFlow(
        UiState(
            screenOrientation = settings.screenOrientationString,
            shouldUseSpeechRecognizer = settings.shouldUseSpeechRecognizer,
            shouldShowCandidateList = settings.shouldShowCandidateList,
            shouldShowEditorAfterSelect = settings.shouldShowEditorAfterSelect,
            shouldShowEditorWhenLongTap = settings.shouldShowEditorWhenLongTap,
            useFont = settings.useFont,
            fontName = settings.fontName,
        ),
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _dialogUiState = MutableStateFlow<DialogUiState>(DialogUiState.None)
    val dialogUiState: StateFlow<DialogUiState> = _dialogUiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun updateFontName(
        name: String,
    ) {
        _uiState.update { it.copy(fontName = name) }
    }

    fun onEvent(
        event: UiEvent,
    ) {
        when (event) {
            UiEvent.OnBackClick -> {
                sendEffect(UiEffect.NavigateBack)
            }

            UiEvent.OnSelectOrientationClick -> {
                _dialogUiState.value = DialogUiState.Orientation(_uiState.value.screenOrientation)
            }

            UiEvent.OnDismissOrientationDialog -> {
                _dialogUiState.value = DialogUiState.None
            }

            is UiEvent.OnSelectScreenOrientation -> {
                settings.screenOrientationString = event.value
                _dialogUiState.value = DialogUiState.None
                _uiState.update {
                    it.copy(
                        screenOrientation = event.value,
                    )
                }
            }

            is UiEvent.OnChangeSpeechRecognizer -> {
                settings.shouldUseSpeechRecognizer = event.checked
                _uiState.update { it.copy(shouldUseSpeechRecognizer = event.checked) }
            }

            is UiEvent.OnChangeCandidateList -> {
                settings.shouldShowCandidateList = event.checked
                _uiState.update { it.copy(shouldShowCandidateList = event.checked) }
            }

            is UiEvent.OnChangeEditorAfterSelect -> {
                settings.shouldShowEditorAfterSelect = event.checked
                _uiState.update { it.copy(shouldShowEditorAfterSelect = event.checked) }
            }

            is UiEvent.OnChangeEditorWhenLongTap -> {
                settings.shouldShowEditorWhenLongTap = event.checked
                _uiState.update { it.copy(shouldShowEditorWhenLongTap = event.checked) }
            }

            is UiEvent.OnChangeUseFont -> {
                settings.useFont = event.checked
                _uiState.update { it.copy(useFont = event.checked) }
            }

            UiEvent.OnSelectFontClick -> {
                sendEffect(UiEffect.LaunchFontChooser)
            }

            is UiEvent.OnOpenUrl -> {
                sendEffect(UiEffect.OpenUrl(event.url))
            }

            UiEvent.OnOpenLicense -> {
                sendEffect(UiEffect.NavigateToLicense)
            }
        }
    }

    private fun sendEffect(
        effect: UiEffect,
    ) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
