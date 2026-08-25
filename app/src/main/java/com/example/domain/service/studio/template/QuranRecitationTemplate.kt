package com.example.domain.service.studio.template

import com.example.domain.model.quran.PhraseTimeline
import com.example.domain.model.quran.SourceCard
import com.example.domain.model.studio.BackgroundLayer
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.model.studio.TextAnimation
import com.example.domain.model.studio.TextLayer
import com.example.domain.service.studio.TextBitmapRenderer

/**
 * القالب الذي يحوّل [PhraseTimeline] (تلاوة موزّعة إلى عبارات وفق إيقاع الوقفات)
 * إلى [CompositionStoryboard] جاهز للتصدير: مشهد واحد يحمل الآية كاملة كـ
 * [SyncedAyahOverlay] ديناميكي بتظليل متعدد الطبقات (وضع قبس الذكي)، مع
 * بطاقة المصدر الموثّقة [SourceCard] مرسومة داخل الفيديو نفسه، وخلفية داكنة
 * افتراضية وصوت التلاوة المعزول.
 *
 * هذا يربط الـ Overlay الجديد (التظليل متعدد الطبقات + بطاقة المصدر) بمحرك
 * التصدير فعليًا: الناتج [CompositionStoryboard] يُمرَّر إلى
 * [com.example.domain.service.studio.StudioCompositionEngine.export] فيُرسم
 * الـ overlay داخل الفيديو المُصدَّر.
 *
 * @param textRenderer منشئ Bitmap للنص العربي.
 * @param width عرض الفيديو الهدف.
 * @param height ارتفاع الفيديو الهدف.
 * @param fps معدل الإطارات.
 * @param backgroundArgb لون الخلفية الافتراضي الداكن (يلائم نصًا ذهبيًا).
 * @param audioUri مسار ملف الصوت المعزول للآية (من QuranRepository.cacheVerseAudio).
 */
class QuranRecitationTemplate(
    private val textRenderer: TextBitmapRenderer,
    private val width: Int = 720,
    private val height: Int = 1280,
    private val fps: Int = 30,
    private val backgroundArgb: Int = 0xFF0B1020.toInt(),
    private val audioUri: String
) {

    /**
     * يبني لوحة القصة لمزامنة كاملة: مدة المشهد = مدة التلاوة + هامش بسيط
     * بعدها لئلا يُقطع الصوت فجأة في آخر كلمة. يضيف بطاقة المصدر [sourceCard]
     * كطبقة نص في أسفل الإطار إن توفّرت (نسبة مصدر التلاوة داخل الفيديو نفسه).
     */
    fun build(
        phraseTimeline: PhraseTimeline,
        sourceCard: SourceCard? = null
    ): CompositionStoryboard {
        val timeline = phraseTimeline.timeline
        val sceneDurationMs = (timeline.durationMs + TAIL_PADDING_MS)

        val ayahOverlay = SyncedAyahOverlay(
            phraseTimeline = phraseTimeline,
            renderer = textRenderer,
            videoWidth = width,
            videoHeight = height,
            baseAnchorX = 0f,
            baseAnchorY = 0f
        )

        val sourceCardLayer = sourceCard?.let {
            TextLayer(
                text = it.displayText(),
                fontSizeSp = 18,
                textColorArgb = 0xFFB9C2D6.toInt(),
                glow = false,
                alignment = LayerHorizontalAlignment.CENTER,
                verticalAnchor = LayerVerticalAlignment.BOTTOM,
                yOffsetPercent = -0.12f,
                animation = TextAnimation.FADE_IN,
                animationStartMs = 0L,
                animationDurationMs = TextAnimation.FADE_IN.durationMs
            )
        }

        val scene = CompositionScene(
            durationMs = sceneDurationMs,
            background = BackgroundLayer(
                type = BackgroundType.SOLID_COLOR,
                colorArgb = backgroundArgb
            ),
            textLayers = listOfNotNull(sourceCardLayer),
            overlayLayers = emptyList(),
            transitionMs = 0L,
            dynamicOverlay = ayahOverlay
        )

        return CompositionStoryboard(
            width = width,
            height = height,
            fps = fps,
            scenes = listOf(scene),
            audioUri = audioUri
        )
    }

    private companion object {
        const val TAIL_PADDING_MS = 400L
    }
}
