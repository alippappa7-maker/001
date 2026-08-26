package com.example.domain.service.studio.template

import com.example.domain.model.studio.TextAnimation
import kotlin.math.sin

/**
 * حسابات الحركة الزمنية للنص — **نقية** تمامًا (لا تلمس android.graphics.Bitmap)،
 * لذا يمكن اختبارها في JVM دون Robolectric. [AnimatedTextOverlay] يستهلك ناتجها
 * ويحوّله إلى OverlaySettings داخل Media3.
 *
 * كل القيم مُقاسة بالميلي ثانية، و[presentationTimeMs] نسبي لبدء المشهد.
 */
object TextAnimationMath {

    /** إطار حركة واحد لطبقة نص: شدّة الشفافية وإزاحة المحور الصادي النسبية. */
    data class Frame(val alphaScale: Float, val yOffset: Float)

    /**
     * يحسب إطار الحركة للحظة [presentationTimeMs] حسب نوع [animation].
     * النطاقات المضمونة: alphaScale ∈ [0,1]، yOffset ∈ [-0.5, 0].
     */
    fun compute(
        animation: TextAnimation,
        presentationTimeMs: Long,
        startMs: Long,
        durationMs: Long
    ): Frame {
        val dur = durationMs.coerceAtLeast(1L)
        return when (animation) {
            TextAnimation.NONE -> Frame(1f, 0f)
            TextAnimation.FADE_IN -> Frame(fadeAlpha(presentationTimeMs, startMs, dur), 0f)
            TextAnimation.SLIDE_UP -> Frame(1f, slideYOffset(presentationTimeMs, startMs, dur))
            TextAnimation.GLOW_PULSE -> Frame(glowPulse(presentationTimeMs, startMs), 0f)
            TextAnimation.WORD_BY_WORD -> Frame(1f, 0f) // الكشف يحدث داخل [KineticTextOverlay]
            // TYPEWRITER غير مدعوم فعليًا (تقطيع النص العربي يكسر التشكيل) — يُعامل كتلاشٍ تدريجي.
            TextAnimation.TYPEWRITER -> Frame(fadeAlpha(presentationTimeMs, startMs, dur), 0f)
        }
    }

    /** نسبة التقدّم في الفترة [0,1] مع تثبيت ما قبل البداية بـ0 وما بعد النهاية بـ1. */
    private fun progress(tMs: Long, startMs: Long, dur: Long): Float {
        if (dur <= 0L) return 1f
        val p = (tMs - startMs).toFloat() / dur
        return p.coerceIn(0f, 1f)
    }

    private fun easeOutQuad(p: Float): Float = 1f - (1f - p) * (1f - p)

    private fun easeOutCubic(p: Float): Float {
        val inv = (1f - p).coerceAtLeast(0f)
        return 1f - inv * inv * inv
    }

    /** تلاشٍ تدريجي للدخول: 0 قبل البداية → 1 بعد اكتمال المدة (مع easeOut). */
    fun fadeAlpha(tMs: Long, startMs: Long, dur: Long): Float =
        easeOutQuad(progress(tMs, startMs, dur))

    /** صعود من الأسفل: يبدأ بإزاحة -0.45 وينتهي عند 0 (مع easeOutCubic). */
    fun slideYOffset(tMs: Long, startMs: Long, dur: Long): Float {
        val eased = easeOutCubic(progress(tMs, startMs, dur))
        return START_SLIDE_OFFSET * (1f - eased)
    }

    /**
     * توهج نابض (تنفّس بصري): alpha تتراوح بين 0.6 و1.0 بدورة 1200 مللي ثانية.
     * قبل [startMs] يكون alpha = 1 (ظهور طبيعي).
     */
    fun glowPulse(tMs: Long, startMs: Long): Float {
        val t = (tMs - startMs).toFloat()
        if (t < 0f) return 1f
        val periodMs = 1200f
        val wave = sin(2.0 * Math.PI * (t / periodMs))
        return (0.6f + 0.4f * (0.5f + 0.5f * wave.toFloat())).coerceIn(0.6f, 1f)
    }

    private const val START_SLIDE_OFFSET = -0.45f
}
