/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.settings

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.StringRes
import androidx.core.database.getStringOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.font.FontUtils
import net.mm2d.android.vmb.settings.Settings
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: Settings,
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
        data class OnSelectFontResult(
            val context: Context,
            val uri: Uri,
        ) : UiEvent

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
        data class ShowToast(
            @StringRes val resId: Int,
        ) : UiEffect
    }

    val uiState: StateFlow<UiState> = settings.settingsFlow
        .map { data ->
            UiState(
                screenOrientation = data.screenOrientationString,
                shouldUseSpeechRecognizer = data.shouldUseSpeechRecognizer,
                shouldShowCandidateList = data.shouldShowCandidateList,
                shouldShowEditorAfterSelect = data.shouldShowEditorAfterSelect,
                shouldShowEditorWhenLongTap = data.shouldShowEditorWhenLongTap,
                useFont = data.useFont,
                fontName = data.fontName,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(),
        )

    private val _dialogUiState = MutableStateFlow<DialogUiState>(DialogUiState.None)
    val dialogUiState: StateFlow<DialogUiState> = _dialogUiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(
        event: UiEvent,
    ) {
        when (event) {
            UiEvent.OnBackClick ->
                sendEffect(UiEffect.NavigateBack)

            UiEvent.OnSelectOrientationClick ->
                _dialogUiState.value = DialogUiState.Orientation(uiState.value.screenOrientation)

            UiEvent.OnDismissOrientationDialog ->
                _dialogUiState.value = DialogUiState.None

            is UiEvent.OnSelectScreenOrientation -> {
                _dialogUiState.value = DialogUiState.None
                viewModelScope.launch {
                    settings.updateScreenOrientation(event.value)
                }
            }

            is UiEvent.OnChangeSpeechRecognizer ->
                viewModelScope.launch {
                    settings.updateShouldUseSpeechRecognizer(event.checked)
                }

            is UiEvent.OnChangeCandidateList ->
                viewModelScope.launch {
                    settings.updateShouldShowCandidateList(event.checked)
                }

            is UiEvent.OnChangeEditorAfterSelect ->
                viewModelScope.launch {
                    settings.updateShouldShowEditorAfterSelect(event.checked)
                }

            is UiEvent.OnChangeEditorWhenLongTap ->
                viewModelScope.launch {
                    settings.updateShouldShowEditorWhenLongTap(event.checked)
                }

            is UiEvent.OnChangeUseFont ->
                viewModelScope.launch {
                    settings.updateUseFont(event.checked)
                }

            UiEvent.OnSelectFontClick ->
                sendEffect(UiEffect.LaunchFontChooser)

            is UiEvent.OnSelectFontResult ->
                onSelectFontResult(event)

            is UiEvent.OnOpenUrl ->
                sendEffect(UiEffect.OpenUrl(event.url))

            UiEvent.OnOpenLicense ->
                sendEffect(UiEffect.NavigateToLicense)
        }
    }

    private fun onSelectFontResult(
        event: UiEvent.OnSelectFontResult,
    ) {
        viewModelScope.launch {
            val (path, name) = withContext(Dispatchers.IO) {
                prepareFontFile(event.context, event.uri)
            }
            settings.updateFontInfo(path, name)
            if (path.isEmpty()) {
                sendEffect(UiEffect.ShowToast(R.string.toast_not_a_valid_font))
            }
        }
    }

    private fun prepareFontFile(
        context: Context,
        uri: Uri,
    ): Pair<String, String> {
        val name: String = context.contentResolver
            .query(uri, null, null, null, null)
            ?.use {
                if (it.moveToFirst()) {
                    it.getStringOrNull(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                } else {
                    null
                }
            } ?: return "" to ""

        val stream = context.contentResolver.openInputStream(uri) ?: return "" to ""
        val data = stream.use { it.readBytes() }
        val file = File(context.filesDir, "font").also {
            if (it.exists()) it.delete()
        }
        file.writeBytes(data)
        return if (FontUtils.isValidFontFile(file)) {
            file.absolutePath to name
        } else {
            file.delete()
            "" to ""
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
