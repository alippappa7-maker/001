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

enum class VideoLanguage {
    ARABIC, ENGLISH
}

enum class VideoOrientation {
    PORTRAIT, // 9:16
    LANDSCAPE, // 16:9
    SQUARE // 1:1
}

enum class VideoDuration(val seconds: Int) {
    SHORT(15),
    MEDIUM(30),
    LONG(60)
}

enum class VideoTone {
    CALM, INSPIRING, EDUCATIONAL, STORYTELLING, FAST, CINEMATIC
}

enum class EditingStyle {
    CINEMATIC, DOCUMENTARY, EDUCATIONAL, STORYTELLING, MEDITATIVE, FAST_REELS, SHORT_AD, ANIMATION, MOVING_QUOTES
}

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
data class VideoPlan(
    val summary: String = "",
    val goal: String = "",
    val targetAudience: String = "",
    val suggestedEditingStyle: String = "",
    val requiredResources: List<String> = emptyList(),
    val suggestedTexts: List<String> = emptyList(),
    val missingQuestions: List<String> = emptyList(),
    val scenes: List<VideoScene> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VideoScene(
    val id: String = UUID.randomUUID().toString(),
    val durationSeconds: Int = 5,
    val visualDescription: String = "",
    val onScreenText: String = "",
    val voiceoverText: String = "",
    val transition: String = "",
    val requiredAsset: String = "",
    val instructions: String = ""
)

@JsonClass(generateAdapter = true)
data class VideoProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "مشروع جديد",
    val status: VideoStatus = VideoStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val idea: VideoIdea = VideoIdea(),
    val plan: VideoPlan = VideoPlan(),
    val errorMessage: String? = null
)
