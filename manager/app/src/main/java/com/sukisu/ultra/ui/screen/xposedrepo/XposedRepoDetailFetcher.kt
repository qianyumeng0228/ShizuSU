package com.sukisu.ultra.ui.screen.xposedrepo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RepoExtra(
    val stars: Int = 0,
    val issues: Int = -1,  // -1 = unknown/hidden
    val readmeZh: String = "",
    val readmeEn: String = "",
    val readmeRaw: String = "",  // raw README.md markdown for GithubMarkdown
    val iconUrl: String = "",
    val releaseBodies: Map<String, String> = emptyMap(),
    val tagline: String = "",
    val descZh: String = "",
    val descEn: String = "",
    val contributors: List<String> = emptyList(),
    val androidBadge: String = "",
    val xposedBadge: String = "",
)

object XposedRepoDetailFetcher {
    internal val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun log(msg: String) = android.util.Log.e("XposedDetail", msg)

    /** Try multiple mirrors, return body string or null */
    private fun fetchRaw(pkg: String, file: String): String? {
        val base = "Xposed-Modules-Repo/$pkg/main/$file"
        val candidates = listOf(
            "https://ghproxy.net/https://raw.githubusercontent.com/$base",
            "https://gh-proxy.com/https://raw.githubusercontent.com/$base",
            "https://raw.githubusercontent.com/$base",
            "https://cdn.jsdelivr.net/gh/Xposed-Modules-Repo/$pkg@main/$file",
            "https://ghfast.top/https://raw.githubusercontent.com/$base",
        )
        for (url in candidates) {
            runCatching {
                val req = Request.Builder().url(url).build()
                client.newCall(req).execute().use { resp ->
                    log("raw $file -> ${resp.code} ($url)")
                    if (resp.isSuccessful) return resp.body.string()
                }
            }.onFailure { log("raw $url FAIL: ${it.message}") }
        }
        return null
    }

    private fun tryRequest(url: String, headers: Map<String, String> = emptyMap()): okhttp3.Response? {
        repeat(2) { attempt ->
            runCatching {
                val b = Request.Builder().url(url)
                headers.forEach { (k, v) -> b.header(k, v) }
                client.newCall(b.build()).execute().use { resp ->
                    if (resp.isSuccessful) return resp
                    log("request $url attempt=$attempt code=${resp.code}")
                }
            }.onFailure { log("request $url attempt=$attempt FAIL: ${it.message}") }
            if (attempt == 0) Thread.sleep(1000)
        }
        return null
    }

    suspend fun fetch(pkg: String): RepoExtra = withContext(Dispatchers.IO) {
        log("FETCH START pkg=$pkg")
        var extra = RepoExtra()
        val repoBase = "https://raw.githubusercontent.com/Xposed-Modules-Repo/$pkg/main"
        val apiBase = "https://api.github.com/repos/Xposed-Modules-Repo/$pkg"

        // 0. Enrich mirror (pre-fetched changelog/contributors/issues, no API rate limit)
        runCatching {
            val enrichUrls = listOf(
                "https://qianyumeng0228.github.io/ShizuSU-Xposed/enrich/$pkg.json",
                "https://cdn.jsdelivr.net/gh/qianyumeng0228/ShizuSU-Xposed@main/enrich/$pkg.json",
            )
            for (u in enrichUrls) {
                val req = Request.Builder().url(u).header("User-Agent", "ShizuSU").build()
                client.newCall(req).execute().use { resp ->
                    log("enrich -> ${resp.code}")
                    if (resp.isSuccessful) {
                        val j = JSONObject(resp.body.string())
                        val changelogMap = mutableMapOf<String, String>()
                        val cl = j.optJSONArray("changelog")
                        if (cl != null) for (i in 0 until cl.length()) {
                            val e = cl.getJSONObject(i)
                            changelogMap[e.optString("tagName","")] = e.optString("body","")
                        }
                        val contribs = mutableListOf<String>()
                        val ca = j.optJSONArray("contributors")
                        if (ca != null) for (i in 0 until ca.length()) contribs.add(ca.optString(i,""))
                        extra = extra.copy(
                            issues = j.optInt("openIssues", -1),
                            releaseBodies = changelogMap,
                            contributors = contribs.filter { it.isNotBlank() },
                        )
                        return@use
                    }
                }
            }
        }.onFailure { log("enrich FAIL: ${it.message}") }

        // 1. GitHub repo metadata (fallback if enrich missing stars/issues)
        if (extra.stars == 0 || extra.issues < 0) {
            runCatching {
                val req = Request.Builder().url("$apiBase").header("User-Agent", "ShizuSU").build()
                client.newCall(req).execute().use { resp ->
                    log("api repo -> ${resp.code}")
                    if (resp.isSuccessful) {
                        val j = JSONObject(resp.body.string())
                        val newStars = if (extra.stars == 0) j.optInt("stargazers_count", 0) else extra.stars
                        val newIssues = if (extra.issues < 0) j.optInt("open_issues_count", 0) else extra.issues
                        extra = extra.copy(stars = newStars, issues = newIssues)
                    }
                }
            }.onFailure { log("api repo FAIL: ${it.message}") }
        }

        // 2. Releases (fallback if enrich has no bodies)
        if (extra.releaseBodies.isEmpty()) {
            runCatching {
                val req = Request.Builder().url("$apiBase/releases").header("User-Agent", "ShizuSU").build()
                client.newCall(req).execute().use { resp ->
                    log("api releases -> ${resp.code}")
                    if (resp.isSuccessful) {
                        val arr = JSONArray(resp.body.string())
                        val bodies = mutableMapOf<String, String>()
                        for (i in 0 until arr.length()) {
                            val r = arr.getJSONObject(i)
                            val tag = r.optString("tag_name", "")
                            val body = r.optString("body", "")
                            if (tag.isNotBlank() && body.isNotBlank()) bodies[tag] = body
                        }
                        extra = extra.copy(releaseBodies = bodies)
                        log("releases parsed: ${arr.length()} total, ${bodies.size} with body")
                    }
                }
            }.onFailure { log("api releases FAIL: ${it.message}") }
        }

        // 3. README zh
        runCatching {
            fetchRaw(pkg, "README.md")?.let { extra = extra.copy(readmeZh = it) }
        }.onFailure { log("README.md FAIL: ${it.message}") }

        // 4. README en
        runCatching {
            fetchRaw(pkg, "README_EN.md")?.let { extra = extra.copy(readmeEn = it) }
        }.onFailure { log("README_EN.md FAIL: ${it.message}") }

        // 5. Icon (candidate filenames × 3 mirrors)
        runCatching {
            val iconNames = listOf(
                "icon_$pkg.png",
                "icon.png",
                "icon_${pkg.substringAfterLast('.')}.png",
                "icon_${pkg.substringAfterLast('.').lowercase()}.png",
            )
            val mirrors = listOf(
                "https://raw.githubusercontent.com/Xposed-Modules-Repo/$pkg/main/",
                "https://cdn.jsdelivr.net/gh/Xposed-Modules-Repo/$pkg@main/",
                "https://ghproxy.net/https://raw.githubusercontent.com/Xposed-Modules-Repo/$pkg/main/",
            )
            outer@ for (name in iconNames) {
                for (base in mirrors) {
                    val u = "$base$name"
                    runCatching {
                        val req = Request.Builder().url(u).build()
                        client.newCall(req).execute().use { resp ->
                            log("icon $u -> ${resp.code}")
                            if (resp.isSuccessful) { extra = extra.copy(iconUrl = u); break@outer }
                        }
                    }
                }
            }
        }.onFailure { log("icon FAIL: ${it.message}") }

        // 5b. Contributors (fallback if enrich has none)
        if (extra.contributors.isEmpty()) {
            runCatching {
                val req = Request.Builder().url("$apiBase/contributors").header("User-Agent", "ShizuSU").build()
                client.newCall(req).execute().use { resp ->
                    log("contributors -> ${resp.code}")
                    if (resp.isSuccessful) {
                        val arr = JSONArray(resp.body.string())
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            list.add(arr.getJSONObject(i).optString("login", ""))
                        }
                        extra = extra.copy(contributors = list.filter { it.isNotBlank() })
                    }
                }
            }.onFailure { log("contributors FAIL: ${it.message}") }
        }

        val zh = extra.readmeZh
        val en = extra.readmeEn

        // 6. Parse badges from README
        val badgeRegex = Regex("""img.shields.io/badge/([^"'\)]+)""")
        val badges = badgeRegex.findAll(zh).map { it.groupValues[1].replace("%20", " ").replace("%2B", "+") }.toList()
        // strip color suffix (-3DDC84, -5C6BC0) and query string (?logo=...)
        fun cleanBadge(raw: String): String {
            var s = raw.substringBefore("?")           // strip ?logo=...
            s = s.replace(Regex("-[0-9A-Fa-f]{6}$"), "") // strip -3DDC84
            return s.trim()
        }
        // Android-16+ -> "Android 16+"
        val androidBadge = badges.firstOrNull { it.startsWith("Android-") }?.let { cleanBadge(it).replace("-", " ") }.orEmpty()
        // Xposed-API 101 -> "Xposed API 101"
        val xposedBadge = badges.firstOrNull { it.startsWith("Xposed-") }?.let { cleanBadge(it).replace("-", " ") }.orEmpty()

        // 7. Parse tagline + desc
        val tagline = Regex("<b>(.*?)</b>").find(zh)?.groupValues?.getOrNull(1).orEmpty().trim()
        val descZh = Regex("<span[^>]*>(.*?)</span>").find(zh)?.groupValues?.getOrNull(1).orEmpty().trim()
            .ifBlank {
                Regex("##\\s*简介\\s*\\n+([^#]+)").find(zh)?.groupValues?.getOrNull(1).orEmpty().trim()
            }
        val descEn = Regex("<span[^>]*>(.*?)</span>").find(en)?.groupValues?.getOrNull(1).orEmpty().trim()
            .ifBlank {
                Regex("##\\s*(Introduction|About)\\s*\\n+([^#]+)", RegexOption.IGNORE_CASE).find(en)?.groupValues?.getOrNull(2).orEmpty().trim()
            }
        extra = extra.copy(tagline = tagline, descZh = descZh, descEn = descEn, androidBadge = androidBadge, xposedBadge = xposedBadge)

        log("FETCH DONE pkg=$pkg stars=${extra.stars} issues=${extra.issues} iconUrl=${extra.iconUrl} bodies=${extra.releaseBodies.size} tagline='$tagline' descZh.len=${descZh.length} descEn.len=${descEn.length}")
        extra
    }
}

data class ChangelogSection(val title: String, val items: List<String>)

fun parseChangelog(body: String): List<ChangelogSection> {
    if (body.isBlank()) return emptyList()
    val sections = mutableListOf<ChangelogSection>()
    val lines = body.lines()
    var curTitle = ""
    var curItems = mutableListOf<String>()
    fun flush() {
        if (curTitle.isNotBlank() && curItems.isNotEmpty()) {
            sections.add(ChangelogSection(curTitle, curItems.toList()))
        }
    }
    val sectionHeaders = setOf("修复", "新增", "优化", "其他", "修复问题", "新功能", "改进")
    for (raw in lines) {
        val t = raw.trim()
        if (t.isEmpty()) continue
        // Strip markdown prefixes
        val clean = t.removePrefix("## ").removePrefix("### ").removePrefix("# ").trim()
        // Check if this line is a section header (exact match, or starts with known headers)
        val isHeader = clean in sectionHeaders ||
            sectionHeaders.any { clean.startsWith("$it：") || clean.startsWith("$it:") }
        if (isHeader) {
            flush()
            curTitle = clean.removeSuffix("：").removeSuffix(":").trim()
            curItems = mutableListOf()
        } else if (curTitle.isNotBlank()) {
            // Item line: keep as-is (preserve "1. xxx" numbering)
            curItems.add(t)
        }
    }
    flush()
    return sections
}
