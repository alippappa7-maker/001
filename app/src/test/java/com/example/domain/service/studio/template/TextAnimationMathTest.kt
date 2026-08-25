package com.example.domain.service.studio.template

import com.example.domain.model.studio.TextAnimation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات وحدة نقية (JVM بلا Robolectric) لـ [TextAnimationMath].
 * لا تلمس android.graphics.Bitmap — فقط دوال حسابية.
 */
class TextAnimationMathTest {

    @Test
    fun `fadeAlpha قبل البداية يساوي صفر`() {
        val alpha = TextAnimationMath.fadeAlpha(tMs = 0, startMs = 500, dur = 800)
        assertEquals(0f, alpha, 0.001f)
    }

    @Test
    fun `fadeAlpha عند اكتمال المدة يساوي واحد`() {
        val alpha = TextAnimationMath.fadeAlpha(tMs = 1300, startMs = 500, dur = 800)
        assertEquals(1f, alpha, 0.001f)
    }

    @Test
    fun `fadeAlpha في منتصف المدة بين صفر وواحد`() {
        val alpha = TextAnimationMath.fadeAlpha(tMs = 900, startMs = 500, dur = 800)
        assertTrue("expected in (0,1) but was $alpha", alpha > 0f && alpha < 1f)
    }

    @Test
    fun `fadeAlpha لا يتجاوز واحد بعد النهاية بكثير`() {
        val alpha = TextAnimationMath.fadeAlpha(tMs = 10_000, startMs = 500, dur = 800)
        assertEquals(1f, alpha, 0.001f)
    }

    @Test
    fun `slideYOffset يبدأ بإزاحة سالبة`() {
        val offset = TextAnimationMath.slideYOffset(tMs = 0, startMs = 200, dur = 700)
        assertEquals(-0.45f, offset, 0.001f)
    }

    @Test
    fun `slideYOffset ينتهي عند صفر بعد اكتمال المدة`() {
        val offset = TextAnimationMath.slideYOffset(tMs = 1000, startMs = 200, dur = 700)
        assertEquals(0f, offset, 0.001f)
    }

    @Test
    fun `slideYOffset يقترب من صفر مع تقدم الزمن`() {
        val early = TextAnimationMath.slideYOffset(tMs = 250, startMs = 200, dur = 700)
        val late = TextAnimationMath.slideYOffset(tMs = 800, startMs = 200, dur = 700)
        assertTrue(late > early)
        assertTrue(late <= 0f)
    }

    @Test
    fun `glowPulse قبل البداية يساوي واحد`() {
        val alpha = TextAnimationMath.glowPulse(tMs = 0, startMs = 500)
        assertEquals(1f, alpha, 0.001f)
    }

    @Test
    fun `glowPulse يبقى دائمًا داخل النطاق المسموح`() {
        val startMs = 100L
        for (t in 0..5000 step 37) {
            val alpha = TextAnimationMath.glowPulse(tMs = t.toLong(), startMs = startMs)
            assertTrue("alpha=$alpha at t=$t out of range", alpha in 0.6f..1f)
        }
    }

    @Test
    fun `glowPulse يتذبذب فعلاً ولا يبقى ثابتًا`() {
        val values = (0..1200 step 100).map { TextAnimationMath.glowPulse(tMs = it.toLong(), startMs = 0) }
        assertTrue(values.maxOrNull()!! - values.minOrNull()!! > 0.2f)
    }

    @Test
    fun `compute مع NONE يعيد alpha واحد بلا إزاحة`() {
        val frame = TextAnimationMath.compute(TextAnimation.NONE, presentationTimeMs = 500, startMs = 0, durationMs = 800)
        assertEquals(1f, frame.alphaScale, 0.001f)
        assertEquals(0f, frame.yOffset, 0.001f)
    }

    @Test
    fun `compute مع FADE_IN يفوّض إلى fadeAlpha بلا إزاحة`() {
        val frame = TextAnimationMath.compute(TextAnimation.FADE_IN, presentationTimeMs = 0, startMs = 0, durationMs = 800)
        assertEquals(0f, frame.alphaScale, 0.001f)
        assertEquals(0f, frame.yOffset, 0.001f)
    }

    @Test
    fun `compute مع SLIDE_UP يفوّض إلى slideYOffset مع alpha ثابت واحد`() {
        val frame = TextAnimationMath.compute(TextAnimation.SLIDE_UP, presentationTimeMs = 0, startMs = 0, durationMs = 700)
        assertEquals(1f, frame.alphaScale, 0.001f)
        assertEquals(-0.45f, frame.yOffset, 0.001f)
    }

    @Test
    fun `compute مع TYPEWRITER يُعامل كتلاشٍ تدريجي لا كتقطيع نص`() {
        val fade = TextAnimationMath.compute(TextAnimation.FADE_IN, presentationTimeMs = 300, startMs = 0, durationMs = 800)
        val typewriter = TextAnimationMath.compute(TextAnimation.TYPEWRITER, presentationTimeMs = 300, startMs = 0, durationMs = 800)
        assertEquals(fade.alphaScale, typewriter.alphaScale, 0.001f)
        assertEquals(0f, typewriter.yOffset, 0.001f)
    }

    @Test
    fun `مدة صفر لا تسبب قسمة على صفر`() {
        val frame = TextAnimationMath.compute(TextAnimation.FADE_IN, presentationTimeMs = 50, startMs = 0, durationMs = 0)
        assertEquals(1f, frame.alphaScale, 0.001f)
    }
}
