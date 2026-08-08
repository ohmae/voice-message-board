/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.theme.Theme
import android.graphics.Color as AndroidColor

@Composable
fun SelectThemeDialog(
    themes: List<Theme>,
    onSelect: (Theme) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.theme_select)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(themes) { theme ->
                    SelectThemeItem(
                        theme = theme,
                        onClick = { onSelect(theme) },
                    )
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun SelectThemeItem(
    theme: Theme,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .border(width = 1.dp, color = Color.Black)
                .background(Color(theme.backgroundColor))
                .padding(8.dp),
        ) {
            Text(
                text = stringResource(R.string.theme_sample),
                color = Color(theme.foregroundColor),
                fontSize = 24.sp,
            )
        }
        Text(
            text = theme.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Preview
@Composable
private fun SelectThemeDialogPreview() {
    MaterialTheme {
        SelectThemeDialog(
            themes = listOf(
                Theme("White / Black", AndroidColor.WHITE, AndroidColor.BLACK),
                Theme("Black / White", AndroidColor.BLACK, AndroidColor.WHITE),
                Theme("Black / Yellow", AndroidColor.BLACK, AndroidColor.YELLOW),
                Theme("Black / Green", AndroidColor.BLACK, AndroidColor.GREEN),
            ),
            onSelect = {},
            onDismiss = {},
        )
    }
}
