package com.sukisu.ultra.ui.component.material

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.sukisu.ultra.R

/**
 * 「点一下弹输入框改字符串」的设置项(Material 版,对应 Miuix 的 `StringEditArrow`)。
 *
 * · 摘要显示调用方给的 summary(通常含当前值)
 * · 每次打开都用当前值做初值;点确定才写回
 *
 * ---- ShizuSU 移植说明（照搬 7kimisu v2.29 SegmentedSettingsItems.kt:84-141 的 SegmentedStringItem）----
 *   包名 com.sevenk.core -> com.sukisu.ultra；[自研] ShizuSU 无 materialDialogColor()，
 *   其 ExpressiveDialog 的 containerColor 已有合理默认值，故省略该行；defaultSegmentedColors()
 *   在 SegmentedList.kt 内文件私有，故本组件不再外露 colors 参数，由 SegmentedListItem 自身默认。
 */
@Composable
fun SegmentedStringItem(
    title: String,
    value: String,
    icon: ImageVector? = null,
    summary: String? = null,
    emptySummary: String = "未设置（点一下输入）",
    enabled: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    SegmentedListItem(
        onClick = { showDialog = true },
        enabled = enabled,
        headlineContent = { Text(title) },
        leadingContent = icon?.let { { Icon(it, title) } },
        supportingContent = { Text(summary ?: value.ifBlank { emptySummary }) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        },
    )

    if (showDialog) {
        // 每次打开都拿最新值当草稿(和 Miuix 的 StringEditDialog 行为一致)
        var draft by remember(value) { mutableStateOf(value) }

        ExpressiveDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(draft)
                        showDialog = false
                    },
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}
