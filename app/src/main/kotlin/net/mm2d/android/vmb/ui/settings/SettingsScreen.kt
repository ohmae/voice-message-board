/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import net.mm2d.android.vmb.BuildConfig
import net.mm2d.android.vmb.LicenseActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.constant.Constants
import net.mm2d.android.vmb.customtabs.CustomTabsHelperHolder
import net.mm2d.android.vmb.ui.settings.SettingsViewModel.DialogUiState
import net.mm2d.android.vmb.ui.settings.SettingsViewModel.UiEffect
import net.mm2d.android.vmb.ui.settings.SettingsViewModel.UiEvent
import net.mm2d.android.vmb.ui.settings.SettingsViewModel.UiState
import net.mm2d.android.vmb.ui.theme.AppTheme
import net.mm2d.android.vmb.util.Toaster

private val URLS = listOf(
    Constants.PRIVACY_POLICY_URL,
    Constants.SOURCE_CODE_URL,
)

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    LifecycleResumeEffect(Unit) {
        CustomTabsHelperHolder.mayLaunchUrl(URLS)
        onPauseOrDispose {}
    }

    val context = LocalContext.current
    val fontChooserLauncher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onEvent(UiEvent.SelectFontResult(context, it)) }
    }

    val currentOnBackClick by rememberUpdatedState(onBackClick)

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                UiEffect.NavigateBack -> currentOnBackClick()
                UiEffect.LaunchFontChooser -> fontChooserLauncher.launch("*/*")
                is UiEffect.OpenUrl -> CustomTabsHelperHolder.openUrl(context, effect.url)
                UiEffect.NavigateToLicense -> LicenseActivity.start(context)
                is UiEffect.ShowToast -> Toaster.show(context, effect.resId)
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )

    val dialogUiState by viewModel.dialogUiState.collectAsStateWithLifecycle()

    SettingsDialog(
        dialogUiState = dialogUiState,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_activity_settings)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(UiEvent.ClickBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        SettingsContent(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun SettingsContent(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        settingsItems(
            uiState = uiState,
            onEvent = onEvent,
        )
    }
}

private fun LazyListScope.settingsItems(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    item {
        CategoryHeader(title = stringResource(R.string.pref_header_general))
    }
    item {
        val orientationTitles = stringArrayResource(R.array.pref_screen_orientation_titles)
        val orientationValues = stringArrayResource(R.array.pref_screen_orientation_values)
        val currentOrientationTitle = remember(uiState.screenOrientation, orientationTitles, orientationValues) {
            val index = orientationValues.indexOf(uiState.screenOrientation)
            if (index >= 0 && index < orientationTitles.size) orientationTitles[index] else orientationTitles[0]
        }
        ClickableItem(
            title = stringResource(R.string.pref_title_screen_orientation),
            summary = currentOrientationTitle,
            onClick = { onEvent(UiEvent.ClickSelectOrientation) },
        )
    }
    item {
        SwitchItem(
            title = stringResource(R.string.pref_title_speech_recognizer),
            summary = if (uiState.shouldUseSpeechRecognizer) {
                stringResource(R.string.pref_description_speech_recognizer_on)
            } else {
                stringResource(R.string.pref_description_speech_recognizer_off)
            },
            checked = uiState.shouldUseSpeechRecognizer,
            onCheckedChange = { checked ->
                onEvent(UiEvent.ChangeSpeechRecognizer(checked))
            },
        )
    }
    item {
        SwitchItem(
            title = stringResource(R.string.pref_title_candidate_list),
            summary = if (uiState.shouldShowCandidateList) {
                stringResource(R.string.pref_description_candidate_list_on)
            } else {
                stringResource(R.string.pref_description_candidate_list_off)
            },
            checked = uiState.shouldShowCandidateList,
            onCheckedChange = { checked ->
                onEvent(UiEvent.ChangeCandidateList(checked))
            },
        )
    }
    item {
        SwitchItem(
            title = stringResource(R.string.pref_title_list_edit),
            summary = if (uiState.shouldShowEditorAfterSelect) {
                stringResource(R.string.pref_description_list_edit_on)
            } else {
                stringResource(R.string.pref_description_list_edit_off)
            },
            checked = uiState.shouldShowEditorAfterSelect,
            enabled = uiState.shouldShowCandidateList,
            onCheckedChange = { checked ->
                onEvent(UiEvent.ChangeEditorAfterSelect(checked))
            },
        )
    }
    item {
        SwitchItem(
            title = stringResource(R.string.pref_title_long_tap_edit),
            summary = if (uiState.shouldShowEditorWhenLongTap) {
                stringResource(R.string.pref_description_long_tap_edit_on)
            } else {
                stringResource(R.string.pref_description_long_tap_edit_off)
            },
            checked = uiState.shouldShowEditorWhenLongTap,
            onCheckedChange = { checked ->
                onEvent(UiEvent.ChangeEditorWhenLongTap(checked))
            },
        )
    }

    item {
        CategoryHeader(title = stringResource(R.string.pref_header_display))
    }
    item {
        SwitchItem(
            title = stringResource(R.string.pref_title_use_font),
            summary = if (uiState.useFont) {
                stringResource(R.string.pref_description_use_font_on)
            } else {
                stringResource(R.string.pref_description_use_font_off)
            },
            checked = uiState.useFont,
            onCheckedChange = { checked ->
                onEvent(UiEvent.ChangeUseFont(checked))
            },
        )
    }
    item {
        ClickableItem(
            title = stringResource(R.string.pref_title_font_path),
            summary = uiState.fontName.ifEmpty { stringResource(R.string.pref_description_font_path) },
            enabled = uiState.useFont,
            onClick = { onEvent(UiEvent.ClickSelectFont) },
        )
    }

    item {
        CategoryHeader(title = stringResource(R.string.pref_header_information))
    }
    item {
        ClickableItem(
            title = stringResource(R.string.pref_title_version_number),
            summary = BuildConfig.VERSION_NAME,
        )
    }
    item {
        ClickableItem(
            title = stringResource(R.string.pref_title_play_store),
            summary = stringResource(R.string.pref_description_play_store),
            onClick = { onEvent(UiEvent.ClickUrl(Constants.MARKET_URL)) },
        )
    }
    item {
        ClickableItem(
            title = stringResource(R.string.pref_title_privacy_policy),
            summary = stringResource(R.string.pref_description_privacy_policy),
            onClick = { onEvent(UiEvent.ClickUrl(Constants.PRIVACY_POLICY_URL)) },
        )
    }
    item {
        ClickableItem(
            title = stringResource(R.string.pref_title_source_code),
            summary = stringResource(R.string.pref_description_source_code),
            onClick = { onEvent(UiEvent.ClickUrl(Constants.SOURCE_CODE_URL)) },
        )
    }
    item {
        ClickableItem(
            title = stringResource(R.string.pref_title_license),
            summary = stringResource(R.string.pref_description_license),
            onClick = { onEvent(UiEvent.ClickLicense) },
        )
    }
    item {
        ClickableItem(
            title = stringResource(R.string.pref_title_copyright),
            summary = stringResource(R.string.pref_description_copyright),
        )
    }
}

@Composable
private fun SettingsDialog(
    dialogUiState: DialogUiState,
    onEvent: (UiEvent) -> Unit,
) {
    when (dialogUiState) {
        DialogUiState.None -> Unit

        is DialogUiState.Orientation -> {
            ScreenOrientationDialog(
                screenOrientation = dialogUiState.screenOrientation,
                onSelectScreenOrientation = { value ->
                    onEvent(UiEvent.SelectScreenOrientation(value))
                },
                onDismiss = { onEvent(UiEvent.DismissOrientationDialog) },
            )
        }
    }
}

@Composable
private fun ScreenOrientationDialog(
    screenOrientation: String,
    onSelectScreenOrientation: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val orientationTitles = stringArrayResource(R.array.pref_screen_orientation_titles)
    val orientationValues = stringArrayResource(R.array.pref_screen_orientation_values)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.pref_title_screen_orientation)) },
        text = {
            Column {
                orientationTitles.forEachIndexed { index, title ->
                    val value = orientationValues[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (screenOrientation == value),
                                onClick = { onSelectScreenOrientation(value) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 12.dp),
                    ) {
                        RadioButton(
                            selected = (screenOrientation == value),
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = title, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun CategoryHeader(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SwitchItem(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val alpha = if (enabled) 1.0f else 0.38f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .alpha(alpha)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun ClickableItem(
    title: String,
    summary: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .alpha(if (enabled) 1.0f else 0.38f)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            uiState = UiState(),
            onEvent = {},
        )
    }
}
