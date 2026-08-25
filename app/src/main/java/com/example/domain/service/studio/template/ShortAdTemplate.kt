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
 * قالب "إعلان قصير" — هيكل تسويقي ثلاثي المراحل دون اختلاق محتوى:
 *
 * 1) Hook: أول نص (أو نص الفكرة احتياطيًا).
 * 2) Benefit/Proof: النصوص التالية كنقاط بيع.
 * 3) CTA: نص دعوة الإجراء — من المستخدم إن وجد، وإلا placeholder واضح
 *    "أدخل دعوة الإجراء هنا" (لا يُولَّد نص تسويقي من الكود).
 *
 * ألوان قوية، خط أكبر، مدد قصيرة، آخر مشهد مختلف بوضوح.
 */
class ShortAdTemplate : CompositionTemplate {

    override fun build(project: VideoProject): CompositionStoryboard {
        val texts = TemplateCommon.collectTexts(project)
        val orientation = project.idea.orientation
        val (width, height) = TemplateCommon.resolveDimensions(orientation)
        val brand = "قبس"
        val scenes = buildScenes(texts, project, brand)
            .mapIndexed { index, scene ->
                scene.copy(
                    resourceIntent = TemplateCommon.resourceIntentForScene(
                        project = project,
                        sceneIndex = index,
                        durationMs = scene.durationMs,
                        preferMotion = true
                    )
                )
            }
        return CompositionStoryboard(width = width, height = height, fps = 30, scenes = scenes)
    }

    private fun buildScenes(
        texts: List<String>,
        project: VideoProject,
        brand: String
    ): List<CompositionScene> {
        val scenes = mutableListOf<CompositionScene>()

        // 1) Hook: أول نص (أو نص الفكرة احتياطيًا).
        val hook = texts.firstOrNull() ?: TemplateCommon.fallbackIdeaLine(project, "أدخل جملة الجذب هنا")
        scenes += hookScene(hook, brand)

        // 2) Benefits: كل النصوص بعد الأول، باستثناء الأخير الذي يُحجز كـ CTA
        //    إن وجد أكثر من نصين؛ وإلا كل النصوص الوسطى فوائد.
        val benefits = when {
            texts.size > 2 -> texts.subList(1, texts.size - 1)
            texts.size == 2 -> listOf(texts[1])
            else -> emptyList()
        }
        benefits.forEachIndexed { index, line -> scenes += benefitScene(line, index, brand) }

        // 3) CTA: آخر نص من المستخدم إن وُجد وأختلف عن الـ hook؛ وإلا placeholder واضح.
        val cta = when {
            texts.size >= 2 -> texts.lastOrNull { it.isNotBlank() && it != hook }?.takeIf { it.isNotBlank() }
            else -> null
        } ?: "أدخل دعوة الإجراء هنا"
        scenes += ctaScene(cta, brand)
        return scenes
    }

    private fun hookScene(text: String, brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = text,
            fontSizeSp = 52,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = ACCENT,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.SLIDE_UP,
            animationStartMs = 150,
            animationDurationMs = 900,
            maxLines = 3
        )
        layers += TemplateCommon.brandLayer(brand, ACCENT, fontSizeSp = 20)
        return CompositionScene(
            id = "ad_hook",
            durationMs = 3000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = BG),
            textLayers = layers,
            transitionMs = 250
        )
    }

    private fun benefitScene(text: String, index: Int, brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = TemplateCommon.visualMarker(index),
            fontSizeSp = 40,
            textColorArgb = ACCENT,
            glow = true,
            glowColorArgb = ACCENT,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = -0.12f,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 100,
            maxLines = 1
        )
        layers += TextLayer(
            text = text,
            fontSizeSp = 44,
            textColorArgb = WHITE,
            glow = false,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = 0.06f,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 300,
            animationDurationMs = 900,
            maxLines = 4
        )
        layers += TemplateCommon.brandLayer(brand, ACCENT, fontSizeSp = 18)
        return CompositionScene(
            id = "ad_benefit_$index",
            durationMs = 2600,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = BG),
            textLayers = layers,
            transitionMs = 200
        )
    }

    private fun ctaScene(cta: String, brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = cta,
            fontSizeSp = 50,
            textColorArgb = ACCENT,
            glow = true,
            glowColorArgb = ACCENT,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.GLOW_PULSE,
            animationStartMs = 100,
            animationDurationMs = 1100,
            maxLines = 2
        )
        layers += TemplateCommon.brandLayer(brand, WHITE, fontSizeSp = 20)
        return CompositionScene(
            id = "ad_cta",
            durationMs = 3200,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = CTA_BG),
            textLayers = layers,
            transitionMs = 300
        )
    }

    companion object {
        private val WHITE = 0xFFFFFFFF.toInt()
        private val ACCENT = 0xFFFFC400.toInt()
        private val BG = 0xFF1A1400.toInt()
        private val CTA_BG = 0xFF0B0B0F.toInt()
    }
}
