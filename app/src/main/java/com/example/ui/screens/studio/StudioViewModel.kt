package com.example.ui.screens.studio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.StudioRepository
import com.example.data.repository.StudioRepositoryImpl
import com.example.domain.model.studio.AssetAttribution
import com.example.domain.model.studio.AssetLicense
import com.example.domain.model.studio.AssetType
import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.FallbackResourceMode
import com.example.domain.model.studio.GenerationStage
import com.example.domain.model.studio.LicensedAsset
import com.example.domain.model.studio.VideoAsset
import com.example.domain.model.studio.VideoDuration
import com.example.domain.model.studio.VideoGenerationJob
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoLanguage
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoPlan
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoRenderStatus
import com.example.domain.model.studio.VideoScene
import com.example.domain.model.studio.VideoStatus
import com.example.domain.model.studio.VideoStyle
import com.example.domain.model.studio.VideoTone
import com.example.domain.service.studio.LocalResourceProvider
import com.example.domain.service.studio.LocalVideoRenderService
import com.example.domain.service.studio.MockVideoGenerationService
import com.example.domain.service.studio.ResourceProvider
import com.example.domain.service.studio.VideoExportResult
import com.example.domain.service.studio.VideoGenerationService
import com.example.domain.service.studio.VideoRenderResult
import com.example.domain.service.studio.VideoRenderService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class StudioFilter {
    ALL,
    PROCESSING,
    COMPLETED,
    DRAFTS_FAILED
}

class StudioViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: StudioRepository = StudioRepositoryImpl(application),
    generationService: VideoGenerationService? = null,
    private val resourceProvider: ResourceProvider = LocalResourceProvider(),
    private val renderService: VideoRenderService = LocalVideoRenderService()
) : AndroidViewModel(application) {

    private val generationService: VideoGenerationService = generationService ?: MockVideoGenerationService(viewModelScope)


    val projects: StateFlow<List<VideoProject>> = repository.getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val localAvailableResources: StateFlow<List<LicensedAsset>> = resourceProvider.observeLocalResources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentProject = MutableStateFlow<VideoProject?>(null)
    val currentProject: StateFlow<VideoProject?> = _currentProject.asStateFlow()

    private val _selectedFilter = MutableStateFlow(StudioFilter.ALL)
    val selectedFilter: StateFlow<StudioFilter> = _selectedFilter.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _exportNotice = MutableStateFlow<VideoExportResult?>(null)
    val exportNotice: StateFlow<VideoExportResult?> = _exportNotice.asStateFlow()

    private var activeJobObservation: Job? = null

    fun setFilter(filter: StudioFilter) {
        _selectedFilter.value = filter
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun clearExportNotice() {
        _exportNotice.value = null
    }

    fun createNewProject() {
        val newProject = VideoProject(
            id = UUID.randomUUID().toString(),
            title = "مشروع فيديو جديد",
            status = VideoStatus.DRAFT,
            renderStatus = VideoRenderStatus.IDLE,
            generationStage = GenerationStage.IDLE,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            idea = VideoIdea(),
            plan = VideoPlan(),
            style = VideoStyle(),
            assets = emptyList(),
            licensedAssets = emptyList(),
            fallbackMode = FallbackResourceMode()
        )
        _currentProject.value = newProject
    }

    fun loadProject(id: String) {
        viewModelScope.launch {
            val project = repository.getProjectById(id)
            _currentProject.value = project
            project?.currentJob?.let { job ->
                if (job.status == VideoRenderStatus.PROCESSING) {
                    observeGenerationJob(job.jobId)
                }
            }
        }
    }

    fun updateProjectTitle(title: String) {
        _currentProject.value = _currentProject.value?.copy(
            title = title.ifBlank { "مشروع فيديو جديد" },
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateIdea(idea: VideoIdea) {
        val current = _currentProject.value ?: return
        val generatedTitle = if (idea.ideaText.isNotBlank() && (current.title == "مشروع فيديو جديد" || current.title.isBlank())) {
            idea.ideaText.take(28).trim() + "..."
        } else {
            current.title
        }
        _currentProject.value = current.copy(
            title = generatedTitle,
            idea = idea,
            style = current.style.copy(
                tone = idea.tone,
                editingStyle = idea.editingStyle
            ),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun analyzeIdea() {
        val project = _currentProject.value ?: return
        val idea = project.idea
        val text = idea.ideaText.trim()

        val isTooShort = text.length < 20
        val missingQuestions = mutableListOf<String>()

        if (isTooShort) {
            missingQuestions.add("ما هي الرسالة أو العبرة الأساسية المراد إيصالها للمشاهد؟")
            missingQuestions.add("هل ترغب في تضمين آية أو حديث شريف أو نص محدد؟")
        }
        if (idea.audience.isBlank()) {
            missingQuestions.add("من هي الشريحة المستهدفة (مثال: الشباب، الطلاب، عامة الناس)؟")
        }

        val summary = if (text.isNotBlank()) {
            "فيديو ${idea.tone.titleAr} بأسلوب ${idea.editingStyle.titleAr} يتناول: ${text.take(90)}${if (text.length > 90) "..." else ""}"
        } else {
            "فكرة فيديو إسلامي هادف ومؤثر"
        }

        val goal = "إيصال معنى إيماني عميق وتثبيت الأثر الإيجابي لدى ${idea.audience.ifBlank { "المشاهدين" }} من خلال قالب ${idea.editingStyle.titleAr} متزن."

        val targetAudience = idea.audience.ifBlank { "عامة المهتمين بالمحتوى الهادف والروحاني" }

        val resources = mutableListOf<String>()
        if (idea.hasVoiceover) {
            resources.add("تسجيل تعليق صوتي واضح بنبرة ${idea.tone.titleAr}")
        }
        if (idea.hasMusicOrEffects) {
            resources.add("مؤثرات صوتية هادئة ملائمة للأجواء الروحانية")
        }
        if (idea.hasOnScreenText) {
            resources.add("خطوط عربية واضحة ومقروءة لعناوين المشاهد")
        }
        resources.add("لقطات بصرية ملائمة لأبعاد ${idea.orientation.aspectRatioText}")

        val suggestedTexts = listOf(
            "«خطوة نحو السكينة»",
            "«تأمل يجدد في القلب الإيمان»",
            if (text.length > 10) "«${text.take(30)}...»" else "«أثر يبقى وينفع»"
        )

        val initialScenes = createInitialScenes(idea)

        val analyzedPlan = VideoPlan(
            summary = summary,
            goal = goal,
            targetAudience = targetAudience,
            suggestedEditingStyle = "${idea.editingStyle.titleAr} (${idea.tone.titleAr})",
            durationSeconds = idea.duration.seconds,
            orientation = idea.orientation,
            sceneCount = initialScenes.size,
            requiredResources = resources,
            suggestedTexts = suggestedTexts,
            missingQuestions = missingQuestions,
            scenes = initialScenes
        )

        val updated = project.copy(
            plan = analyzedPlan,
            status = VideoStatus.ANALYZING,
            generationStage = GenerationStage.ANALYZING,
            updatedAt = System.currentTimeMillis()
        )

        _currentProject.value = updated
        viewModelScope.launch {
            repository.saveProject(updated)
        }
    }

    fun generatePlan() {
        val project = _currentProject.value ?: return
        val scenes = if (project.plan.scenes.isNotEmpty()) {
            project.plan.scenes
        } else {
            createInitialScenes(project.idea)
        }

        val updatedPlan = project.plan.copy(
            scenes = scenes,
            sceneCount = scenes.size,
            durationSeconds = scenes.sumOf { it.durationSeconds }
        )

        val updatedProject = project.copy(
            plan = updatedPlan,
            status = VideoStatus.PLANNING,
            generationStage = GenerationStage.PLANNING,
            updatedAt = System.currentTimeMillis()
        )

        _currentProject.value = updatedProject
        viewModelScope.launch {
            repository.saveProject(updatedProject)
        }
    }

    private fun createInitialScenes(idea: VideoIdea): List<VideoScene> {
        val totalSecs = idea.duration.seconds
        val toneText = idea.tone.titleAr
        val isEn = idea.language == VideoLanguage.ENGLISH

        return when (idea.duration) {
            VideoDuration.SHORT -> listOf(
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 4,
                    visualDescription = if (isEn) "Captivating opening visual hook matching the theme" else "مشهد افتتاحي جذاب يشد الانتباه ويعكس روح الفكرة",
                    onScreenText = if (isEn) "Are you ready?" else "لحظة تأمل...",
                    voiceoverText = if (idea.hasVoiceover) (if (isEn) "Take a moment to reflect on your journey..." else "هل وقفت يومًا تتأمل أثر خطواتك؟") else "",
                    transition = "تلاشي ضوئي ناعم",
                    requiredAsset = "لقطة طبيعة أو سماء فجرية",
                    instructions = "حركة كاميرا بطيئة وتأثير إضاءة ذهبي خافت"
                ),
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 7,
                    visualDescription = if (isEn) "Core reflection scene highlighting the main idea" else "المشهد المركزي: عرض الفكرة الأساسية بوضوح وجمال",
                    onScreenText = idea.ideaText.take(35).ifBlank { "خطوة واحدة تصنع فارقًا كبيرًا" },
                    voiceoverText = if (idea.hasVoiceover) (if (isEn) "A sincere intention turns daily moments into enduring impact." else "كل عمل يبدأ بنية صادقة يثمر أثرًا دائمًا.") else "",
                    transition = "قطع سينمائي سلس",
                    requiredAsset = "مخطوطة عربية وزخرفة هندسية هادئة",
                    instructions = "تركيز على النص في وسط الشاشة بتباين مريح للعين"
                ),
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 4,
                    visualDescription = if (isEn) "Inspiring closing call to action and mindful pause" else "مشهد ختامي هادئ مع رسالة أثر وبصمة ختام",
                    onScreenText = if (isEn) "Peace begins within" else "قبس • نور يضيء دربك",
                    voiceoverText = if (idea.hasVoiceover) (if (isEn) "May peace accompany your every step." else "اجعل يومك مليئًا بالنور والسكينة.") else "",
                    transition = "تلاشي تدريجي للظلام",
                    requiredAsset = "شعار قبس الذهبي المتوهج",
                    instructions = "انتقال هادئ نحو التوهج الختامي"
                )
            )
            VideoDuration.MEDIUM -> listOf(
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 5,
                    visualDescription = "مشهد استهلالي عميق يجذب المشاهد نحو فكرة المقطع",
                    onScreenText = "في زحام الحياة...",
                    voiceoverText = if (idea.hasVoiceover) "وسط تسارع الأيام وضجيج المشتتات..." else "",
                    transition = "تلاشي ضوئي",
                    requiredAsset = "لقطة حركة سريعة تتحول لبطيئة",
                    instructions = "تدرج لوني دافئ يرمز للهدوء"
                ),
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 8,
                    visualDescription = "تأصيل المعنى وعرض السياق بأسلوب ${idea.editingStyle.titleAr}",
                    onScreenText = "نحتاج إلى وقفة صدق مع النفس",
                    voiceoverText = if (idea.hasVoiceover) "تأتي لحظات السكون لتذكرنا بما هو أهم وأبقى." else "",
                    transition = "انتقال مسح ناعم",
                    requiredAsset = "لقطة مصحف أو كتاب أو نافذة نور",
                    instructions = "إبراز خط الرقعة الأنيق"
                ),
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 11,
                    visualDescription = "استعراض الرسالة الجوهرية والتطبيق العملي",
                    onScreenText = idea.ideaText.take(45).ifBlank { "الاستمرار على العمل الصالح وإن قل" },
                    voiceoverText = if (idea.hasVoiceover) "إن أحب الأعمال أدومها، والبركة في الإخلاص." else "",
                    transition = "قطع سلس متناسق",
                    requiredAsset = "مشهد رمزي يمثل العطاء والأثر",
                    instructions = "مؤثر صوتي رقيق يرافق النص"
                ),
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 6,
                    visualDescription = "مشهد الختام والدعوة للأثر",
                    onScreenText = "ابدأ خطوتك اليوم مع قبس",
                    voiceoverText = if (idea.hasVoiceover) "ابدأ الآن، فالطريق يبدأ بخطوة مباركة." else "",
                    transition = "تلاشي للنور الذهبي",
                    requiredAsset = "لوحة ختامية أنيقة",
                    instructions = "ثبات النص لمدة ثانيتين قبل انتهاء المقطع"
                )
            )
            VideoDuration.LONG -> listOf(
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 8,
                    visualDescription = "مقدمة سينمائية مدعمة بنبرة $toneText",
                    onScreenText = "رسالة إلى قلبك",
                    voiceoverText = if (idea.hasVoiceover) "تأمل كيف تمر الساعات، وما الذي يبقى حقًا في ميزان الأثر؟" else "",
                    transition = "تلاشي بطيء",
                    requiredAsset = "لقطة أفقية واسعة للطبيعة",
                    instructions = "زوم بطيء للداخل"
                ),
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 14,
                    visualDescription = "بسط الموضوع وشرح الفكرة من عدة زوايا",
                    onScreenText = "المعرفة التي تهدي وتبني",
                    voiceoverText = if (idea.hasVoiceover) "حين نقترن بالعلم والذكر، تصبح كل دقيقة محطة للبناء الروحي." else "",
                    transition = "قطع سينمائي",
                    requiredAsset = "خطوط عربية متحركة",
                    instructions = "ظهور الكلمات تباعًا مع الصوت"
                ),
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 16,
                    visualDescription = "القلب النابض للفكرة والبرهان المؤثر",
                    onScreenText = idea.ideaText.take(50).ifBlank { "أثر الإخلاص في تزكية النفوس" },
                    voiceoverText = if (idea.hasVoiceover) "كل جهد تبذله في سبيل الخير هو غرس لا يضيع، يثمر في الدنيا والآخرة." else "",
                    transition = "انتقال تكبير سلس",
                    requiredAsset = "مشهد معبر عن الاستمرارية",
                    instructions = "إضاءة جانبية وزخارف ضوئية هادئة"
                ),
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 14,
                    visualDescription = "توجيه عملي وخطوات تطبيقية سهلة",
                    onScreenText = "خطواتك العملية اليوم",
                    voiceoverText = if (idea.hasVoiceover) "حدد وردك، احفظ وقتك، واجعل نيتك خالصة تنل السكينة." else "",
                    transition = "مسح أفقي",
                    requiredAsset = "أيقونات إرشادية",
                    instructions = "تنظيم النقاط في بطاقات بصرية"
                ),
                VideoScene(
                    id = UUID.randomUUID().toString(),
                    durationSeconds = 8,
                    visualDescription = "خاتمة جامعة ودعاء مؤثر",
                    onScreenText = "قبس • رفيقك في رحلة السكينة",
                    voiceoverText = if (idea.hasVoiceover) "نسأل الله أن يجعل أعمالنا خالصة لوجهه الكريم." else "",
                    transition = "تلاشي ذهبي",
                    requiredAsset = "الشعار الختامي",
                    instructions = "إغلاق المشهد بتدرج هادئ"
                )
            )
        }
    }

    fun addScene() {
        val project = _currentProject.value ?: return
        val currentScenes = project.plan.scenes
        val newScene = VideoScene(
            id = UUID.randomUUID().toString(),
            durationSeconds = 5,
            visualDescription = "مشهد إضافي جديد",
            onScreenText = "نص المشهد الجديد",
            voiceoverText = "",
            transition = "قطع سلس",
            requiredAsset = "لقطة عامة",
            instructions = "تعليمات المشهد"
        )
        val updatedScenes = currentScenes + newScene
        val updatedPlan = project.plan.copy(
            scenes = updatedScenes,
            sceneCount = updatedScenes.size,
            durationSeconds = updatedScenes.sumOf { it.durationSeconds }
        )
        val updatedProject = project.copy(
            plan = updatedPlan,
            updatedAt = System.currentTimeMillis()
        )
        _currentProject.value = updatedProject
        viewModelScope.launch {
            repository.saveProject(updatedProject)
        }
    }

    fun updateScene(updatedScene: VideoScene) {
        val project = _currentProject.value ?: return
        val updatedScenes = project.plan.scenes.map { if (it.id == updatedScene.id) updatedScene else it }
        val updatedPlan = project.plan.copy(
            scenes = updatedScenes,
            durationSeconds = updatedScenes.sumOf { it.durationSeconds }
        )
        val updatedProject = project.copy(
            plan = updatedPlan,
            updatedAt = System.currentTimeMillis()
        )
        _currentProject.value = updatedProject
        viewModelScope.launch {
            repository.saveProject(updatedProject)
        }
    }

    fun deleteScene(sceneId: String) {
        val project = _currentProject.value ?: return
        val updatedScenes = project.plan.scenes.filter { it.id != sceneId }
        val updatedPlan = project.plan.copy(
            scenes = updatedScenes,
            sceneCount = updatedScenes.size,
            durationSeconds = updatedScenes.sumOf { it.durationSeconds }
        )
        val updatedProject = project.copy(
            plan = updatedPlan,
            updatedAt = System.currentTimeMillis()
        )
        _currentProject.value = updatedProject
        viewModelScope.launch {
            repository.saveProject(updatedProject)
        }
    }

    fun moveSceneUp(index: Int) {
        if (index <= 0) return
        val project = _currentProject.value ?: return
        val scenes = project.plan.scenes.toMutableList()
        if (index < scenes.size) {
            val item = scenes.removeAt(index)
            scenes.add(index - 1, item)
            val updatedPlan = project.plan.copy(scenes = scenes)
            val updatedProject = project.copy(plan = updatedPlan, updatedAt = System.currentTimeMillis())
            _currentProject.value = updatedProject
            viewModelScope.launch {
                repository.saveProject(updatedProject)
            }
        }
    }

    fun moveSceneDown(index: Int) {
        val project = _currentProject.value ?: return
        val scenes = project.plan.scenes.toMutableList()
        if (index >= 0 && index < scenes.size - 1) {
            val item = scenes.removeAt(index)
            scenes.add(index + 1, item)
            val updatedPlan = project.plan.copy(scenes = scenes)
            val updatedProject = project.copy(plan = updatedPlan, updatedAt = System.currentTimeMillis())
            _currentProject.value = updatedProject
            viewModelScope.launch {
                repository.saveProject(updatedProject)
            }
        }
    }

    fun saveCurrentProject() {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            repository.saveProject(project)
            _feedbackMessage.value = "تم حفظ المشروع محليًا بنجاح"
        }
    }

    fun retryProject(projectId: String) {
        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: return@launch
            val resetProject = project.copy(
                status = VideoStatus.PLANNING,
                renderStatus = VideoRenderStatus.IDLE,
                generationStage = GenerationStage.PLANNING,
                errorMessage = null,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveProject(resetProject)
            _currentProject.value = resetProject
            _feedbackMessage.value = "تمت إعادة تهيئة المشروع للتعديل والتخطيط"
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_currentProject.value?.id == projectId) {
                _currentProject.value = null
            }
            _feedbackMessage.value = "تم حذف المشروع"
        }
    }

    // --- Generation Lifecycle, Cancellation & Retry ---

    fun startGeneratingVideo() {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            val job = generationService.generateFromPlan(project.plan, project.id)
            val updated = project.copy(
                status = VideoStatus.GENERATING,
                renderStatus = VideoRenderStatus.PROCESSING,
                generationStage = job.stage,
                currentJob = job,
                errorMessage = null,
                updatedAt = System.currentTimeMillis()
            )
            _currentProject.value = updated
            repository.saveProject(updated)
            observeGenerationJob(job.jobId)
        }
    }

    private fun observeGenerationJob(jobId: String) {
        activeJobObservation?.cancel()
        activeJobObservation = viewModelScope.launch {
            generationService.observeJob(jobId).collect { job ->
                if (job == null) return@collect
                val current = _currentProject.value ?: return@collect
                val newStatus = when (job.status) {
                    VideoRenderStatus.COMPLETED -> VideoStatus.COMPLETED
                    VideoRenderStatus.FAILED -> VideoStatus.FAILED
                    VideoRenderStatus.CANCELLED -> VideoStatus.CANCELLED
                    else -> VideoStatus.GENERATING
                }
                val updated = current.copy(
                    status = newStatus,
                    renderStatus = job.status,
                    generationStage = job.stage,
                    currentJob = job,
                    errorMessage = if (job.status == VideoRenderStatus.FAILED) job.message else null,
                    updatedAt = System.currentTimeMillis()
                )
                _currentProject.value = updated
                repository.saveProject(updated)
            }
        }
    }

    fun cancelActiveGeneration() {
        val current = _currentProject.value ?: return
        val jobId = current.currentJob?.jobId ?: return
        viewModelScope.launch {
            generationService.cancelGeneration(jobId)
            val updated = current.copy(
                status = VideoStatus.CANCELLED,
                renderStatus = VideoRenderStatus.CANCELLED,
                generationStage = GenerationStage.CANCELLED,
                errorMessage = "تم إلغاء عملية التوليد بناءً على طلبك.",
                updatedAt = System.currentTimeMillis()
            )
            _currentProject.value = updated
            repository.saveProject(updated)
            _feedbackMessage.value = "تم إلغاء عملية التوليد"
        }
    }

    fun retryActiveGeneration() {
        val current = _currentProject.value ?: return
        val oldJobId = current.currentJob?.jobId ?: UUID.randomUUID().toString()
        viewModelScope.launch {
            val newJob = generationService.retryGeneration(oldJobId, current)
            val updated = current.copy(
                status = VideoStatus.GENERATING,
                renderStatus = VideoRenderStatus.PROCESSING,
                generationStage = newJob.stage,
                currentJob = newJob,
                errorMessage = null,
                updatedAt = System.currentTimeMillis()
            )
            _currentProject.value = updated
            repository.saveProject(updated)
            observeGenerationJob(newJob.jobId)
            _feedbackMessage.value = "بدأت إعادة محاولة التوليد (محاكاة)"
        }
    }

    // --- Fallback Resource Mode & Asset Validation ---

    fun toggleFallbackResourceMode(enabled: Boolean) {
        val current = _currentProject.value ?: return
        val currentFallback = current.fallbackMode
        val updated = current.copy(
            fallbackMode = currentFallback.copy(isEnabled = enabled),
            updatedAt = System.currentTimeMillis()
        )
        _currentProject.value = updated
        viewModelScope.launch {
            repository.saveProject(updated)
        }
    }

    fun setFallbackConsent(given: Boolean) {
        val current = _currentProject.value ?: return
        val currentFallback = current.fallbackMode
        val updated = current.copy(
            fallbackMode = currentFallback.copy(userConfirmedConsent = given),
            updatedAt = System.currentTimeMillis()
        )
        _currentProject.value = updated
        viewModelScope.launch {
            repository.saveProject(updated)
        }
    }

    fun validateAndAddExternalResource(
        title: String,
        uriOrPath: String,
        assetType: AssetType,
        fileSizeBytes: Long,
        source: String,
        license: AssetLicense,
        author: String = "",
        sourceUrl: String = "",
        isUserProvided: Boolean = true,
        isConsentGiven: Boolean = false
    ): Result<LicensedAsset> {
        val attribution = AssetAttribution(
            author = author,
            sourceTitle = source,
            sourceUrlOrPath = sourceUrl,
            licenseNotice = license.titleAr,
            requiresAttribution = license == AssetLicense.CREATIVE_COMMONS_BY
        )
        val candidate = LicensedAsset(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "مورد محلي جديد" },
            uriOrPath = uriOrPath,
            assetType = assetType,
            fileSizeBytes = fileSizeBytes,
            source = source,
            license = license,
            attribution = attribution,
            isUserProvided = isUserProvided,
            isConsentGiven = isConsentGiven,
            createdAt = System.currentTimeMillis()
        )

        return viewModelScope.let {
            // Validate via resource provider rules
            val validationResult = when {
                !candidate.isConsentGiven -> {
                    Result.failure(IllegalArgumentException("يلزم تأكيد موافقة وتعهد المستخدم بملكية أو ترخيص المورد قبل الإضافة."))
                }
                candidate.license == AssetLicense.UNKNOWN_UNLICENSED || !candidate.license.isPermitted -> {
                    Result.failure(IllegalArgumentException("تم رفض المورد: الترخيص غير صالح أو غير مصرح به للاستخدام."))
                }
                !candidate.isUserProvided && candidate.source.isBlank() -> {
                    Result.failure(IllegalArgumentException("تم رفض المورد: يجب تحديد مصدر واضح وموثق للمورد الخارجي."))
                }
                else -> Result.success(candidate)
            }

            if (validationResult.isSuccess) {
                val current = _currentProject.value
                if (current != null) {
                    val updatedAssets = current.licensedAssets.filter { it.id != candidate.id } + candidate
                    val updatedProject = current.copy(
                        licensedAssets = updatedAssets,
                        fallbackMode = current.fallbackMode.copy(isEnabled = true),
                        updatedAt = System.currentTimeMillis()
                    )
                    _currentProject.value = updatedProject
                    viewModelScope.launch {
                        repository.saveProject(updatedProject)
                        resourceProvider.saveUserResource(candidate)
                    }
                }
            }
            validationResult
        }
    }

    fun attachResourceToScene(sceneId: String, asset: LicensedAsset) {
        val current = _currentProject.value ?: return
        val updatedScenes = current.plan.scenes.map { scene ->
            if (scene.id == sceneId) {
                scene.copy(
                    attachedAssetId = asset.id,
                    attachedAssetTitle = asset.title,
                    attachedAssetType = asset.assetType
                )
            } else {
                scene
            }
        }
        val updatedProject = current.copy(
            plan = current.plan.copy(scenes = updatedScenes),
            updatedAt = System.currentTimeMillis()
        )
        _currentProject.value = updatedProject
        viewModelScope.launch {
            repository.saveProject(updatedProject)
            _feedbackMessage.value = "تم إرفاق «${asset.title}» بالمشهد"
        }
    }

    fun detachResourceFromScene(sceneId: String) {
        val current = _currentProject.value ?: return
        val updatedScenes = current.plan.scenes.map { scene ->
            if (scene.id == sceneId) {
                scene.copy(
                    attachedAssetId = null,
                    attachedAssetTitle = null,
                    attachedAssetType = null
                )
            } else {
                scene
            }
        }
        val updatedProject = current.copy(
            plan = current.plan.copy(scenes = updatedScenes),
            updatedAt = System.currentTimeMillis()
        )
        _currentProject.value = updatedProject
        viewModelScope.launch {
            repository.saveProject(updatedProject)
            _feedbackMessage.value = "تمت إزالة المورد من المشهد"
        }
    }

    fun exportCurrentVideo() {
        val current = _currentProject.value ?: return
        viewModelScope.launch {
            val result = renderService.exportVideo(current)
            _exportNotice.value = result
        }
    }
}

