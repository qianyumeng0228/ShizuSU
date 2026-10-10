package com.sukisu.ultra.ui.screen.xposedrepo

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.sukisu.ultra.ui.component.material.TonalCard
import com.sukisu.ultra.ui.component.material.TopBarBackButton
import com.sukisu.ultra.ui.component.material.ExpressiveScaffold
import com.sukisu.ultra.ui.component.material.ExpressiveTabRow
import com.sukisu.ultra.ui.component.material.expressiveTopAppBarColors
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import androidx.compose.material.icons.automirrored.outlined.ChromeReaderMode
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.util.ApkDownloader
import com.sukisu.ultra.ui.viewmodel.XposedRepoUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private fun formatRelative(ts: Long): String {
    if (ts <= 0) return "未知"
    val now = System.currentTimeMillis()
    val diff = now - ts
    return when {
        diff < 60_000L -> "刚刚"
        diff < 3600_000L -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} 分钟前"
        diff < 86400_000L -> "${TimeUnit.MILLISECONDS.toHours(diff)} 小时前"
        diff < 30L * 86400_000L -> "${TimeUnit.MILLISECONDS.toDays(diff)} 天前"
        else -> SimpleDateFormat("yyyy年M月d日", Locale.getDefault()).format(Date(ts))
    }
}

internal fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return ""
    val mb = bytes / 1024.0 / 1024.0
    return String.format(Locale.getDefault(), "%.1f MiB", mb)
}

internal fun displayName(mod: XposedModule): String {
    val d = mod.description.trim()
    return d.ifBlank { mod.name }
}

internal fun safe(v: String?): String = when {
    v.isNullOrBlank() || v == "null" -> "未知"
    else -> v
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XposedRepoScreenMaterial(state: XposedRepoUiState, actions: XposedRepoActions) {
    var query by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Xposed 仓库") },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = androidx.compose.ui.graphics.Color.Black,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.Black,
                ),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索模块...") },
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
                        Button(onClick = actions.onRetry) { Text("重试") }
                    }
                    else -> {
                        val filtered = state.modules.filter {
                            query.isBlank() ||
                                displayName(it).contains(query, ignoreCase = true) ||
                                it.name.contains(query, ignoreCase = true) ||
                                it.summary.contains(query, ignoreCase = true)
                        }
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(13.dp),
                        ) {
                            items(filtered, key = { it.name }) { mod ->
                                val installedLabel = remember(mod.name) {
                                    runCatching {
                                        ctx.packageManager.getApplicationInfo(mod.name, 0)?.let {
                                            ctx.packageManager.getApplicationLabel(it).toString()
                                        }
                                    }.getOrNull()
                                }
                                val installedVersion = remember(mod.name) {
                                    runCatching { ctx.packageManager.getPackageInfo(mod.name, 0).versionName }.getOrNull()
                                }
                                val needsUpdate = installedVersion != null && installedVersion != mod.latestRelease
                                TonalCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { actions.onOpenModule(mod) }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp, 14.dp, 16.dp, 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                installedLabel ?: displayName(mod),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            if (needsUpdate) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.errorContainer,
                                                    modifier = Modifier.padding(end = 8.dp),
                                                ) {
                                                    Text(
                                                        "需要更新",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            "ID: ${mod.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        val authors = mod.authors.filter { it.isNotBlank() && it != "null" }
                                        if (authors.isNotEmpty()) {
                                            Text(
                                                "作者: ${authors.joinToString(", ")}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (mod.summary.isNotBlank() && mod.summary != "null") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                mod.summary,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                        HorizontalDivider(thickness = Dp.Hairline)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Spacer(Modifier.weight(1f))
                                            if (mod.latestReleaseTimeStr.isNotBlank()) {
                                                Text(
                                                    mod.latestReleaseTimeStr,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XposedRepoDetailScreen(mod: XposedModule, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val dlStates by ApkDownloader.states.collectAsStateWithLifecycle()
    var extra by remember { mutableStateOf<RepoExtra?>(null) }

    LaunchedEffect(mod.name) {
        extra = XposedRepoDetailFetcher.fetch(mod.name, fallbackReadme = mod.readme)
    }

    fun openUrl(u: String) {
        if (u.isBlank()) return
        runCatching {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    val installedVersion = remember(mod.name) {
        runCatching { ctx.packageManager.getPackageInfo(mod.name, 0).versionName }.getOrNull()
    }
    val installedLabel = remember(mod.name) {
        runCatching {
            ctx.packageManager.getApplicationInfo(mod.name, 0)?.let { ctx.packageManager.getApplicationLabel(it).toString() }
        }.getOrNull()
    }

    val statusText = when {
        installedVersion == null -> "未安装"
        else -> "✓ 已安装 $installedVersion"
    }
    val showUpdateBadge = installedVersion != null && installedVersion != mod.latestRelease

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    ExpressiveScaffold(
        containerColor = Color.White,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(installedLabel ?: displayName(mod)) },
                navigationIcon = { TopBarBackButton(onClick = onBack) },
                actions = {
                    if (mod.homepageUrl.isNotBlank()) {
                        IconButton(onClick = { openUrl(mod.homepageUrl) }) {
                            Icon(Icons.AutoMirrored.Outlined.ChromeReaderMode, contentDescription = "打开主页")
                        }
                    }
                },
                colors = expressiveTopAppBarColors(containerColor = Color.White),
                scrollBehavior = scrollBehavior,
            )
        }
    ) { pad ->
        val tabs = listOf("自述", "版本", "信息")
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), overscrollEffect = null) { page ->
                when (page) {
                    0 -> ReadmeTab(mod, extra, { openUrl(it) }, pad)
                    1 -> ReleasesTab(mod, extra, dlStates, scope, ctx, pad)
                    2 -> InfoTab(mod, extra, { openUrl(it) }, pad)
                }
            }
            ExpressiveTabRow(
                selectedTabIndex = pagerState.currentPage,
                tabs = tabs,
                onTabClick = { i -> scope.launch { pagerState.animateScrollToPage(i, animationSpec = PagerNavigationSpringSpec) } },
                modifier = Modifier.padding(top = pad.calculateTopPadding()),
            )
        }
    }
}

@Composable
private fun ReadmeTab(mod: XposedModule, extra: RepoExtra?, openUrl: (String) -> Unit, pad: PaddingValues) {
    val readme = extra?.readmeZh ?: ""
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 56.dp, bottom = 16.dp)) {
        item {
            when {
                readme.isNotBlank() -> {
                    com.sukisu.ultra.ui.component.markdown.GithubMarkdown(
                        content = readme,
                        isMarkdown = true,
                        baseUrl = "Xposed-Modules-Repo/${mod.name}/main/",
                    )
                }
                extra != null -> Text("README 加载失败", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> Text("加载中...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReleasesTab(mod: XposedModule, extra: RepoExtra?, dlStates: Map<String, ApkDownloader.DlState>, scope: CoroutineScope, ctx: Context, pad: PaddingValues) {
    // Use enrich's full release list if available (30 versions), fallback to list data (1 latest)
    val releases = extra?.releasesFull?.takeIf { it.isNotEmpty() } ?: mod.releases
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 56.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        items(releases) { r ->
            val fileName = "xposed-${mod.name}-${r.tagName}.apk"
            val st = dlStates[fileName]
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(r.name.ifBlank { r.tagName }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(r.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val body = extra?.releaseBodies?.get(r.tagName) ?: ""
                val sections = parseChangelog(body)
                if (sections.isNotEmpty()) {
                    sections.forEach { sec ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(sec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        sec.items.forEach { item ->
                            Text("• $item", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(safe(r.downloadUrl.substringAfterLast("/")), style = MaterialTheme.typography.bodySmall)
                        val meta = buildList {
                            if (r.size > 0) add(formatBytes(r.size))
                            if (r.downloadCount > 0) add("${r.downloadCount} downloads")
                        }.joinToString(" · ")
                        if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    when (st) {
                        is ApkDownloader.DlState.Downloading -> {
                            androidx.compose.material3.CircularProgressIndicator(progress = { st.percent / 100f }, modifier = Modifier.size(20.dp))
                        }
                        is ApkDownloader.DlState.Failed -> {
                            androidx.compose.material3.FilledTonalButton(onClick = { scope.launch { ApkDownloader.downloadAndInstall(ctx, r.downloadUrl, fileName) } }) { Text("重试") }
                        }
                        else -> {
                            androidx.compose.material3.FilledTonalButton(onClick = { scope.launch { ApkDownloader.downloadAndInstall(ctx, r.downloadUrl, fileName) } }) { Text("下载") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoTab(mod: XposedModule, extra: RepoExtra?, openUrl: (String) -> Unit, pad: PaddingValues) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 56.dp, bottom = 16.dp)) {
        item {
            Text("协作者", style = MaterialTheme.typography.titleSmall)
            val colls = (extra?.contributors?.takeIf { it.isNotEmpty() } ?: mod.authors.takeIf { it.isNotEmpty() })?.filter { it.isNotBlank() && it != "null" }
            Text(if (colls.isNullOrEmpty()) "未知" else colls.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            Text("主页", style = MaterialTheme.typography.titleSmall)
            Text(if (mod.homepageUrl.isNotBlank() && mod.homepageUrl != "null") mod.homepageUrl else "未知",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { openUrl(mod.homepageUrl) })
        }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            Text("源码", style = MaterialTheme.typography.titleSmall)
            Text(if (mod.sourceUrl.isNotBlank() && mod.sourceUrl != "null") mod.sourceUrl else "未知",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { openUrl(mod.sourceUrl) })
        }
    }
}

@Composable
private fun rememberBitmap(url: String): android.graphics.Bitmap? {
    if (url.isBlank()) return null
    return produceState<android.graphics.Bitmap?>(initialValue = null, url) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val req = okhttp3.Request.Builder().url(url).build()
                XposedRepoDetailFetcher.client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) android.graphics.BitmapFactory.decodeStream(resp.body.byteStream()) else null
                }
            }.getOrNull()
        }
    }.value
}

@Composable
private fun InfoChip(label: String, value: String) {
    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}
