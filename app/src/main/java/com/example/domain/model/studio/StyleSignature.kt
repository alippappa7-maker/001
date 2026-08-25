package com.example.domain.model.studio

import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * The general visual medium a reference video is built from.
 * This drives what kind of assets the app should later instruct the user to source or create.
 */
enum class VisualMedium(val titleAr: String, val titleEn: String) {
    ILLUSTRATED_CHARACTERS("رسوم كرتونية / شخصيات مرسومة", "Illustrated / Cartoon Characters"),
    REAL_CINEMATIC_FOOTAGE("تصوير حقيقي سينمائي", "Real Cinematic Footage"),
    INFOGRAPHIC_MOTION("رسوم بيانية متحركة (إنفوجرافيك)", "Animated Infographic"),
    TEXT_ONLY_TYPOGRAPHY("نص وحروفيات فقط بدون عناصر بصرية", "Typography-only"),
    MIXED("مزيج من عدة أساليب بصرية", "Mixed Media")
}

/**
 * Narrative/structural pattern the reference video follows.
 */
enum class NarrativeStructure(val titleAr: String, val titleEn: String) {
    NUMBERED_LIST("قائمة نصائح مرقّمة", "Numbered List / Tips"),
    POETIC_REFLECTION("تأمل شعري متسلسل بجمل قصيرة", "Poetic Reflection"),
    DATA_PROGRESSION("عرض تراكمي لبيانات أو تقدّم", "Data Progression"),
    SINGLE_QUOTE_FOCUS("تركيز على اقتباس أو حديث واحد", "Single Quote Focus"),
    STORY_ARC("قصة بمقدمة وحبكة وخاتمة", "Story Arc")
}

/**
 * A structured "recipe" describing the visual/narrative identity extracted from a reference
 * video, so the app can guide the user to reproduce the same feeling with their own content
 * instead of literally generating/copying the reference video itself.
 */
@JsonClass(generateAdapter = true)
data class StyleSignature(
    val id: String = UUID.randomUUID().toString(),
    val sourceLabel: String = "",
    val visualMedium: VisualMedium = VisualMedium.MIXED,
    val narrativeStructure: NarrativeStructure = NarrativeStructure.STORY_ARC,
    val colorPalette: String = "",
    val backgroundTreatment: String = "",
    val textTreatment: String = "",
    val transitionStyle: String = "",
    val signatureMotif: String = "",
    val paceDescription: String = "",
    val summary: String = "",
    val recommendedTone: VideoTone = VideoTone.INSPIRING,
    val recommendedEditingStyle: EditingStyle = EditingStyle.CINEMATIC,
    val createdAt: Long = System.currentTimeMillis(),
    val isMock: Boolean = false
)
