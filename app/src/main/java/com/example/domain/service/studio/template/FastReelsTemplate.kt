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
 * قالب "ريلز سريع" — إيقاع سريع متقطّع:
 *
 * كل نص يصبح مشهدًا قصيرًا جدًا (٢–٢٫٥ ثانية)، حجم خط كبير، توهج نابض،
 * وموضع نص يتبدّل بين الأعلى/الوسط/الأسفل بين المشاهد لإكساب الإيقاع حركة،
 * مع انتقالات قصيرة جدًا. النصوص كلها من المستخدم، مع placeholder عند الفراغ.
 */
class FastReelsTemplate : CompositionTemplate {

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
        val resolved = texts.ifEmpty {
            listOf(TemplateCommon.fallbackIdeaLine(project, "أدخل نصك السريع هنا"))
        }
        return resolved.mapIndexed { index, line -> fastScene(line, index, brand) }
    }

    private fun fastScene(text: String, index: Int, brand: String): CompositionScene {
        // تبديل الموضع العمودي بين المشاهد لإكساب الإيقاع حركة بصرية.
        val yOffset = when (index % 3) {
            0 -> -0.08f
            1 -> 0f
            else -> 0.08f
        }
        val verticalAnchor = when (index % 3) {
            0 -> LayerVerticalAlignment.TOP
            1 -> LayerVerticalAlignment.CENTER
            else -> LayerVerticalAlignment.BOTTOM
        }
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = text,
            fontSizeSp = 50,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = ACCENT,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = verticalAnchor,
            yOffsetPercent = yOffset,
            animation = TextAnimation.GLOW_PULSE,
            animationStartMs = 100,
            animationDurationMs = 900,
            maxLines = 3
        )
        layers += TemplateCommon.brandLayer(brand, ACCENT, fontSizeSp = 18)
        return CompositionScene(
            id = "reels_$index",
            durationMs = 2200,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = HIGH_CONTRAST_BG),
            textLayers = layers,
            transitionMs = 200
        )
    }

    companion object {
        private val WHITE = 0xFFFFFFFF.toInt()
        private val ACCENT = 0xFFFF6E40.toInt()
        private val HIGH_CONTRAST_BG = 0xFF0B0B0F.toInt()
    }
}
