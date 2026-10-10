package com.sukisu.ultra.ui.screen.xposedrepo

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sukisu.ultra.ui.util.ApkDownloader
import com.sukisu.ultra.ui.viewmodel.XposedRepoUiState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XposedRepoScreenMiuix(state: XposedRepoUiState, actions: XposedRepoActions) {
    var query by remember { mutableStateOf("") }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var selected by remember { mutableStateOf<XposedModule?>(null) }

    if (selected != null) {
        XposedRepoDetailMiuix(mod = selected!!, onBack = { selected = null })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Xposed 仓库",
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(imageVector = MiuixIcons.Back, contentDescription = null, tint = colorScheme.onSurface)
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            androidx.compose.material3.OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("搜索模块...", color = colorScheme.onSurfaceVariantSummary) },
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
                        Text(state.error ?: "未知")
                        TextButton(text = "重试", onClick = actions.onRetry)
                    }
                    else -> {
                        val q = query.trim()
                        val filtered = state.modules.filter {
                            q.isBlank() ||
                                displayName(it).contains(q, ignoreCase = true) ||
                                it.name.contains(q, ignoreCase = true) ||
                                it.summary.contains(q, ignoreCase = true)
                        }
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(13.dp),
                        ) {
                            items(filtered, key = { it.name }) { mod ->
                                val installedVersion = remember(mod.name) {
                                    runCatching { ctx.packageManager.getPackageInfo(mod.name, 0).versionName }.getOrNull()
                                }
                                val needsUpdate = installedVersion != null && installedVersion != mod.latestRelease
                                Card(modifier = Modifier.fillMaxWidth().clickable { selected = mod }) {
                                    Column(modifier = Modifier.padding(16.dp, 14.dp, 16.dp, 10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = displayName(mod),
                                                fontSize = 16.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f),
                                            )
                                            if (needsUpdate) {
                                                Text(
                                                    text = "需要更新",
                                                    fontSize = 12.sp,
                                                    color = colorScheme.primary,
                                                    modifier = Modifier.padding(start = 8.dp),
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "ID: ${mod.name}",
                                            fontSize = 12.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight(550),
                                            color = colorScheme.onSurfaceVariantSummary,
                                        )
                                        val authorStr = mod.authors.filter { it.isNotBlank() && it != "null" }
                                        if (authorStr.isNotEmpty()) {
                                            Text(
                                                text = "作者: ${authorStr.joinToString(", ")}",
                                                fontSize = 12.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight(550),
                                                color = colorScheme.onSurfaceVariantSummary,
                                            )
                                        }
                                        if (mod.summary.isNotBlank() && mod.summary != "null") {
                                            Text(
                                                text = mod.summary,
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
                                                text = mod.latestReleaseTimeStr,
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
    }
}

@Composable
internal fun XposedRepoDetailMiuix(mod: XposedModule, onBack: () -> Unit) {
    android.util.Log.e("XposedDetail", "MIUIX DETAIL ENTER pkg=${mod.name}")
    androidx.activity.compose.BackHandler(onBack = onBack)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val dlStates by ApkDownloader.states.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    var extra by remember { mutableStateOf<RepoExtra?>(null) }
    androidx.compose.runtime.LaunchedEffect(mod.name) {
        extra = XposedRepoDetailFetcher.fetch(mod.name, fallbackReadme = mod.readme)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = displayName(mod),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = MiuixIcons.Back, contentDescription = null, tint = colorScheme.onSurface)
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("自述", "版本", "信息").forEachIndexed { i, t ->
                    TextButton(text = t, onClick = { tab = i })
                }
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (tab) {
                    0 -> {
                        val readme = extra?.readmeZh ?: ""
                        when {
                            readme.isNotBlank() -> {
                                com.sukisu.ultra.ui.component.markdown.GithubMarkdown(
                                    content = readme,
                                    isMarkdown = true,
                                    baseUrl = "Xposed-Modules-Repo/${mod.name}/main/",
                                )
                            }
                            extra != null -> Text("README 加载失败", color = colorScheme.onSurfaceVariantSummary)
                            else -> Text("加载中...", color = colorScheme.onSurfaceVariantSummary)
                        }
                    }
                    1 -> {
                        val relList = extra?.releasesFull?.takeIf { it.isNotEmpty() } ?: mod.releases
                        android.util.Log.e("XposedDetail", "UI version tab: ${relList.size} releases (extra=${extra != null}, releasesFull=${extra?.releasesFull?.size ?: 0}, mod.releases=${mod.releases.size})")
                        relList.forEach { r ->
                            val fileName = "xposed-${mod.name}-${r.tagName}.apk"
                            val st = dlStates[fileName]
                            Card {
                                Column {
                                    // Header row: title + tagName (left) + date (right)
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                                                .weight(1f)
                                        ) {
                                            Text(
                                                text = r.name.ifBlank { r.tagName },
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight(550),
                                                color = colorScheme.onSurface
                                            )
                                            Text(
                                                text = r.tagName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight(550),
                                                color = colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = r.createdAt,
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurfaceVariantSummary,
                                            modifier = Modifier
                                                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                                        )
                                    }
                                    // Changelog sections
                                    Spacer(Modifier.height(8.dp))
                                    val body = extra?.releaseBodies?.get(r.tagName) ?: ""
                                    val sections = parseChangelog(body)
                                    if (sections.isNotEmpty()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                                            thickness = 0.5.dp,
                                            color = colorScheme.outline.copy(alpha = 0.5f)
                                        )
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            sections.forEach { sec ->
                                                Spacer(Modifier.height(4.dp))
                                                Text(sec.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                                sec.items.forEach { item ->
                                                    Text("• $item", fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
                                                }
                                            }
                                        }
                                    }
                                    // Download row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                    ) {
                                        Icon(MiuixIcons.FileDownloads, contentDescription = null, tint = colorScheme.onSurfaceVariantSummary)
                                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                            Text(safe(r.downloadUrl.substringAfterLast("/")), fontSize = 13.sp)
                                            val meta = buildList {
                                                if (r.size > 0) add(formatBytes(r.size))
                                                if (r.downloadCount > 0) add("${r.downloadCount} downloads")
                                            }.joinToString(" · ")
                                            if (meta.isNotBlank()) Text(meta, fontSize = 11.sp, color = colorScheme.onSurfaceVariantSummary)
                                        }
                                        when (st) {
                                            is ApkDownloader.DlState.Downloading -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                            is ApkDownloader.DlState.Failed -> TextButton(text = "重试", onClick = {
                                                scope.launch { ApkDownloader.downloadAndInstall(ctx, r.downloadUrl, fileName) }
                                            })
                                            else -> TextButton(text = "下载", onClick = {
                                                scope.launch { ApkDownloader.downloadAndInstall(ctx, r.downloadUrl, fileName) }
                                            })
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                    2 -> {
                        val colls = mod.authors.filter { it.isNotBlank() && it != "null" }
                        Text("协作者", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        Text(if (colls.isEmpty()) "未知" else colls.joinToString(", "), fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
                        Spacer(Modifier.height(8.dp))
                        Text("主页", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        Text(if (mod.homepageUrl.isNotBlank() && mod.homepageUrl != "null") mod.homepageUrl else "未知", fontSize = 14.sp, color = colorScheme.primary,
                            modifier = Modifier.clickable {
                                if (mod.homepageUrl.isNotBlank() && mod.homepageUrl != "null") {
                                    runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(mod.homepageUrl))) }
                                }
                            })
                        Spacer(Modifier.height(8.dp))
                        Text("源码", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        Text(if (mod.sourceUrl.isNotBlank() && mod.sourceUrl != "null") mod.sourceUrl else "未知", fontSize = 14.sp, color = colorScheme.primary,
                            modifier = Modifier.clickable {
                                if (mod.sourceUrl.isNotBlank() && mod.sourceUrl != "null") {
                                    runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(mod.sourceUrl))) }
                                }
                            })
                    }
                }
            }
        }
    }
}
