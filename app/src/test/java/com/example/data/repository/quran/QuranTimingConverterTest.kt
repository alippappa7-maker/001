package com.example.data.repository.quran

import com.example.data.api.model.quran.VerseTimingDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * اختبار وحدة JVM صرف للتحويل الزمني الأهم في الميزة: تحويل إزاحات
 * QuranCDN (نسبية لبداية ملف السورة الكاملة) إلى توقيتات نسبية لبداية
 * ملف الآية المعزول، مطابقة كل كلمة بموضعها الصحيح.
 */
class QuranTimingConverterTest {

    private val words = listOf(1 to "بِسْمِ", 2 to "اللَّهِ", 3 to "الرَّحْمَنِ", 4 to "الرَّحِيمِ")

    @Test
    fun `يطرح إزاحة بداية الآية من كل segment بشكل صحيح`() {
        // بيانات مطابقة لمثال حقيقي من التقرير التقني: verse timestamp_from=6153050
        val verseTiming = VerseTimingDto(
            verseKey = "1:1",
            timestampFrom = 6_153_050L,
            timestampTo = 6_158_050L,
            duration = 5000L,
            segments = listOf(
                listOf(1L, 6_153_050L, 6_153_630L), // 0..580 نسبيًا
                listOf(2L, 6_153_750L, 6_154_550L), // 700..1500 نسبيًا
                listOf(3L, 6_154_750L, 6_156_050L), // 1700..3000 نسبيًا
                listOf(4L, 6_156_250L, 6_158_050L)  // 3200..5000 نسبيًا
            )
        )

        val result = QuranTimingConverter.buildTimedWords(words, verseTiming)

        requireNotNull(result)
        assertEquals(4, result.size)
        assertEquals(0L, result[0].startMs)
        assertEquals(580L, result[0].endMs)
        assertEquals(700L, result[1].startMs)
        assertEquals(1500L, result[1].endMs)
        assertEquals(3200L, result[3].startMs)
        assertEquals(5000L, result[3].endMs)
    }

    @Test
    fun `segment بزاوية واحدة فقط يُكمَل بنهاية الآية`() {
        val verseTiming = VerseTimingDto(
            verseKey = "1:1",
            timestampFrom = 1000L,
            timestampTo = 2000L,
            duration = 1000L,
            segments = listOf(
                listOf(1L, 1000L, 1500L),
                listOf(2L, 1500L) // بلا نهاية: يجب أن تُصبح نهايتها = timestampTo - verseStart
            )
        )
        val twoWords = listOf(1 to "أ", 2 to "ب")

        val result = QuranTimingConverter.buildTimedWords(twoWords, verseTiming)

        requireNotNull(result)
        assertEquals(500L, result[1].startMs) // 1500 - 1000
        assertEquals(1000L, result[1].endMs) // timestampTo(2000) - verseStart(1000)
    }

    @Test
    fun `عدم تطابق عدد الكلمات مع التوقيتات يرفض البناء بالكامل`() {
        // توقيت لكلمة واحدة فقط بينما لدينا 4 كلمات — يجب رفض الخط الزمني بأكمله.
        val verseTiming = VerseTimingDto(
            verseKey = "1:1",
            timestampFrom = 0L,
            timestampTo = 1000L,
            duration = 1000L,
            segments = listOf(listOf(1L, 0L, 500L))
        )

        val result = QuranTimingConverter.buildTimedWords(words, verseTiming)

        assertNull(result)
    }

    @Test
    fun `segments فارغة ترفض البناء`() {
        val verseTiming = VerseTimingDto(
            verseKey = "1:1",
            timestampFrom = 0L,
            timestampTo = 1000L,
            duration = 1000L,
            segments = emptyList()
        )

        val result = QuranTimingConverter.buildTimedWords(words, verseTiming)

        assertNull(result)
    }

    @Test
    fun `توقيتات أكثر من عدد الكلمات ترفض لتلافي مصدر فاسد`() {
        // 4 كلمات لكن 5 توقيتات (عنصر زائد) — يجب رفض البناء بالكامل.
        val verseTiming = VerseTimingDto(
            verseKey = "1:1",
            timestampFrom = 0L,
            timestampTo = 5000L,
            duration = 5000L,
            segments = listOf(
                listOf(1L, 0L, 1000L),
                listOf(2L, 1000L, 2000L),
                listOf(3L, 2000L, 3000L),
                listOf(4L, 3000L, 4000L),
                listOf(5L, 4000L, 5000L) // عنصر زائد غير مطابق لكلمة
            )
        )

        val result = QuranTimingConverter.buildTimedWords(words, verseTiming)

        assertNull(result)
    }
}
