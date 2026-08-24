package com.example.ui.screens.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.domain.model.studio.VideoScene
import com.example.domain.model.studio.VideoStatus
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
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

    var showDeleteDialogFor by remember { mutableStateOf<String?>(null) }
    var sceneToEdit by remember { mutableStateOf<VideoScene?>(null) }

    Scaffold(
        topBar = {
            QabasTopBar(
                title = project?.title ?: "مخطط الفيديو",
                onBack = onBack
            )
        },
        containerColor = colors.background,
        floatingActionButton = {
            if (project?.status != VideoStatus.GENERATING && project?.status != VideoStatus.COMPLETED) {
                FloatingActionButton(
                    onClick = { viewModel.addScene() },
                    containerColor = StudioBlue,
                    contentColor = colors.surface
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة مشهد")
                }
            }
        },
        bottomBar = {
            if (project?.status != VideoStatus.GENERATING && project?.status != VideoStatus.COMPLETED) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                        .padding(QabasDimens.Space16),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QabasButton(
                        text = "حفظ المشروع",
                        onClick = {
                            viewModel.saveCurrentProject()
                        },
                        variant = QabasButtonVariant.OutlineGold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    QabasButton(
                        text = "ابدأ إنشاء الفيديو",
                        onClick = {
                            viewModel.startGeneratingVideo()
                        },
                        variant = QabasButtonVariant.PrimaryGold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (project?.status == VideoStatus.GENERATING) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text("سيتم ربط محرك التوليد لاحقاً", color = StudioBlue, style = MaterialTheme.typography.bodyLarge)
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
                    .padding(QabasDimens.Space16),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                itemsIndexed(scenes) { index, scene ->
                    SceneCard(
                        index = index + 1,
                        scene = scene,
                        onEdit = { sceneToEdit = scene },
                        onDelete = { showDeleteDialogFor = scene.id }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialogFor != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialogFor = null },
                title = { Text("حذف المشهد") },
                text = { Text("هل أنت متأكد من رغبتك في حذف هذا المشهد؟") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteScene(showDeleteDialogFor!!)
                        showDeleteDialogFor = null
                    }) {
                        Text("حذف", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialogFor = null }) {
                        Text("إلغاء", color = colors.textPrimary)
                    }
                },
                containerColor = colors.surfaceElevated,
                titleContentColor = colors.textPrimary,
                textContentColor = colors.textSecondary
            )
        }
        
        // Edit Dialog
        if (sceneToEdit != null) {
            EditSceneDialog(
                scene = sceneToEdit!!,
                onDismiss = { sceneToEdit = null },
                onSave = { updated ->
                    viewModel.updateScene(updated)
                    sceneToEdit = null
                }
            )
        }
    }
}

@Composable
fun SceneCard(index: Int, scene: VideoScene, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = QabasThemeTokens.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceElevated)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("مشهد $index", color = colors.gold, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = StudioBlue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        Text("المدة: ${scene.durationSeconds} ثانية", color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (scene.visualDescription.isNotBlank()) {
            Text("الوصف المرئي:", color = StudioBlue, style = MaterialTheme.typography.labelMedium)
            Text(scene.visualDescription, color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        if (scene.onScreenText.isNotBlank()) {
            Text("النص الظاهر:", color = StudioBlue, style = MaterialTheme.typography.labelMedium)
            Text(scene.onScreenText, color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (scene.voiceoverText.isNotBlank()) {
            Text("التعليق الصوتي:", color = StudioBlue, style = MaterialTheme.typography.labelMedium)
            Text(scene.voiceoverText, color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EditSceneDialog(scene: VideoScene, onDismiss: () -> Unit, onSave: (VideoScene) -> Unit) {
    val colors = QabasThemeTokens.colors
    var visualDescription by remember { mutableStateOf(scene.visualDescription) }
    var onScreenText by remember { mutableStateOf(scene.onScreenText) }
    var voiceoverText by remember { mutableStateOf(scene.voiceoverText) }
    var duration by remember { mutableStateOf(scene.durationSeconds.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل المشهد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("المدة (ثواني)") }
                )
                OutlinedTextField(
                    value = visualDescription,
                    onValueChange = { visualDescription = it },
                    label = { Text("الوصف المرئي") }
                )
                OutlinedTextField(
                    value = onScreenText,
                    onValueChange = { onScreenText = it },
                    label = { Text("النص الظاهر") }
                )
                OutlinedTextField(
                    value = voiceoverText,
                    onValueChange = { voiceoverText = it },
                    label = { Text("التعليق الصوتي") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updated = scene.copy(
                    visualDescription = visualDescription,
                    onScreenText = onScreenText,
                    voiceoverText = voiceoverText,
                    durationSeconds = duration.toIntOrNull() ?: 5
                )
                onSave(updated)
            }) {
                Text("حفظ", color = colors.gold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = colors.textPrimary)
            }
        },
        containerColor = colors.surfaceElevated,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textPrimary
    )
}
