package com.example.ui.screens.studio

import com.example.ui.screens.studio.components.SceneResourceBadge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.R
import com.example.domain.model.studio.VideoRenderStatus
import com.example.domain.model.studio.VideoScene
import com.example.domain.model.studio.VideoStatus
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.Routes
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import com.example.ui.theme.StudioBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlanScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors
    val project by viewModel.currentProject.collectAsState()
    val scenes = project?.plan?.scenes ?: emptyList()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    var showDeleteDialogForSceneId by remember { mutableStateOf<String?>(null) }
    var sceneToEdit by remember { mutableStateOf<VideoScene?>(null) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var titleInputValue by remember { mutableStateOf(project?.title ?: "") }
    var showGenerationNoticeDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(project?.title) {
        if (!isEditingTitle) {
            titleInputValue = project?.title ?: ""
        }
    }

    val totalDurationSeconds = remember(scenes) {
        scenes.sumOf { it.durationSeconds }
    }

    val isProcessing = project?.status == VideoStatus.GENERATING || project?.renderStatus == VideoRenderStatus.PROCESSING

    Scaffold(
        topBar = {
            QabasTopBar(
                title = stringResource(R.string.studio_plan_title),
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
        modifier = Modifier.testTag("screen_studio_plan"),
        floatingActionButton = {
            if (!isProcessing) {
                FloatingActionButton(
                    onClick = { viewModel.addScene() },
                    containerColor = colors.gold,
                    contentColor = colors.surface,
                    modifier = Modifier.testTag("btn_fab_add_scene")
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.studio_btn_add_scene))
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space12),
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
            ) {
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(QabasDimens.Radius12))
                            .background(StudioBlue.copy(alpha = 0.15f))
                            .border(1.dp, StudioBlue.copy(alpha = 0.3f), RoundedCornerShape(QabasDimens.Radius12))
                            .padding(QabasDimens.Space12),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = StudioBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.studio_engine_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioBlue,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    QabasButton(
                        text = stringResource(R.string.studio_btn_start_creation),
                        onClick = {
                            viewModel.renderVideoForPreview()
                            navController.navigate(Routes.STUDIO_PREVIEW)
                        },
                        variant = QabasButtonVariant.PrimaryGold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_start_creation")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                    ) {
                        QabasButton(
                            text = "المحرر والموارد",
                            onClick = { navController.navigate(Routes.STUDIO_EDITOR) },
                            variant = QabasButtonVariant.OutlineGold,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_editor")
                        )

                        QabasButton(
                            text = stringResource(R.string.studio_btn_save_project),
                            onClick = { viewModel.saveCurrentProject() },
                            variant = QabasButtonVariant.OutlineGold,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_save_project")
                        )
                    }
                }

            }
        }
    ) { padding ->
        StarryBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = QabasDimens.Space16),
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space16),
                contentPadding = PaddingValues(top = QabasDimens.Space16, bottom = 140.dp)
            ) {
                // Project Title & Stats Header Card
                item {
                    ProjectTitleAndStatsCard(
                        title = project?.title ?: "مشروع فيديو جديد",
                        duration = totalDurationSeconds,
                        sceneCount = scenes.size,
                        aspectRatio = project?.idea?.orientation?.aspectRatioText ?: "9:16",
                        onEditTitle = { isEditingTitle = true }
                    )
                }

                // Scenes Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.studio_plan_scenes_list),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.gold
                        )
                        if (!isProcessing) {
                            TextButton(
                                onClick = { viewModel.addScene() },
                                modifier = Modifier.testTag("btn_add_scene"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.studio_btn_add_scene),
                                    color = colors.gold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                // Scenes List
                if (scenes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QabasDimens.Radius12))
                                .background(colors.surfaceElevated)
                                .padding(QabasDimens.Space24),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "لا توجد مشاهد في هذا المخطط",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                QabasButton(
                                    text = stringResource(R.string.studio_btn_add_scene),
                                    onClick = { viewModel.addScene() },
                                    variant = QabasButtonVariant.OutlineGold
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(scenes, key = { _, s -> s.id }) { index, scene ->
                        DetailedSceneCard(
                            sceneIndex = index + 1,
                            scene = scene,
                            isFirst = index == 0,
                            isLast = index == scenes.size - 1,
                            isProcessing = isProcessing,
                            onMoveUp = { viewModel.moveSceneUp(index) },
                            onMoveDown = { viewModel.moveSceneDown(index) },
                            onEdit = { sceneToEdit = scene },
                            onDelete = { showDeleteDialogForSceneId = scene.id }
                        )
                    }
                }
            }
        }

        // Edit Project Title Dialog
        if (isEditingTitle) {
            AlertDialog(
                onDismissRequest = { isEditingTitle = false },
                title = {
                    Text(
                        text = stringResource(R.string.studio_project_title_label),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                },
                text = {
                    OutlinedTextField(
                        value = titleInputValue,
                        onValueChange = { titleInputValue = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.gold,
                            unfocusedBorderColor = colors.surfaceElevated,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateProjectTitle(titleInputValue)
                            isEditingTitle = false
                        }
                    ) {
                        Text(text = "حفظ", color = colors.gold, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isEditingTitle = false }) {
                        Text(text = stringResource(R.string.cancel), color = colors.textPrimary)
                    }
                },
                containerColor = colors.surfaceElevated,
                shape = RoundedCornerShape(QabasDimens.Radius16)
            )
        }

        // Delete Scene Confirmation Dialog
        showDeleteDialogForSceneId?.let { sceneId ->
            AlertDialog(
                onDismissRequest = { showDeleteDialogForSceneId = null },
                title = {
                    Text(
                        text = "حذف المشهد",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.studio_delete_scene_confirm),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteScene(sceneId)
                            showDeleteDialogForSceneId = null
                        }
                    ) {
                        Text(
                            text = "حذف المشهد",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialogForSceneId = null }) {
                        Text(text = stringResource(R.string.cancel), color = colors.textPrimary)
                    }
                },
                containerColor = colors.surfaceElevated,
                shape = RoundedCornerShape(QabasDimens.Radius16)
            )
        }

        // Edit Scene Dialog
        sceneToEdit?.let { scene ->
            EditSceneComprehensiveDialog(
                scene = scene,
                onDismiss = { sceneToEdit = null },
                onSave = { updated ->
                    viewModel.updateScene(updated)
                    sceneToEdit = null
                }
            )
        }

        // Generation Notice Dialog (No Fake Video Generated, Real Status Notice)
        if (showGenerationNoticeDialog) {
            AlertDialog(
                onDismissRequest = { showGenerationNoticeDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = StudioBlue,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "حالة معالجة وتوليد الفيديو",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "تم حفظ مخطط ومشاهد المشروع محليًا بنجاح.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.studio_engine_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioBlue,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showGenerationNoticeDialog = false
                            navController.navigate(Routes.STUDIO) {
                                popUpTo(Routes.STUDIO) { inclusive = true }
                            }
                        }
                    ) {
                        Text(
                            text = "العودة للاستوديو",
                            color = colors.gold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = colors.surfaceElevated,
                shape = RoundedCornerShape(QabasDimens.Radius16)
            )
        }
    }
}

@Composable
private fun ProjectTitleAndStatsCard(
    title: String,
    duration: Int,
    sceneCount: Int,
    aspectRatio: String,
    onEditTitle: () -> Unit
) {
    val colors = QabasThemeTokens.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QabasDimens.Radius16))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.gold.copy(alpha = 0.25f), RoundedCornerShape(QabasDimens.Radius16))
            .padding(QabasDimens.Space16)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditTitle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "تعديل العنوان",
                        tint = colors.gold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space12))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(label = "المدة الإجمالية", value = "$duration ثانية", modifier = Modifier.weight(1f))
                StatBadge(label = "عدد المشاهد", value = "$sceneCount مشاهد", modifier = Modifier.weight(1f))
                StatBadge(label = "الأبعاد", value = aspectRatio, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = QabasThemeTokens.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(QabasDimens.Radius8))
            .background(colors.background.copy(alpha = 0.5f))
            .padding(vertical = 6.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = colors.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.gold
            )
        }
    }
}

@Composable
private fun DetailedSceneCard(
    sceneIndex: Int,
    scene: VideoScene,
    isFirst: Boolean,
    isLast: Boolean,
    isProcessing: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = QabasThemeTokens.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QabasDimens.Radius12))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.gold.copy(alpha = 0.15f), RoundedCornerShape(QabasDimens.Radius12))
            .padding(QabasDimens.Space16)
            .testTag("card_scene_$sceneIndex")
    ) {
        Column {
            // Header Row: Scene Number, Duration, and Reorder / Edit / Delete Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.gold.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "مشهد #$sceneIndex",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.gold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${scene.durationSeconds} ثوانٍ",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // شارة حالة المورد: صورة/فيديو حقيقي أم لون تلقائي.
                    SceneResourceBadge(
                        hasRealAsset = scene.attachedAssetId != null,
                        isVideo = scene.attachedAssetType == com.example.domain.model.studio.AssetType.VIDEO_CLIP
                    )
                }

                if (!isProcessing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = !isFirst,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "تحريك للأعلى",
                                tint = if (!isFirst) colors.gold else colors.textSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onMoveDown,
                            enabled = !isLast,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "تحريك للأسفل",
                                tint = if (!isLast) colors.gold else colors.textSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "تعديل المشهد",
                                tint = StudioBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف المشهد",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space12))

            // Visual Description
            if (scene.visualDescription.isNotBlank()) {
                SceneDetailRow(
                    label = stringResource(R.string.studio_scene_visual_label),
                    value = scene.visualDescription
                )
            }

            // On-Screen Text
            if (scene.onScreenText.isNotBlank()) {
                SceneDetailRow(
                    label = stringResource(R.string.studio_scene_text_label),
                    value = scene.onScreenText
                )
            }

            // Voiceover Text
            if (scene.voiceoverText.isNotBlank()) {
                SceneDetailRow(
                    label = stringResource(R.string.studio_scene_voiceover_label),
                    value = scene.voiceoverText
                )
            }

            // Transition & Required Asset Tags Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (scene.transition.isNotBlank()) {
                    SceneMetaChip(
                        label = stringResource(R.string.studio_scene_transition_label),
                        value = scene.transition
                    )
                }
                if (scene.requiredAsset.isNotBlank()) {
                    SceneMetaChip(
                        label = stringResource(R.string.studio_scene_asset_label),
                        value = scene.requiredAsset
                    )
                }
            }

            // Instructions
            if (scene.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                SceneDetailRow(
                    label = stringResource(R.string.studio_scene_instructions_label),
                    value = scene.instructions,
                    valueColor = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun SceneDetailRow(
    label: String,
    value: String,
    valueColor: Color = QabasThemeTokens.colors.textPrimary
) {
    val colors = QabasThemeTokens.colors
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = StudioBlue
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor
        )
    }
}

@Composable
private fun SceneMetaChip(label: String, value: String) {
    val colors = QabasThemeTokens.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(QabasDimens.Radius6))
            .background(colors.background.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label ",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = colors.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = colors.gold
            )
        }
    }
}

@Composable
private fun EditSceneComprehensiveDialog(
    scene: VideoScene,
    onDismiss: () -> Unit,
    onSave: (VideoScene) -> Unit
) {
    val colors = QabasThemeTokens.colors
    var durationText by remember { mutableStateOf(scene.durationSeconds.toString()) }
    var visualDescription by remember { mutableStateOf(scene.visualDescription) }
    var onScreenText by remember { mutableStateOf(scene.onScreenText) }
    var voiceoverText by remember { mutableStateOf(scene.voiceoverText) }
    var transition by remember { mutableStateOf(scene.transition) }
    var requiredAsset by remember { mutableStateOf(scene.requiredAsset) }
    var instructions by remember { mutableStateOf(scene.instructions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.studio_edit_scene_dialog_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("المدة (بالثواني)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = visualDescription,
                    onValueChange = { visualDescription = it },
                    label = { Text("الوصف المرئي") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = onScreenText,
                    onValueChange = { onScreenText = it },
                    label = { Text("النص الظاهر على الشاشة") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = voiceoverText,
                    onValueChange = { voiceoverText = it },
                    label = { Text("نص التعليق الصوتي") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = transition,
                    onValueChange = { transition = it },
                    label = { Text("نوع الانتقال (مثال: تلاشي ناعم / قطع سلس)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = requiredAsset,
                    onValueChange = { requiredAsset = it },
                    label = { Text("المورد المطلوب (مثال: لقطة طبيعية / مخطوطة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("تعليمات المشهد") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updated = scene.copy(
                        durationSeconds = durationText.toIntOrNull() ?: scene.durationSeconds,
                        visualDescription = visualDescription.trim(),
                        onScreenText = onScreenText.trim(),
                        voiceoverText = voiceoverText.trim(),
                        transition = transition.trim(),
                        requiredAsset = requiredAsset.trim(),
                        instructions = instructions.trim()
                    )
                    onSave(updated)
                }
            ) {
                Text(text = "حفظ التعديلات", color = colors.gold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel), color = colors.textPrimary)
            }
        },
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(QabasDimens.Radius16)
    )
}
