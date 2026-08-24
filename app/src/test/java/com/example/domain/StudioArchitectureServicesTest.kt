package com.example.domain

import com.example.domain.model.studio.AssetLicense
import com.example.domain.model.studio.AssetType
import com.example.domain.model.studio.FallbackResourceMode
import com.example.domain.model.studio.GenerationStage
import com.example.domain.model.studio.LicensedAsset
import com.example.domain.model.studio.ResourceSearchQuery
import com.example.domain.model.studio.VideoGenerationJob
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoPlan
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoRenderStatus
import com.example.domain.model.studio.VideoScene
import com.example.domain.model.studio.VideoStatus
import com.example.domain.service.studio.LocalResourceProvider
import com.example.domain.service.studio.LocalVideoRenderService
import com.example.domain.service.studio.MockVideoGenerationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudioArchitectureServicesTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `mock video generation service completes full lifecycle pipeline`() = testScope.runTest {
        val service = MockVideoGenerationService(coroutineScope = this)
        val idea = VideoIdea(ideaText = "فيديو روحاني قصير عن الشكر")
        val job = service.generateFromText(idea, "proj_123")

        assertNotNull(job)
        assertTrue(job.isMock)
        assertEquals(VideoRenderStatus.PROCESSING, job.status)

        // Advance coroutine delay progression
        advanceUntilIdle()

        val completedJob = service.getCurrentJob(job.jobId)
        assertNotNull(completedJob)
        assertEquals(VideoRenderStatus.COMPLETED, completedJob?.status)
        assertEquals(GenerationStage.COMPLETED, completedJob?.stage)
        assertEquals(100, completedJob?.progressPercent)
        assertTrue(completedJob?.message?.contains("اكتملت") == true)
    }

    @Test
    fun `mock video generation service handles cancellation cleanly`() = testScope.runTest {
        val service = MockVideoGenerationService(coroutineScope = this)
        val plan = VideoPlan(summary = "مخطط فيديو هادف")
        val job = service.generateFromPlan(plan, "proj_cancel_test")

        val cancelled = service.cancelGeneration(job.jobId)
        assertTrue(cancelled)

        val finalJob = service.getCurrentJob(job.jobId)
        assertNotNull(finalJob)
        assertEquals(VideoRenderStatus.CANCELLED, finalJob?.status)
        assertEquals(GenerationStage.CANCELLED, finalJob?.stage)
        assertTrue(finalJob?.message?.contains("إلغاء") == true)
    }

    @Test
    fun `mock video generation service handles simulation failure and retry`() = testScope.runTest {
        val service = MockVideoGenerationService(coroutineScope = this, simulateFailure = true)
        val project = VideoProject(id = "proj_fail_test", title = "مشروع تجربة الخطأ")
        val job = service.generateFromScenes(emptyList(), project.id)

        advanceUntilIdle()

        val failedJob = service.getCurrentJob(job.jobId)
        assertNotNull(failedJob)
        assertEquals(VideoRenderStatus.FAILED, failedJob?.status)
        assertEquals(GenerationStage.FAILED, failedJob?.stage)

        // Now test retry with a standard service
        val normalService = MockVideoGenerationService(coroutineScope = this, simulateFailure = false)
        val retriedJob = normalService.retryGeneration(job.jobId, project)
        assertEquals(VideoRenderStatus.PROCESSING, retriedJob.status)

        advanceUntilIdle()
        val finishedRetried = normalService.getCurrentJob(retriedJob.jobId)
        assertEquals(VideoRenderStatus.COMPLETED, finishedRetried?.status)
    }

    @Test
    fun `local resource provider accepts valid licensed user asset with consent`() = testScope.runTest {
        val provider = LocalResourceProvider()
        val validAsset = LicensedAsset(
            id = "asset_valid_1",
            title = "صورة غروب شمس شخصية",
            uriOrPath = "file:///storage/sunset.jpg",
            assetType = AssetType.IMAGE,
            source = "تصوير شخصي من هاتف المستخدم",
            license = AssetLicense.USER_OWN_WORK,
            isUserProvided = true,
            isConsentGiven = true
        )

        val result = provider.validateAndRegisterResource(validAsset)
        assertTrue(result.isSuccess)

        val retrieved = provider.getResourceById("asset_valid_1")
        assertNotNull(retrieved)
        assertEquals("صورة غروب شمس شخصية", retrieved?.title)
    }

    @Test
    fun `local resource provider rejects asset without user consent`() = testScope.runTest {
        val provider = LocalResourceProvider()
        val noConsentAsset = LicensedAsset(
            id = "asset_no_consent",
            title = "صورة بدون موافقة",
            license = AssetLicense.USER_OWN_WORK,
            isConsentGiven = false
        )

        val result = provider.validateAndRegisterResource(noConsentAsset)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("موافقة") == true)
    }

    @Test
    fun `local resource provider rejects unlicensed unknown asset`() = testScope.runTest {
        val provider = LocalResourceProvider()
        val unlicensedAsset = LicensedAsset(
            id = "asset_unlicensed",
            title = "ملف مجهول المصدر",
            source = "إنترنت عشوائي",
            license = AssetLicense.UNKNOWN_UNLICENSED,
            isConsentGiven = true
        )

        val result = provider.validateAndRegisterResource(unlicensedAsset)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("الترخيص غير محدد") == true)
    }

    @Test
    fun `local resource provider rejects external asset with blank source`() = testScope.runTest {
        val provider = LocalResourceProvider()
        val blankSourceAsset = LicensedAsset(
            id = "asset_blank_source",
            title = "مقطع بدون مصدر",
            source = "   ",
            license = AssetLicense.LICENSED_STOCK,
            isUserProvided = false,
            isConsentGiven = true
        )

        val result = provider.validateAndRegisterResource(blankSourceAsset)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("مصدر واضح") == true)
    }

    @Test
    fun `local resource provider searches curated and added assets`() = testScope.runTest {
        val provider = LocalResourceProvider()
        val query = ResourceSearchQuery(queryText = "مخطوطة", assetType = AssetType.CALLIGRAPHY)
        val results = provider.searchResources(query)

        assertTrue(results.isNotEmpty())
        assertEquals(AssetType.CALLIGRAPHY, results.first().assetType)
    }

    @Test
    fun `local video render service renders storyboard and communicates export upcoming notice`() = testScope.runTest {
        val renderService = LocalVideoRenderService()
        val project = VideoProject(
            id = "proj_render_test",
            plan = VideoPlan(
                scenes = listOf(
                    VideoScene(id = "s1", durationSeconds = 5, onScreenText = "مشهد ١"),
                    VideoScene(id = "s2", durationSeconds = 7, onScreenText = "مشهد ٢", attachedAssetId = "asset_1")
                )
            )
        )

        val renderResult = renderService.renderStoryboard(project, FallbackResourceMode(isEnabled = true))
        assertTrue(renderResult.isSuccess)
        assertEquals(2, renderResult.totalScenes)
        assertEquals(12, renderResult.totalDurationSeconds)
        assertEquals(1, renderResult.attachedResourcesCount)

        assertFalse(renderService.isRealExportAvailable())
        val exportResult = renderService.exportVideo(project)
        assertFalse(exportResult.isAvailable)
        assertTrue(exportResult.message.contains("قيد التطوير"))
    }
}
