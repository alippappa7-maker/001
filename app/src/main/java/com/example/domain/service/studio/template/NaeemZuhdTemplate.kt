package com.example.domain.service.studio.template

import com.example.domain.model.studio.BackgroundLayer
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.model.studio.NaeemZuhdTemplateInput
import com.example.domain.model.studio.TextAnimation
import com.example.domain.model.studio.TextLayer
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoProject

/**
 * قالب \"النعيم والزهد\" — يحاكي أسلوب الفيديوهات التأملية الهادئة:
 *
 * 1) مشهد افتتاحي: خلفية غروب ذهبية (إجرائية عبر Canvas) + نص الحديث/الأثر
 *    الرئيسي يظهر بتلاشٍ هادئ (FADE_IN)، مع مصدره أسفله إن وُجد.
 * 2) مشاهد تأملية متتابعة (اختيارية): جمل قصيرة إضافية فوق تدرّجات كهرمانية
 *    مختلفة، كل واحدة تتلاشى بدورها — قليلة الحركة، عمدًا، لتبقى هادئة.
 * 3) مشهد خاتمة: شفق داكن + اسم العلامة.
 *
 * التزامًا بمبدأ عدم اختراع النصوص الدينية: الحديث/الأثر والجمل التأملية كلها
 * من إدخال المستخدم. إن تُرك حقل الحديث فارغًا، يظهر placeholder واضح يطلب
 * الإدخال — لا يُولَّد حديث أو أثر من الكود.
 */
class NaeemZuhdTemplate(
    private val sunsetProvider: SunsetBackgroundProvider = NoOpSunsetBackgroundProvider
) : CompositionTemplate {

    override fun build(project: VideoProject): CompositionStoryboard {
        val input = fromProject(project)
        return build(input, project.idea.orientation)
    }

    /**
     * يبني لوحة القصة مباشرة من [NaeemZuhdTemplateInput]. مُتاح للاختبار وللإنشاء
     * المباشر دون المرور بـ [VideoProject].
     */
    fun build(input: NaeemZuhdTemplateInput, orientation: VideoOrientation): CompositionStoryboard {
        val (width, height) = resolveDimensions(orientation)
        val scenes = buildScenes(input, width, height)
        return CompositionStoryboard(width = width, height = height, fps = 30, scenes = scenes)
    }

    /**
     * يستخرج مدخلات القالب من [VideoProject] دون اختلاق أي نص: يأخذ النصوص من
     * مشاهد الخطة أو النصوص المقترحة فقط. نص الفكرة (ideaText) يُستخدم فقط
     * كاحتياط أخير عند غياب أي نص آخر — وليس كحديث/أثر مضمّن في الكود.
     */
    fun fromProject(project: VideoProject): NaeemZuhdTemplateInput {
        val texts = collectTexts(project)
        val ideaLine = project.idea.ideaText.trim().takeIf { it.isNotBlank() }
        val hadith = texts.firstOrNull() ?: ideaLine ?: ""
        val reflective = texts.drop(1).take(2)
        return NaeemZuhdTemplateInput(
            hadithText = hadith,
            hadithSource = "",
            reflectiveLines = reflective,
            brandName = "قبس"
        )
    }

    private fun collectTexts(project: VideoProject): List<String> {
        val fromScenes = project.plan.scenes.mapNotNull { it.onScreenText.trim().takeIf { s -> s.isNotBlank() } }
        val fromPlan = project.plan.suggestedTexts.mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
        return (fromScenes + fromPlan).distinct()
    }

    private fun buildScenes(input: NaeemZuhdTemplateInput, width: Int, height: Int): List<CompositionScene> {
        val scenes = mutableListOf<CompositionScene>()
        scenes += openingScene(input, width, height)
        input.reflectiveLines.forEachIndexed { i, line -> scenes += reflectiveScene(line, i, input, width, height) }
        scenes += closingScene(input, width, height)
        return scenes
    }

    private fun openingScene(input: NaeemZuhdTemplateInput, width: Int, height: Int): CompositionScene {
        val hadith = input.hadithText.ifBlank { "أدخل نص الحديث أو الأثر هنا" }
        val bg = sunsetOrFallback(width, height, SunsetVariant.GOLDEN, FALLBACK_GOLDEN)

        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = hadith,
            fontSizeSp = 40,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = GOLD,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = -0.06f,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 500,
            animationDurationMs = 1400,
            maxLines = 5
        )
        if (input.hadithSource.isNotBlank()) {
            layers += TextLayer(
                text = input.hadithSource,
                fontSizeSp = 22,
                textColorArgb = GOLD,
                glow = false,
                alignment = LayerHorizontalAlignment.CENTER,
                verticalAnchor = LayerVerticalAlignment.CENTER,
                yOffsetPercent = 0.18f,
                animation = TextAnimation.FADE_IN,
                animationStartMs = 1200,
                maxLines = 1
            )
        }
        input.brandName.takeIf { it.isNotBlank() }?.let { layers += brandLayer(it) }

        return CompositionScene(
            id = "naeem_opening",
            durationMs = 8000,
            background = bg,
            textLayers = layers,
            transitionMs = 600
        )
    }

    private fun reflectiveScene(
        line: String,
        index: Int,
        input: NaeemZuhdTemplateInput,
        width: Int,
        height: Int
    ): CompositionScene {
        val variant = if (index % 2 == 0) SunsetVariant.AMBER else SunsetVariant.DUSK
        val fallback = if (index % 2 == 0) FALLBACK_AMBER else FALLBACK_DUSK
        val bg = sunsetOrFallback(width, height, variant, fallback)

        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = line,
            fontSizeSp = 40,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = WHITE,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 400,
            animationDurationMs = 1200,
            maxLines = 4
        )
        input.brandName.takeIf { it.isNotBlank() }?.let { layers += brandLayer(it) }

        return CompositionScene(
            id = "naeem_reflection_$index",
            durationMs = 6000,
            background = bg,
            textLayers = layers,
            transitionMs = 500
        )
    }

    private fun closingScene(input: NaeemZuhdTemplateInput, width: Int, height: Int): CompositionScene {
        val bg = sunsetOrFallback(width, height, SunsetVariant.DUSK, FALLBACK_DUSK)
        val layers = mutableListOf<TextLayer>()
        input.brandName.takeIf { it.isNotBlank() }?.let {
            layers += TextLayer(
                text = it,
                fontSizeSp = 30,
                textColorArgb = GOLD,
                glow = true,
                glowColorArgb = GOLD,
                alignment = LayerHorizontalAlignment.CENTER,
                verticalAnchor = LayerVerticalAlignment.CENTER,
                animation = TextAnimation.FADE_IN,
                animationStartMs = 300,
                maxLines = 1
            )
        }

        return CompositionScene(
            id = "naeem_closing",
            durationMs = 4000,
            background = bg,
            textLayers = layers,
            transitionMs = 600
        )
    }

    /** يحاول توليد خلفية غروب إجرائية؛ عند غياب المزوّد (الاختبارات) يعود إلى لون ثابت. */
    private fun sunsetOrFallback(
        width: Int,
        height: Int,
        variant: SunsetVariant,
        fallbackColor: Int
    ): BackgroundLayer {
        val bitmap = sunsetProvider.renderSunset(width, height, variant)
        return if (bitmap != null) {
            BackgroundLayer(type = BackgroundType.IMAGE, staticImage = bitmap)
        } else {
            BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = fallbackColor)
        }
    }

    private fun brandLayer(brand: String): TextLayer = TextLayer(
        text = brand,
        fontSizeSp = 22,
        textColorArgb = GOLD,
        glow = false,
        alignment = LayerHorizontalAlignment.START,
        verticalAnchor = LayerVerticalAlignment.TOP,
        marginPercent = 0.06f,
        animation = TextAnimation.NONE
    )

    private fun resolveDimensions(orientation: VideoOrientation): Pair<Int, Int> = when (orientation) {
        VideoOrientation.PORTRAIT -> 720 to 1280
        VideoOrientation.LANDSCAPE -> 1280 to 720
        VideoOrientation.SQUARE -> 1080 to 1080
    }

    companion object {
        private val WHITE = 0xFFFFFFFF.toInt()
        private val GOLD = 0xFFE8C97A.toInt()
        private val FALLBACK_GOLDEN = 0xFF3A2410.toInt()
        private val FALLBACK_AMBER = 0xFF2E1A0C.toInt()
        private val FALLBACK_DUSK = 0xFF120A06.toInt()
    }
}
