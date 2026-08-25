package com.example.domain.service.studio.template

import com.example.domain.model.studio.BackgroundLayer
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.model.studio.MovingQuotesTemplateInput
import com.example.domain.model.studio.TextAnimation
import com.example.domain.model.studio.TextLayer
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoProject

/**
 * قالب \"اقتباسات متحركة\" — عام وسريع الإنتاج: خلفية داكنة موحّدة، اقتباس/موعظة
 * قصيرة واحدة في كل مشهد، تظهر بتوهج نابض (GLOW_PULSE) طوال عرضها لتمنحها
 * حيوية بصرية دون تعقيد، مع انتقال تلاشٍ ناعم (transitionMs) بين مشهد وآخر.
 *
 * مناسب لأي نص قصير يُدخله المستخدم — لا حديث أو آية مضمّنة في الكود؛
 * كل اقتباس من [MovingQuotesTemplateInput.quotes] فقط. عند عدم وجود اقتباسات
 * يُعرض placeholder واحد واضح يطلب من المستخدم الإدخال.
 */
class MovingQuotesTemplate : CompositionTemplate {

    override fun build(project: VideoProject): CompositionStoryboard {
        val input = fromProject(project)
        return build(input, project.idea.orientation)
    }

    /**
     * يبني لوحة القصة مباشرة من [MovingQuotesTemplateInput]. مُتاح للاختبار
     * وللإنشاء المباشر دون المرور بـ [VideoProject].
     */
    fun build(input: MovingQuotesTemplateInput, orientation: VideoOrientation): CompositionStoryboard {
        val (width, height) = resolveDimensions(orientation)
        val scenes = buildScenes(input)
        return CompositionStoryboard(width = width, height = height, fps = 30, scenes = scenes)
    }

    /**
     * يستخرج مدخلات القالب من [VideoProject] دون اختلاق أي نص: يأخذ الاقتباسات
     * من مشاهد الخطة أو النصوص المقترحة فقط. نص الفكرة (ideaText) يُستخدم فقط
     * كاحتياط أخير عند غياب أي نص آخر.
     */
    fun fromProject(project: VideoProject): MovingQuotesTemplateInput {
        val texts = collectTexts(project)
        val ideaLine = project.idea.ideaText.trim().takeIf { it.isNotBlank() }
        val quotes = texts.ifEmpty { listOfNotNull(ideaLine) }
        return MovingQuotesTemplateInput(quotes = quotes, brandName = "قبس")
    }

    private fun collectTexts(project: VideoProject): List<String> {
        val fromScenes = project.plan.scenes.mapNotNull { it.onScreenText.trim().takeIf { s -> s.isNotBlank() } }
        val fromPlan = project.plan.suggestedTexts.mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
        return (fromScenes + fromPlan).distinct()
    }

    private fun buildScenes(input: MovingQuotesTemplateInput): List<CompositionScene> {
        val quotes = input.quotes.ifEmpty { listOf("أدخل اقتباسك هنا") }
        return quotes.mapIndexed { index, quote -> quoteScene(quote, index, input) }
    }

    private fun quoteScene(quote: String, index: Int, input: MovingQuotesTemplateInput): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = quote,
            fontSizeSp = 44,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = WHITE,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.GLOW_PULSE,
            animationStartMs = 250,
            maxLines = 4
        )
        input.brandName.takeIf { it.isNotBlank() }?.let { layers += brandLayer(it) }

        return CompositionScene(
            id = "moving_quote_$index",
            durationMs = 5500,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = BLACK),
            textLayers = layers,
            transitionMs = 450
        )
    }

    private fun brandLayer(brand: String): TextLayer = TextLayer(
        text = brand,
        fontSizeSp = 20,
        textColorArgb = GRAY,
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
        private val GRAY = 0xFFB8B8B8.toInt()
        private val BLACK = 0xFF060606.toInt()
    }
}
