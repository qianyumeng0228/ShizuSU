package com.sukisu.ultra.ui.screen.xposedrepo

data class XposedModule(
    val name: String,
    val summary: String,
    val description: String,
    val url: String,
    val homepageUrl: String,
    val sourceUrl: String,
    val latestRelease: String,
    val latestReleaseTime: Long,
)
