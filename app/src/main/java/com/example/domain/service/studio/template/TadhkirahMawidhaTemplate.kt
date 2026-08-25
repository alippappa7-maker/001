package com.example.domain.service.studio.template

import com.example.domain.model.studio.BackgroundLayer
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.ImageOverlayLayer
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.model.studio.TextAnimation
import com.example.domain.model.studio.TextLayer
import com.example.domain.model.studio.TadhkirahTemplateInput
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoProject

/**
 * قالب "تذكرة وموعظة" — يحاكي الأسلوب البصري لفيديوهات التذكير الدعوية:
 *
 * 1) مشهد افتتاحي: خلفية خضراء داكنة + إطار زخرفي حول آية (نص يُدخله المستخدم).
 * 2) مشاهد تأملية متتابعة: نص أبيض متوهج فوق خلفيات داكنة متدرجة (أحمر ← زيتي).
 * 3) مشهد كلمة مؤثرة (اختياري): كلمة واحدة كبيرة تصعد من الأسفل.
 * 4) مشاهد خاتمة: نص أبيض بسيط فوق خلفية ليلية داكنة + شعار العلامة أعلى اليسار.
 *
 * التزامًا بمبدأ عدم اختراع النصوص الدينية: الآية والاقتباسات كلها من إدخال المستخدم.
 * إن تُرك حقل الآية فارغًا، يظهر placeholder واضح يطلب الإدخال — لا تُولَّد آية من الكود.
 */
class TadhkirahMawidhaTemplate(
    private val frameProvider: OrnamentalFrameProvider = NoOpOrnamentalFrameProvider
) : CompositionTemplate {

    override fun build(project: VideoProject): CompositionStoryboard {
        val input = fromProject(project)
        return build(input, project.idea.orientation)
    }

    /**
     * يبني لوحة القصة مباشرة من [TadhkirahTemplateInput]. مُتاح للاختبار وللإنشاء
     * المباشر دون المرور بـ [VideoProject].
     */
    fun build(input: TadhkirahTemplateInput, orientation: VideoOrientation): CompositionStoryboard {
        val (width, height) = resolveDimensions(orientation)
        val scenes = buildScenes(input, width, height)
        return CompositionStoryboard(width = width, height = height, fps = 30, scenes = scenes)
    }

    /**
     * يستخرج مدخلات القالب من [VideoProject] الحالي دون اختلاق أي نص:
     * يأخذ النصوص من مشاهد الخطة أو النصوص المقترحة فقط. ولا يستخدم نص الفكرة
     * (ideaText) كآية افتتاحية — يُستخدم فقط كعبارة تأملية احتياطية عند غياب النصوص.
     */
    fun fromProject(project: VideoProject): TadhkirahTemplateInput {
        val texts = collectTexts(project)
        val opening = texts.firstOrNull() ?: ""
        val ideaLine = project.idea.ideaText.trim().takeIf { it.isNotBlank() }
        val reflective = texts.drop(1).take(2).ifEmpty { listOfNotNull(ideaLine) }
        val closing = texts.drop(3).take(2)
        return TadhkirahTemplateInput(
            openingVerseText = opening,
            reflectiveLines = reflective,
            impactWord = "",
            closingLines = closing,
            brandName = "قبس"
        )
    }

    private fun collectTexts(project: VideoProject): List<String> {
        val fromScenes = project.plan.scenes.mapNotNull { it.onScreenText.trim().takeIf { s -> s.isNotBlank() } }
        val fromPlan = project.plan.suggestedTexts.mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
        return (fromScenes + fromPlan).distinct()
    }

    private fun buildScenes(
        input: TadhkirahTemplateInput,
        width: Int,
        height: Int
    ): List<CompositionScene> {
        val scenes = mutableListOf<CompositionScene>()
        scenes += openingScene(input, width, height)

        val reflective = input.reflectiveLines.ifEmpty { listOf("أدخل فكرة التذكير") }
        reflective.forEachIndexed { i, line -> scenes += reflectiveScene(line, i, input) }

        if (input.impactWord.isNotBlank()) {
            scenes += impactScene(input)
        }

        val closing = input.closingLines.ifEmpty { listOf("أدخل جملة الخاتمة") }
        closing.forEach { line -> scenes += closingScene(line, input) }

        return scenes
    }

    private fun openingScene(input: TadhkirahTemplateInput, width: Int, height: Int): CompositionScene {
        val verse = input.openingVerseText.ifBlank { "أدخل الآية الكريمة هنا" }
        val frameBitmap = frameProvider.renderVerseFrame(width, height)
        val overlays = frameBitmap?.let {
            listOf(ImageOverlayLayer(bitmap = it, widthPercent = 0.92f, alpha = 0.9f))
        } ?: emptyList()

        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = verse,
            fontSizeSp = 38,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = GOLD,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 400,
            maxLines = 4
        )
        input.brandName.takeIf { it.isNotBlank() }?.let { layers += brandLayer(it) }

        return CompositionScene(
            id = "tadhkirah_opening",
            durationMs = 7000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = GREEN_DARK),
            textLayers = layers,
            overlayLayers = overlays,
            transitionMs = 500
        )
    }

    private fun reflectiveScene(line: String, index: Int, input: TadhkirahTemplateInput): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = line,
            fontSizeSp = 46,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = WHITE,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = if (index == 0) TextAnimation.GLOW_PULSE else TextAnimation.FADE_IN,
            animationStartMs = 200,
            maxLines = 3
        )
        input.brandName.takeIf { it.isNotBlank() }?.let { layers += brandLayer(it) }

        return CompositionScene(
            id = "tadhkirah_reflection_$index",
            durationMs = 6000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = reflectiveColor(index)),
            textLayers = layers,
            transitionMs = 450
        )
    }

    private fun impactScene(input: TadhkirahTemplateInput): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = input.impactWord,
            fontSizeSp = 72,
            textColorArgb = WHITE,
            glow = true,
            glowColorArgb = GOLD,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            animation = TextAnimation.SLIDE_UP,
            animationStartMs = 150,
            maxLines = 1
        )
        input.brandName.takeIf { it.isNotBlank() }?.let { layers += brandLayer(it) }

        return CompositionScene(
            id = "tadhkirah_impact",
            durationMs = 5000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = OLIVE_DARK),
            textLayers = layers,
            transitionMs = 450
        )
    }

    private fun closingScene(line: String, input: TadhkirahTemplateInput): CompositionScene {
        val layers = mutableListOf<TextLayer>()
        layers += TextLayer(
            text = line,
            fontSizeSp = 42,
            textColorArgb = WHITE,
            glow = false,
            alignment = LayerHorizontalAlignment.CENTER,
            verticalAnchor = LayerVerticalAlignment.CENTER,
            yOffsetPercent = -0.05f,
            animation = TextAnimation.FADE_IN,
            animationStartMs = 200,
            maxLines = 3
        )
        input.brandName.takeIf { it.isNotBlank() }?.let { layers += brandLayer(it) }

        return CompositionScene(
            id = "tadhkirah_closing",
            durationMs = 8000,
            background = BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = NIGHT),
            textLayers = layers,
            transitionMs = 500
        )
    }

    private fun brandLayer(brand: String): TextLayer = TextLayer(
        text = brand,
        fontSizeSp = 22,
        textColorArgb = GOLD,
        glow = false,
        alignment = LayerHorizontalAlignment.START,
        verticalAnchor = LayerVerticalAlignment.TOP,
        yOffsetPercent = 0f,
        marginPercent = 0.06f,
        animation = TextAnimation.NONE
    )

    private fun reflectiveColor(index: Int): Int = when (index % 3) {
        0 -> RED_DARK
        1 -> TEAL_DARK
        else -> OLIVE_DARK
    }

    private fun resolveDimensions(orientation: VideoOrientation): Pair<Int, Int> = when (orientation) {
        VideoOrientation.PORTRAIT -> 720 to 1280
        VideoOrientation.LANDSCAPE -> 1280 to 720
        VideoOrientation.SQUARE -> 1080 to 1080
    }

    companion object {
        private val WHITE = 0xFFFFFFFF.toInt()
        private val GOLD = 0xFFE8D9A0.toInt()
        private val GREEN_DARK = 0xFF073021.toInt()
        private val RED_DARK = 0xFF3A0A14.toInt()
        private val TEAL_DARK = 0xFF103A33.toInt()
        private val OLIVE_DARK = 0xFF2A2A0A.toInt()
        private val NIGHT = 0xFF060A0F.toInt()
    }
}
