package com.example.domain.model.quran

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * اختبارات وحدة لمنطق الحالات البصرية في [PhraseTimeline] — التحقق من أن
 * الكلمة النشطة تُعطى [WordVisualState.ACTIVE]، وبقية كلمات عبارتها الحالية
 * تُعطى [WordVisualState.CURRENT]، وكل ما هو خارج العبارة الحالية يُعطى
 * [WordVisualState.PAST].
 */
class PhraseTimelineTest {

    /**
     * نفس بنية الكلمات في اختبار المُكتشف: عبارتان،
     * الأولى (1, 2, 3) والثانية (4).
     */
    private val timeline = RecitationTimeline(
        verseKey = "1:1",
        textUthmani = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
        reciterId = 7,
        reciterName = "مشاري",
        verseAudioUrl = "x",
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
    fun `الكلمة النشطة في العبارة الأولى تُعطى ACTIVE وبقية كلماتها CURRENT`() {
        // نطق الكلمة الثانية (وسط العبارة الأولى)
        val states = phraseTimeline.visualStatesAt(1_000L)

        assertEquals(WordVisualState.PAST, states[0])
        assertEquals(WordVisualState.ACTIVE, states[1])
        assertEquals(WordVisualState.CURRENT, states[2])
        assertEquals(WordVisualState.PAST, states[3])
    }

    @Test
    fun `عبارة العبارة الثانية تكون تماماً بمفردها ولا تختلط مع الأولى`() {
        // نطق الكلمة الرابعة (العبارة الثانية كلها)
        val states = phraseTimeline.visualStatesAt(4_000L)

        // العبارة الأولى بأكملها تُعدّ الآن "سابقة" (PAST) لأنها قبل العبارة الحالية
        states.subList(0, 3).forEach { assertEquals(WordVisualState.PAST, it) }
        assertEquals(WordVisualState.ACTIVE, states[3])
    }

    @Test
    fun `قبل بداية أول كلمة تعامل كل الكلمات كـ PAST`() {
        val states = phraseTimeline.visualStatesAt(-100L)

        assertEquals(4, states.size)
        states.forEach { assertEquals(WordVisualState.PAST, it) }
    }

    @Test
    fun `phraseAt يعيد العبارة الصحيحة لكل كلمة`() {
        // فهرس 1 (الكلمة الثانية) ضمن العبارة الأولى
        val phrase1 = phraseTimeline.phraseAt(1)
        assertEquals(0, phrase1?.startIndex)
        assertEquals(2, phrase1?.endIndex)

        // فهرس 3 (الكلمة الرابعة) ضمن العبارة الثانية
        val phrase2 = phraseTimeline.phraseAt(3)
        assertEquals(3, phrase2?.startIndex)
        assertEquals(3, phrase2?.endIndex)
    }
}
