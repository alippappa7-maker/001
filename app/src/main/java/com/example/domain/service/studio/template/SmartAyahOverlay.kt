@file:androidx.annotation.OptIn(
    markerClass = [androidx.media3.common.util.UnstableApi::class]
)

package com.example.domain.service.studio.template

import android.graphics.Bitmap
import androidx.media3.effect.OverlaySettings
import androidx.media3.effect.BitmapOverlay
import com.example.domain.model.quran.PhraseTimeline
import com.example.domain.model.quran.WordVisualState
import com.example.domain.service.studio.TextBitmapRenderer

/**
 * طبقة آية متزامنة مع التظليل متعدد الطبقات لـ "وضع قبس الذكي": ترث [BitmapOverlay]
 * وتتجاوز [getBitmap] لإرجاع Bitmap الآية مع تظليل ثلاثي الطبقات (نشطة/جارية/سابقة)
 * المحسوب من [PhraseTimeline] في تلك اللحظة.
 *
 * بدل إضاءة كلمة واحدة فقط (كما في [SyncedAyahOverlay])، تُلوَّن الكلمة النشطة
 * بالذهبي مع توهّج، وبقية كلمات العبارة الحالية بلون أوضح، وما خارجها بأخفّ —
 * فيبدو الفيديو مصمَّمًا على إيقاع التلاوة نفسه. تُرسم هذه الطبقات فعليًا داخل
 * الفيديو المُصدَّر عبر [com.example.domain.service.studio.StudioCompositionEngine]
 * بعد اجتياز بوابة [com.example.domain.model.quran.ShariaExportValidator].
 *
 * لتجنب إعادة الرسم لكل إطار، نخزّن مسبقًا Bitmap لكل كلمة نشطة ممكنة + Bitmap
 * لحالة "لا توجد كلمة نشطة" (كل الكلمات بلونها الأخفّ). عدد الكلمات صغير جدًا
 * (نادرًا يتجاوز 30)، فالتكلفة الإجمالية معقولة والذاكرة محدودة.
 *
 * @param phraseTimeline خط زمني التلاوة الموزّع إلى عبارات (من
 *        [com.example.domain.model.quran.RecitationCueDetector]).
 * @param renderer منشئ Bitmap للنص العربي متعدد الطبقات.
 * @param videoWidth عرض الفيديو الهدف.
 * @param videoHeight ارتفاع الفيديو الهدف.
 * @param baseAnchorX إحداثي X النسبي لموضع الآية داخل الإطار.
 * @param baseAnchorY إحداثي Y النسبي لموضع الآية داخل الإطار.
 */
class SmartAyahOverlay(
    private val phraseTimeline: PhraseTimeline,
    private val renderer: TextBitmapRenderer,
    private val videoWidth: Int,
    private val videoHeight: Int,
    private val baseAnchorX: Float = 0f,
    private val baseAnchorY: Float = 0f
) : BitmapOverlay() {

    private val timeline = phraseTimeline.timeline

    // قائمة الكلمات بالترتيب (نص فقط).
    private val wordTexts: List<String> = timeline.words.map { it.text }

    // خريطة (position -> index 0-based) لتسريع البحث.
    private val positionToIndex: Map<Int, Int> =
        timeline.words.mapIndexed { idx, w -> w.position to idx }.toMap()

    // Bitmap لكل كلمة نشطة ممكنة + حالة "لا توجد كلمة نشطة".
    private val bitmapsByIndex: List<Bitmap> by lazy { preRenderBitmaps() }

    // إعداد التراكب ثابت: لا نُنشئ OverlaySettings في كل إطار (تفاديًا للأداء).
    private val fixedOverlaySettings: OverlaySettings = OverlaySettings.Builder()
        .setOverlayFrameAnchor(0f, 0f)
        .setBackgroundFrameAnchor(baseAnchorX, baseAnchorY)
        .setAlphaScale(1f)
        .build()

    private fun preRenderBitmaps(): List<Bitmap> {
        val result = ArrayList<Bitmap>(wordTexts.size + 1)
        for (i in wordTexts.indices) {
            result.add(renderForActiveIndex(i))
        }
        // الحالة بلا كلمة نشطة: كل الكلمات سابقة (أخفّ).
        result.add(renderForStates(wordTexts.indices.map { WordVisualState.PAST }))
        return result
    }

    private fun renderForActiveIndex(activeIndex: Int): Bitmap {
        val activePhrase = phraseTimeline.phraseAt(activeIndex)
        val states = wordTexts.indices.map { idx ->
            when {
                activePhrase == null || !activePhrase.contains(idx) -> WordVisualState.PAST
                idx == activeIndex -> WordVisualState.ACTIVE
                else -> WordVisualState.CURRENT
            }
        }
        return renderForStates(states)
    }

    private fun renderForStates(states: List<WordVisualState>): Bitmap =
        renderer.renderMultiLayer(
            words = wordTexts,
            states = states,
            videoWidth = videoWidth,
            videoHeight = videoHeight
        )

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        val timeMs = presentationTimeUs / MS_PER_US
        val activePosition = timeline.activeWordPositionAt(timeMs)
        val index = activePosition?.let { positionToIndex[it] }
            ?: wordTexts.size // الحالة بلا تظليل نشط
        return bitmapsByIndex[index]
    }

    override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings =
        fixedOverlaySettings

    private companion object {
        const val MS_PER_US = 1_000L
    }
}
