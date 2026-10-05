package com.sukisu.ultra.ui.screen.settings.hosts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * [ShizuSU Phase3 · 补丁3 自研·占位] hosts 隐藏设置项占位。
 *
 * 本阶段只放入口占位，**不实现** hosts 读写逻辑。完整「写/合并 /data/adb/ksu/hosts、
 * 配合模块 system/etc/hosts 挂载」将在 Phase 4（Root 隐藏增强）与隐藏开关合并统一实现
 * （清单 3.6 / 4.3；核验 notes/next.md §6：Next 全仓无 hosts 集成代码，SnackbarHostState
 * 等命中与 hosts 屏蔽无关）。
 *
 * 接入点：把 [HideHostsPlaceholder] 嵌入设置页（Material / Miuix 两套主题任选其一渲染），
 * 或在 ModuleScreen 工具菜单加同名入口；ksud 侧对应 `module hosts` 占位命令（见
 * module.rs::hosts_hide）。本占位不读取/写入任何 hosts 文件，不接任何 supercall。
 */
@Composable
fun HideHostsPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hosts 隐藏",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "占位入口（Phase 3）：hosts 隐藏尚未实现，将在 Phase 4 与 Root 隐藏增强统一提供。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
