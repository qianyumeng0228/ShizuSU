package com.sukisu.ultra.ui.screen.xposedrepo

data class XposedRelease(
    val name: String,
    val tagName: String,
    val createdAt: String,
    val downloadUrl: String,
    val size: Long = 0L,
    val downloadCount: Int = 0,
)

data class XposedModule(
    val name: String,           // package name
    val summary: String,
    val description: String,
    val url: String,
    val homepageUrl: String,
    val sourceUrl: String,
    val latestRelease: String,
    val latestReleaseTime: Long,
    val latestReleaseTimeStr: String = "",
    val stars: Int = 0,
    val authors: List<String> = emptyList(),
    val releases: List<XposedRelease> = emptyList(),
    val readme: String = "",
)
