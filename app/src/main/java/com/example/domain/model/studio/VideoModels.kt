package com.example.domain.model.studio

import com.squareup.moshi.JsonClass
import java.util.UUID

enum class GenerationStage(val titleAr: String, val titleEn: String, val descriptionAr: String = "") {
    IDLE("جاهز للبدء", "Idle", "المشروع في وضع الاستعداد لبدء المعالجة"),
    ANALYZING("تحليل الفكرة والمعنى", "Analyzing", "فحص الأهداف والدلالات الروحانية والأسلوب"),
    PLANNING("تجهيز المشاهد واللوحة", "Planning", "بناء التسلسل البصري والزمني وتوزيع النصوص"),
    GENERATING("توليد المشاهد الذكية", "Generating", "معالجة الوسائط والأصول البصرية محليًا"),
    RENDERING("معالجة ورندرة الفيديو", "Rendering", "تطبيق الانتقالات والمؤثرات ومزامنة الصوت"),
    COMPLETED("اكتمل التوليد بنجاح", "Completed", "المشروع جاهز للمعاينة والمراجعة الكاملة"),
    FAILED("تعذر التوليد", "Failed", "حدث خطأ أثناء المحاكاة، يمكنك إعادة المحاولة"),
    CANCELLED("تم إلغاء التوليد", "Cancelled", "تم إلغاء عملية المعالجة بناءً على طلبك")
}


enum class VideoStatus {
    DRAFT,
    ANALYZING,
    PLANNING,
    GENERATING,
    COMPLETED,
    FAILED,
    CANCELLED
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
    MOVING_QUOTES("اقتباسات مؤثرة", "Moving Quotes"),
    QURAN_RECITATION("تلاوة قرآنية", "Quran Recitation")
}

enum class AssetType(val titleAr: String, val titleEn: String) {
    IMAGE("صورة", "Image"),
    VIDEO_CLIP("مقطع فيديو", "Video Clip"),
    AUDIO_TRACK("مسار صوتي", "Audio Track"),
    SOUND_EFFECT("مؤثر صوتي", "Sound Effect"),
    GRAPHIC("عنصر رسومي", "Graphic Element"),
    CALLIGRAPHY("مخطوطة عربية", "Calligraphy")
}

enum class AssetLicense(val titleAr: String, val titleEn: String, val isPermitted: Boolean) {
    PUBLIC_DOMAIN("ملكية عامة (Public Domain)", "Public Domain", true),
    CREATIVE_COMMONS_CC0("مشاع إبداعي (CC0)", "Creative Commons Zero", true),
    CREATIVE_COMMONS_BY("مشاع إبداعي مع نسبة العمل (CC BY)", "Creative Commons By", true),
    USER_OWN_WORK("عمل خاص بالمستخدم (تصوير/تصميم شخصي)", "User Own Work", true),
    LICENSED_STOCK("مورد مرخص نظاميًا", "Licensed Stock", true),
    UNKNOWN_UNLICENSED("مجهول / غير مرخص (مرفوض)", "Unknown / Unlicensed", false)
}

@JsonClass(generateAdapter = true)
data class AssetAttribution(
    val author: String = "",
    val sourceTitle: String = "",
    val sourceUrlOrPath: String = "",
    val licenseNotice: String = "",
    val requiresAttribution: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LicensedAsset(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val uriOrPath: String = "",
    val assetType: AssetType = AssetType.IMAGE,
    val fileSizeBytes: Long = 0L,
    val source: String = "",
    val license: AssetLicense = AssetLicense.USER_OWN_WORK,
    val attribution: AssetAttribution = AssetAttribution(),
    val isUserProvided: Boolean = true,
    val isConsentGiven: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedSize: String
        get() {
            if (fileSizeBytes <= 0) return "حجم محلي غير محدد"
            val kb = fileSizeBytes / 1024.0
            return if (kb < 1024) {
                String.format(java.util.Locale.US, "%.1f KB", kb)
            } else {
                val mb = kb / 1024.0
                String.format(java.util.Locale.US, "%.1f MB", mb)
            }
        }

    fun isValid(): Boolean {
        val hasSource = source.isNotBlank() || isUserProvided
        val hasValidLicense = license.isPermitted && license != AssetLicense.UNKNOWN_UNLICENSED
        return hasSource && hasValidLicense && isConsentGiven
    }
}

data class ResourceSearchQuery(
    val queryText: String = "",
    val assetType: AssetType? = null,
    val orientation: VideoOrientation = VideoOrientation.PORTRAIT,
    val requiredLicense: AssetLicense? = null
)

@JsonClass(generateAdapter = true)
data class FallbackResourceMode(
    val isEnabled: Boolean = false,
    val allowUserLocalAssets: Boolean = true,
    val userConfirmedConsent: Boolean = false,
    val description: String = "المسار الاحتياطي لاستخدام موارد محلية مرفوعة من هاتف المستخدم بترخيص واضح وموافق عليه."
)

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
    val stage: GenerationStage = GenerationStage.IDLE,
    val progressPercent: Int = 0,
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val elapsedTimeSeconds: Int = 0,
    val isMock: Boolean = true
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
    val hasMusicOrEffects: Boolean = true,
    val verseKey: String = ""
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
    val instructions: String = "",
    val attachedAssetId: String? = null,
    val attachedAssetTitle: String? = null,
    val attachedAssetType: AssetType? = null
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
    val scenes: List<VideoScene> = emptyList(),
    val appliedStyleSignature: StyleSignature? = null
)

@JsonClass(generateAdapter = true)
data class VideoProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "مشروع فيديو جديد",
    val status: VideoStatus = VideoStatus.DRAFT,
    val renderStatus: VideoRenderStatus = VideoRenderStatus.IDLE,
    val generationStage: GenerationStage = GenerationStage.IDLE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val idea: VideoIdea = VideoIdea(),
    val plan: VideoPlan = VideoPlan(),
    val style: VideoStyle = VideoStyle(),
    val assets: List<VideoAsset> = emptyList(),
    val licensedAssets: List<LicensedAsset> = emptyList(),
    val fallbackMode: FallbackResourceMode = FallbackResourceMode(),
    val currentJob: VideoGenerationJob? = null,
    val errorMessage: String? = null
)


