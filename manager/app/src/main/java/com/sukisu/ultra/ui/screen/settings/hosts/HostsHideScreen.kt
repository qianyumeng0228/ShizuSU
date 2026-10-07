package com.sukisu.ultra.ui.screen.settings.hosts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.LocalUiMode
import com.sukisu.ultra.ui.UiMode
import com.sukisu.ultra.ui.component.material.ExpressiveScaffold
import com.sukisu.ultra.ui.component.material.TopBarBackButton
import com.sukisu.ultra.ui.component.material.expressiveTopAppBarColors
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.theme.LocalEnableBlur
import com.sukisu.ultra.ui.util.BlurredBar
import com.sukisu.ultra.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

/**
 * [ShizuSU 补丁 · 4.3] hosts 隐藏屏幕：把已存在的自研 [HostsHidePanel] 嵌入设置页路由。
 *
 * 双主题骨架照抄 ToolsScreen（Material / Miuix 两套 Scaffold），内容区直接放 [HostsHidePanel]——
 * 面板本身是 Material3 组件，在 Miuix 主题下可正常渲染（功能优先，不改 HostsHidePanel 本身）。
 * TopAppBar 标题用 R.string.hosts_title。同目录旧占位 HideHostsPlaceholder.kt 不动。
 */
@Composable
fun HostsHideScreen() {
    val navigator = LocalNavigator.current
    when (LocalUiMode.current) {
        UiMode.Miuix -> HostsHideScreenMiuix(onBack = { navigator.pop() })
        UiMode.Material -> HostsHideScreenMaterial(onBack = { navigator.pop() })
    }
}

@Composable
private fun HostsHideScreenMaterial(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    ExpressiveScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hosts_title)) },
                navigationIcon = {
                    TopBarBackButton(onClick = onBack)
                },
                scrollBehavior = scrollBehavior,
                colors = expressiveTopAppBarColors(),
            )
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            HostsHidePanel()
        }
    }
}

@Composable
private fun HostsHideScreenMiuix(onBack: () -> Unit) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else colorScheme.surface

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.hosts_title),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            val layoutDirection = LocalLayoutDirection.current
                            Icon(
                                modifier = Modifier.graphicsLayer {
                                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                                },
                                imageVector = MiuixIcons.Back,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            HostsHidePanel()
        }
    }
}
