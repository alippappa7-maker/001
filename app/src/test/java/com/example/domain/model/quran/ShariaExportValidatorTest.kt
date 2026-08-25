package com.example.domain.model.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * اختبارات وحدة لـ [ShariaExportValidator]: التحقق من أن أي فيديو قرآني
 * لا يُصدَّر دون أن يجتاز كل القيود الشرعية معًا (توقيت سليم + صوت فعلي
 * على القرص + مدة مطابقة + بطاقة مصدر كاملة).
 *
 * هذه كلها JVM فقط — لا تعتمد على أندرويد أو شبكة.
 */
class ShariaExportValidatorTest {

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
    fun `جميع الشروط صحيحة تنتج تقريرًا صالحًا للتصدير`() {
        val tempFile = File.createTempFile("quran_test", ".mp3").apply { writeBytes(ByteArray(100)) }
        try {
            val report = ShariaExportValidator.validate(
                timeline = validTimeline,
                audioFile = tempFile,
                actualAudioDurationMs = 5_100L, // ضمن هامش 500ms
                sourceCard = validSourceCard
            )

            assertTrue(report.isValid)
            assertTrue(report.errors.isEmpty())
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `ملف الصوت المفقود يمنع التصدير`() {
        val report = ShariaExportValidator.validate(
            timeline = validTimeline,
            audioFile = File("/nonexistent/path.mp3"),
            actualAudioDurationMs = 5_000L,
            sourceCard = validSourceCard
        )

        assertFalse(report.isValid)
        assertTrue(report.errors.contains("ملف الصوت المعزول غير موجود على القرص"))
    }

    @Test
    fun `بطاقة المصدر الغائبة تمنع التصدير`() {
        val tempFile = File.createTempFile("quran_test", ".mp3").apply { writeBytes(ByteArray(100)) }
        try {
            val report = ShariaExportValidator.validate(
                timeline = validTimeline,
                audioFile = tempFile,
                actualAudioDurationMs = 5_000L,
                sourceCard = null
            )

            assertFalse(report.isValid)
            assertTrue(report.errors.contains("بطاقة المصدر غائبة أو ناقصة"))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `اختلاف مدة الصوت عن التوقيت بأكثر من الهامش يمنع التصدير`() {
        val tempFile = File.createTempFile("quran_test", ".mp3").apply { writeBytes(ByteArray(100)) }
        try {
            val report = ShariaExportValidator.validate(
                timeline = validTimeline,
                audioFile = tempFile,
                actualAudioDurationMs = 10_000L, // اختلاف 5000ms، أكبر بكثير من الهامش
                sourceCard = validSourceCard
            )

            assertFalse(report.isValid)
            assertTrue(report.errors.contains("مدة ملف الصوت لا تطابق توقيت الكلمات"))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `بطاقة المصدر ترضى شرط https فقط`() {
        // http غير آمن يُرفض في البناء، وليس مجرد فشل تحقق
        try {
            SourceCard(
                reciterName = "مشاري",
                reciterStyle = "مرتل",
                verseKey = "1:1",
                recitationSourceUrl = "http://verses.quran.com/test.mp3",
                textSourceUrl = "https://api.quran.com/test",
                timingsSourceUrl = "https://api.qurancdn.com/test"
            )
            assert(false) { "يجب أن يفشل بناء بطاقة المصدر بـ http" }
        } catch (e: IllegalArgumentException) {
            // متوقع تمامًا
        }
    }
}
