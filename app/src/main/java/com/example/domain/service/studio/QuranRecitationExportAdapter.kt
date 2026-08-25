package com.example.domain.service.studio

import android.content.Context
import android.media.MediaMetadataRetriever
import com.example.data.repository.quran.QuranRepository
import com.example.data.repository.quran.QuranReciters
import com.example.domain.model.quran.QuranExportPlan
import com.example.domain.model.quran.QuranExportPreparation
import com.example.domain.model.quran.SourceCard
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.service.studio.template.QuranRecitationTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * الجسر الفعلي بين «النمط الذكي للقرآن» (المخزّن في [QuranRepository] و
 * [QuranExportPreparation] و[QuranRecitationTemplate]) وخط تصدير الفيديو
 * ([Media3VideoRenderService] / [StudioCompositionEngine]).
 *
 * يبني المسار الكامل لآية واحدة:
 * 1. يحلّ مفتاح الآية (مثل `2:255`) إلى رقم السورة والآية.
 * 2. يبني [com.example.domain.model.quran.RecitationTimeline] عبر الشبكة.
 * 3. يخزّن الصوت المعزول محليًا ويتحقق من مدته.
 * 4. يُجري البوابة الشرعية الصارمة عبر [QuranExportPreparation.prepare] — فإن فشل
 *    أي شرط لا يصل أي فيديو ناقص إلى محرك التصدير.
 * 5. ينتج [CompositionStoryboard] جاهزًا للتصدير (overlay متعدد الطبقات + بطاقة مصدر).
 *
 * كل الفشل يُرمى كاستثناء برسالة واضحة، كي يلتقطه [Media3VideoRenderService]
 * ضمن try/catch الموجود ويعرضه للمستخدم بدل إنهيار صامت.
 */
class QuranRecitationExportAdapter(context: Context) {

    private val quranRepository = QuranRepository(context)
    private val textRenderer = TextBitmapRenderer(context)

    /**
     * يبني [CompositionStoryboard] لآية محددة مع قارئ افتراضي (العفاسي).
     * @param verseKey مفتاح الآية بصيغة `السورة:الآية` (مثال: `2:255`).
     * @throws IllegalArgumentException صيغة المفتاح غير صالحة.
     * @throws IllegalStateException فشل جلب/تخزين التلاوة أو البوابة الشرعية.
     * @throws com.example.domain.model.quran.ShariaValidationException خرق التحقق الشرعي.
     */
    internal suspend fun buildStoryboard(
        verseKey: String,
        reciterId: Int = QuranReciters.ALAFASY
    ): CompositionStoryboard = withContext(Dispatchers.IO) {
        val parsed = parseVerseKey(verseKey)
            ?: throw IllegalArgumentException(
                "صيغة مفتاح الآية غير صالحة: «$verseKey». مثال صحيح: 2:255"
            )

        val timeline = quranRepository.buildRecitationTimeline(parsed.first, parsed.second, reciterId)
            ?: throw IllegalStateException(
                "تعذّر بناء الخط الزمني للتلاوة للآية $verseKey (تحقق من الاتصال بالشبكة أو صحة المفتاح)."
            )

        val audioFile = quranRepository.cacheVerseAudio(timeline)
            ?: throw IllegalStateException(
                "تعذّر تخزين صوت التلاوة محليًا أو التحقق من مدته للآية $verseKey."
            )

        // اقرأ المدة الفعلية من الملف المخزّن. إن تعذّلت القراءة، اعتمد مدة الخط الزمني
        // (الملف اجتاز فحص المطابقة الأصغر داخل cacheVerseAudio بالفعل، فالمدة ضمن التفاوت).
        val actualDurationMs = readAudioDurationMs(audioFile) ?: timeline.durationMs

        val sourceCard = SourceCard(
            reciterName = timeline.reciterName,
            reciterStyle = "مرتل",
            verseKey = timeline.verseKey,
            recitationSourceUrl = timeline.verseAudioUrl,
            textSourceUrl = "https://api.quran.com/api/v4/verses/by_key/${timeline.verseKey}",
            timingsSourceUrl = "https://api.qurancdn.com/api/qdc/audio/reciters/${timeline.reciterId}/audio_files"
        )

        val plan: QuranExportPlan = QuranExportPreparation.prepare(
            timeline = timeline,
            sourceCard = sourceCard,
            audioFile = audioFile,
            actualAudioDurationMs = actualDurationMs
        )
        QuranRecitationTemplate(textRenderer).build(plan)
    }

    private fun parseVerseKey(verseKey: String): Pair<Int, Int>? {
        val parts = verseKey.trim().split(":")
        if (parts.size != 2) return null
        val chapter = parts[0].toIntOrNull() ?: return null
        val verse = parts[1].toIntOrNull() ?: return null
        if (chapter in 1..114 && verse in 1..286) return chapter to verse
        return null
    }

    private fun readAudioDurationMs(audioFile: File): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(audioFile.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        } catch (t: Throwable) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Throwable) {
                // تجاهل: لا يؤثر على صحة النتيجة.
            }
        }
    }
}
