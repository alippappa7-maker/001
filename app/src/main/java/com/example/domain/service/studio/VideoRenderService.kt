package com.example.domain.service.studio

import com.example.domain.model.studio.FallbackResourceMode
import com.example.domain.model.studio.VideoProject

data class VideoRenderResult(
    val isSuccess: Boolean,
    val totalScenes: Int,
    val totalDurationSeconds: Int,
    val attachedResourcesCount: Int,
    val message: String,
    val renderedAt: Long = System.currentTimeMillis()
)

data class VideoExportResult(
    val isAvailable: Boolean,
    val exportPath: String? = null,
    val message: String,
    val notice: String = "التصدير الفعلي كملف MP4 قيد التطوير في إصدار قادم"
)

/**
 * Service interface responsible for compiling storyboard data and rendering/exporting video.
 */
interface VideoRenderService {
    /**
     * Render and validate the internal storyboard configuration with optional fallback resources.
     */
    suspend fun renderStoryboard(project: VideoProject, fallbackMode: FallbackResourceMode): VideoRenderResult

    /**
     * Attempt video export. If real export is not linked yet, clearly communicates this status.
     */
    suspend fun exportVideo(project: VideoProject): VideoExportResult

    /**
     * Query whether real hardware/server MP4 rendering is currently active.
     */
    fun isRealExportAvailable(): Boolean
}

/**
 * Local implementation of VideoRenderService.
 * Validates storyboard parameters locally and explicitly marks real video export as a planned future feature.
 */
class LocalVideoRenderService : VideoRenderService {

    override suspend fun renderStoryboard(
        project: VideoProject,
        fallbackMode: FallbackResourceMode
    ): VideoRenderResult {
        val scenes = project.plan.scenes
        val totalSecs = scenes.sumOf { it.durationSeconds }
        val attachedCount = scenes.count { !it.attachedAssetId.isNullOrBlank() }

        return VideoRenderResult(
            isSuccess = scenes.isNotEmpty(),
            totalScenes = scenes.size,
            totalDurationSeconds = totalSecs,
            attachedResourcesCount = attachedCount,
            message = if (scenes.isNotEmpty()) {
                "تم تجهيز لوحة المشاهد بنجاح ($totalSecs ثانية، $attachedCount موارد مرفقة)"
            } else {
                "لا توجد مشاهد مضافة في لوحة القصة"
            }
        )
    }

    override suspend fun exportVideo(project: VideoProject): VideoExportResult {
        return VideoExportResult(
            isAvailable = false,
            exportPath = null,
            message = "وظيفة تصدير ملف الفيديو الفعلي قيد التطوير وستتاح في تحديث قادم.",
            notice = "يمكنك حاليًا استعراض وتعديل لوحة القصة، وإدارة الموارد المرخصة عبر المسار الاحتياطي."
        )
    }

    override fun isRealExportAvailable(): Boolean = false
}
