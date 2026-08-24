package com.example.ui.screens.studio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.StudioRepository
import com.example.data.repository.StudioRepositoryImpl
import com.example.domain.model.studio.AssetType
import com.example.domain.model.studio.EditingStyle
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

class StudioViewModel(
    application: Application,
    private val repository: StudioRepository = StudioRepositoryImpl(application)
) : AndroidViewModel(application) {

    val projects: StateFlow<List<VideoProject>> = repository.getAllProjects()
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

    fun setFilter(filter: StudioFilter) {
        _selectedFilter.value = filter
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun createNewProject() {
        val newProject = VideoProject(
            id = UUID.randomUUID().toString(),
            title = "مشروع فيديو جديد",
            status = VideoStatus.DRAFT,
            renderStatus = VideoRenderStatus.IDLE,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            idea = VideoIdea(),
            plan = VideoPlan(),
            style = VideoStyle(),
            assets = emptyList()
        )
        _currentProject.value = newProject
    }

    fun loadProject(id: String) {
        viewModelScope.launch {
            val project = repository.getProjectById(id)
            _currentProject.value = project
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

    fun startGeneratingVideo() {
        val project = _currentProject.value ?: return
        val job = VideoGenerationJob(
            jobId = UUID.randomUUID().toString(),
            projectId = project.id,
            status = VideoRenderStatus.PROCESSING,
            progressPercent = 10,
            message = "تم حفظ المخطط محليًا. محرك توليد الفيديو الفعلي قيد التطوير وسيتم ربطه وتفعيله في تحديث قادم.",
            createdAt = System.currentTimeMillis()
        )

        val updated = project.copy(
            status = VideoStatus.GENERATING,
            renderStatus = VideoRenderStatus.PROCESSING,
            currentJob = job,
            errorMessage = "محرك التوليد الفعلي قيد التطوير وسيتم ربطه وتفعيله في تحديث قادم.",
            updatedAt = System.currentTimeMillis()
        )

        _currentProject.value = updated
        viewModelScope.launch {
            repository.saveProject(updated)
        }
    }
}
