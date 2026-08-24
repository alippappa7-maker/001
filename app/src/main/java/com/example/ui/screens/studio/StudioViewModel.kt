package com.example.ui.screens.studio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.StudioRepository
import com.example.data.repository.StudioRepositoryImpl
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoPlan
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoScene
import com.example.domain.model.studio.VideoStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudioRepository = StudioRepositoryImpl(application)

    val projects: StateFlow<List<VideoProject>> = repository.getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentProject = MutableStateFlow<VideoProject?>(null)
    val currentProject: StateFlow<VideoProject?> = _currentProject.asStateFlow()

    fun createNewProject() {
        val newProject = VideoProject()
        _currentProject.value = newProject
    }

    fun loadProject(id: String) {
        viewModelScope.launch {
            _currentProject.value = repository.getProjectById(id)
        }
    }

    fun updateIdea(idea: VideoIdea) {
        _currentProject.value = _currentProject.value?.copy(idea = idea, updatedAt = System.currentTimeMillis())
    }

    fun analyzeIdea() {
        val project = _currentProject.value ?: return
        
        // Mocking analysis process
        val mockPlan = VideoPlan(
            summary = "ملخص الفكرة: ${project.idea.ideaText.take(50)}...",
            goal = "إنتاج فيديو مؤثر يحقق الهدف",
            targetAudience = project.idea.audience.ifBlank { "الجمهور العام" },
            suggestedEditingStyle = project.idea.editingStyle.name,
            requiredResources = listOf("فيديوهات طبيعة", "موسيقى هادئة", "تعليق صوتي احترافي"),
            suggestedTexts = listOf("اقتباس 1", "اقتباس 2"),
            missingQuestions = if (project.idea.ideaText.length < 20) listOf("ما هو الهدف الرئيسي؟", "هل هناك شخصيات محددة؟") else emptyList()
        )
        
        _currentProject.value = project.copy(
            plan = mockPlan,
            status = VideoStatus.PLANNING,
            updatedAt = System.currentTimeMillis()
        )
        saveCurrentProject()
    }

    fun generatePlan() {
        val project = _currentProject.value ?: return
        
        // Mocking scenes generation
        val mockScenes = listOf(
            VideoScene(
                durationSeconds = 5,
                visualDescription = "مشهد افتتاحي يجذب الانتباه",
                onScreenText = "العنوان الرئيسي",
                voiceoverText = "هل تساءلت يوماً...",
                transition = "Fade In",
                requiredAsset = "لقطة طبيعية"
            ),
            VideoScene(
                durationSeconds = 10,
                visualDescription = "شرح الفكرة الأساسية",
                onScreenText = "التفاصيل",
                voiceoverText = "في هذا الفيديو سنشرح لكم...",
                transition = "Cut",
                requiredAsset = "لقطة توضيحية"
            )
        )
        
        _currentProject.value = project.copy(
            plan = project.plan.copy(scenes = mockScenes),
            updatedAt = System.currentTimeMillis()
        )
        saveCurrentProject()
    }

    fun updateScene(updatedScene: VideoScene) {
        val project = _currentProject.value ?: return
        val updatedScenes = project.plan.scenes.map { if (it.id == updatedScene.id) updatedScene else it }
        _currentProject.value = project.copy(
            plan = project.plan.copy(scenes = updatedScenes),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun addScene() {
        val project = _currentProject.value ?: return
        val newScene = VideoScene()
        val updatedScenes = project.plan.scenes + newScene
        _currentProject.value = project.copy(
            plan = project.plan.copy(scenes = updatedScenes),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun deleteScene(sceneId: String) {
        val project = _currentProject.value ?: return
        val updatedScenes = project.plan.scenes.filter { it.id != sceneId }
        _currentProject.value = project.copy(
            plan = project.plan.copy(scenes = updatedScenes),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun saveCurrentProject() {
        _currentProject.value?.let { project ->
            viewModelScope.launch {
                repository.saveProject(project)
            }
        }
    }

    fun startGeneratingVideo() {
        val project = _currentProject.value ?: return
        // Mock generating state, as requested we just show it will be linked later
        _currentProject.value = project.copy(
            status = VideoStatus.GENERATING,
            updatedAt = System.currentTimeMillis(),
            errorMessage = "سيتم ربط محرك التوليد لاحقاً"
        )
        saveCurrentProject()
    }
}
