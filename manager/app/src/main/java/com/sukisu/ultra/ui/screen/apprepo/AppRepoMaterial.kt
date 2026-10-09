package com.sukisu.ultra.ui.screen.apprepo

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ChromeReaderMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.material.ExpressiveScaffold
import com.sukisu.ultra.ui.component.material.TonalCard
import com.sukisu.ultra.ui.component.material.TopBarBackButton
import com.sukisu.ultra.ui.component.material.expressiveTopAppBarColors
import com.sukisu.ultra.ui.util.ApkDownloader
import com.sukisu.ultra.ui.viewmodel.AppRepoUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun isoTime(ts: Long): String {
    if (ts <= 0) return ""
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(ts * 1000))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRepoScreenMaterial(state: AppRepoUiState, actions: AppRepoActions) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<AppEntry?>(null) }

    if (selected != null) {
        AppDetailMaterial(app = selected!!, onBack = { selected = null }, actions = actions)
        return
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.White,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.app_repo_title)) },
                navigationIcon = { TopBarBackButton(onClick = actions.onBack) },
                colors = expressiveTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.White),
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState()),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索应用...") },
                singleLine = true,
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    state.error != null -> Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(state.error)
                        Button(onClick = actions.onRetry) { Text(stringResource(R.string.retry)) }
                    }
                    else -> {
                        val filtered = state.apps.filter {
                            query.isBlank() || it.name.contains(query, ignoreCase = true) ||
                                it.pkg.contains(query, ignoreCase = true)
                        }
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(13.dp),
                        ) {
                            items(filtered, key = { it.id }) { app ->
                                TonalCard(onClick = { selected = app }) {
                                    Column(modifier = Modifier.padding(16.dp, 14.dp, 16.dp, 10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                app.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        val sub = buildList {
                                            if (app.pkg.isNotBlank()) add(app.pkg)
                                            if (app.versionName.isNotBlank()) add(app.versionName)
                                        }.joinToString(" · ")
                                        if (sub.isNotBlank()) {
                                            Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (app.summary.isNotBlank()) {
                                            Spacer(Modifier.height(6.dp))
                                            Text(app.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        HorizontalDivider()
                                        Spacer(Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            Text(isoTime(app.updatedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailMaterial(app: AppEntry, onBack: () -> Unit, actions: AppRepoActions) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val dlState by ApkDownloader.states.collectAsStateWithLifecycle()
    val fileName = "${app.id}.apk"
    val st = dlState[fileName]

    fun openUrl(u: String) {
        if (u.isBlank()) return
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    ExpressiveScaffold(
        containerColor = androidx.compose.ui.graphics.Color.White,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(app.name) },
                navigationIcon = { TopBarBackButton(onClick = onBack) },
                colors = expressiveTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.White),
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState()),
            )
        }
    ) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text(app.pkg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Text("简介", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(app.summary, style = MaterialTheme.typography.bodyMedium)
            }
            item {
                Text("版本信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                if (app.versionName.isNotBlank()) Text("版本: ${app.versionName}", style = MaterialTheme.typography.bodyMedium)
                if (app.author.isNotBlank()) Text("作者: ${app.author}", style = MaterialTheme.typography.bodyMedium)
                Text("更新时间: ${isoTime(app.updatedAt)}", style = MaterialTheme.typography.bodyMedium)
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${app.id}.apk", style = MaterialTheme.typography.bodySmall)
                    }
                    when (st) {
                        is ApkDownloader.DlState.Downloading -> {
                            CircularProgressIndicator(progress = { st.percent / 100f }, modifier = Modifier.size(20.dp))
                        }
                        is ApkDownloader.DlState.Failed -> {
                            FilledTonalButton(onClick = {
                                val url = app.downloadUrl.ifBlank { "https://github.com/qianyumeng0228/ShizuSU-Apps/raw/main/private/${app.id}.apk" }
                                scope.launch { ApkDownloader.downloadAndInstall(ctx, url, fileName) }
                            }) { Text("重试") }
                        }
                        else -> {
                            FilledTonalButton(onClick = {
                                val url = app.downloadUrl.ifBlank { "https://github.com/qianyumeng0228/ShizuSU-Apps/raw/main/private/${app.id}.apk" }
                                scope.launch { ApkDownloader.downloadAndInstall(ctx, url, fileName) }
                            }) { Text("下载") }
                        }
                    }
                }
            }
        }
    }
}
