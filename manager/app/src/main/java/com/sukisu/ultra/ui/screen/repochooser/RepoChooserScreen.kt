package com.sukisu.ultra.ui.screen.repochooser

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.LocalUiMode
import com.sukisu.ultra.ui.UiMode
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.navigation3.Route
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun RepoChooserScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val onModuleRepo: () -> Unit = { navigator.push(Route.ModuleRepo) }
    val onXposedRepo: () -> Unit = { navigator.push(Route.XposedRepo) }
    val onAppRepo: () -> Unit = { navigator.push(Route.AppRepo) }
    val onComingSoon: () -> Unit = {
        Toast.makeText(context, R.string.repo_chooser_coming_soon, Toast.LENGTH_SHORT).show()
    }
    when (LocalUiMode.current) {
        UiMode.Miuix -> RepoChooserMiuix(onBack = { navigator.pop() }, onModuleRepo, onXposedRepo, onAppRepo, onComingSoon)
        UiMode.Material -> RepoChooserMaterial(onBack = { navigator.pop() }, onModuleRepo, onXposedRepo, onAppRepo, onComingSoon)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepoChooserMaterial(
    onBack: () -> Unit,
    onModuleRepo: () -> Unit,
    onXposedRepo: () -> Unit,
    onAppRepo: () -> Unit,
    onComingSoon: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.repo_chooser_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.repo_chooser_module_name)) },
                        supportingContent = { Text(stringResource(R.string.repo_chooser_module_desc)) },
                        leadingContent = { Icon(Icons.Outlined.Download, null) },
                        modifier = Modifier.clickable(onClick = onModuleRepo),
                    )
                }
            }
            item {
                Card {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.repo_chooser_xposed_name)) },
                        supportingContent = { Text(stringResource(R.string.repo_chooser_xposed_desc)) },
                        leadingContent = { Icon(Icons.Outlined.Extension, null) },
                        modifier = Modifier.clickable(onClick = onXposedRepo),
                    )
                }
            }
            item {
                Card {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.repo_chooser_app_name)) },
                        supportingContent = { Text(stringResource(R.string.repo_chooser_app_desc)) },
                        leadingContent = { Icon(Icons.Filled.Android, null) },
                        modifier = Modifier.clickable(onClick = onAppRepo),
                    )
                }
            }
        }
    }
}

@Composable
private fun RepoChooserMiuix(
    onBack: () -> Unit,
    onModuleRepo: () -> Unit,
    onXposedRepo: () -> Unit,
    onAppRepo: () -> Unit,
    onComingSoon: () -> Unit,
) {
    MiuixScaffold(
        topBar = {
            MiuixTopAppBar(
                title = stringResource(R.string.repo_chooser_title),
                navigationIcon = {
                    MiuixIconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BasicComponent(
                title = stringResource(R.string.repo_chooser_module_name),
                summary = stringResource(R.string.repo_chooser_module_desc),
                onClick = onModuleRepo,
            )
            BasicComponent(
                title = stringResource(R.string.repo_chooser_xposed_name),
                summary = stringResource(R.string.repo_chooser_xposed_desc),
                onClick = onXposedRepo,
            )
            BasicComponent(
                title = stringResource(R.string.repo_chooser_app_name),
                summary = stringResource(R.string.repo_chooser_app_desc),
                onClick = onAppRepo,
            )
        }
    }
}
