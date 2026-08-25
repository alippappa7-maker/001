@file:androidx.annotation.OptIn(
    markerClass = [androidx.media3.common.util.UnstableApi::class]
)

package com.example.domain.service.studio.template

import android.graphics.Bitmap
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlaySettings
import com.example.domain.model.quran.RecitationTimeline
import com.example.domain.service.studio.TextBitmapRenderer

/**
 * طبقة آية متزامنة مع التلاوة: ترث [BitmapOverlay] وتتجاوز [getBitmap]
 * لإرجاع Bitmap الآية مع تظليل الكلمة النشطة في تلك اللحظة.
 *
 * النمط من [AnimatedTextOverlay]: المحتوى يبدّل عبر [getBitmap] حسب
 * presentationTimeUs، بينما يبقى الموضع ثابتًا عبر [getOverlaySettings].
 *
 * لتفادي إعادة رسم Bitmap لكل إطار (مكلف)، نخزّن مسبقًا Bitmap لكل كلمة
 * ممكنة + Bitmap لفترة "لا توجد كلمة نشطة" (كل الكلمات بلونها العادي)،
 * ثم نختار المناسب فقط. عدد الكلمات صغير جدًا (نادرًا يتجاوز 30)،
 * لذا التكلفة الإجمالية معقولة، والذاكرة محدودة أيضًا.
 *
 * @param timeline خط زمني التلاوة المبني من [QuranRepository].
 * @param renderer منشئ Bitmap للنص العربي (يضمن التشكيل وRTL).
 * @param videoWidth عرض الفيديو الهدف.
 * @param videoHeight ارتفاع الفيديو الهدف.
 * @param baseAnchorX إحداثي X النسبي لموضع الآية داخل الإطار.
 * @param baseAnchorY إحداثي Y النسبي لموضع الآية داخل الإطار.
 */
class SyncedAyahOverlay(
    private val timeline: RecitationTimeline,
    private val renderer: TextBitmapRenderer,
    private val videoWidth: Int,
    private val videoHeight: Int,
    private val baseAnchorX: Float = 0f,
    private val baseAnchorY: Float = 0f
) : BitmapOverlay() {

    // قائمة الكلمات بالترتيب (نص فقط).
    private val wordTexts: List<String> = timeline.words.map { it.text }

    // خرائط الكلمات (position -> index 0-based) لتسريع البحث.
    private val positionToIndex: Map<Int, Int> =
        timeline.words.mapIndexed { idx, w -> w.position to idx }.toMap()

    // تخزين Bitmap مسبقًا لكل كلمة نشطة ممكنة + حالة "لا توجد كلمة".
    private val bitmapsByIndex: List<Bitmap> by lazy { preRenderBitmaps() }

    // إعداد التراكب ثابت: لا نُنشئ OverlaySettings في كل إطار (تفاديًا للأداء).
    private val fixedOverlaySettings: OverlaySettings = OverlaySettings.Builder()
        .setOverlayFrameAnchor(0f, 0f)
        .setBackgroundFrameAnchor(baseAnchorX, baseAnchorY)
        .setAlphaScale(1f)
        .build()

    private fun preRenderBitmaps(): List<Bitmap> {
        val total = wordTexts.size + 1 // +1 للحالة بلا تظليل
        val result = ArrayList<Bitmap>(total)
        // index 0..n-1 = تظليل الكلمة رقم index، index n = لا تظليل.
        for (i in wordTexts.indices) {
            result.add(renderForIndex(i))
        }
        result.add(renderForIndex(-1))
        return result
    }

    private fun renderForIndex(activeIndex: Int): Bitmap =
        renderer.renderHighlighted(
            words = wordTexts,
            activeWordIndex = activeIndex,
            videoWidth = videoWidth,
            videoHeight = videoHeight
        )

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        val timeMs = presentationTimeUs / MS_PER_US
        val activePosition = timeline.activeWordPositionAt(timeMs)
        val index = activePosition?.let { positionToIndex[it] }
            ?: wordTexts.size // الحالة بلا تظليل
        return bitmapsByIndex[index]
    }

    override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings =
        fixedOverlaySettings

    private companion object {
        const val MS_PER_US = 1_000L
    }
}
