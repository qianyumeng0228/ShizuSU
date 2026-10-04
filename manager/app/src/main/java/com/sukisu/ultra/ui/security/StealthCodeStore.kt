package com.sukisu.ultra.ui.security

import android.content.Context
import com.topjohnwu.superuser.ShellUtils
// TODO(ShizuSU 集成)：基线设置仓库为 com.sukisu.ultra.data.repository.SettingsRepositoryImpl，
//   需新增可写属性 `stealthCode: String?`（对应 7kimisu SettingsRepositoryImpl().stealthCode）。
// import com.sukisu.ultra.data.repository.SettingsRepositoryImpl
import com.sukisu.ultra.ui.util.getRootShell // TODO(ShizuSU 集成)：确认基线 root shell 入口（KsuCli.kt 附近）

/**
 * 隐身密令的「跨卸载备份」。
 *
 * 为什么需要它(这是个真会把人锁死的 bug):
 *   · 隐身状态存在**内核侧** /data/adb/shizusu/stealth —— 卸载 App 也不会丢;
 *   · 而密令数字以前只存在 App 自己的 shared_prefs 里 —— 卸载就没了。
 *   于是:改了自定义密令 -> 卸载重装 -> 内核仍处于隐身(界面伪装成"未安装"),
 *   而 App 里的"期望密令"退回默认值 -> 输自己那个自定义密令不匹配 -> 静默忽略 -> 锁死。
 *
 * 所以这里把密令额外写一份到 /data/adb/shizusu/stealth_code(root,卸载不丢),
 * 读取时优先用它,拿不到才退回 App 设置,再退回默认值。
 *
 * ---- ShizuSU 移植说明（照搬 7kimisu StealthCodeStore.kt v2.29）----
 *   [自研] 数据目录 /data/adb/sevenk -> /data/adb/shizusu（与内核 KSU_STEALTH_PATH 同目录）。
 */
object StealthCodeStore {

    private const val TAG = "shizusu-stealth"

    // [自研] 路径适配：7kimisu 原值 DIR="/data/adb/sevenk"
    private const val DIR = "/data/adb/shizusu"
    private const val PATH = "$DIR/stealth_code"

    /**
     * 改名迁移的**只读兜底**：旧目录下的那份密令。
     * [自研] ShizuSU 以基线 SukiSU 数据目录 /data/adb/ksu 为旧路径兜底
     * （7kimisu 原值 LEGACY_PATH="/data/adb/ksu/stealth_code"，语义沿用）。
     */
    private const val LEGACY_PATH = "/data/adb/ksu/stealth_code"

    /**
     * 默认密令。
     * [自研] ShizuSU 编译期可配：优先取 BuildConfig.STEALTH_SECRET_CODE，未配置时回落 "70707"。
     *   （对应清单 2.4「密令常量建议编译期可配」。注：密令全程在 App 层，内核不存任何密令常量。）
     */
    const val DEFAULT_CODE: String =
        // TODO(ShizuSU 集成)：在 app/build.gradle.kts 配 BuildConfigField
        //   "STEALTH_SECRET_CODE" to "\"70707\""，然后改为引用 com.sukisu.ultra.BuildConfig。
        "70707"

    /**
     * KernelSU 自己那些文件的 SELinux 类型(和 ksud 的 restorecon.rs 里同一个值)。
     */
    private const val KSU_CON = "u:object_r:ksu_file:s0"

    private fun sanitize(raw: String?): String? =
        raw?.trim()?.filter { it.isDigit() }?.take(12)?.takeIf { it.isNotBlank() }

    /** 读 /data/adb 里那份(需要 root;失败返回 null)。新路径读不到时兜底读旧路径(见 [LEGACY_PATH]) */
    fun read(): String? = runCatching {
        sanitize(ShellUtils.fastCmd(getRootShell(), "cat $PATH 2>/dev/null"))
            ?: sanitize(ShellUtils.fastCmd(getRootShell(), "cat $LEGACY_PATH 2>/dev/null"))
                ?.also { write(it) } // 自愈:补写到新路径
    }.getOrNull()

    /**
     * 把密令写到 /data/adb(卸载 App 也不会丢)。
     *
     * 先写临时文件、验过了才顶替真文件：
     *   ① 写 $PATH.new -> chmod 600 -> chown 0:0 -> chcon ksu_file(不行就 restorecon);
     *   ② 回读核验:内容一字不差 + 权限正好 600 + 属主 0;
     *   ③ 全对才 mv -f 覆盖真文件,并再验一次。
     * 任何一步不对就删掉临时文件、真文件一个字节都不动 —— 密令文件坏掉 = 用户被锁死。
     *
     * @return 是否**真的**落盘并且核验通过
     */
    fun write(code: String): Boolean = runCatching {
        val c = sanitize(code) ?: return@runCatching false
        val tmp = "$PATH.new"
        // 用 ${'$'} 转义 shell 变量:这段是 sh 脚本,不是 Kotlin 模板
        val script = """
            mkdir -p $DIR
            printf '%s' '$c' > $tmp
            chmod 600 $tmp 2>/dev/null
            chown 0:0 $tmp 2>/dev/null
            chcon $KSU_CON $tmp 2>/dev/null || restorecon $tmp 2>/dev/null || true
            got=${'$'}(cat $tmp 2>/dev/null)
            mode=${'$'}(stat -c %a $tmp 2>/dev/null)
            owner=${'$'}(stat -c %u $tmp 2>/dev/null)
            if [ "${'$'}got" = '$c' ] && [ "${'$'}mode" = '600' ] && [ "${'$'}owner" = '0' ]; then
              mv -f $tmp $PATH
              chmod 600 $PATH 2>/dev/null
              chown 0:0 $PATH 2>/dev/null
              got2=${'$'}(cat $PATH 2>/dev/null)
              if [ "${'$'}got2" = '$c' ]; then echo "7K_OK mode=${'$'}mode owner=${'$'}owner"; else echo 7K_FAIL_AFTER_MV; fi
            else
              rm -f $tmp
              echo "7K_FAIL mode=${'$'}mode owner=${'$'}owner"
            fi
        """.trimIndent()
        val out = ShellUtils.fastCmd(getRootShell(), script)
        // 只记成败，不记 shell 原文。
        android.util.Log.i(TAG, "密令落盘核验:${if (out.contains("7K_OK")) "OK" else "FAIL"}")
        out.contains("7K_OK")
    }.getOrDefault(false)

    /**
     * 真正生效的密令:优先 /data/adb 那份(能跨卸载),其次 App 设置,最后默认值。
     * **界面显示**用它(设置页要展示"当前生效的是哪个")。
     * ⚠️ 但**接收器比对不要用这个** —— 见 [acceptedCodes]。
     */
    fun effectiveCode(context: Context? = null): String {
        read()?.let { return it }
        val pref = runCatching { /* SettingsRepositoryImpl().stealthCode TODO */ null }.getOrNull()
        return sanitize(pref) ?: DEFAULT_CODE
    }

    /**
     * **可接受的密令集合(取并集)** —— 接收器比对用这个。
     * 为什么必须是并集:密令存在两个地方 —— /data/adb/shizusu/stealth_code(跨卸载不丢)
     * 和 App 设置。两份任意一个都能开锁,不会因为不一致而锁死。
     * ⚠️ 有意**不**把默认值作为"万能兜底"塞进来 —— 只有两边都没配时才用默认值。
     */
    fun acceptedCodes(context: Context? = null): Set<String> {
        val codes = LinkedHashSet<String>()
        read()?.let { codes.add(it) }
        runCatching { /* SettingsRepositoryImpl().stealthCode TODO */ null }
            ?.let { sanitize(it) }?.let { codes.add(it) }
        return if (codes.isEmpty()) setOf(DEFAULT_CODE) else codes
    }

    /**
     * 自愈/迁移(App 启动、打开设置页时各调一次,跑在 IO 线程):
     *   · /data/adb 有、App 设置没有 -> 用 /data/adb 的覆盖 App 设置
     *   · App 设置有、/data/adb 没有 -> 把 App 设置写进 /data/adb
     * @return 是否需要界面刷新
     */
    fun sync(): Boolean {
        // TODO(ShizuSU 集成)：接入 SettingsRepositoryImpl().stealthCode 后补全本函数的双向同步逻辑。
        val repo = runCatching { /* SettingsRepositoryImpl() TODO */ null }.getOrNull() ?: return false
        val pref = sanitize(null)
        val disk = read()
        return when {
            disk == null && pref != null -> {
                write(pref)
                false
            }

            disk != null && disk != pref -> {
                // repo.stealthCode = disk  // TODO
                true
            }

            else -> false
        }
    }
}
