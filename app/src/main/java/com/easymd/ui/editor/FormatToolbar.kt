package com.easymd.ui.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class ToolItem(
    val icon: ImageVector,
    val label: String,
    val action: FormatAction
)

@Composable
fun FormatToolbar(
    onAction: (FormatAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val groups: List<List<ToolItem>> = listOf(
        listOf(
            ToolItem(Icons.Outlined.FormatBold,          "粗体",  FormatAction.BOLD),
            ToolItem(Icons.Outlined.FormatItalic,        "斜体",  FormatAction.ITALIC),
            ToolItem(Icons.Outlined.FormatStrikethrough, "删除线", FormatAction.STRIKETHROUGH),
            ToolItem(Icons.Outlined.Code,                "代码",  FormatAction.INLINE_CODE),
        ),
        listOf(
            ToolItem(Icons.Outlined.FormatListBulleted,  "无序列表", FormatAction.UNORDERED_LIST),
            ToolItem(Icons.Outlined.FormatListNumbered,  "有序列表", FormatAction.ORDERED_LIST),
            ToolItem(Icons.Outlined.CheckBox,            "任务列表", FormatAction.TASK_LIST),
            ToolItem(Icons.Outlined.FormatQuote,         "引用",   FormatAction.QUOTE),
        ),
        listOf(
            ToolItem(Icons.Outlined.Link,       "链接",  FormatAction.LINK),
            ToolItem(Icons.Outlined.Image,      "图片",  FormatAction.IMAGE),
            ToolItem(Icons.Outlined.TableChart, "表格",  FormatAction.TABLE),
        ),
        listOf(
            ToolItem(Icons.Outlined.KeyboardHide, "收键盘", FormatAction.HR), // reused as dismiss
        )
    )

    Surface(
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            groups.forEachIndexed { gi, group ->
                group.forEach { tool ->
                    IconButton(onClick = { onAction(tool.action) }) {
                        Icon(
                            tool.icon,
                            contentDescription = tool.label,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (gi < groups.lastIndex) {
                    VerticalDivider(
                        modifier = Modifier.height(24.dp).padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}
