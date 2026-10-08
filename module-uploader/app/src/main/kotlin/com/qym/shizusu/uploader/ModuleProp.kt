package com.qym.shizusu.uploader

data class ModuleProp(
    val id: String,
    val name: String,
    val author: String,
    val version: String,
    val versionCode: Int,
    val description: String,
    val updateJson: String
) {
    companion object {
        fun parse(text: String): ModuleProp {
            val map = LinkedHashMap<String, String>()
            for (line in text.lineSequence()) {
                val idx = line.indexOf('=')
                if (idx > 0) {
                    map[line.substring(0, idx).trim()] = line.substring(idx + 1).trim()
                }
            }
            fun get(k: String) = map[k] ?: ""
            return ModuleProp(
                id = get("id"),
                name = get("name"),
                author = get("author"),
                version = get("version"),
                versionCode = get("versionCode").toIntOrNull() ?: 0,
                description = get("description"),
                updateJson = get("updateJson")
            )
        }
    }
}
