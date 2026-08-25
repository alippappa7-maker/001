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
 * قالب "رسوم متحركة" — بطاقات مرحة بألوان زاهية متبادلة:
 *
 * كل نص على خلفية لون زاهٍ مختلف من لوحة دوّارة، مع علامة شكلية بصرية
 * (◆ ● ■ ▲) كعنصر واجهة بحت (ليست نصًا دينيًا) تبديلًا للمحاذاة والإزاحة،
 * وتوهج نابض خفيف. حجم خط أكبر وأقل رسمية. النصوص كلها من المستخدم.
 */
class AnimationTemplate : CompositionTemplate {

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
            listOf(TemplateCommon.fallbackIdeaLine(project, "أدخل نصك هنا"))
        }
        return resolved.mapIndexed { index, line -> playfulScene(line, index, brand) }
    }

    private fun playfulScene(text: String, index: Int, brand: String): CompositionScene {
        val palette = PLAYFUL_PALETTES[index % PLAYFUL_PALETTES.size]
        // تبديل المحاذاة الأفقية والإزاحة بين المشاهد لإكساب حركة بصرية.
        val alignment = if (index % 2 == 0) LayerHorizontalAlignment.START else LayerHorizontalAlignment.END
        val yOffset = if (index % 2 == 0) -0.05f else 0.05f

        val layers = mutableListOf<TextLayer>()
        // علامة شكلية كعنصر بصري (ليست نصًا دينيًا).
        layers += TextLayer(
            text = TemplateCommon.visualMarker(index),
            fontSizeSp = 56,
            textColorArgb = palette.markerColor,
            glow = true,
            glowColorArgb = palette.markerColor,
            alignment = alignment,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = -0.14f,
            animation = TextAnimation.GLOW_PULSE,
            animationStartMs = 100,
            animationDurationMs = 1000,
            maxLines = 1
        )
        layers += TextLayer(
            text = text,
            fontSizeSp = 46,
            textColorArgb = WHITE,
            glow = false,
            alignment = alignment,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = yOffset,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 250,
            animationDurationMs = 900,
            maxLines = 4
        )
        layers += TemplateCommon.brandLayer(brand, palette.markerColor, fontSizeSp = 18)
        return CompositionScene(
            id = "anim_$index",
            durationMs = 3500,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = palette.bgColor),
            textLayers = layers,
            transitionMs = 350
        )
    }

    private data class PlayfulPalette(val bgColor: Int, val markerColor: Int)

    companion object {
        private val WHITE = 0xFFFFFFFF.toInt()
        private val PLAYFUL_PALETTES = listOf(
            PlayfulPalette(0xFF1E3A8A.toInt(), 0xFF60A5FA.toInt()), // أزرق
            PlayfulPalette(0xFF312E81.toInt(), 0xFFA78BFA.toInt()), // بنفسجي
            PlayfulPalette(0xFF991B1B.toInt(), 0xFFF87171.toInt()), // أحمر
            PlayfulPalette(0xFF065F46.toInt(), 0xFF34D399.toInt()), // أخضر
            PlayfulPalette(0xFF9A3412.toInt(), 0xFFFB923C.toInt()), // برتقالي
            PlayfulPalette(0xFF581C87.toInt(), 0xFFC084FC.toInt())  // أرجواني
        )
    }
}
