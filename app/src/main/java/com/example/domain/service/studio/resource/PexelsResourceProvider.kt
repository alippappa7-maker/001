package com.example.domain.service.studio.resource

import com.example.domain.model.studio.ResourceIntent
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * مزود موارد من Pexels (صور + فيديوهات مجانية).
 *
 * يتطلب مفتاح API من https://www.pexels.com/api/ (مجاني).
 * إن لم يُضبط المفتاح، يُعتبر المزود غير متاح [isAvailable = false]
 * وتتابع سلسلة fallback للمزود التالي.
 *
 * التدفّق:
 * 1. يبني استعلام بحث من [ResourceIntent.keywords] و[ResourceIntent.category].
 * 2. يطلب من Pexels API (صور أو فيديو حسب preferMotion).
 * 3. يختار أول نتيجة مناسبة ويُنزّلها محليًا عبر [StockMediaCache].
 * 4. يعيد [MediaResource.LocalImage] أو [MediaResource.LocalVideo].
 */
class PexelsResourceProvider(
    private val apiKeyProvider: () -> String,
    private val cache: StockMediaCache,
    private val client: OkHttpClient = defaultClient()
) : MediaResourceProvider {

    private val photosEndpoint = "https://api.pexels.com/v1/search"
    private val videosEndpoint = "https://api.pexels.com/videos/search"

    override val isAvailable: Boolean = isKeyConfigured(apiKeyProvider())

    override val displayName: String = "Pexels"

    override suspend fun resolve(intent: ResourceIntent): MediaResource? {
        val apiKey = apiKeyProvider().takeIf { isKeyConfigured(it) } ?: return null
        val query = buildQuery(intent)

        return if (intent.preferMotion) {
            resolveVideo(apiKey, query, intent)
        } else {
            resolvePhoto(apiKey, query) ?: resolveVideo(apiKey, query, intent)
        }
    }

    private suspend fun resolvePhoto(apiKey: String, query: String): MediaResource? {
        val url = "$photosEndpoint?query=$query&per_page=5&orientation=portrait"
        val json = getJson(apiKey, url) ?: return null
        val photo = json.optJSONArray("photos")?.optJSONObject(0) ?: return null
        // نفضّل صورة بالحجم المتوسط، وإلا الحجم الأصلي.
        val imageUrl = photo.optJSONObject("src")?.optString("large")
            ?: photo.optJSONObject("src")?.optString("original")
            ?: return null
        val localPath = cache.downloadIfAbsent(imageUrl) ?: return null
        return MediaResource.LocalImage(localPath)
    }

    private suspend fun resolveVideo(apiKey: String, query: String, intent: ResourceIntent): MediaResource? {
        val url = "$videosEndpoint?query=$query&per_page=5&orientation=portrait"
        val json = getJson(apiKey, url) ?: return null
        val video = json.optJSONArray("videos")?.optJSONObject(0) ?: return null
        // نختار ملف فيديو بجودة معقولة (HD أو أقل) لتفادي الملفات الضخمة.
        val files = video.optJSONArray("video_files") ?: return null
        val chosen = (0 until files.length()).mapNotNull { files.optJSONObject(it) }
            .sortedBy { it.optInt("width") }
            .firstOrNull { it.optInt("width") >= 720 } ?: files.optJSONObject(0) ?: return null
        val videoUrl = chosen.optString("link").takeIf { it.isNotBlank() } ?: return null
        val localPath = cache.downloadIfAbsent(videoUrl) ?: return null
        return MediaResource.LocalVideo(localPath, intent.durationMs)
    }

    private suspend fun getJson(apiKey: String, url: String): JSONObject? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder().url(url)
                    .addHeader("Authorization", apiKey)
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    JSONObject(body)
                }
            }.getOrNull()
        }
    }

    private fun buildQuery(intent: ResourceIntent): String {
        val terms = (intent.category.searchTerms + intent.keywords)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        return terms.joinToString(" ").ifBlank { "nature" }
    }

    companion object {
        private fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        /** هل المفتاح مُضبوط فعلًا (وليس قيمة وصفية فارغة)؟ */
        private fun isKeyConfigured(key: String): Boolean {
            val trimmed = key.trim()
            if (trimmed.isBlank()) return false
            // رفض القيم الوصفية الشائعة للحقول غير المُضبوطة.
            return trimmed.lowercase() !in setOf("none", "default", "empty", "null", "todo")
        }
    }
}
