/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.settings

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.mm2d.android.vmb.BuildConfig
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.constant.Constants
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSelectFontClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenLicense: () -> Unit,
    settings: Settings = Settings.get(),
) {
    var screenOrientation by remember { mutableStateOf(settings.screenOrientationString) }
    var shouldUseSpeechRecognizer by remember { mutableStateOf(settings.shouldUseSpeechRecognizer) }
    var shouldShowCandidateList by remember { mutableStateOf(settings.shouldShowCandidateList) }
    var shouldShowEditorAfterSelect by remember { mutableStateOf(settings.shouldShowEditorAfterSelect) }
    var shouldShowEditorWhenLongTap by remember { mutableStateOf(settings.shouldShowEditorWhenLongTap) }
    var useFont by remember { mutableStateOf(settings.useFont) }
    var fontName by remember { mutableStateOf(settings.fontName) }

    var showOrientationDialog by remember { mutableStateOf(false) }

    val orientationTitles = stringArrayResource(R.array.pref_screen_orientation_titles)
    val orientationValues = stringArrayResource(R.array.pref_screen_orientation_values)

    val currentOrientationTitle = remember(screenOrientation, orientationTitles, orientationValues) {
        val index = orientationValues.indexOf(screenOrientation)
        if (index >= 0 && index < orientationTitles.size) orientationTitles[index] else orientationTitles[0]
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_activity_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item {
                PreferenceCategoryHeader(title = stringResource(R.string.pref_header_general))
            }
            item {
                PreferenceClickableItem(
                    title = stringResource(R.string.pref_title_screen_orientation),
                    summary = currentOrientationTitle,
                    onClick = { showOrientationDialog = true },
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.pref_title_speech_recognizer),
                    summary = if (shouldUseSpeechRecognizer) {
                        stringResource(R.string.pref_description_speech_recognizer_on)
                    } else {
                        stringResource(R.string.pref_description_speech_recognizer_off)
                    },
                    checked = shouldUseSpeechRecognizer,
                    onCheckedChange = { checked ->
                        shouldUseSpeechRecognizer = checked
                        settings.shouldUseSpeechRecognizer = checked
                    },
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.pref_title_candidate_list),
                    summary = if (shouldShowCandidateList) {
                        stringResource(R.string.pref_description_candidate_list_on)
                    } else {
                        stringResource(R.string.pref_description_candidate_list_off)
                    },
                    checked = shouldShowCandidateList,
                    onCheckedChange = { checked ->
                        shouldShowCandidateList = checked
                        settings.shouldShowCandidateList = checked
                    },
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.pref_title_list_edit),
                    summary = if (shouldShowEditorAfterSelect) {
                        stringResource(R.string.pref_description_list_edit_on)
                    } else {
                        stringResource(R.string.pref_description_list_edit_off)
                    },
                    checked = shouldShowEditorAfterSelect,
                    enabled = shouldShowCandidateList,
                    onCheckedChange = { checked ->
                        shouldShowEditorAfterSelect = checked
                        settings.shouldShowEditorAfterSelect = checked
                    },
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.pref_title_long_tap_edit),
                    summary = if (shouldShowEditorWhenLongTap) {
                        stringResource(R.string.pref_description_long_tap_edit_on)
                    } else {
                        stringResource(R.string.pref_description_long_tap_edit_off)
                    },
                    checked = shouldShowEditorWhenLongTap,
                    onCheckedChange = { checked ->
                        shouldShowEditorWhenLongTap = checked
                        settings.shouldShowEditorWhenLongTap = checked
                    },
                )
            }

            item {
                PreferenceCategoryHeader(title = stringResource(R.string.pref_header_display))
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.pref_title_use_font),
                    summary = if (useFont) {
                        stringResource(R.string.pref_description_use_font_on)
                    } else {
                        stringResource(R.string.pref_description_use_font_off)
                    },
                    checked = useFont,
                    onCheckedChange = { checked ->
                        useFont = checked
                        settings.useFont = checked
                    },
                )
            }
            item {
                PreferenceClickableItem(
                    title = stringResource(R.string.pref_title_font_path),
                    summary = fontName.ifEmpty { stringResource(R.string.pref_description_font_path) },
                    enabled = useFont,
                    onClick = onSelectFontClick,
                )
            }

            item {
                PreferenceCategoryHeader(title = stringResource(R.string.pref_header_information))
            }
            item {
                PreferenceClickableItem(
                    title = stringResource(R.string.pref_title_version_number),
                    summary = BuildConfig.VERSION_NAME,
                )
            }
            item {
                PreferenceClickableItem(
                    title = stringResource(R.string.pref_title_play_store),
                    summary = stringResource(R.string.pref_description_play_store),
                    onClick = { onOpenUrl(Constants.MARKET_URL) },
                )
            }
            item {
                PreferenceClickableItem(
                    title = stringResource(R.string.pref_title_privacy_policy),
                    summary = stringResource(R.string.pref_description_privacy_policy),
                    onClick = { onOpenUrl(Constants.PRIVACY_POLICY_URL) },
                )
            }
            item {
                PreferenceClickableItem(
                    title = stringResource(R.string.pref_title_source_code),
                    summary = stringResource(R.string.pref_description_source_code),
                    onClick = { onOpenUrl(Constants.SOURCE_CODE_URL) },
                )
            }
            item {
                PreferenceClickableItem(
                    title = stringResource(R.string.pref_title_license),
                    summary = stringResource(R.string.pref_description_license),
                    onClick = onOpenLicense,
                )
            }
            item {
                PreferenceClickableItem(
                    title = stringResource(R.string.pref_title_copyright),
                    summary = stringResource(R.string.pref_description_copyright),
                )
            }
        }
    }

    if (showOrientationDialog) {
        AlertDialog(
            onDismissRequest = { showOrientationDialog = false },
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
                                    onClick = {
                                        screenOrientation = value
                                        settings.screenOrientationString = value
                                        showOrientationDialog = false
                                    },
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
                TextButton(onClick = { showOrientationDialog = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun PreferenceCategoryHeader(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun PreferenceSwitchItem(
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
private fun PreferenceClickableItem(
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
            onBackClick = {},
            onSelectFontClick = {},
            onOpenUrl = {},
            onOpenLicense = {},
        )
    }
}
