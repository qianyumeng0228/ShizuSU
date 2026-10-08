package com.sukisu.ultra.ui.screen.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import com.sukisu.ultra.BuildConfig
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.LocalUiMode
import com.sukisu.ultra.ui.UiMode
import com.sukisu.ultra.ui.navigation3.LocalNavigator

@Composable
fun AboutScreen() {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val htmlString = stringResource(
        id = R.string.about_source_code,
        "<b><a href=\"https://github.com/qianyumeng0228/ShizuSU\">GitHub</a></b>",
        "<b><a href=\"https://t.me/shizusux\">Telegram</a></b>",
    )
    val state = AboutUiState(
        title = stringResource(R.string.about),
        appName = stringResource(R.string.app_name),
        versionName = BuildConfig.VERSION_NAME,
        links = extractLinks(htmlString) + listOf(
            LinkInfo(
                fullText = stringResource(R.string.about_qq_group),
                url = "https://qm.qq.com/q/pp4DKkP3NK",
            ),
            LinkInfo(
                fullText = stringResource(R.string.about_website),
                url = "https://shizusu.cc.cd",
            ),
        ),
    )
    val actions = AboutScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onOpenLink = { url ->
            runCatching {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if (url.startsWith("mqqapi://")) {
                    val resolved = context.packageManager.resolveActivity(intent, 0)
                    if (resolved == null) {
                        Toast.makeText(context, "未检测到 QQ", Toast.LENGTH_SHORT).show()
                        return@runCatching
                    }
                }
                context.startActivity(intent)
            }.onFailure {
                runCatching { uriHandler.openUri(url) }
                    .onFailure { e ->
                        Toast.makeText(context, "无法打开链接: $url", Toast.LENGTH_SHORT).show()
                        android.util.Log.e("AboutScreen", "openLink failed", e)
                    }
            }
        },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> AboutScreenMiuix(state, actions)
        UiMode.Material -> AboutScreenMaterial(state, actions)
    }
}
