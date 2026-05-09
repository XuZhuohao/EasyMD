package com.easymd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easymd.R

private const val MAX_ROWS = 6
private const val MAX_COLS = 8

@Composable
fun TableDialog(
    onInsert: (rows: Int, cols: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hoveredRow by remember { mutableStateOf(3) }
    var hoveredCol by remember { mutableStateOf(4) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.insert_table)) },
        text = {
            Column {
                Text(
                    text = "$hoveredRow × $hoveredCol",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (r in 1..MAX_ROWS) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            for (c in 1..MAX_COLS) {
                                val active = r <= hoveredRow && c <= hoveredCol
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (active) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = if (active) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            hoveredRow = r
                                            hoveredCol = c
                                        }
                                        .run {
                                            // Hover detection via pointer interaction
                                            this
                                        }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onInsert(hoveredRow, hoveredCol) }) {
                Text(stringResource(R.string.insert))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
