package com.sukisu.ultra.ui.screen.apprepo

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.LocalUiMode
import com.sukisu.ultra.ui.UiMode
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.util.ApkDownloader
import com.sukisu.ultra.ui.viewmodel.AppRepoUiState
import com.sukisu.ultra.ui.viewmodel.AppRepoViewModel
import kotlinx.coroutines.launch

@Composable
fun AppRepoScreen() {
    val navigator = LocalNavigator.current
    val vm = viewModel<AppRepoViewModel>()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val actions = AppRepoActions(
        onBack = { navigator.pop() },
        onRetry = vm::refresh,
        onOpenApp = { app ->
            val url = app.downloadUrl.ifBlank {
                "https://github.com/qianyumeng0228/ShizuSU-Apps/raw/main/private/${app.id}.apk"
            }
            scope.launch {
                com.sukisu.ultra.ui.util.ApkDownloader.downloadAndInstall(
                    ctx, url, "${app.id}.apk"
                )
            }
        },
    )
    when (LocalUiMode.current) {
        UiMode.Miuix -> AppRepoScreenMiuix(uiState, actions)
        UiMode.Material -> AppRepoScreenMaterial(uiState, actions)
    }
}

data class AppRepoActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onOpenApp: (AppEntry) -> Unit,
)
