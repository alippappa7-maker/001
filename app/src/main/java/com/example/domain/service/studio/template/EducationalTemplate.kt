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
 * قالب "تعليمي" — درس مصغّر بترقيم واضح للنقاط:
 *
 * 1) مشهد افتتاح يعرض "الفكرة" أو العنوان (أول نص).
 * 2) مشاهد تعليمية مرقّمة: كل مشهد يحوي رقمًا كبيرًا (١، ٢، ٣ ...) كطبقة
 *    بصرية + شرح النص الأساسي تحته. الأرقام عناصر واجهة وليست نصًا دينيًا.
 * 3) مشهد مراجعة/خاتمة بالعلامة.
 *
 * ألوان زرقاء/تركواز، انتقالات قصيرة منظمة، حجم خط أكبر للوضوح التعليمي.
 */
class EducationalTemplate : CompositionTemplate {

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
                        preferMotion = false
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

        // مشهد العنوان الافتتاحي.
        val title = texts.firstOrNull() ?: TemplateCommon.fallbackIdeaLine(project, "أدخل عنوان الدرس هنا")
        scenes += titleScene(title, brand)

        // النقاط التعليمية (بقية النصوص).
        val lessonTexts = texts.drop(1).ifEmpty {
            // إن لم يوجد سوى النص الأول، كرّره كنقطة تعليمية واحدة.
            listOf(title)
        }
        lessonTexts.forEachIndexed { index, line -> scenes += lessonScene(line, index + 1, brand) }

        // مشهد الخاتمة.
        scenes += closingScene(brand)
        return scenes
    }

    private fun titleScene(title: String, brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = title,
            fontSizeSp = 46,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = TEAL,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 300,
            animationDurationMs = 1200,
            maxLines = 6
        )
        layers += TemplateCommon.brandLayer(brand, TEAL, fontSizeSp = 20)
        return CompositionScene(
            id = "edu_title",
            durationMs = 5000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = BLUE_DARK),
            textLayers = layers,
            transitionMs = 400
        )
    }

    private fun lessonScene(text: String, pointNumber: Int, brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        // طبقة الرقم الكبيرة كعنصر بصري تعليمي.
        layers += TextLayer(
            text = arabicNumber(pointNumber),
            fontSizeSp = 64,
            textColorArgb = TEAL_BRIGHT,
            glow = true,
            glowColorArgb = TEAL_BRIGHT,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = -0.16f,
            animation = TextAnimation.SLIDE_UP,
            animationStartMs = 150,
            animationDurationMs = 700,
            maxLines = 1
        )
        layers += TextLayer(
            text = text,
            fontSizeSp = 40,
            textColorArgb = WHITE,
            glow = false,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = 0.08f,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 400,
            animationDurationMs = 1000,
            maxLines = 6
        )
        layers += TemplateCommon.brandLayer(brand, TEAL, fontSizeSp = 20)
        return CompositionScene(
            id = "edu_lesson_$pointNumber",
            durationMs = 5500,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = BLUE_DARK),
            textLayers = layers,
            transitionMs = 350
        )
    }

    private fun closingScene(brand: String): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = brand,
            fontSizeSp = 34,
            textColorArgb = TEAL_BRIGHT,
            glow = true,
            glowColorArgb = TEAL_BRIGHT,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 200,
            maxLines = 1
        )
        return CompositionScene(
            id = "edu_closing",
            durationMs = 3500,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = BLUE_DARK),
            textLayers = layers,
            transitionMs = 400
        )
    }

    /** يحوّل رقمًا إلى صيغة عربية-هندية للعرض البصري (عنصر واجهة بحت). */
    private fun arabicNumber(value: Int): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return value.toString().map { digit ->
            val d = digit.digitToIntOrNull()
            if (d != null && d in arabicDigits.indices) arabicDigits[d] else digit
        }.joinToString("")
    }

    companion object {
        private val WHITE = 0xFFFFFFFF.toInt()
        private val TEAL = 0xFF4DB6AC.toInt()
        private val TEAL_BRIGHT = 0xFF80CBC4.toInt()
        private val BLUE_DARK = 0xFF0B1A24.toInt()
    }
}
