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
 * قالب "سينمائي" — افتتاح سينمائي بثلاث مراحل بصرية متميّزة:
 *
 * 1) مشهد عنوان رئيسي: النص الأول كبير ووسط الشاشة، مدة أطول، خلفية داكنة جدًا،
 *    مع تلاشٍ هادئ بطيء لمنح إحساس الافتتاح السينمائي.
 * 2) مشاهد نبضية متتابعة للنصوص التالية: كل نص يظهر بتلاشٍ مع انتقال أطول،
 *    وإزاحة عمودية متبادلة لإكساب الإيقاع حركة بصرية.
 * 3) مشهد ختامي بالعلامة/الخلاصة بخلفية كحلية وذهبية.
 *
 * لا يولّد أي نص ديني — كل النصوص من إدخال المستخدم عبر [VideoProject]،
 * مع placeholder واضح عند الفراغ.
 */
class CinematicTemplate : CompositionTemplate {

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
        val scenes = mutableListOf<CompositionScene>()

        // 1) مشهد العنوان الافتتاحي.
        val title = texts.firstOrNull() ?: TemplateCommon.fallbackIdeaLine(project, "أدخل عنوان المشهد هنا")
        scenes += titleScene(title, brand)

        // 2) مشاهد نبضية للنصوص التالية.
        texts.drop(1).forEachIndexed { index, line -> scenes += pulseScene(line, index, brand) }

        // 3) مشهد الخاتمة.
        scenes += closingScene(brand)
        return scenes
    }

    private fun titleScene(title: String, brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = title,
            fontSizeSp = 48,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = GOLD,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 400,
            animationDurationMs = 1600,
            maxLines = 5
        )
        layers += TemplateCommon.brandLayer(brand, GOLD, fontSizeSp = 22)

        return CompositionScene(
            id = "cinematic_title",
            durationMs = 7000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = DEEP_BLACK),
            textLayers = layers,
            transitionMs = 800
        )
    }

    private fun pulseScene(line: String, index: Int, brand: String): CompositionScene {
        // إزاحة عمودية متبادلة لإكساب الإيقاع حركة بصرية.
        val yOffset = if (index % 2 == 0) -0.04f else 0.05f
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = line,
            fontSizeSp = 44,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = WHITE,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = yOffset,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 300,
            animationDurationMs = 1200,
            maxLines = 5
        )
        layers += TemplateCommon.brandLayer(brand, GOLD, fontSizeSp = 20)
        return CompositionScene(
            id = "cinematic_pulse_$index",
            durationMs = 5000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = NAVY_DARK),
            textLayers = layers,
            transitionMs = 750
        )
    }

    private fun closingScene(brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = brand,
            fontSizeSp = 36,
            textColorArgb = GOLD,
            glow = true,
            glowColorArgb = GOLD,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 300,
            animationDurationMs = 1400,
            maxLines = 1
        )
        return CompositionScene(
            id = "cinematic_closing",
            durationMs = 4500,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = MIDNIGHT),
            textLayers = layers,
            transitionMs = 850
        )
    }

    companion object {
        private val WHITE = 0xFFFFFFFF.toInt()
        private val GOLD = 0xFFE8C97A.toInt()
        private val DEEP_BLACK = 0xFF05080F.toInt()
        private val NAVY_DARK = 0xFF0B1426.toInt()
        private val MIDNIGHT = 0xFF0A0A16.toInt()
    }
}
