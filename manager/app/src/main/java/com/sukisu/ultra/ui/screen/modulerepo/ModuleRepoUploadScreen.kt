package com.sukisu.ultra.ui.screen.modulerepo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.ui.LocalUiMode
import com.sukisu.ultra.ui.UiMode
import com.sukisu.ultra.ui.navigation3.LocalNavigator

/**
 * [ShizuSU] 模块社区提交页入口。
 * 双端 UI 变体：Miuix / Material；共享 [ModuleRepoUploadViewModel]。
 */
@Composable
fun ModuleRepoUploadScreen(
    viewModel: ModuleRepoUploadViewModel = viewModel(),
) {
    val navigator = LocalNavigator.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val zipPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let(viewModel::onLocalZipPicked)
    }

    val actions = ModuleRepoUploadActions(
        onBack = { navigator.pop() },
        onSelectSource = viewModel::onSelectSource,
        onSelectInstalled = viewModel::onSelectInstalled,
        onPickLocalZip = { zipPicker.launch("application/zip") },
        onModuleIdChange = viewModel::onModuleIdChange,
        onModuleNameChange = viewModel::onModuleNameChange,
        onAuthorChange = viewModel::onAuthorChange,
        onVersionNameChange = viewModel::onVersionNameChange,
        onVersionCodeChange = viewModel::onVersionCodeChange,
        onSummaryZhChange = viewModel::onSummaryZhChange,
        onZygiskChange = viewModel::onZygiskChange,
        onTokenChange = viewModel::onTokenChange,
        onSubmit = viewModel::submit,
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> ModuleRepoUploadScreenMiuix(uiState, actions)
        UiMode.Material -> ModuleRepoUploadScreenMaterial(uiState, actions)
    }
}
