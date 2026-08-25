package com.example.domain.service.studio.template

import com.example.domain.model.quran.RecitationCueDetector
import com.example.domain.model.quran.RecitationTimeline
import com.example.domain.model.quran.TimedQuranWord
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * اختبارات وحدة لمنطق اختيار الـ Bitmap في [activeBitmapIndexFor] — وهي الدالة
 * الخالصة التي تربط حالة كل كلمة (من [com.example.domain.model.quran.PhraseTimeline.visualStatesAt])
 * بفهرس الـ Bitmap المُجهَّز مسبقًا داخل [SyncedAyahOverlay].
 *
 * هذه كلها JVM فقط — لا تعتمد على أندرويد أو Media3.
 */
class SyncedAyahOverlaySelectionTest {

    /**
     * نفس بنية الكلمات في اختبارات [PhraseTimeline] و[RecitationCueDetector]:
     * عبارتان، الأولى (1, 2, 3) والثانية (4).
     */
    private val timeline = RecitationTimeline(
        verseKey = "1:1",
        textUthmani = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
        reciterId = 7,
        reciterName = "مشاري",
        verseAudioUrl = "https://verses.quran.com/Alafasy/mp3/001001.mp3",
        durationMs = 10_000L,
        words = listOf(
            TimedQuranWord(position = 1, text = "بِسْمِ", startMs = 0, endMs = 500),
            TimedQuranWord(position = 2, text = "اللَّهِ", startMs = 600, endMs = 1500),
            TimedQuranWord(position = 3, text = "الرَّحْمَنِ", startMs = 1600, endMs = 3000),
            TimedQuranWord(position = 4, text = "الرَّحِيمِ", startMs = 3300, endMs = 5000)
        )
    )

    private val phraseTimeline = RecitationCueDetector.detect(timeline)

    @Test
    fun `الكلمة النشطة في وسط العبارة الأولى تعيد فهرسها الصحيح`() {
        // نطق الكلمة الثانية (فهرس 1) ضمن العبارة الأولى
        assertEquals(1, activeBitmapIndexFor(phraseTimeline, 1_000L))
    }

    @Test
    fun `الكلمة النشطة في العبارة الثانية تعيد فهرسها الصحيح`() {
        // نطق الكلمة الرابعة (فهرس 3) ضمن العبارة الثانية
        assertEquals(3, activeBitmapIndexFor(phraseTimeline, 4_000L))
    }

    @Test
    fun `الكلمة الأولى في بداية التلاوة تعيد فهرس 0`() {
        assertEquals(0, activeBitmapIndexFor(phraseTimeline, 100L))
    }

    @Test
    fun `قبل بداية أول كلمة تعيد فهرس خانة بلا كلمة نشطة`() {
        // قبل أول كلمة: لا توجد كلمة نشطة → فهرس = عدد الكلمات (الخانة الإضافية)
        assertEquals(4, activeBitmapIndexFor(phraseTimeline, -100L))
    }

    @Test
    fun `فجوة صغيرة بين كلمتين تبقي الكلمة السابقة نشطة`() {
        // فجوة بين نهاية الكلمة الأولى (500) وبداية الثانية (600): سياسة الإبقاء
        // تُبقي الكلمة الأولى نشطة، فيعود فهرسها 0.
        assertEquals(0, activeBitmapIndexFor(phraseTimeline, 550L))
    }

    @Test
    fun `فجوة بين العبارتين تبقي آخر كلمة منتهية نشطة`() {
        // في فجوة الصمت بين نهاية الكلمة الثالثة (3000) وبداية الرابعة (3300)،
        // تبقي سياسة activeWordPositionAt آخر كلمة منتهية (الثالثة، فهرس 2) نشطة.
        assertEquals(2, activeBitmapIndexFor(phraseTimeline, 3_100L))
    }
}
