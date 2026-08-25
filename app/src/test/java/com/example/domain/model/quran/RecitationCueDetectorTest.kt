package com.example.domain.model.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات وحدة لـ [RecitationCueDetector]: التحقق من أن الكلمات تُجمّع
 * في عبارات صحيحة بناءً على فجوات الصمت بينها فقط — لا وفق أي معنى نصّي.
 *
 * هندسة الاختبار: نُنشئ خطًا زمنيًا مبنيًا بوضوح يدويًا مع فجوات صامتة
 * معروفة المسافة (أكبر من الحد، وأصغر منه)، ونتحقق أن التقسيم يتبع الفجوات
 * تمامًا لا العدد، وأن الفجوات الأصغر من الحد لا تكسر العبارة.
 */
class RecitationCueDetectorTest {

    /**
     * خريطة كلمات اختبارية بهذه الفجوات:
     * 1 → 2: فجوة 100ms (أصغر من 250) → نفس العبارة
     * 2 → 3: فجوة 100ms (أصغر من 250) → نفس العبارة
     * 3 → 4: فجوة 300ms (أكبر من 250) → نهاية عبارة، تبدأ عبارة جديدة
     *
     * النتيجة المتوقعة: عبارتان، الأولى (1,2,3) والثانية (4).
     */
    private val timeline = RecitationTimeline(
        verseKey = "1:1",
        textUthmani = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
        reciterId = 7,
        reciterName = "مشاري راشد العفاسي",
        verseAudioUrl = "https://verses.quran.com/Alafasy/mp3/001001.mp3",
        durationMs = 10_000L,
        words = listOf(
            TimedQuranWord(position = 1, text = "بِسْمِ", startMs = 0, endMs = 500),
            TimedQuranWord(position = 2, text = "اللَّهِ", startMs = 600, endMs = 1500),
            TimedQuranWord(position = 3, text = "الرَّحْمَنِ", startMs = 1600, endMs = 3000),
            TimedQuranWord(position = 4, text = "الرَّحِيمِ", startMs = 3300, endMs = 5000)
        )
    )

    @Test
    fun `فجوة صامتة أكبر من الحد تقسم العبارات بشكل صحيح`() {
        val phraseTimeline = RecitationCueDetector.detect(timeline)

        assertEquals(2, phraseTimeline.phrases.size)

        val first = phraseTimeline.phrases[0]
        assertEquals(0, first.startIndex)
        assertEquals(2, first.endIndex)
        assertEquals(3, first.words.size)

        val second = phraseTimeline.phrases[1]
        assertEquals(3, second.startIndex)
        assertEquals(3, second.endIndex)
        assertEquals(1, second.words.size)
    }

    @Test
    fun `جميع العبارات تغطي الكلمات بالكامل دون فجوات أو تداخل`() {
        val phraseTimeline = RecitationCueDetector.detect(timeline)

        // لا توجد فجوة بين العبارات المتجاورة
        for (i in 0 until phraseTimeline.phrases.size - 1) {
            val current = phraseTimeline.phrases[i]
            val next = phraseTimeline.phrases[i + 1]
            assertEquals(current.endIndex + 1, next.startIndex)
        }

        // أول عبارة تبدأ من الصفر وآخرها تنتهي عند آخر فهرس
        assertEquals(0, phraseTimeline.phrases.first().startIndex)
        assertEquals(timeline.words.lastIndex, phraseTimeline.phrases.last().endIndex)
    }

    @Test
    fun `فجوات صغيرة تحت الحد لا تقسم العبارة`() {
        val noGapTimeline = RecitationTimeline(
            verseKey = "1:1",
            textUthmani = "بِسْمِ اللَّهِ",
            reciterId = 7,
            reciterName = "مشاري",
            verseAudioUrl = "x",
            durationMs = 5_000L,
            words = listOf(
                TimedQuranWord(position = 1, text = "بِسْمِ", startMs = 0, endMs = 500),
                TimedQuranWord(position = 2, text = "اللَّهِ", startMs = 560, endMs = 1500),
                TimedQuranWord(position = 3, text = "الرَّحْمَنِ", startMs = 1560, endMs = 3000),
                TimedQuranWord(position = 4, text = "الرَّحِيمِ", startMs = 3060, endMs = 5000)
            )
        )

        val phraseTimeline = RecitationCueDetector.detect(noGapTimeline)

        assertEquals(1, phraseTimeline.phrases.size)
        assertEquals(0, phraseTimeline.phrases.first().startIndex)
        assertEquals(3, phraseTimeline.phrases.first().endIndex)
    }

    @Test
    fun `آية من كلمة واحدة تنتج عبارة واحدة`() {
        val singleWordTimeline = RecitationTimeline(
            verseKey = "112:1",
            textUthmani = "قُلْ",
            reciterId = 7,
            reciterName = "مشاري",
            verseAudioUrl = "x",
            durationMs = 2_000L,
            words = listOf(
                TimedQuranWord(position = 1, text = "قُلْ", startMs = 0, endMs = 1000)
            )
        )

        val phraseTimeline = RecitationCueDetector.detect(singleWordTimeline)

        assertEquals(1, phraseTimeline.phrases.size)
        assertEquals(0, phraseTimeline.phrases.first().startIndex)
        assertEquals(0, phraseTimeline.phrases.first().endIndex)
    }
}
