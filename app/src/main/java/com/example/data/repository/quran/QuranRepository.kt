package com.example.data.repository.quran

import android.content.Context
import com.example.data.api.RetrofitClient
import com.example.data.api.model.quran.VerseTimingDto
import com.example.domain.model.quran.RecitationTimeline
import com.example.domain.model.quran.TimedQuranWord
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * ثوابت المصادر: القارئ الافتراضي هو المشاري راشد العفاسي (id=7).
 */
object QuranReciters {
    const val ALAFASY = 7
    const val SUDAIS = 3
    const val ABDULBASIT_MURATTAL = 2
    const val HUSARY = 6
    const val MINSHAWI_MURATTAL = 9
    const val SHURAIM = 10

    val SUPPORTED = listOf(
        ALAFASY to "الشيخ مشاري راشد العفاسي",
        SUDAIS to "عبد الرحمن السديس",
        ABDULBASIT_MURATTAL to "عبد الباسط عبد الصمد (مرتل)",
        HUSARY to "محمود خليل الحصري",
        MINSHAWI_MURATTAL to "محمد صديق المنشاوي (مرتل)",
        SHURAIM to "سعود الشريم"
    )
}

/**
 * يجلب نص الآية الكامل، ملف الصوت المعزول لها، وتوقيت كل كلمة (بالميلي ثانية)
 * من واجهة QuranCDN، ثم يبني [RecitationTimeline] واحدًا متسقًا.
 *
 * القلب التقني: توقيتات QuranCDN هي إزاحات داخل ملف السورة الكاملة، وليس
 * ملف الآية المعزول. نطرح timestampFrom للآية لتحويل كل توقيت كلمة إلى زمن
 * نسبي لبداية ملف الآية المعزول الذي يُحمَّل من verses.quran.com.
 * تفاصيل هذا التحويل في [QuranTimingConverter] (مفصول للقابلية للاختبار).
 */
class QuranRepository(
    private val context: Context,
    private val httpClient: OkHttpClient = OkHttpClient()
) {

    private val api = RetrofitClient.quranApi

    /**
     * يبني خط زمني كامل لآية معينة وقارئ معين، أو يُرجع null عند الفشل
     * أو عدم تطابق عدد الكلمات مع عدد التوقيتات (التحقق الشرعي).
     *
     * @param chapter رقم السورة (1-114).
     * @param verse رقم الآية داخل السورة.
     * @param reciterId معرّف القارئ من Quran.com.
     */
    suspend fun buildRecitationTimeline(
        chapter: Int,
        verse: Int,
        reciterId: Int
    ): RecitationTimeline? {
        val verseKey = "$chapter:$verse"

        // 1. نص الآية + كلماتها
        val verseResponse = api.fetchVerse(verseKey)
        val verseDto = verseResponse.verse
        val textUthmani = verseDto.textUthmani ?: return null

        // الكلمات الفعلية فقط (تجاهل علامات النهاية char_type=end).
        // نستخرج مواضع الكلمات (position) بالترتيب؛ النص العثماني الفعلي
        // لكل كلمة غير متوفر على مستوى الكلمة في API، لذلك نقسم النص الكامل
        // للآية (verse.text_uthmani) حسب المسافات ونربطه بالترتيب.
        val wordDtos = verseDto.words.filter { it.charType == "word" }
        if (wordDtos.isEmpty()) return null

        // تقسيم النص العثماني الكامل إلى كلمات بالترتيب. التحقق الشرعي: عدد
        // الكلمات المقسومة يجب أن يطابق عدد الكلمات في المصفوفة، وإلا نرفض.
        val splitWords = splitUthmaniWords(textUthmani)
        if (splitWords.size != wordDtos.size) return null
        val wordTexts = wordDtos.mapIndexed { i, dto -> dto.position to splitWords[i] }

        // 2. توقيت كل كلمة داخل السورة الكاملة (بالميلي ثانية)
        val timingResponse = api.fetchChapterTiming(reciterId, chapter)
        val verseTiming = timingResponse.audioFiles
            .firstOrNull()
            ?.verseTimings
            ?.firstOrNull { it.verseKey == verseKey }
            ?: return null

        // 3. التحويل + التحقق الشرعي: إن نقص توقيت كلمة، نرفض التصدير بالكامل.
        val timedWords = QuranTimingConverter.buildTimedWords(wordTexts, verseTiming)
            ?: return null

        // 4. رابط ملف الآية المعزول + اسم القارئ
        val reciterInfo = QuranReciters.SUPPORTED.firstOrNull { it.first == reciterId }
        val reciterName = reciterInfo?.second ?: "القارئ رقم $reciterId"
        val verseAudioUrl = fetchVerseAudioUrl(reciterId, verseKey) ?: return null

        val durationMs = maxOf(verseTiming.duration, timedWords.last().endMs)

        return RecitationTimeline(
            verseKey = verseKey,
            textUthmani = textUthmani,
            reciterId = reciterId,
            reciterName = reciterName,
            verseAudioUrl = verseAudioUrl,
            durationMs = durationMs,
            words = timedWords
        )
    }

    /**
     * يجلب رابط ملف الآية المعزول من واجهة Quran.com، ثم يحوّل المسار النسبي
     * إلى رابط كامل على verses.quran.com.
     */
    private suspend fun fetchVerseAudioUrl(reciterId: Int, verseKey: String): String? {
        val response = api.fetchRecitationFile(reciterId, verseKey)
        val file = response.audioFiles.firstOrNull() ?: return null
        return "https://verses.quran.com/${file.url.trimStart('/')}"
    }

    /**
     * يحمّل ملف الصوت المعزول للآية إلى دليل مؤقت داخل التطبيق.
     * يُستخدم من محرك التصدير (StudioCompositionEngine) كـ audioUri.
     *
     * تحقّق إلزامي إضافي: توقيت QuranCDN مبني على ملف السورة الكاملة،
     * بينما يُرفّ هنا ملف الآية المعزول من مصدر مختلف. لا ضمانة أن الملفين
     * يتطابقان في مدة الصمت قبل/بعد الكلمة. لذلك نقارن مدة الملف المحمّل فعليًا
     * (عبر MediaMetadataRetriever) بمدة [RecitationTimeline.durationMs] المتوقعة؛ إن
     * زاد الفرق عن هامش [AUDIO_DURATION_TOLERANCE_MS] نرفض الملف لتفادي مزامنة
     * مغلوطة بين الصوت وتلوين الكلمات.
     */
    suspend fun cacheVerseAudio(timeline: RecitationTimeline): File? {
        val target = File(
            context.cacheDir,
            "quran_${timeline.verseKey.replace(":", "_")}_${timeline.reciterId}.mp3"
        )
        val alreadyCached = target.exists() && target.length() > 0
        if (!alreadyCached) {
            val request = Request.Builder().url(timeline.verseAudioUrl).build()
            try {
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return null
                    response.body?.byteStream()?.use { input ->
                        target.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            } catch (e: IOException) {
                return null
            }
            if (!target.exists() || target.length() == 0L) return null
        }

        // تحقّق شرعي صارم: مدة الملف الفعلية يجب أن تقارب مدة التوقيت المحوّلة.
        // إن لم نتمكّن من قراءة المدة (خطأ جهاز محلي) نقبل الملف بتحفّز؛
        // أما إن تم قراءة المدة وخرجت مخالفة بوضوح للهامش، نرفض الملف لتلافي
        // تصدير مزامنة مكسورة بين الصوت والتظليل.
        val actualDurationMs = readAudioDurationMs(target)
        if (actualDurationMs != null) {
            val drift = kotlin.math.abs(actualDurationMs - timeline.durationMs)
            if (drift > AUDIO_DURATION_TOLERANCE_MS) {
                target.delete()
                return null
            }
        }

        return target
    }

    /**
     * يقرأ مدة ملف الصوت بالملي ثانية عبر MediaMetadataRetriever؛ يعيد null
     * عند أي فشل (ملف فاسد، ترميز مدعوم مفقود، الخ).
     */
    private fun readAudioDurationMs(file: File): Long? {
        val retriever = android.media.MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    /**
     * يقسم نص آية عثماني إلى كلماته حسب المسافات. يتجاهل المسافات المتعددة
     * والبادئة/اللاحقة. تُستخدم لأن API لا يُرجع نص كلمة عثمانية مستقلة
     * على مستوى الكلمة، فقط النص الكامل على مستوى الآية.
     */
    private fun splitUthmaniWords(textUthmani: String): List<String> =
        textUthmani.trim().split(Regex("\\s+"))

    private companion object {
        // هامش مقبول للاختلاف بين مدة ملف الآية المعزول الفعلية والمدة
        // المتوقعة من توقيت QuranCDN. اختلاف أكبر يعني اختلاف المصدر
        // في حشو قبل/بعد الكلمة، ويُفسد المزامنة بالكامل.
        const val AUDIO_DURATION_TOLERANCE_MS = 350L
    }
}
