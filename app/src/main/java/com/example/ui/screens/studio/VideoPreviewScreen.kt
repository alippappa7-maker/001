package com.example.ui.screens.studio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.domain.model.studio.VideoOrientation
import com.example.ui.navigation.Routes
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasGoldDark
import com.example.ui.theme.QabasGoldLight
import com.example.ui.theme.QabasSurfaceDarkElevated
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPreviewScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel
) {
    val currentProject by viewModel.currentProject.collectAsState()
    val exportNotice by viewModel.exportNotice.collectAsState()

    val scenes = currentProject?.plan?.scenes ?: emptyList()
    var currentSceneIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Auto-advance scenes during interactive preview playback
    LaunchedEffect(isPlaying, currentSceneIndex, scenes.size) {
        if (isPlaying && scenes.isNotEmpty()) {
            val scene = scenes.getOrNull(currentSceneIndex)
            val durationSecs = scene?.durationSeconds ?: 4
            delay(durationSecs * 1000L)
            if (currentSceneIndex < scenes.size - 1) {
                currentSceneIndex += 1
            } else {
                isPlaying = false
                currentSceneIndex = 0
            }
        }
    }

    val currentScene = scenes.getOrNull(currentSceneIndex)
    val orientation = currentProject?.idea?.orientation ?: VideoOrientation.PORTRAIT

    val previewAspectRatio = when (orientation) {
        VideoOrientation.PORTRAIT -> 9f / 16f
        VideoOrientation.LANDSCAPE -> 16f / 9f
        VideoOrientation.SQUARE -> 1f
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("video_preview_screen"),
            containerColor = QabasDarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "معاينة لوحة المشاهد والمخطط",
                            color = QabasGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("preview_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "الرجوع",
                                tint = QabasGold
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Routes.STUDIO_EDITOR) },
                            modifier = Modifier.testTag("preview_to_editor_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "فتح المحرر",
                                tint = QabasGold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = QabasSurfaceDarkElevated
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(QabasSurfaceDarkElevated, QabasDarkBackground, Color(0xFF04060A))
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Disclaimer Banner explaining that this is an interactive storyboard preview (no fake mp4)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("preview_notice_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x2BFFD54F)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(QabasGoldDark, QabasGold))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = QabasGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "معاينة حقيقية للوحة القصة والتسلسل الزمني والموارد. التصدير الفعلي لملف MP4 سيتاح لاحقًا.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Interactive Storyboard Preview Canvas Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth(if (orientation == VideoOrientation.PORTRAIT) 0.85f else 1f)
                        .padding(bottom = 16.dp)
                        .testTag("storyboard_canvas_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1523)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(QabasGold.copy(alpha = 0.6f), QabasGoldDark))
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(previewAspectRatio)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF1E2846), Color(0xFF0A0F1D), Color(0xFF030509))
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentScene,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "sceneTransition"
                        ) { scene ->
                            if (scene != null) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Top Badge: Scene Indicator & Duration
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(QabasGold.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "المشهد ${currentSceneIndex + 1} من ${scenes.size}",
                                                color = QabasGold,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.1f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${scene.durationSeconds} ثوانٍ",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Center Visual & On-Screen Typography
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = QabasGold.copy(alpha = 0.7f),
                                            modifier = Modifier.size(36.dp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Prominent Display Typography for On-Screen Text
                                        Text(
                                            text = scene.onScreenText.ifBlank { "نص المشهد التفاعلي" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 24.sp,
                                            modifier = Modifier.testTag("preview_scene_text")
                                        )

                                        if (!scene.attachedAssetTitle.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF2E7D32).copy(alpha = 0.35f))
                                                    .border(1.dp, Color(0xFF81C784), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "مورد مرفق: ${scene.attachedAssetTitle}",
                                                    color = Color(0xFFA5D6A7),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }

                                    // Bottom Voiceover / Transition Note
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (scene.voiceoverText.isNotBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.RecordVoiceOver,
                                                    contentDescription = null,
                                                    tint = QabasGoldLight,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = scene.voiceoverText,
                                                    color = Color.White.copy(alpha = 0.75f),
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "انتقال: ${scene.transition}",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "لا توجد مشاهد متاحة للمعاينة",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Scene Playback Controls
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("preview_controls_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2D))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentSceneIndex > 0) currentSceneIndex -= 1
                                },
                                enabled = currentSceneIndex > 0,
                                modifier = Modifier.testTag("preview_prev_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "المشهد السابق",
                                    tint = if (currentSceneIndex > 0) QabasGold else Color.White.copy(alpha = 0.2f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(QabasGold)
                                    .clickable { isPlaying = !isPlaying }
                                    .testTag("preview_play_toggle_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل المعاينة",
                                    tint = QabasDarkBackground,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (currentSceneIndex < scenes.size - 1) currentSceneIndex += 1
                                },
                                enabled = currentSceneIndex < scenes.size - 1,
                                modifier = Modifier.testTag("preview_next_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "المشهد التالي",
                                    tint = if (currentSceneIndex < scenes.size - 1) QabasGold else Color.White.copy(alpha = 0.2f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Timeline Segment Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            scenes.forEachIndexed { index, sc ->
                                Box(
                                    modifier = Modifier
                                        .weight(sc.durationSeconds.toFloat().coerceAtLeast(1f))
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (index == currentSceneIndex) QabasGold else Color.White.copy(alpha = 0.2f)
                                        )
                                        .clickable { currentSceneIndex = index }
                                )
                            }
                        }
                    }
                }

                // Main Navigation / Action Buttons
                Button(
                    onClick = { navController.navigate(Routes.STUDIO_EDITOR) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(bottom = 10.dp)
                        .testTag("preview_edit_scenes_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QabasGold, contentColor = QabasDarkBackground)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تعديل المشاهد والموارد في المحرر", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.exportCurrentVideo()
                        showExportDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 10.dp)
                        .testTag("preview_export_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QabasGoldLight),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(QabasGoldDark, QabasGold))
                    )
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تصدير الفيديو (حالة التصدير)", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Export Notice Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = {
                    showExportDialog = false
                    viewModel.clearExportNotice()
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = QabasGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير الفيديو", color = QabasGold, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = exportNotice?.message ?: "وظيفة تصدير ملف الفيديو الفعلي قيد التطوير وستتاح في تحديث قادم.",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exportNotice?.notice ?: "يمكنك حاليًا استعراض وتعديل لوحة القصة، وإدارة الموارد المرخصة عبر المسار الاحتياطي.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            viewModel.clearExportNotice()
                        },
                        modifier = Modifier.testTag("export_dialog_confirm")
                    ) {
                        Text("حسنًا", color = QabasGold, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = QabasSurfaceDarkElevated
            )
        }
    }
}
