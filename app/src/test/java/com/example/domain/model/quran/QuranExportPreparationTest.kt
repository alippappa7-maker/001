package com.example.domain.model.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * اختبارات وحدة لـ [QuranExportPreparation] — البوابة الفاصلة بين بيانات
 * التلاوة الخام وخط التصدير. تثبت أن أي فيديو قرآني ناقص يُمنع من الوصول إلى
 * محرك التصدير، وأن الخطة الصالحة تحوي [PhraseTimeline] جاهزة للتظليل متعدد
 * الطبقات.
 *
 * كلها JVM فقط — لا تعتمد على أندرويد أو شبكة.
 */
class QuranExportPreparationTest {

    private val validTimeline = RecitationTimeline(
        verseKey = "1:1",
        textUthmani = "بِسْمِ اللَّهِ",
        reciterId = 7,
        reciterName = "مشاري راشد العفاسي",
        verseAudioUrl = "https://verses.quran.com/Alafasy/mp3/001001.mp3",
        durationMs = 5_000L,
        words = listOf(
            TimedQuranWord(position = 1, text = "بِسْمِ", startMs = 0, endMs = 500),
            TimedQuranWord(position = 2, text = "اللَّهِ", startMs = 600, endMs = 1500)
        )
    )

    private val validSourceCard = SourceCard(
        reciterName = "مشاري راشد العفاسي",
        reciterStyle = "مرتل",
        verseKey = "1:1",
        recitationSourceUrl = "https://verses.quran.com/Alafasy/mp3/001001.mp3",
        textSourceUrl = "https://api.quran.com/api/v4/verses/by_key/1:1",
        timingsSourceUrl = "https://api.qurancdn.com/api/qdc/audio/reciters/7/audio_files"
    )

    @Test
    fun `خطة صالحة تُنتج QuranExportPlan مع phraseTimeline جاهزة للطبقات المتعددة`() {
        val tempFile = File.createTempFile("quran_test", ".mp3").apply { writeBytes(ByteArray(100)) }
        try {
            val plan = QuranExportPreparation.prepare(
                timeline = validTimeline,
                sourceCard = validSourceCard,
                audioFile = tempFile,
                actualAudioDurationMs = 5_100L // ضمن هامش 500ms
            )

            assertEquals(validTimeline, plan.timeline)
            assertEquals(validSourceCard, plan.sourceCard)
            assertEquals(tempFile, plan.audioFile)
            assertEquals(5_100L, plan.actualAudioDurationMs)
            // phraseTimeline مبنية فعلاً مع عبارات (نفس عدد الكلمات هنا = عبارة واحدة).
            assertTrue(plan.phraseTimeline.phrases.isNotEmpty())
            assertEquals(plan.timeline.words.size, plan.phraseTimeline.timeline.words.size)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `بطاقة المصدر الغائبة تمنع التصدير برمي ShariaValidationException`() {
        val tempFile = File.createTempFile("quran_test", ".mp3").apply { writeBytes(ByteArray(100)) }
        try {
            val ex = assertThrows(ShariaValidationException::class.java) {
                QuranExportPreparation.prepare(
                    timeline = validTimeline,
                    sourceCard = null,
                    audioFile = tempFile,
                    actualAudioDurationMs = 5_000L
                )
            }
            assertTrue(ex.report.errors.contains("بطاقة المصدر غائبة أو ناقصة"))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `ملف الصوت المفقود يمنع التصدير برمي ShariaValidationException`() {
        val ex = assertThrows(ShariaValidationException::class.java) {
            QuranExportPreparation.prepare(
                timeline = validTimeline,
                sourceCard = validSourceCard,
                audioFile = File("/nonexistent/path.mp3"),
                actualAudioDurationMs = 5_000L
            )
        }
        assertTrue(ex.report.errors.contains("ملف الصوت المعزول غير موجود على القرص"))
    }

    @Test
    fun `اختلاف مدة الصوت عن التوقيت بأكثر من الهامش يمنع التصدير`() {
        val tempFile = File.createTempFile("quran_test", ".mp3").apply { writeBytes(ByteArray(100)) }
        try {
            val ex = assertThrows(ShariaValidationException::class.java) {
                QuranExportPreparation.prepare(
                    timeline = validTimeline,
                    sourceCard = validSourceCard,
                    audioFile = tempFile,
                    actualAudioDurationMs = 10_000L // اختلاف 5000ms، أكبر بكثير من الهامش
                )
            }
            assertTrue(ex.report.errors.contains("مدة ملف الصوت لا تطابق توقيت الكلمات"))
        } finally {
            tempFile.delete()
        }
    }
}
