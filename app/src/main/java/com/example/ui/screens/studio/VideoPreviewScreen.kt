@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.domain.model.studio.VideoOrientation
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.navigation.Routes
import com.example.ui.screens.studio.components.StudioGlassCard
import com.example.ui.screens.studio.components.StudioSectionHeader
import com.example.ui.screens.studio.components.StudioStatusChip
import com.example.ui.screens.studio.components.StudioIconBadge
import com.example.ui.screens.studio.components.studioBackgroundBrush
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasGoldDark
import com.example.ui.theme.QabasGoldLight
import com.example.ui.theme.QabasSurfaceDarkElevated
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPreviewScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel
) {
    val currentProject by viewModel.currentProject.collectAsState()
    val exportNotice by viewModel.exportNotice.collectAsState()
    val renderState by viewModel.renderState.collectAsState()

    val scenes = currentProject?.plan?.scenes ?: emptyList()
    var currentSceneIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // تشغيل تلقائي للتصدير الحقيقي عند دخول المعاينة، ما لم يوحد ملف جاهز
    // للمشروع الحالي بالفعل (يحميه renderVideoForPreview من التكرار).
    LaunchedEffect(currentProject?.id, currentProject?.updatedAt) {
        if (scenes.isNotEmpty()) {
            viewModel.renderVideoForPreview()
        }
    }

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
                            text = "معاينة الفيديو",
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
                    .background(studioBackgroundBrush())
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // عنوان الشاشة + وصف موجز
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudioIconBadge(
                        icon = Icons.Default.AutoAwesome,
                        size = 40.dp,
                        iconSize = 22.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "فيديو حقيقي",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يُنتج عبر محرك Media3 ويُشغّل هنا مباشرة",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                    StudioStatusChip(
                        label = when (renderState) {
                            is VideoRenderState.Ready -> "جاهز"
                            VideoRenderState.Rendering -> "قيد الإنتاج"
                            is VideoRenderState.Error -> "فشل"
                            VideoRenderState.Idle -> "بانتظار"
                        },
                        color = when (renderState) {
                            is VideoRenderState.Ready -> Color(0xFF4CAF50)
                            VideoRenderState.Rendering -> QabasGold
                            is VideoRenderState.Error -> MaterialTheme.colorScheme.error
                            VideoRenderState.Idle -> Color.White.copy(alpha = 0.5f)
                        }
                    )
                }

                // ===== بطاقة الفيديو الحقيقي (نتيجة محرك Media3) =====
                StudioGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rendered_video_card"),
                    gradientBorder = renderState is VideoRenderState.Ready,
                    cornerRadius = 22.dp,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(previewAspectRatio)
                            .background(Color(0xFF05080F)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val state = renderState) {
                            is VideoRenderState.Ready -> {
                                RenderedVideoPlayer(
                                    path = state.path,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            VideoRenderState.Rendering -> Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = QabasGold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "جارٍ إنتاج الفيديو…",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "محرك Media3 يرسم المشاهد الآن",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is VideoRenderState.Error -> Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = QabasGold,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { viewModel.renderVideoForPreview() }) {
                                    Text("إعادة المحاولة", color = QabasGold)
                                }
                            }
                            VideoRenderState.Idle -> {
                                Text(
                                    text = "سيُنتج الفيديو تلقائيًا",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // ===== معاينة المشهد (لوحة القصة التفاعلية) =====
                StudioSectionHeader(
                    title = "لوحة المشاهد",
                    subtitle = "المشهد ${currentSceneIndex + 1} من ${scenes.size} — ${currentScene?.durationSeconds ?: 0} ثانية",
                    modifier = Modifier.fillMaxWidth()
                )

                StudioGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("storyboard_canvas_card"),
                    gradientBorder = false,
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(previewAspectRatio)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF1E2846), Color(0xFF0A0F1D), Color(0xFF030509))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentScene,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "sceneTransition"
                        ) { scene ->
                            if (scene != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(QabasGold.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${currentSceneIndex + 1} / ${scenes.size}",
                                            color = QabasGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = QabasGold.copy(alpha = 0.7f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = scene.onScreenText.ifBlank { "نص المشهد" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 26.sp,
                                            modifier = Modifier.testTag("preview_scene_text")
                                        )
                                    }

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
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }
                                }
                            } else {
                                Text(
                                    text = "لا توجد مشاهد",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // ===== أدوات التحكم بالتشغيل =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentSceneIndex > 0) currentSceneIndex -= 1 },
                        enabled = currentSceneIndex > 0,
                        modifier = Modifier.testTag("preview_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "السابق",
                            tint = if (currentSceneIndex > 0) QabasGold else Color.White.copy(alpha = 0.2f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(QabasGold)
                            .clickable { isPlaying = !isPlaying }
                            .testTag("preview_play_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                            tint = QabasDarkBackground,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = { if (currentSceneIndex < scenes.size - 1) currentSceneIndex += 1 },
                        enabled = currentSceneIndex < scenes.size - 1,
                        modifier = Modifier.testTag("preview_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "التالي",
                            tint = if (currentSceneIndex < scenes.size - 1) QabasGold else Color.White.copy(alpha = 0.2f)
                        )
                    }
                }

                // الخط الزمني
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    scenes.forEachIndexed { index, sc ->
                        Box(
                            modifier = Modifier
                                .weight(sc.durationSeconds.toFloat().coerceAtLeast(1f))
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (index == currentSceneIndex) QabasGold else Color.White.copy(alpha = 0.18f)
                                )
                                .clickable { currentSceneIndex = index }
                        )
                    }
                }

                // ===== أزرار الإجراء =====
                QabasButton(
                    text = "إعادة إنتاج الفيديو",
                    onClick = {
                        viewModel.renderVideoForPreview()
                        showExportDialog = true
                    },
                    variant = QabasButtonVariant.PrimaryGold,
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preview_export_button")
                )

                QabasButton(
                    text = "تعديل المشاهد والموارد",
                    onClick = { navController.navigate(Routes.STUDIO_EDITOR) },
                    variant = QabasButtonVariant.SecondarySurface,
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preview_edit_scenes_button")
                )
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
                            text = exportNotice?.message ?: "يُنتج الاستوديو ملف MP4 محلياً حقيقياً عبر محرك Media3 ويعرضه أعلاه. يعمل بدون إنترنت.",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exportNotice?.notice ?: "يمكنك إعادة الإنتاج بعد تعديل المشاهد، أو مشاركة الفيديو الناتج لاحقاً.",
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

/**
 * مشغّل فيديو حقيقي يعتمد على ExoPlayer (Media3) — يقرأ ملف MP4 الذي أنتجه
 * محرك التصدير ويشغّله داخل التطبيق. لا محاكاة: ملف حقيقي من filesDir.
 */
@Composable
fun RenderedVideoPlayer(
    path: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    LaunchedEffect(path) {
        if (path.isNotBlank()) {
            player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(path))))
            player.prepare()
            player.playWhenReady = true
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
            }
        },
        update = { it.player = player }
    )
}
