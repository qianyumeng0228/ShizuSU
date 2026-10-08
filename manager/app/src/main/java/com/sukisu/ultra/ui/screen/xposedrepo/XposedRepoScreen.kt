package com.sukisu.ultra.ui.screen.xposedrepo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.ui.LocalUiMode
import com.sukisu.ultra.ui.UiMode
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.viewmodel.XposedRepoUiState
import com.sukisu.ultra.ui.viewmodel.XposedRepoViewModel

@Composable
fun XposedRepoScreen() {
    val navigator = LocalNavigator.current
    val vm = viewModel<XposedRepoViewModel>()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val actions = XposedRepoActions(
        onBack = { navigator.pop() },
        onRetry = vm::refresh,
        onOpenModule = { mod ->
            val target = mod.url.ifBlank { mod.homepageUrl }.ifBlank { mod.sourceUrl }
            if (target.isNotBlank()) {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(target))
                com.sukisu.ultra.ksuApp.startActivity(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        },
    )
    when (LocalUiMode.current) {
        UiMode.Miuix -> XposedRepoScreenMiuix(uiState, actions)
        UiMode.Material -> XposedRepoScreenMaterial(uiState, actions)
    }
}

data class XposedRepoActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onOpenModule: (XposedModule) -> Unit,
)
