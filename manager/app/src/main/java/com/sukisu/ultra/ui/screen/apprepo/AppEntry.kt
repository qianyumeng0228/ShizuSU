package com.sukisu.ultra.ui.screen.apprepo

data class AppEntry(
    val id: String,
    val name: String,
    val pkg: String,
    val versionName: String,
    val versionCode: Long,
    val summary: String,
    val author: String,
    val downloadUrl: String,
    val updatedAt: Long,
)
