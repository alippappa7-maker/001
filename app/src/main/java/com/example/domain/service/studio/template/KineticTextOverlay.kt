@file:androidx.annotation.OptIn(
    markerClass = [androidx.media3.common.util.UnstableApi::class]
)

package com.example.domain.service.studio.template

import android.graphics.Bitmap
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlaySettings
import com.example.domain.service.studio.TextBitmapRenderer

/**
 * تايبوغرافي حركي: يكشف عبارة عربية كلمة بكلمة (وليس حرفًا بحرفًا، لتفادي
 * كسر التشكيل والربط العربي). يرث [BitmapOverlay] بنمط [SyncedAyahOverlay]
 * المُثبَت: يُعيد Bitmap مختلفًا حسب presentationTimeUs بدل تحريك طبقة ثابتة.
 *
 * آلية الكشف: كل كلمة تحصل على نافذة زمنية مدتها [wordStaggerMs]، تنقسم إلى
 * عدد صغير من "خطوات الشفافية" ([fadeSteps]) بدل ظهور مفاجئ دفعة واحدة —
 * هذا يعطي تلاشيًا سلسًا شبيهًا بأسلوب "الظهور بتركيز مفاجئ" (snap-to-focus)
 * الملاحَظ في الفيديوهات المرجعية، دون الحاجة لرسم Bitmap في كل إطار.
 *
 * كل حالة (كلمة نشطة × خطوة شفافية) تُرسم كعبارة كاملة عبر
 * [TextBitmapRenderer.renderWordReveal] حتى يبقى التخطيط وموضع كل كلمة
 * ثابتًا تمامًا عبر كل الإطارات — فقط الشفافية تتغيّر، فلا قفز أو ارتجاف.
 *
 * @param words نصوص الكلمات بالترتيب الصحيح (مقسّمة بالمسافات، لا بالأحرف).
 * @param renderer منشئ Bitmap للنص العربي (يضمن التشكيل والاتجاه RTL الصحيحين).
 * @param videoWidth عرض الفيديو الهدف.
 * @param videoHeight ارتفاع الفيديو الهدف.
 * @param fontSizeSp حجم خط العبارة.
 * @param colorArgb لون النص الأساسي.
 * @param startMs لحظة بدء الكشف داخل المشهد (بالمللي ثانية).
 * @param wordStaggerMs مدة نافذة كشف كلمة واحدة (بالمللي ثانية).
 * @param baseAnchorX إحداثي X النسبي لموضع العبارة داخل الإطار.
 * @param baseAnchorY إحداثي Y النسبي لموضع العبارة داخل الإطار.
 * @param fadeSteps عدد درجات الشفافية الوسيطة لكل كلمة (دقّة التلاشي).
 */
class KineticTextOverlay(
    private val words: List<String>,
    private val renderer: TextBitmapRenderer,
    private val videoWidth: Int,
    private val videoHeight: Int,
    private val fontSizeSp: Int = 44,
    private val colorArgb: Int = 0xFFFFFFFF.toInt(),
    private val startMs: Long = 0L,
    private val wordStaggerMs: Long = 260L,
    private val baseAnchorX: Float = 0f,
    private val baseAnchorY: Float = 0f,
    fadeSteps: Int = 4
) : BitmapOverlay() {

    // حماية من نصوص طويلة جدًا: كل حالة (كلمة × خطوة) تُخزَّن كـ Bitmap في الذاكرة،
    // فنُسقّف عدد الكلمات الفعلية المُستخدمة في الكشف التدريجي.
    private val effectiveWords: List<String> = words.take(MAX_WORDS)
    private val fadeSteps: Int = fadeSteps.coerceIn(1, MAX_FADE_STEPS)

    // إعداد التراكب ثابت الموضع — فقط محتوى الـ Bitmap يتغيّر بمرور الوقت.
    private val fixedOverlaySettings: OverlaySettings = OverlaySettings.Builder()
        .setOverlayFrameAnchor(0f, 0f)
        .setBackgroundFrameAnchor(baseAnchorX, baseAnchorY)
        .setAlphaScale(1f)
        .build()

    // bitmapsByState[wordIndex * fadeSteps + step] = العبارة الكاملة مع الكلمة
    // رقم wordIndex بشفافية (step/fadeSteps)، وكل ما بعدها مخفي.
    // العنصر الأخير الإضافي = العبارة كاملة مكشوفة (نهاية الحركة).
    private val bitmapsByState: List<Bitmap> by lazy { preRenderBitmaps() }

    private val totalStates: Int get() = effectiveWords.size * fadeSteps

    private fun preRenderBitmaps(): List<Bitmap> {
        if (effectiveWords.isEmpty()) {
            return listOf(
                renderer.renderWordReveal(
                    words = listOf(""),
                    revealingWordIndex = 1,
                    revealProgress = 1f,
                    videoWidth = videoWidth,
                    videoHeight = videoHeight,
                    fontSizeSp = fontSizeSp,
                    colorArgb = colorArgb
                )
            )
        }
        val result = ArrayList<Bitmap>(totalStates + 1)
        for (wordIndex in effectiveWords.indices) {
            for (step in 0 until fadeSteps) {
                val progress = (step + 1).toFloat() / fadeSteps
                result.add(
                    renderer.renderWordReveal(
                        words = effectiveWords,
                        revealingWordIndex = wordIndex,
                        revealProgress = progress,
                        videoWidth = videoWidth,
                        videoHeight = videoHeight,
                        fontSizeSp = fontSizeSp,
                        colorArgb = colorArgb
                    )
                )
            }
        }
        // حالة نهائية: كل الكلمات مكشوفة بالكامل — تبقى معروضة إلى نهاية المشهد.
        result.add(
            renderer.renderWordReveal(
                words = effectiveWords,
                revealingWordIndex = effectiveWords.size,
                revealProgress = 1f,
                videoWidth = videoWidth,
                videoHeight = videoHeight,
                fontSizeSp = fontSizeSp,
                colorArgb = colorArgb
            )
        )
        return result
    }

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        if (effectiveWords.isEmpty()) return bitmapsByState[0]
        val tMs = presentationTimeUs / MS_PER_US
        val elapsed = (tMs - startMs).coerceAtLeast(0L)
        val slot = (elapsed / wordStaggerMs).toInt()
        if (slot >= effectiveWords.size) {
            return bitmapsByState.last()
        }
        val within = elapsed % wordStaggerMs
        val step = ((within.toFloat() / wordStaggerMs) * fadeSteps).toInt().coerceIn(0, fadeSteps - 1)
        val index = slot * fadeSteps + step
        return bitmapsByState[index.coerceIn(0, bitmapsByState.size - 1)]
    }

    override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings = fixedOverlaySettings

    /** المدة الإجمالية التقديرية لاكتمال الكشف (لحساب مدة المشهد إن لزم). */
    fun totalRevealDurationMs(): Long = startMs + effectiveWords.size.toLong() * wordStaggerMs

    private companion object {
        const val MS_PER_US = 1_000L
        const val MAX_WORDS = 36
        const val MAX_FADE_STEPS = 6
    }
}
