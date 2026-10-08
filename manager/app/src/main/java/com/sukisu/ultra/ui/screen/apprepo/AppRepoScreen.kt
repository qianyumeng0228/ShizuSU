package com.sukisu.ultra.ui.screen.apprepo

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.LocalUiMode
import com.sukisu.ultra.ui.UiMode
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.viewmodel.AppRepoUiState
import com.sukisu.ultra.ui.viewmodel.AppRepoViewModel

@Composable
fun AppRepoScreen() {
    val navigator = LocalNavigator.current
    val vm = viewModel<AppRepoViewModel>()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val actions = AppRepoActions(
        onBack = { navigator.pop() },
        onRetry = vm::refresh,
        onOpenApp = { app ->
            val url = app.downloadUrl.ifBlank {
                "https://github.com/qianyumeng0228/ShizuSU-Apps/raw/main/private/${app.id}.apk"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            ksuApp.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
