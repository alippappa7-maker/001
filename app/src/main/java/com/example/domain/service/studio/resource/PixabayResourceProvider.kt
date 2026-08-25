package com.example.domain.service.studio.resource

import com.example.domain.model.studio.ResourceIntent
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * مزود موارد من Pixabay (صور + فيديوهات مجانية) — مصدر ثانوي احتياطي
 * يُجرَّب بعد Pexels إن لم يجد الأخير نتائج.
 *
 * يتطلب مفتاح API من https://pixabay.com/api/ (مجاني).
 * يتبع نفس نمط [PexelsResourceProvider]: يبحث، يختار نتيجة، يُنزّل محليًا.
 */
class PixabayResourceProvider(
    private val apiKeyProvider: () -> String,
    private val cache: StockMediaCache,
    private val client: OkHttpClient = defaultClient()
) : MediaResourceProvider {

    private val photosEndpoint = "https://pixabay.com/api/"
    private val videosEndpoint = "https://pixabay.com/api/videos/"

    override val isAvailable: Boolean = isKeyConfigured(apiKeyProvider())
    override val displayName: String = "Pixabay"

    override suspend fun resolve(intent: ResourceIntent): MediaResource? {
        val apiKey = apiKeyProvider().takeIf { isKeyConfigured(it) } ?: return null
        val query = buildQuery(intent)

        return if (intent.preferMotion) {
            resolveVideo(apiKey, query, intent)
        } else {
            resolvePhoto(apiKey, query) ?: resolveVideo(apiKey, query, intent)
        }
    }

    private fun resolvePhoto(apiKey: String, query: String): MediaResource? {
        val url = "$photosEndpoint?key=$apiKey&q=$query&per_page=5&image_type=photo&orientation=vertical"
        val json = getJson(url) ?: return null
        val hits = json.optJSONArray("hits") ?: return null
        val hit = hits.optJSONObject(0) ?: return null
        val imageUrl = hit.optString("largeImageURL").ifBlank { hit.optString("webformatURL") }
        if (imageUrl.isBlank()) return null
        val localPath = cache.downloadIfAbsent(imageUrl) ?: return null
        return MediaResource.LocalImage(localPath)
    }

    private fun resolveVideo(apiKey: String, query: String, intent: ResourceIntent): MediaResource? {
        val url = "$videosEndpoint?key=$apiKey&q=$query&per_page=5&video_type=all"
        val json = getJson(url) ?: return null
        val hits = json.optJSONArray("hits") ?: return null
        val hit = hits.optJSONObject(0) ?: return null
        val videos = hit.optJSONObject("videos") ?: return null
        // نختار جودة متوسطة (medium) أو صغيرة (small) لتفادي الملفات الضخمة.
        val medium = videos.optJSONObject("medium")
        val small = videos.optJSONObject("small")
        val chosen = medium ?: small ?: return null
        val videoUrl = chosen.optString("url").takeIf { it.isNotBlank() } ?: return null
        val localPath = cache.downloadIfAbsent(videoUrl) ?: return null
        return MediaResource.LocalVideo(localPath, intent.durationMs)
    }

    private fun getJson(url: String): JSONObject? {
        return runCatching {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                JSONObject(body)
            }
        }.getOrNull()
    }

    private fun buildQuery(intent: ResourceIntent): String {
        val terms = (intent.category.searchTerms + intent.keywords)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        return terms.joinToString("+").ifBlank { "nature" }
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
            return trimmed.lowercase() !in setOf("none", "default", "empty", "null", "todo")
        }
    }
}
