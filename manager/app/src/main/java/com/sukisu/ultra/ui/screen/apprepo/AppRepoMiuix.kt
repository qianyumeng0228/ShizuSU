package com.sukisu.ultra.ui.screen.apprepo

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.util.ApkDownloader
import com.sukisu.ultra.ui.viewmodel.AppRepoUiState
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.FileDownloads
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun isoTime(ts: Long): String {
    if (ts <= 0) return ""
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(ts * 1000))
}

@Composable
fun AppRepoScreenMiuix(state: AppRepoUiState, actions: AppRepoActions) {
    var selected by remember { mutableStateOf<AppEntry?>(null) }

    if (selected != null) {
        AppDetailMiuix(app = selected!!, onBack = { selected = null })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.app_repo_title),
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(imageVector = MiuixIcons.Back, contentDescription = null, tint = colorScheme.onSurface)
                    }
                },
            )
        }
    ) { inner ->
        Box(modifier = Modifier.fillMaxSize().padding(inner)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(state.error ?: "未知")
                    TextButton(text = stringResource(R.string.retry), onClick = actions.onRetry)
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    items(state.apps, key = { it.id }) { app ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { selected = app }) {
                            Column(modifier = Modifier.padding(16.dp, 14.dp, 16.dp, 10.dp)) {
                                Text(
                                    text = app.name,
                                    fontSize = 16.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(4.dp))
                                val sub = buildList {
                                    if (app.pkg.isNotBlank()) add(app.pkg)
                                    if (app.versionName.isNotBlank()) add(app.versionName)
                                }.joinToString(" · ")
                                if (sub.isNotBlank()) {
                                    Text(
                                        text = sub,
                                        fontSize = 12.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight(550),
                                        color = colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                                if (app.summary.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = app.summary,
                                        fontSize = 14.sp,
                                        color = colorScheme.onSurfaceVariantSummary,
                                        maxLines = 4,
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    Text(
                                        text = isoTime(app.updatedAt),
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppDetailMiuix(app: AppEntry, onBack: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val dlStates by ApkDownloader.states.collectAsStateWithLifecycle()
    val fileName = "${app.id}.apk"
    val st = dlStates[fileName]

    Scaffold(
        topBar = {
            TopAppBar(
                title = app.name,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = MiuixIcons.Back, contentDescription = null, tint = colorScheme.onSurface)
                    }
                },
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(app.pkg, fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
            Text("简介", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Text(app.summary, fontSize = 14.sp)
            Text("版本信息", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            if (app.versionName.isNotBlank()) Text("版本: ${app.versionName}", fontSize = 14.sp)
            if (app.author.isNotBlank()) Text("作者: ${app.author}", fontSize = 14.sp)
            Text("更新时间: ${isoTime(app.updatedAt)}", fontSize = 14.sp)
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(MiuixIcons.FileDownloads, contentDescription = null, tint = colorScheme.onSurfaceVariantSummary)
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text("${app.id}.apk", fontSize = 13.sp)
                }
                when (st) {
                    is ApkDownloader.DlState.Downloading -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    is ApkDownloader.DlState.Failed -> TextButton(text = "重试", onClick = {
                        val url = app.downloadUrl.ifBlank { "https://github.com/qianyumeng0228/ShizuSU-Apps/raw/main/private/${app.id}.apk" }
                        scope.launch { ApkDownloader.downloadAndInstall(ctx, url, fileName) }
                    })
                    else -> TextButton(text = "下载", onClick = {
                        val url = app.downloadUrl.ifBlank { "https://github.com/qianyumeng0228/ShizuSU-Apps/raw/main/private/${app.id}.apk" }
                        scope.launch { ApkDownloader.downloadAndInstall(ctx, url, fileName) }
                    })
                }
            }
        }
    }
}
