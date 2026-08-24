package com.example.domain.model.studio

import com.squareup.moshi.JsonClass
import java.util.UUID

enum class VideoStatus {
    DRAFT,
    ANALYZING,
    PLANNING,
    GENERATING,
    COMPLETED,
    FAILED
}

enum class VideoRenderStatus {
    IDLE,
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class VideoLanguage(val code: String, val titleAr: String, val titleEn: String) {
    ARABIC("ar", "العربية", "Arabic"),
    ENGLISH("en", "الإنجليزية", "English")
}

enum class VideoOrientation(val ratioLabel: String, val aspectRatioText: String) {
    PORTRAIT("عمودي (9:16)", "9:16"),
    LANDSCAPE("أفقي (16:9)", "16:9"),
    SQUARE("مربع (1:1)", "1:1")
}

enum class VideoDuration(val seconds: Int, val labelAr: String, val labelEn: String) {
    SHORT(15, "١٥ ثانية (قصير)", "15s (Short)"),
    MEDIUM(30, "٣٠ ثانية (متوسط)", "30s (Medium)"),
    LONG(60, "٦٠ ثانية (شامل)", "60s (Comprehensive)")
}

enum class VideoTone(val titleAr: String, val titleEn: String) {
    CALM("هادئ", "Calm"),
    INSPIRING("ملهم", "Inspiring"),
    EDUCATIONAL("تعليمي", "Educational"),
    STORYTELLING("قصصي", "Storytelling"),
    FAST("سريع", "Fast"),
    CINEMATIC("سينمائي", "Cinematic")
}

enum class EditingStyle(val titleAr: String, val titleEn: String) {
    CINEMATIC("سينمائي", "Cinematic"),
    DOCUMENTARY("وثائقي", "Documentary"),
    EDUCATIONAL("تعليمي", "Educational"),
    STORYTELLING("قصصي", "Storytelling"),
    MEDITATIVE("تأملي", "Meditative"),
    FAST_REELS("ريلز سريع", "Fast Reels"),
    SHORT_AD("إعلان قصير", "Short Ad"),
    ANIMATION("رسوم متحركة", "Animation"),
    MOVING_QUOTES("اقتباسات مؤثرة", "Moving Quotes")
}

enum class AssetType {
    IMAGE,
    VIDEO_CLIP,
    AUDIO_TRACK,
    SOUND_EFFECT,
    GRAPHIC,
    CALLIGRAPHY
}

@JsonClass(generateAdapter = true)
data class VideoAsset(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: AssetType = AssetType.IMAGE,
    val description: String = "",
    val isLocal: Boolean = true,
    val sourceUrlOrPath: String? = null
)

@JsonClass(generateAdapter = true)
data class VideoStyle(
    val tone: VideoTone = VideoTone.INSPIRING,
    val editingStyle: EditingStyle = EditingStyle.CINEMATIC,
    val visualMood: String = "إسلامي روحاني حديث",
    val colorPalette: String = "الذهبي والأزرق الليلي",
    val typographyStyle: String = "خط عربي أصيل وحديث"
)

@JsonClass(generateAdapter = true)
data class VideoGenerationJob(
    val jobId: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val status: VideoRenderStatus = VideoRenderStatus.IDLE,
    val progressPercent: Int = 0,
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@JsonClass(generateAdapter = true)
data class VideoIdea(
    val ideaText: String = "",
    val language: VideoLanguage = VideoLanguage.ARABIC,
    val orientation: VideoOrientation = VideoOrientation.PORTRAIT,
    val duration: VideoDuration = VideoDuration.SHORT,
    val audience: String = "",
    val tone: VideoTone = VideoTone.INSPIRING,
    val editingStyle: EditingStyle = EditingStyle.CINEMATIC,
    val hasVoiceover: Boolean = true,
    val hasOnScreenText: Boolean = true,
    val hasMusicOrEffects: Boolean = true
)

@JsonClass(generateAdapter = true)
data class VideoScene(
    val id: String = UUID.randomUUID().toString(),
    val durationSeconds: Int = 5,
    val visualDescription: String = "",
    val onScreenText: String = "",
    val voiceoverText: String = "",
    val transition: String = "تلاشي ناعم",
    val requiredAsset: String = "",
    val instructions: String = ""
)

@JsonClass(generateAdapter = true)
data class VideoPlan(
    val summary: String = "",
    val goal: String = "",
    val targetAudience: String = "",
    val suggestedEditingStyle: String = "",
    val durationSeconds: Int = 15,
    val orientation: VideoOrientation = VideoOrientation.PORTRAIT,
    val sceneCount: Int = 0,
    val requiredResources: List<String> = emptyList(),
    val suggestedTexts: List<String> = emptyList(),
    val missingQuestions: List<String> = emptyList(),
    val scenes: List<VideoScene> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VideoProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "مشروع فيديو جديد",
    val status: VideoStatus = VideoStatus.DRAFT,
    val renderStatus: VideoRenderStatus = VideoRenderStatus.IDLE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val idea: VideoIdea = VideoIdea(),
    val plan: VideoPlan = VideoPlan(),
    val style: VideoStyle = VideoStyle(),
    val assets: List<VideoAsset> = emptyList(),
    val currentJob: VideoGenerationJob? = null,
    val errorMessage: String? = null
)
