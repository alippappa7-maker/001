package com.example.domain.service.studio.template

import com.example.domain.model.quran.RecitationTimeline
import com.example.domain.model.studio.BackgroundLayer
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.service.studio.TextBitmapRenderer

/**
 * القالب الذي يحوّل [RecitationTimeline] إلى [CompositionStoryboard] جاهز
 * للتصدير: مشهد واحد يحمل الآية كاملة كـ [SyncedAyahOverlay] ديناميكي،
 * مع خلفية داكنة افتراضية وصوت التلاوة المعزول.
 *
 * هذا يمثّل الحد الأدنى القابل للعمل (MVP) للميزة الثورية. و"وضع قبس الذكي"
 * الذي سيُبنى فوق هذا سيستبدل الخلفية الثابتة بخلفية تناسب معنى الآية،
 * ويضيف تقسيمًا حسب الوقفات وإيقاع تنفس القارئ.
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
     * بعدها لئلا يُقطع الصوت فجأة في آخر كلمة.
     */
    fun build(timeline: RecitationTimeline): CompositionStoryboard {
        val sceneDurationMs = (timeline.durationMs + TAIL_PADDING_MS)

        val ayahOverlay = SyncedAyahOverlay(
            timeline = timeline,
            renderer = textRenderer,
            videoWidth = width,
            videoHeight = height,
            baseAnchorX = 0f,
            baseAnchorY = 0f
        )

        val scene = CompositionScene(
            durationMs = sceneDurationMs,
            background = BackgroundLayer(
                type = BackgroundType.SOLID_COLOR,
                colorArgb = backgroundArgb
            ),
            textLayers = emptyList(),
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
