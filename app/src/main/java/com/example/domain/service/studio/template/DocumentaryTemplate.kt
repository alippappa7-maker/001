package com.example.domain.service.studio.template

import com.example.domain.model.studio.BackgroundLayer
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.model.studio.TextAnimation
import com.example.domain.model.studio.TextLayer
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoProject

/**
 * قالب "وثائقي" — بطاقات وثائقية متميّزة بطبقة عنوان صغيرة فوق كل نص:
 *
 * كل مشهد يحتوي طبقتين نصيتين: عنوان/وسم صغير (مثل "نقطة ١") في الأعلى،
 * والنص الأساسي أسفله مع محاذاة مائلة للأسفل. الوسم هو عنصر واجهة بحت
 * (ترقيم تسلسلي) وليس نصًا دينيًا — النص الأساسي كله من إدخال المستخدم.
 *
 * ألوان ورقية/كحلية، مدد متوسطة، انتقالات منظمة.
 */
class DocumentaryTemplate : CompositionTemplate {

    override fun build(project: VideoProject): CompositionStoryboard {
        val texts = TemplateCommon.collectTexts(project)
        val orientation = project.idea.orientation
        val (width, height) = TemplateCommon.resolveDimensions(orientation)
        val brand = "قبس"
        val scenes = buildScenes(texts, project, brand)
        return CompositionStoryboard(width = width, height = height, fps = 30, scenes = scenes)
    }

    private fun buildScenes(
        texts: List<String>,
        project: VideoProject,
        brand: String
    ): List<CompositionScene> {
        val resolved = texts.ifEmpty {
            listOf(TemplateCommon.fallbackIdeaLine(project, "أدخل نص الوثيقة هنا"))
        }
        val scenes = mutableListOf<CompositionScene>()

        // مشهد المقدمة.
        scenes += introScene(resolved.first(), brand)

        // مشاهد النقاط المرقّمة (تخطّي الأول لأنه المقدمة).
        resolved.drop(1).forEachIndexed { index, line ->
            scenes += pointScene(line, index + 1, brand)
        }

        // مشهد الخاتمة.
        scenes += closingScene(brand)
        return scenes
    }

    private fun introScene(text: String, brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += labelLayer("المقدمة")
        layers += TextLayer(
            text = text,
            fontSizeSp = 42,
            textColorArgb = PAPER,
            glow = false,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = 0.04f,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 250,
            animationDurationMs = 1100,
            maxLines = 6
        )
        layers += TemplateCommon.brandLayer(brand, MUTED_GOLD, fontSizeSp = 20)
        return CompositionScene(
            id = "doc_intro",
            durationMs = 6000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = PAPER_DARK),
            textLayers = layers,
            transitionMs = 550
        )
    }

    private fun pointScene(text: String, pointNumber: Int, brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += labelLayer("نقطة $pointNumber")
        layers += TextLayer(
            text = text,
            fontSizeSp = 40,
            textColorArgb = PAPER,
            glow = false,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = 0.04f,
            animation = TextAnimation.SLIDE_UP,
            animationStartMs = 200,
            maxLines = 6
        )
        layers += TemplateCommon.brandLayer(brand, MUTED_GOLD, fontSizeSp = 20)
        return CompositionScene(
            id = "doc_point_$pointNumber",
            durationMs = 5500,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = PAPER_DARK),
            textLayers = layers,
            transitionMs = 500
        )
    }

    private fun closingScene(brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += labelLayer("الخاتمة")
        layers += TextLayer(
            text = brand,
            fontSizeSp = 34,
            textColorArgb = MUTED_GOLD,
            glow = false,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = 0.04f,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 200,
            maxLines = 1
        )
        return CompositionScene(
            id = "doc_closing",
            durationMs = 4000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = PAPER_DARK),
            textLayers = layers,
            transitionMs = 550
        )
    }

    private fun labelLayer(labelText: String): TextLayer = TextLayer(
        text = labelText,
        fontSizeSp = 22,
        textColorArgb = MUTED_GOLD,
        glow = false,
        alignment = LayerHorizontalAlignment.CENTER,
        verticalAnchor = LayerVerticalAlignment.CENTER,
        yOffsetPercent = -0.14f,
        animation = TextAnimation.FADE_IN,
        animationStartMs = 100,
        animationDurationMs = 700,
        maxLines = 1
    )

    companion object {
        private val PAPER = 0xFFEDE6D6.toInt()
        private val PAPER_DARK = 0xFF1A1D22.toInt()
        private val MUTED_GOLD = 0xFFC9B07A.toInt()
    }
}
