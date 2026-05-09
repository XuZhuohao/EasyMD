package com.easymd.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easymd.R

@Composable
fun LinkDialog(
    initialText: String = "",
    onInsert: (text: String, url: String) -> Unit,
    onDismiss: () -> Unit
) {
    var linkText by remember { mutableStateOf(initialText) }
    var url      by remember { mutableStateOf("") }
    val urlFocus = remember { FocusRequester() }

    // Auto-prepend https://
    val normalizedUrl = if (url.isNotBlank() && !url.startsWith("http")) "https://$url" else url
    val isValid = normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.insert_link)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    label = { Text(stringResource(R.string.link_text)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.url)) },
                    placeholder = { Text("https://") },
                    leadingIcon = { Icon(Icons.Outlined.Link, null) },
                    singleLine = true,
                    isError = url.isNotBlank() && !isValid,
                    supportingText = if (url.isNotBlank() && !isValid) {
                        { Text("请输入有效的 URL") }
                    } else null,
                    modifier = Modifier.fillMaxWidth().focusRequester(urlFocus)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onInsert(linkText, normalizedUrl) },
                enabled = linkText.isNotBlank() && isValid
            ) { Text(stringResource(R.string.insert)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
