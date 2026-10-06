package com.sukisu.ultra.data.repository

import android.content.Context
import com.sukisu.ultra.ksuApp

/**
 * 模块商店（在线模块仓库）源配置。
 *
 * 默认源为 ShizuSU 自建模块仓库（替代已失效的官方 modules.kernelsu.org），
 * 可通过 settings 偏好键 [KEY_BASE_URL] 运行时覆盖为任意可用源。
 */
object ModuleRepoConfig {

    /** 默认模块仓库基地址：ShizuSU 自建源（GitHub Pages）。 */
    const val DEFAULT_BASE_URL = "https://qianyumeng0228.github.io/ShizuSU-Modules"

    private const val PREFS_NAME = "settings"
    private const val KEY_BASE_URL = "module_repo_base_url"

    /** 当前生效的模块仓库基地址（settings 键覆盖优先，未写入时回退默认源）。 */
    val baseUrl: String
        get() = ksuApp.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BASE_URL, DEFAULT_BASE_URL)
            ?.trim().orEmpty().ifBlank { DEFAULT_BASE_URL }

    /** 模块目录 JSON 地址。 */
    val modulesUrl: String
        get() = baseUrl.trimEnd('/') + "/modules.json"
}
