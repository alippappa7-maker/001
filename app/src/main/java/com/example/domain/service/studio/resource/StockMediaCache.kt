package com.example.domain.service.studio.resource

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * يُنزّل الموارد الخارجية (صور/فيديوهات) ويخزّنها محليًا في ذاكرة مؤقتة.
 *
 * التخزين المحلي قبل التصدير ضروري لأن Media3 Transformer يستهلك ملفات/URIs
 * محلية بشكل موثوق، ولا يُنصح بالاعتماد على رابط مباشر أثناء التصدير.
 *
 * مفتاح التخزين: تجزئة SHA-1 للرابط، لتفادي إعادة تنزيل نفس المورد.
 */
class StockMediaCache(
    private val cacheDir: File,
    private val client: OkHttpClient = defaultClient()
) {
    init {
        cacheDir.mkdirs()
    }

    /** هل يوجد ملف محلي لهذا الرابط مسبقًا؟ */
    fun has(url: String): Boolean = cachedFile(url).exists()

    /** مسار الملف المحلي للرابط (موجود أم لا). */
    private fun cachedFile(url: String): File {
        val hash = MessageDigest.getInstance("SHA-1")
            .digest(url.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val ext = guessExtension(url)
        return File(cacheDir, "$hash.$ext")
    }

    /**
     * يُنزّل المورد من الرابط إن لم يكن مخزّنًا مسبقًا، ويعيد مساره المحلي.
     * يفشل ويرجع null عند أي خطأ شبكي/إدخال-إخراج (لا يُسقط التصدير).
     */
    fun downloadIfAbsent(url: String): String? {
        val target = cachedFile(url)
        if (target.exists() && target.length() > 0) return target.absolutePath
        return runCatching {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body ?: return null
                target.outputStream().use { body.byteStream().copyTo(it) }
            }
            target.absolutePath.takeIf { target.length() > 0 }
        }.getOrNull()
    }

    private fun guessExtension(url: String): String {
        val clean = url.substringBefore('?').substringAfterLast('/')
        return when {
            clean.endsWith(".mp4", ignoreCase = true) -> "mp4"
            clean.endsWith(".mov", ignoreCase = true) -> "mov"
            clean.endsWith(".webp", ignoreCase = true) -> "webp"
            clean.endsWith(".png", ignoreCase = true) -> "png"
            clean.endsWith(".jpg", ignoreCase = true) || clean.endsWith(".jpeg", ignoreCase = true) -> "jpg"
            else -> "bin"
        }
    }

    companion object {
        private fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
