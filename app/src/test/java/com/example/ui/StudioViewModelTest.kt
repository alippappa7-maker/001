package com.example.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.StudioRepository
import com.example.domain.model.studio.*
import com.example.ui.screens.studio.StudioFilter
import com.example.ui.screens.studio.StudioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class FakeStudioRepository : StudioRepository {
    private val _projects = MutableStateFlow<List<VideoProject>>(emptyList())

    override fun getAllProjects(): Flow<List<VideoProject>> = _projects.asStateFlow()

    override suspend fun getProjectById(id: String): VideoProject? {
        return _projects.value.find { it.id == id }
    }

    override suspend fun saveProject(project: VideoProject) {
        val current = _projects.value.toMutableList()
        val index = current.indexOfFirst { it.id == project.id }
        if (index != -1) {
            current[index] = project
        } else {
            current.add(0, project)
        }
        _projects.value = current
    }

    override suspend fun deleteProject(id: String) {
        _projects.value = _projects.value.filterNot { it.id == id }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StudioViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var fakeRepository: FakeStudioRepository
    private lateinit var viewModel: StudioViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        fakeRepository = FakeStudioRepository()
        viewModel = StudioViewModel(application, fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createNewProject initializes a fresh project in state`() = runTest(testDispatcher) {
        viewModel.createNewProject()
        val project = viewModel.currentProject.value

        assertNotNull(project)
        assertEquals("مشروع فيديو جديد", project?.title)
        assertEquals(VideoStatus.DRAFT, project?.status)
        assertEquals(VideoRenderStatus.IDLE, project?.renderStatus)
    }

    @Test
    fun `analyzeIdea generates structured plan based on idea`() = runTest(testDispatcher) {
        viewModel.createNewProject()
        val idea = VideoIdea(
            ideaText = "فيديو تعريفي عن أهمية فضل أذكار الصباح في بدء اليوم بسكينة وبركة",
            audience = "الشباب والعائلات",
            duration = VideoDuration.SHORT,
            orientation = VideoOrientation.PORTRAIT,
            tone = VideoTone.INSPIRING,
            editingStyle = EditingStyle.CINEMATIC
        )
        viewModel.updateIdea(idea)
        viewModel.analyzeIdea()

        val project = viewModel.currentProject.value
        assertNotNull(project)
        assertNotNull(project?.plan)
        val plan = project?.plan!!
        assertTrue(plan.summary.isNotBlank())
        assertTrue(plan.goal.isNotBlank())
        assertEquals(VideoDuration.SHORT.seconds, plan.durationSeconds)
        assertEquals(VideoOrientation.PORTRAIT, plan.orientation)
        assertEquals(VideoStatus.ANALYZING, project.status)
    }

    @Test
    fun `generatePlan creates scenes and transitions`() = runTest(testDispatcher) {
        viewModel.createNewProject()
        val idea = VideoIdea(
            ideaText = "شرح مبسط لكيفية صلاة الاستخارة ودعائها",
            duration = VideoDuration.SHORT
        )
        viewModel.updateIdea(idea)
        viewModel.analyzeIdea()
        viewModel.generatePlan()

        val project = viewModel.currentProject.value
        assertNotNull(project)
        val scenes = project?.plan?.scenes
        assertNotNull(scenes)
        assertTrue(scenes!!.isNotEmpty())
        assertEquals(VideoStatus.PLANNING, project.status)
    }

    @Test
    fun `scene management add, update, delete, reorder`() = runTest(testDispatcher) {
        viewModel.createNewProject()
        val idea = VideoIdea(ideaText = "فيديو قصير عن التوكل")
        viewModel.updateIdea(idea)
        viewModel.analyzeIdea()
        viewModel.generatePlan()

        val initialScenesCount = viewModel.currentProject.value?.plan?.scenes?.size ?: 0
        assertTrue(initialScenesCount > 0)

        // Add Scene
        viewModel.addScene()
        assertEquals(initialScenesCount + 1, viewModel.currentProject.value?.plan?.scenes?.size)

        // Update Scene
        val lastScene = viewModel.currentProject.value?.plan?.scenes?.last()!!
        val updatedScene = lastScene.copy(
            visualDescription = "مشهد معدل",
            durationSeconds = 8
        )
        viewModel.updateScene(updatedScene)
        val fetchedUpdated = viewModel.currentProject.value?.plan?.scenes?.find { it.id == lastScene.id }
        assertEquals("مشهد معدل", fetchedUpdated?.visualDescription)
        assertEquals(8, fetchedUpdated?.durationSeconds)

        // Move Scene Up
        val scene1Before = viewModel.currentProject.value?.plan?.scenes?.get(1)
        viewModel.moveSceneUp(1)
        val scene0After = viewModel.currentProject.value?.plan?.scenes?.get(0)
        assertEquals(scene1Before?.id, scene0After?.id)

        // Delete Scene
        viewModel.deleteScene(lastScene.id)
        assertNull(viewModel.currentProject.value?.plan?.scenes?.find { it.id == lastScene.id })
    }

    @Test
    fun `saveCurrentProject and startGeneratingVideo persist correctly`() = runTest(testDispatcher) {
        viewModel.createNewProject()
        viewModel.updateIdea(VideoIdea(ideaText = "قصة صبر النبي أيوب عليه السلام"))
        viewModel.analyzeIdea()
        viewModel.generatePlan()
        viewModel.saveCurrentProject()
        advanceUntilIdle()

        val savedProjects = fakeRepository.getProjectById(viewModel.currentProject.value!!.id)
        assertNotNull(savedProjects)

        // Start video generation
        viewModel.startGeneratingVideo()
        advanceUntilIdle()

        val generatingProject = fakeRepository.getProjectById(viewModel.currentProject.value!!.id)
        assertNotNull(generatingProject)
        assertEquals(VideoStatus.GENERATING, generatingProject?.status)
        assertEquals(VideoRenderStatus.PROCESSING, generatingProject?.renderStatus)
    }

    @Test
    fun `filter tabs update state correctly`() {
        assertEquals(StudioFilter.ALL, viewModel.selectedFilter.value)
        viewModel.setFilter(StudioFilter.PROCESSING)
        assertEquals(StudioFilter.PROCESSING, viewModel.selectedFilter.value)
        viewModel.setFilter(StudioFilter.COMPLETED)
        assertEquals(StudioFilter.COMPLETED, viewModel.selectedFilter.value)
        viewModel.setFilter(StudioFilter.DRAFTS_FAILED)
        assertEquals(StudioFilter.DRAFTS_FAILED, viewModel.selectedFilter.value)
    }
}
