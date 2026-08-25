@file:androidx.annotation.OptIn(
    markerClass = [androidx.media3.common.util.UnstableApi::class]
)

package com.example.domain.service.studio.template

import android.graphics.Bitmap
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlaySettings
import com.example.domain.model.quran.PhraseTimeline
import com.example.domain.model.quran.WordVisualState
import com.example.domain.service.studio.TextBitmapRenderer

/**
 * طبقة آية متزامنة مع التلاوة: ترث [BitmapOverlay] وتتجاوز [getBitmap]
 * لإرجاع Bitmap الآية مع تظليل متعدد الطبقات (وضع قبس الذكي) في تلك اللحظة.
 *
 * بدل إضاءة كلمة واحدة فقط، نستخدم [PhraseTimeline.visualStatesAt] لتحديد
 * حالة كل كلمة (ACTIVE / CURRENT / PAST) وفق إيقاع الوقفات الفعلية بين
 * الكلمات، ثم نُرجع الـ Bitmap المُجهَّز مسبقًا لتلك الحالة البصرية.
 *
 * لتفادي إعادة رسم Bitmap لكل إطار (مكلف)، نخزّن مسبقًا Bitmap لكل كلمة
 * نشطة ممكنة (يُحسب لها التظليل الكامل متعدد الطبقات عند لحظة نشاطها)،
 * بالإضافة إلى Bitmap لحالة "لا توجد كلمة نشطة" (كل الكلمات PAST).
 * عدد الكلمات صغير جدًا (نادرًا يتجاوز 30)، لذا التكلفة معقولة.
 *
 * منطق اختيار الـ Bitmap المناسب للّحظة معزول في [activeBitmapIndexFor]
 * (دالة خالصة بدون أي اعتماد على أندرويد) ليُختبَر على مستوى JVM مباشرة.
 *
 * @param phraseTimeline خط زمني التلاوة الموزّع إلى عبارات (من [RecitationCueDetector]).
 * @param renderer منشئ Bitmap للنص العربي (يضمن التشكيل وRTL).
 * @param videoWidth عرض الفيديو الهدف.
 * @param videoHeight ارتفاع الفيديو الهدف.
 * @param baseAnchorX إحداثي X النسبي لموضع الآية داخل الإطار.
 * @param baseAnchorY إحداثي Y النسبي لموضع الآية داخل الإطار.
 */
class SyncedAyahOverlay(
    private val phraseTimeline: PhraseTimeline,
    private val renderer: TextBitmapRenderer,
    private val videoWidth: Int,
    private val videoHeight: Int,
    private val baseAnchorX: Float = 0f,
    private val baseAnchorY: Float = 0f
) : BitmapOverlay() {

    // قائمة نصوص الكلمات بالترتيب (مصدرها الخط الزمني الأساسي).
    private val wordTexts: List<String> = phraseTimeline.timeline.words.map { it.text }

    // Bitmap مُجهَّز مسبقًا لكل كلمة نشطة ممكنة + حالة "بلا كلمة نشطة".
    private val bitmapsByIndex: List<Bitmap> by lazy { preRenderBitmaps() }

    // إعداد التراكب ثابت: لا نُنشئ OverlaySettings في كل إطار (تفاديًا للأداء).
    private val fixedOverlaySettings: OverlaySettings = OverlaySettings.Builder()
        .setOverlayFrameAnchor(0f, 0f)
        .setBackgroundFrameAnchor(baseAnchorX, baseAnchorY)
        .setAlphaScale(1f)
        .build()

    private fun preRenderBitmaps(): List<Bitmap> {
        val n = wordTexts.size
        val result = ArrayList<Bitmap>(n + 1)
        // index 0..n-1 = حالة نشاط الكلمة رقم index (مع تظليلها متعدد الطبقات الكامل).
        for (i in wordTexts.indices) {
            val states = phraseTimeline.visualStatesAt(phraseTimeline.timeline.words[i].startMs)
            result.add(
                renderer.renderWithVisualStates(
                    words = wordTexts,
                    visualStates = states,
                    videoWidth = videoWidth,
                    videoHeight = videoHeight
                )
            )
        }
        // index n = لا توجد كلمة نشطة (كل الكلمات PAST، قبل بداية أول كلمة).
        val beforeFirst = phraseTimeline.timeline.words.first().startMs - 1
        result.add(
            renderer.renderWithVisualStates(
                words = wordTexts,
                visualStates = phraseTimeline.visualStatesAt(beforeFirst),
                videoWidth = videoWidth,
                videoHeight = videoHeight
            )
        )
        return result
    }

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        val timeMs = presentationTimeUs / MS_PER_US
        return bitmapsByIndex[activeBitmapIndexFor(phraseTimeline, timeMs)]
    }

    override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings =
        fixedOverlaySettings

    private companion object {
        const val MS_PER_US = 1_000L
    }
}

/**
 * يعيد فهرس الـ Bitmap المُجهَّز مسبقًا المناسب للّحظة [timeMs]:
 * - إذا وُجدت كلمة نشطة (ACTIVE)، يعيد فهرسها (0-based ضمن الكلمات).
 * - إذا لم توجد (قبل أول كلمة أو فجوة لا تُغطّيها سياسة الإبقاء)،
 *   يعيد [PhraseTimeline.timeline.words].size (وهو فهرس خانة "بلا كلمة نشطة").
 *
 * دالة خالصة على المستوى الأعلى (لا تنتمي لـ [SyncedAyahOverlay] نفسه)
 * كي تُختبَر على مستوى JVM دون تحميل صنف Media3 الأساسي.
 */
fun activeBitmapIndexFor(phraseTimeline: PhraseTimeline, timeMs: Long): Int {
    val states = phraseTimeline.visualStatesAt(timeMs)
    val activeIndex = states.indexOf(WordVisualState.ACTIVE)
    return if (activeIndex >= 0) activeIndex else phraseTimeline.timeline.words.size
}
