package com.sukisu.ultra.ui.screen.settings.hosts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [ShizuSU 补丁4 · 4.3 自研] hosts 隐藏面板（完成 Phase 3 的 HideHostsPlaceholder）。
 *
 * Next 全仓无 hosts 集成（notes/next.md §6，清单 D8），本面板为 ShizuSU 自研：管理器侧通过 root
 * shell 调用 ksud `module hosts` 子命令，读写全局 hosts 文件 /data/adb/ksu/hosts：
 *  - 刷新：`ksud module hosts list`
 *  - 添加：`ksud module hosts add <host>`（默认 0.0.0.0）
 *  - 删除：`ksud module hosts remove <host>`
 *
 * 复用基线 root shell idiom（与 ui/util/ModuleBackupRestore.kt 同源 `Shell.cmd(...).exec()`）。
 *
 * 待实测：本面板只维护 /data/adb/ksu/hosts；该文件是否被模块挂载/合并到 /system/etc/hosts 对
 * 检测 App 生效，取决于具体模块（Magic Mount），不在本面板能力内。不内置任何检测域名清单。
 *
 * 接入点：把 [HostsHidePanel] 嵌入设置页 hosts 区（Material/Miuix 主题任选其一渲染），或放到
 * App Profile 详情页（清单 4.3：App Profile 联动 hosts 入口）。本文件自包含、不新增 R.string、
 * 不接 supercall，仅依赖已存在的 Shell/Compose。
 */
private fun shellOut(cmd: String): String = runCatching {
    Shell.cmd(cmd).exec().joinToString("\n") { it }
}.getOrDefault("(shell error)")

@Composable
fun HostsHidePanel() {
    val scope = rememberCoroutineScope()
    var hosts by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }

    fun refresh() {
        scope.launch {
            hosts = withContext(Dispatchers.IO) { shellOut("ksud module hosts list") }
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) { refresh() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Hosts 隐藏")
            Text(
                "维护 /data/adb/ksu/hosts 白名单。条目默认指向 0.0.0.0；" +
                    "是否对检测 App 生效取决于模块挂载（待实测）。",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )

            Row(modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    singleLine = true,
                    label = { Text("要隐藏的域名，如 example.com") },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = {
                    val host = input.trim()
                    if (host.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            Shell.cmd("ksud module hosts add $host").exec()
                            withContext(Dispatchers.Main) {
                                input = ""
                                refresh()
                            }
                        }
                    }
                }) { Text("添加并指向 0.0.0.0") }

                Button(onClick = {
                    val host = input.trim()
                    if (host.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            Shell.cmd("ksud module hosts remove $host").exec()
                            withContext(Dispatchers.Main) {
                                refresh()
                            }
                        }
                    }
                }) { Text("删除") }
            }

            Text(
                text = hosts.ifBlank { "(空，尚无 hosts 条目)" },
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
