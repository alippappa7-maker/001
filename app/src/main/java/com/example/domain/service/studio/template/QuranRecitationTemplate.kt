package com.example.domain.service.studio.template

import android.net.Uri
import com.example.domain.model.quran.QuranExportPlan
import com.example.domain.model.quran.SourceCard
import com.example.domain.model.studio.BackgroundLayer
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.ImageOverlayLayer
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.model.studio.TextLayer
import com.example.domain.service.studio.TextBitmapRenderer

/**
 * القالب الذي يحوّل [QuranExportPlan] (الذي اجتاز البوابة الشرعية) إلى
 * [CompositionStoryboard] جاهز للتصدير: مشهد واحد يحمل الآية كاملة كـ
 * [SmartAyahOverlay] ديناميكي بتظليل متعدد الطبقات، مع بطاقة المصدر الموثّقة
 * مرسومة داخل الفيديو نفسه، وخلفية داكنة افتراضية وصوت التلاوة المعزول.
 *
 * هذا هو ربط الـ Overlay بمحرك التصدير فعليًا: التظليل متعدد الطبقات وبطاقة
 * المصدر يُرسمان داخل الفيديو المُصدَّر عبر
 * [com.example.domain.service.studio.StudioCompositionEngine]، ولا يصل الكود
 * إلى هنا أصلًا إلا بعد اجتياز
 * [com.example.domain.model.quran.QuranExportPreparation] (البوابة الشرعية).
 *
 * @param textRenderer منشئ Bitmap للنص العربي (يضمن التشكيل وRTL).
 * @param width عرض الفيديو الهدف.
 * @param height ارتفاع الفيديو الهدف.
 * @param fps معدل الإطارات.
 * @param backgroundArgb لون الخلفية الافتراضي الداكن (يلائم نصًا ذهبيًا).
 */
class QuranRecitationTemplate(
    private val textRenderer: TextBitmapRenderer,
    private val width: Int = 720,
    private val height: Int = 1280,
    private val fps: Int = 30,
    private val backgroundArgb: Int = 0xFF0B1020.toInt()
) {

    /**
     * يبني لوحة القصة لمزامنة كاملة: مدة المشهد = مدة التلاوة + هامش بسيط
     * بعدها لئلا يُقطع الصوت فجأة في آخر كلمة. التظليل متعدد الطبقات وبطاقة
     * المصدر يُركَّبان كـ overlays داخل المشهد، فيُرسمان في الفيديو المُصدَّر.
     */
    fun build(plan: QuranExportPlan): CompositionStoryboard {
        val sceneDurationMs = (plan.timeline.durationMs + TAIL_PADDING_MS)

        val ayahOverlay = SmartAyahOverlay(
            phraseTimeline = plan.phraseTimeline,
            renderer = textRenderer,
            videoWidth = width,
            videoHeight = height,
            baseAnchorX = 0f,
            baseAnchorY = 0f
        )

        val sourceCardLayer = ImageOverlayLayer(
            bitmap = textRenderer.render(sourceCardText(plan.sourceCard), width, height),
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.BOTTOM,
            yOffsetPercent = -0.04f,
            alpha = 0.92f
        )

        val scene = CompositionScene(
            durationMs = sceneDurationMs,
            background = BackgroundLayer(
                type = BackgroundType.SOLID_COLOR,
                colorArgb = backgroundArgb
            ),
            textLayers = emptyList(),
            overlayLayers = listOf(sourceCardLayer),
            transitionMs = 0L,
            dynamicOverlay = ayahOverlay
        )

        return CompositionStoryboard(
            width = width,
            height = height,
            fps = fps,
            scenes = listOf(scene),
            audioUri = Uri.fromFile(plan.audioFile).toString()
        )
    }

    private fun sourceCardText(card: SourceCard): TextLayer = TextLayer(
        text = card.displayText(),
        fontSizeSp = 22,
        textColorArgb = 0xFFC9BFA8.toInt(),
        alignment = LayerHorizontalAlignment.CENTER
    )

    private companion object {
        const val TAIL_PADDING_MS = 400L
    }
}
