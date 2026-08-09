/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.main

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.SettingsActivity
import net.mm2d.android.vmb.dialog.PermissionDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.SelectCandidateDialog
import net.mm2d.android.vmb.permission.rememberPermissionStateWithDialogTracking
import net.mm2d.android.vmb.recognize.RecognizeSpeechContract
import net.mm2d.android.vmb.ui.main.MainViewModel.DialogUiState
import net.mm2d.android.vmb.ui.main.MainViewModel.UiEffect
import net.mm2d.android.vmb.ui.main.MainViewModel.UiEvent
import net.mm2d.android.vmb.ui.main.MainViewModel.UiState
import net.mm2d.android.vmb.util.toHsv

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionStateWithDialogTracking(
        permission = Manifest.permission.RECORD_AUDIO,
        onPermissionResult = { granted, dialogShown ->
            viewModel.onEvent(UiEvent.PermissionResult(granted = granted, dialogShown = dialogShown))
        },
    )
    val recognizerTitle = stringResource(R.string.recognizer_title)

    val speechLauncher = rememberLauncherForActivityResult(
        contract = RecognizeSpeechContract(),
    ) { results ->
        viewModel.onEvent(UiEvent.RecognizeResult(results))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                UiEffect.StartVoiceInput -> {
                    if (viewModel.uiState.value.shouldUseSpeechRecognizer) {
                        if (permissionState.status.isGranted) {
                            viewModel.onEvent(UiEvent.ClickRecognizer)
                        } else {
                            permissionState.launchPermissionRequest()
                        }
                    } else {
                        speechLauncher.launch(recognizerTitle)
                    }
                }

                UiEffect.OpenSettings -> {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }

                is UiEffect.ShareText -> {
                    ShareCompat.IntentBuilder(context)
                        .setText(effect.text)
                        .setType("text/plain")
                        .startChooser()
                }
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MainScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )

    val dialogUiState by viewModel.dialogUiState.collectAsStateWithLifecycle()
    DialogContent(
        dialogUiState = dialogUiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun MainScreenContent(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    val gridColor = remember(uiState.backgroundColor) { gridColor(uiState.backgroundColor) }
    val density = LocalDensity.current
    val textFontSize = with(density) { uiState.fontSizePx.toSp() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.text) {
        scrollState.scrollTo(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .gridBackground(
                background = uiState.backgroundColor,
                grid = gridColor,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .observeMainGestures(
                    onTap = { onEvent(UiEvent.TapText) },
                    onScale = { onEvent(UiEvent.ScaleFont(it)) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            SelectionContainer {
                Text(
                    text = uiState.text,
                    color = uiState.foregroundColor,
                    fontSize = textFontSize,
                    lineHeight = textFontSize * 1.2f,
                    fontFamily = uiState.fontFamily,
                    modifier = Modifier.padding(bottom = 56.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        MainTopBar(
            showHistory = uiState.showHistory,
            onEvent = onEvent,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (uiState.showHistory) {
                FloatingActionButton(onClick = { onEvent(UiEvent.ClickHistory) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = stringResource(R.string.action_show_history),
                    )
                }
            }
            FloatingActionButton(onClick = { onEvent(UiEvent.ClickEdit) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = stringResource(R.string.dialog_title_edit),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainTopBar(
    showHistory: Boolean,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = {},
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                MenuItem(
                    label = R.string.action_settings,
                    onClick = { onEvent(UiEvent.ClickSettings) },
                    dismiss = { expanded = false },
                )
                MenuItem(
                    label = R.string.action_theme,
                    onClick = { onEvent(UiEvent.ClickTheme) },
                    dismiss = { expanded = false },
                )
                MenuItem(
                    label = R.string.action_show_history,
                    onClick = { onEvent(UiEvent.ClickHistory) },
                    enabled = showHistory,
                    dismiss = { expanded = false },
                )
                MenuItem(
                    label = R.string.action_clear_history,
                    onClick = { onEvent(UiEvent.ClickClearHistory) },
                    enabled = showHistory,
                    dismiss = { expanded = false },
                )
                MenuItem(
                    label = R.string.action_share,
                    onClick = { onEvent(UiEvent.ClickShare) },
                    dismiss = { expanded = false },
                )
            }
        },
    )
}

@Composable
private fun MenuItem(
    label: Int,
    onClick: () -> Unit,
    enabled: Boolean = true,
    dismiss: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(label)) },
        onClick = {
            dismiss()
            onClick()
        },
        enabled = enabled,
    )
}

private fun Modifier.gridBackground(
    background: Color,
    grid: Color,
): Modifier =
    drawBehind {
        drawRect(background)
        val gridSize = 10.dp.toPx()
        val start = gridSize - 1f
        var x = start
        while (x < size.width) {
            drawLine(
                color = grid,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = Stroke.HairlineWidth,
            )
            x += gridSize
        }
        var y = start
        while (y < size.height) {
            drawLine(
                color = grid,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = Stroke.HairlineWidth,
            )
            y += gridSize
        }
    }

private fun Modifier.observeMainGestures(
    onTap: () -> Unit,
    onScale: (Float) -> Unit,
): Modifier =
    pointerInput(onTap, onScale) {
        awaitPointerEventScope {
            var downPosition: Offset? = null
            var downTime = 0L
            var isTap = false
            var previousSpan: Float? = null
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val pointers = event.changes.filter { it.pressed }
                if (event.changes.any { it.isConsumed }) {
                    isTap = false
                }
                event.changes
                    .firstOrNull { it.changedToDown() }
                    ?.takeIf { pointers.size == 1 }
                    ?.let {
                        downPosition = it.position
                        downTime = it.uptimeMillis
                        isTap = true
                    }
                if (pointers.size >= 2) {
                    isTap = false
                    val span = (pointers[0].position - pointers[1].position).getDistance()
                    previousSpan?.let { previous ->
                        if (previous != 0f) {
                            onScale(span / previous)
                        }
                    }
                    previousSpan = span
                } else {
                    previousSpan = null
                    downPosition?.let { start ->
                        val distance = pointers.firstOrNull()
                            ?.let { (it.position - start).getDistance() }
                            ?: 0f
                        if (distance > viewConfiguration.touchSlop) {
                            isTap = false
                        }
                    }
                }
                if (pointers.isEmpty()) {
                    val upTime = event.changes.firstOrNull()?.uptimeMillis ?: downTime
                    if (isTap && !event.changes.any { it.isConsumed } &&
                        upTime - downTime < viewConfiguration.longPressTimeoutMillis
                    ) {
                        onTap()
                    }
                    downPosition = null
                    isTap = false
                }
            }
        }
    }

private fun gridColor(
    background: Color,
): Color {
    val hsv = FloatArray(3)
    background.toHsv(hsv)
    hsv[2] += if (hsv[2] > 0.5f) -0.15f else 0.15f
    return Color.hsv(hsv[0], hsv[1], hsv[2])
}

@Composable
private fun DialogContent(
    dialogUiState: DialogUiState,
    onEvent: (UiEvent) -> Unit,
) {
    when (dialogUiState) {
        DialogUiState.None -> Unit

        DialogUiState.Recognizer -> {
            RecognizerDialog(
                onResult = { results ->
                    onEvent(UiEvent.RecognizeResult(results))
                },
                onDismiss = { onEvent(UiEvent.DismissDialog) },
            )
        }

        is DialogUiState.EditString -> {
            EditStringDialog(
                initialText = dialogUiState.text,
                onConfirm = { onEvent(UiEvent.UpdateText(it)) },
                onDismiss = { onEvent(UiEvent.DismissDialog) },
            )
        }

        is DialogUiState.HistorySelect -> {
            HistorySelectDialog(
                history = dialogUiState.history,
                onSelect = { onEvent(UiEvent.UpdateText(it)) },
                onDismiss = { onEvent(UiEvent.DismissDialog) },
            )
        }

        DialogUiState.HistoryClear -> {
            HistoryClearDialog(
                onClear = { onEvent(UiEvent.ClearHistory) },
                onDismiss = { onEvent(UiEvent.DismissDialog) },
            )
        }

        is DialogUiState.SelectTheme -> {
            SelectThemeDialog(
                themes = dialogUiState.themes,
                onSelect = { onEvent(UiEvent.SelectTheme(it)) },
                onDismiss = { onEvent(UiEvent.DismissDialog) },
            )
        }

        DialogUiState.Permission -> {
            PermissionDialog(
                onDismiss = { onEvent(UiEvent.DismissDialog) },
            )
        }

        is DialogUiState.SelectCandidate -> {
            SelectCandidateDialog(
                candidates = dialogUiState.candidates,
                onSelect = { onEvent(UiEvent.UpdateText(it)) },
                onDismiss = { onEvent(UiEvent.DismissDialog) },
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MaterialTheme {
        MainScreenContent(
            uiState = UiState(
                text = "Hello, World!",
                fontSizePx = 48f,
            ),
            onEvent = {},
        )
    }
}
