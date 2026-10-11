package com.sukisu.ultra.ui.util.module

import android.net.Uri
import android.util.Base64
import com.sukisu.ultra.data.repository.ModuleRepoConfig
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.util.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.crypto.util.PublicKeyFactory

/**
 * ShizuSU 模块仓库 Ed25519 签名校验（进阶2 的管理器端实现）。
 *
 * - 公钥内置在 assets/shizusu_sign_public.pem（随 APK 分发，与仓库 signatures/public.pem 一致）
 * - 索引（modules.json）签名位于 {base}/signatures/modules.sig，对索引原始字节验签
 * - 私有模块 zip 签名位于 {base}/signatures/<zip名>.sig，对 zip 原始字节验签
 * - 兼容策略：无签名不拦截（第三方源）；有签名必须验证通过，否则拒绝加载/安装
 * - 官方默认源（ModuleRepoConfig.DEFAULT_BASE_URL）强制要求签名存在
 */
object ModuleSignatureVerifier {

    private const val ASSET_PUBLIC_KEY = "shizusu_sign_public.pem"
    private const val SIG_DIR = "signatures"

    /** 内置公钥（懒加载；失败时 available=false，所有校验返回 false）。 */
    private val publicKey: Ed25519PublicKeyParameters? by lazy { loadPublicKey() }

    private fun loadPublicKey(): Ed25519PublicKeyParameters? = runCatching {
        val pem = ksuApp.assets.open(ASSET_PUBLIC_KEY).bufferedReader().use { it.readText() }
        val body = pem.lineSequence()
            .filter { !it.startsWith("-----") && it.isNotBlank() }
            .joinToString("")
        val spki = SubjectPublicKeyInfo.getInstance(Base64.decode(body, Base64.DEFAULT))
        PublicKeyFactory.createKey(spki) as Ed25519PublicKeyParameters
    }.getOrNull()

    /** 公钥是否可用。 */
    val available: Boolean
        get() = publicKey != null

    /** 对原始字节验签；signatureB64 为 base64 编码的 64 字节 Ed25519 签名。 */
    fun verifyBytes(bytes: ByteArray, signatureB64: String): Boolean {
        val key = publicKey ?: return false
        return runCatching {
            val sig = Base64.decode(signatureB64.trim(), Base64.DEFAULT)
            if (sig.size != 64) return false
            val signer = Ed25519Signer()
            signer.init(false, key)
            signer.update(bytes, 0, bytes.size)
            signer.verifySignature(sig)
        }.getOrDefault(false)
    }

    /** 拉取 {base}/signatures/<file> 的签名文本；不存在或网络失败返回 null。 */
    private suspend fun fetchSignature(file: String): String? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(ksuApp)) return@withContext null
        runCatching {
            val url = "${ModuleRepoConfig.baseUrl.trimEnd('/')}/$SIG_DIR/$file"
            ksuApp.okhttpClient.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                if (!resp.isSuccessful) null else resp.body.string()
            }
        }.getOrNull()
    }

    /**
     * 校验模块仓库索引（modules.json 原始字节）。
     *
     * @return true = 验证通过，或该源无签名且非官方默认源（兼容跳过）；
     *         false = 有签名但校验失败（拒绝加载），或官方默认源签名缺失。
     */
    suspend fun verifyRepoIndex(bodyBytes: ByteArray, strictDefaultSource: Boolean): Boolean {
        val sig = fetchSignature("modules.sig")
        if (sig == null) {
            // 官方默认源：签名缺失视为异常；自定义源：兼容跳过
            return !strictDefaultSource
        }
        return verifyBytes(bodyBytes, sig)
    }

    /**
     * 校验下载完成的模块 zip 签名。
     *
     * 仅校验来自本仓库 private/ 目录的私有模块（每个私有 zip 都有对应签名）；
     * 开源 release 模块（GitHub 直链）不在本仓库签发范围，跳过。
     *
     * @return true = 校验通过（或无需校验）；false = 私有模块签名缺失/校验失败。
     */
    suspend fun verifyModuleZip(downloadUrl: String, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val base = ModuleRepoConfig.baseUrl.trimEnd('/')
        val url = downloadUrl.trim()
        if (!url.startsWith("$base/private/")) return@withContext true
        val stem = url.substringAfterLast('/').removeSuffix(".zip")
        val sig = fetchSignature("$stem.sig") ?: return@withContext false
        val bytes = runCatching {
            ksuApp.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull() ?: return@withContext false
        verifyBytes(bytes, sig)
    }
}
