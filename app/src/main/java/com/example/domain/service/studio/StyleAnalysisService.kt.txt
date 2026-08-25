package com.example.domain.service.studio

import android.app.Application
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.local.SettingsRepository
import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.NarrativeStructure
import com.example.domain.model.studio.StyleSignature
import com.example.domain.model.studio.VideoTone
import com.example.domain.model.studio.VisualMedium
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Contract for analyzing a user-supplied reference video and extracting a reusable
 * [StyleSignature] describing its visual/narrative identity — without generating or
 * copying the video itself.
 */
interface StyleAnalysisService {
    suspend fun analyzeReferenceVideo(videoUri: Uri, sourceLabel: String): Result<StyleSignature>
}

/**
 * Real implementation backed by the Gemini API (multimodal request with the video sent
 * inline as base64). Mirrors the REST call pattern already used by CompanionRepository.
 *
 * Videos are capped at a conservative size before base64 encoding to stay within the
 * Gemini inline-data request limits; larger files should be trimmed by the user first.
 */
class GeminiStyleAnalysisService(
    private val application: Application
) : StyleAnalysisService {

    private val settingsRepository = SettingsRepository(application)

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    companion object {
        // Conservative cap (~18MB raw) to keep the base64 inline payload safely under
        // the Gemini API's request size limits.
        private const val MAX_VIDEO_BYTES = 18L * 1024L * 1024L
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
    }

    override suspend fun analyzeReferenceVideo(
        videoUri: Uri,
        sourceLabel: String
    ): Result<StyleSignature> = withContext(Dispatchers.IO) {
        try {
            val resolver = application.contentResolver
            val mimeType = resolver.getType(videoUri) ?: "video/mp4"

            val bytes = resolver.openInputStream(videoUri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(IllegalArgumentException("تعذر قراءة ملف الفيديو المحدد"))

            if (bytes.size > MAX_VIDEO_BYTES) {
                return@withContext Result.failure(
                    IllegalArgumentException("حجم الفيديو كبير جدًا للتحليل (الحد الأقصى تقريبًا 18 ميجابايت). يرجى اختيار مقطع أقصر.")
                )
            }

            val devCustomKey = settingsRepository.devCustomGeminiApiKeyFlow.first()
            val devModel = settingsRepository.devGeminiModelFlow.first().ifBlank { DEFAULT_MODEL }
            val apiKey = if (devCustomKey.isNotBlank()) {
                devCustomKey
            } else {
                try {
                    val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                    field.get(null) as? String ?: ""
                } catch (e: Exception) {
                    ""
                }
            }

            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("لا يوجد مفتاح Gemini API مضبوط. يرجى إضافته من لوحة المطور.")
                )
            }

            val base64Video = Base64.encodeToString(bytes, Base64.NO_WRAP)

            val requestResult = executeStyleAnalysisCall(apiKey, devModel, base64Video, mimeType)
            requestResult.mapCatching { rawJson -> parseStyleSignature(rawJson, sourceLabel) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildAnalysisPrompt(): String {
        return """
        أنت خبير تحليل بصري وسردي لمقاطع الفيديو القصيرة (Reels/Shorts).
        سيتم تزويدك بفيديو مرجعي. مهمتك ليست وصف محتواه الديني أو الكلامي، بل استخراج
        "بصمة الأسلوب البصري والسردي" له فقط، بحيث يمكن لاحقًا محاكاة نفس الإحساس بمحتوى مختلف كليًا.

        أجب حصرًا بكائن JSON صالح وبدون أي نص إضافي قبله أو بعده، بالمخطط التالي بالضبط:
        {
          "visualMedium": "ILLUSTRATED_CHARACTERS | REAL_CINEMATIC_FOOTAGE | INFOGRAPHIC_MOTION | TEXT_ONLY_TYPOGRAPHY | MIXED",
          "narrativeStructure": "NUMBERED_LIST | POETIC_REFLECTION | DATA_PROGRESSION | SINGLE_QUOTE_FOCUS | STORY_ARC",
          "colorPalette": "وصف موجز للألوان والتدرجات المستخدمة",
          "backgroundTreatment": "وصف موجز لنوع ومعالجة الخلفيات",
          "textTreatment": "وصف موجز لطريقة وضع وتنسيق النص على الشاشة",
          "transitionStyle": "وصف موجز لأسلوب الانتقالات بين المشاهد",
          "signatureMotif": "أي عنصر بصري متكرر يمثل هوية/توقيع ثابت عبر الفيديو، أو فارغ إن لم يوجد",
          "paceDescription": "وصف موجز لسرعة القطع والإيقاع العام",
          "summary": "ملخص من جملتين لأسلوب الفيديو ككل بصيغة يمكن استخدامها كتوجيه لمصمم",
          "recommendedTone": "CALM | INSPIRING | EDUCATIONAL | STORYTELLING | FAST | CINEMATIC",
          "recommendedEditingStyle": "CINEMATIC | DOCUMENTARY | EDUCATIONAL | STORYTELLING | MEDITATIVE | FAST_REELS | SHORT_AD | ANIMATION | MOVING_QUOTES"
        }

        اكتب القيم النصية باللغة العربية باستثناء أسماء الحقول والقيم الثابتة (enum) بالإنجليزية كما هي معطاة.
        """.trimIndent()
    }

    private fun executeStyleAnalysisCall(
        apiKey: String,
        modelName: String,
        base64Video: String,
        mimeType: String
    ): Result<String> {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", buildAnalysisPrompt()))
                put(
                    JSONObject().put(
                        "inlineData",
                        JSONObject().apply {
                            put("mimeType", mimeType)
                            put("data", base64Video)
                        }
                    )
                )
            }

            val contentsArray = JSONArray().put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", partsArray)
                }
            )

            val rootJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 700)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) {
                    return Result.success(text)
                }
            }

            Result.failure(Exception("لم يُرجع النموذج أي نتيجة تحليل صالحة"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseStyleSignature(rawJson: String, sourceLabel: String): StyleSignature {
        val json = JSONObject(rawJson)

        fun enumOrDefault(fieldName: String, default: VisualMedium): VisualMedium =
            runCatching { VisualMedium.valueOf(json.optString(fieldName)) }.getOrDefault(default)

        return StyleSignature(
            sourceLabel = sourceLabel,
            visualMedium = enumOrDefault("visualMedium", VisualMedium.MIXED),
            narrativeStructure = runCatching {
                NarrativeStructure.valueOf(json.optString("narrativeStructure"))
            }.getOrDefault(NarrativeStructure.STORY_ARC),
            colorPalette = json.optString("colorPalette"),
            backgroundTreatment = json.optString("backgroundTreatment"),
            textTreatment = json.optString("textTreatment"),
            transitionStyle = json.optString("transitionStyle"),
            signatureMotif = json.optString("signatureMotif"),
            paceDescription = json.optString("paceDescription"),
            summary = json.optString("summary"),
            recommendedTone = runCatching {
                VideoTone.valueOf(json.optString("recommendedTone"))
            }.getOrDefault(VideoTone.INSPIRING),
            recommendedEditingStyle = runCatching {
                EditingStyle.valueOf(json.optString("recommendedEditingStyle"))
            }.getOrDefault(EditingStyle.CINEMATIC),
            isMock = false
        )
    }
}
