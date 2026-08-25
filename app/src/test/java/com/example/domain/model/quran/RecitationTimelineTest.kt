package com.example.domain.model.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * اختبارات وحدة صريحة لمنطق المزامنة كلمة بكلمة — بدون أي طبقة أندرويد (JVM فقط).
 * التركيز على activeWordPositionAt: أنه يحدّد الكلمة الصحيحة في النقاط
 * الحرجة (بداية، وسط، نهاية كلمة، فجوات صمت بين الكلمات، قبل الكل، بعد الكل).
 *
 * التحقق الشرعي: العلاقة بين عدد الكلمات والتوقيتات، نهاية قبل بداية مرفوضة،
 * position سالب مرفوض — لأن أي خطأ في التوقيت أصلًا في تطبيق قرآن لا يُسمح
 * أبداً: تصدير نص/توقيت خاطئ يعني إخلال ديني، لذلك نرفض بناء الخط الزمني.
 */
class RecitationTimelineTest {

    private val timeline = RecitationTimeline(
        verseKey = "1:1",
        textUthmani = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
        reciterId = 7,
        reciterName = "مشاري راشد العفاسي",
        verseAudioUrl = "https://verses.quran.com/Alafasy/mp3/001001.mp3",
        durationMs = 10_000L,
        words = listOf(
            TimedQuranWord(position = 1, text = "بِسْمِ", startMs = 0, endMs = 580),
            TimedQuranWord(position = 2, text = "اللَّهِ", startMs = 700, endMs = 1500),
            TimedQuranWord(position = 3, text = "الرَّحْمَنِ", startMs = 1700, endMs = 3000),
            TimedQuranWord(position = 4, text = "الرَّحِيمِ", startMs = 3200, endMs = 5000)
        )
    )

    @Test
    fun `تحديد الكلمة الأولى عند بداية النطق`() {
        assertEquals(1, timeline.activeWordPositionAt(0))
        assertEquals(1, timeline.activeWordPositionAt(300))
        assertEquals(1, timeline.activeWordPositionAt(579))
    }

    @Test
    fun `فجوة الصمت بعد الكلمة الأولى تبقي الكلمة الأولى مظللة`() {
        assertEquals(1, timeline.activeWordPositionAt(600))
        assertEquals(1, timeline.activeWordPositionAt(699))
    }

    @Test
    fun `الانتقال إلى الكلمة الثانية عند بدايتها`() {
        assertEquals(2, timeline.activeWordPositionAt(700))
        assertEquals(2, timeline.activeWordPositionAt(1200))
    }

    @Test
    fun `اختيار الكلمة الأخيرة عند النهاية`() {
        assertEquals(4, timeline.activeWordPositionAt(5000))
        assertEquals(4, timeline.activeWordPositionAt(5200))
    }

    @Test
    fun `قبل بداية الكلمة الأولى يعيد null`() {
        assertNull(timeline.activeWordPositionAt(-1))
        assertNull(timeline.activeWordPositionAt(-100))
    }

    @Test
    fun `التحقق الشرعي - عدد الكلمات الفارغ يرفض البناء`() {
        assertThrows(IllegalArgumentException::class.java) {
            RecitationTimeline(
                verseKey = "1:1",
                textUthmani = "",
                reciterId = 7,
                reciterName = "مشاري",
                verseAudioUrl = "x",
                durationMs = 10_000L,
                words = emptyList()
            )
        }
    }

    @Test
    fun `التحقق الشرعي - نهاية قبل بداية ترفض بناء الكلمة`() {
        assertThrows(IllegalArgumentException::class.java) {
            TimedQuranWord(position = 1, text = "كلمة", startMs = 1000, endMs = 500)
        }
    }

    @Test
    fun `التحقق الشرعي - position سالب يرفض بناء الكلمة`() {
        assertThrows(IllegalArgumentException::class.java) {
            TimedQuranWord(position = 0, text = "كلمة", startMs = 0, endMs = 100)
        }
    }
}
