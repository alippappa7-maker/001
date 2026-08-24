package com.example.domain.service.studio

import com.example.domain.model.studio.GenerationStage
import com.example.domain.model.studio.VideoGenerationJob
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoPlan
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoRenderStatus
import com.example.domain.model.studio.VideoScene
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Interface describing future video generation from text, reference image, or discrete scenes.
 * Independent service contract for future AI/Media engine integrations.
 */
interface VideoGenerationService {
    /**
     * Start video generation directly from an initial textual idea.
     */
    suspend fun generateFromText(idea: VideoIdea, projectId: String): VideoGenerationJob

    /**
     * Start video generation from an established storyboard plan.
     */
    suspend fun generateFromPlan(plan: VideoPlan, projectId: String): VideoGenerationJob

    /**
     * Start video generation from custom discrete scenes and reference assets.
     */
    suspend fun generateFromScenes(scenes: List<VideoScene>, projectId: String): VideoGenerationJob

    /**
     * Observe the reactive real-time status of an active video generation job.
     */
    fun observeJob(jobId: String): Flow<VideoGenerationJob?>

    /**
     * Cancel an active in-flight generation job.
     */
    suspend fun cancelGeneration(jobId: String): Boolean

    /**
     * Retry a failed or cancelled generation job for a project.
     */
    suspend fun retryGeneration(jobId: String, project: VideoProject): VideoGenerationJob

    /**
     * Get the active job synchronously if present.
     */
    fun getCurrentJob(jobId: String): VideoGenerationJob?
}

/**
 * Mock implementation used strictly for simulating the generation lifecycle in local mode.
 * Explicitly communicates to the user in every status update that real external generation is not linked.
 */
class MockVideoGenerationService(
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val simulateFailure: Boolean = false
) : VideoGenerationService {

    private val jobsMap = mutableMapOf<String, MutableStateFlow<VideoGenerationJob?>>()
    private val runningJobs = mutableMapOf<String, Job>()

    override suspend fun generateFromText(idea: VideoIdea, projectId: String): VideoGenerationJob {
        val job = createInitialJob(projectId, "بدء تحليل الفكرة ومحاكاة مراحل الإنتاج")
        startSimulationPipeline(job)
        return job
    }

    override suspend fun generateFromPlan(plan: VideoPlan, projectId: String): VideoGenerationJob {
        val job = createInitialJob(projectId, "بدء تجهيز المشاهد ومحاكاة المعالجة")
        startSimulationPipeline(job)
        return job
    }

    override suspend fun generateFromScenes(scenes: List<VideoScene>, projectId: String): VideoGenerationJob {
        val job = createInitialJob(projectId, "بدء معالجة المشاهد المخصصة والمحاكاة")
        startSimulationPipeline(job)
        return job
    }

    override fun observeJob(jobId: String): Flow<VideoGenerationJob?> {
        return getOrCreateFlow(jobId).asStateFlow()
    }

    override fun getCurrentJob(jobId: String): VideoGenerationJob? {
        return jobsMap[jobId]?.value
    }

    override suspend fun cancelGeneration(jobId: String): Boolean {
        val running = runningJobs[jobId]
        running?.cancel()
        runningJobs.remove(jobId)

        val current = jobsMap[jobId]?.value ?: return false
        val cancelledJob = current.copy(
            status = VideoRenderStatus.CANCELLED,
            stage = GenerationStage.CANCELLED,
            message = "تم إلغاء عملية التوليد بناءً على طلب المستخدم (محاكاة محلية)",
            completedAt = System.currentTimeMillis()
        )
        jobsMap[jobId]?.value = cancelledJob
        return true
    }

    override suspend fun retryGeneration(jobId: String, project: VideoProject): VideoGenerationJob {
        cancelGeneration(jobId)
        val newJob = VideoGenerationJob(
            jobId = UUID.randomUUID().toString(),
            projectId = project.id,
            status = VideoRenderStatus.PROCESSING,
            stage = GenerationStage.ANALYZING,
            progressPercent = 5,
            message = "إعادة محاولة التوليد (محاكاة محلية - التوليد الحقيقي غير مربوط)",
            createdAt = System.currentTimeMillis(),
            isMock = true
        )
        startSimulationPipeline(newJob)
        return newJob
    }

    private fun createInitialJob(projectId: String, initialMsg: String): VideoGenerationJob {
        return VideoGenerationJob(
            jobId = UUID.randomUUID().toString(),
            projectId = projectId,
            status = VideoRenderStatus.PROCESSING,
            stage = GenerationStage.ANALYZING,
            progressPercent = 10,
            message = "$initialMsg (محاكاة محلية - التوليد الحقيقي غير مربوط)",
            createdAt = System.currentTimeMillis(),
            isMock = true
        )
    }

    private fun getOrCreateFlow(jobId: String): MutableStateFlow<VideoGenerationJob?> {
        return jobsMap.getOrPut(jobId) { MutableStateFlow(null) }
    }

    private fun startSimulationPipeline(initialJob: VideoGenerationJob) {
        val flow = getOrCreateFlow(initialJob.jobId)
        flow.value = initialJob

        val job = coroutineScope.launch {
            try {
                // Stage 1: ANALYZING
                flow.value = flow.value?.copy(
                    stage = GenerationStage.ANALYZING,
                    progressPercent = 20,
                    elapsedTimeSeconds = 1,
                    message = "تحليل الرسالة الإيمانية وسياق المشاهد (محاكاة محلية - التوليد الحقيقي غير مربوط)"
                )
                delay(800)

                // Stage 2: PLANNING
                flow.value = flow.value?.copy(
                    stage = GenerationStage.PLANNING,
                    progressPercent = 40,
                    elapsedTimeSeconds = 2,
                    message = "تجهيز لوحة القصة والمؤثرات البصرية والصوتية (محاكاة محلية - التوليد الحقيقي غير مربوط)"
                )
                delay(800)

                // Check for intentional failure test
                if (simulateFailure) {
                    flow.value = flow.value?.copy(
                        status = VideoRenderStatus.FAILED,
                        stage = GenerationStage.FAILED,
                        progressPercent = 45,
                        elapsedTimeSeconds = 3,
                        message = "تعذر إكمال التوليد في بيئة المحاكاة. يمكنك استخدام المسار الاحتياطي للموارد المحلية.",
                        completedAt = System.currentTimeMillis()
                    )
                    return@launch
                }

                // Stage 3: GENERATING
                flow.value = flow.value?.copy(
                    stage = GenerationStage.GENERATING,
                    progressPercent = 70,
                    elapsedTimeSeconds = 3,
                    message = "توليد المشاهد واللقطات الروحانية (محاكاة محلية - التوليد الحقيقي غير مربوط)"
                )
                delay(900)

                // Stage 4: RENDERING
                flow.value = flow.value?.copy(
                    stage = GenerationStage.RENDERING,
                    progressPercent = 90,
                    elapsedTimeSeconds = 4,
                    message = "دمج المشاهد ومعالجة الانتقالات (محاكاة محلية - التوليد الحقيقي غير مربوط)"
                )
                delay(900)

                // Stage 5: COMPLETED
                flow.value = flow.value?.copy(
                    status = VideoRenderStatus.COMPLETED,
                    stage = GenerationStage.COMPLETED,
                    progressPercent = 100,
                    elapsedTimeSeconds = 5,
                    message = "اكتملت محاكاة المشروع بنجاح. يمكنك معاينة لوحة القصة وتعديلها أو استخدام المسار الاحتياطي.",
                    completedAt = System.currentTimeMillis()
                )
            } catch (_: Exception) {
                // Cancelled or interrupted
            }
        }

        runningJobs[initialJob.jobId] = job
    }
}
