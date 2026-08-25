package com.example.domain.service.studio.template

import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.TextLayer
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.model.studio.TextAnimation

/**
 * مساعدات مشتركة بين القوالب المتميّزة لتفادي تكرار منطق استخراج النصوص
 * وبناء الطبقات الشائعة (مثل طبقة العلامة). كل دوال هذه الكائن خالية من
 * أي اختلاق لنص ديني — النصوص تأتي حصرًا من [VideoProject].
 */
internal object TemplateCommon {

    /**
     * يجمع النصوص الظاهرة من المشروع بترتيب: مشاهد الخطة أولًا ثم النصوص المقترحة،
     * دون تكرار. لا يضيف أي نص من عنده.
     */
    fun collectTexts(project: VideoProject): List<String> {
        val fromScenes = project.plan.scenes.mapNotNull { it.onScreenText.trim().takeIf { s -> s.isNotBlank() } }
        val fromPlan = project.plan.suggestedTexts.mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
        return (fromScenes + fromPlan).distinct()
    }

    /**
     * يرجع النص الاحتياطي الأخير من نص الفكرة (ideaText) — ليس نصًا دينيًا مضمّنًا،
     * بل نص المستخدم نفسه أو placeholder واضح إن لم يوجد أي نص إطلاقًا.
     */
    fun fallbackIdeaLine(project: VideoProject, placeholder: String): String {
        val ideaLine = project.idea.ideaText.trim().takeIf { it.isNotBlank() }
        return ideaLine ?: placeholder
    }

    /** أبعاد البكسل القياسية لكل اتجاه. */
    fun resolveDimensions(orientation: VideoOrientation): Pair<Int, Int> = when (orientation) {
        VideoOrientation.PORTRAIT -> 720 to 1280
        VideoOrientation.LANDSCAPE -> 1280 to 720
        VideoOrientation.SQUARE -> 1080 to 1080
    }

    /** طبقة علامة ثابتة في الزاوية العلوية، بلا تحريك ولا توهج. */
    fun brandLayer(
        brand: String,
        colorArgb: Int,
        fontSizeSp: Int = 22
    ): TextLayer = TextLayer(
        text = brand,
        fontSizeSp = fontSizeSp,
        textColorArgb = colorArgb,
        glow = false,
        alignment = LayerHorizontalAlignment.START,
        verticalAnchor = LayerVerticalAlignment.TOP,
        marginPercent = 0.06f,
        animation = TextAnimation.NONE
    )

    /** يحوّل رقمًا إلى علامة شكلية بصرية بسيطة (عنصر واجهة وليس نصًا دينيًا). */
    fun visualMarker(index: Int): String = when (index % 4) {
        0 -> "◆"
        1 -> "●"
        2 -> "■"
        else -> "▲"
    }
}
