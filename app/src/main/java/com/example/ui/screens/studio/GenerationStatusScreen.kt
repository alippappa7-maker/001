package com.example.ui.screens.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.domain.model.studio.GenerationStage
import com.example.domain.model.studio.VideoRenderStatus
import com.example.ui.navigation.Routes
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasGoldDark
import com.example.ui.theme.QabasGoldLight
import com.example.ui.theme.QabasSurfaceDarkElevated
import com.example.ui.theme.StudioBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerationStatusScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel
) {
    val currentProject by viewModel.currentProject.collectAsState()
    val scrollState = rememberScrollState()

    val job = currentProject?.currentJob
    val stage = currentProject?.generationStage ?: GenerationStage.IDLE
    val progress = job?.progressPercent ?: 0
    val isRunning = job?.status == VideoRenderStatus.PROCESSING || currentProject?.renderStatus == VideoRenderStatus.PROCESSING
    val isFailed = job?.status == VideoRenderStatus.FAILED || currentProject?.renderStatus == VideoRenderStatus.FAILED || stage == GenerationStage.FAILED
    val isCancelled = job?.status == VideoRenderStatus.CANCELLED || currentProject?.renderStatus == VideoRenderStatus.CANCELLED || stage == GenerationStage.CANCELLED
    val isCompleted = job?.status == VideoRenderStatus.COMPLETED || currentProject?.renderStatus == VideoRenderStatus.COMPLETED || stage == GenerationStage.COMPLETED

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("generation_status_screen"),
            containerColor = QabasDarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "حالة معالجة وتوليد الفيديو",
                            color = QabasGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("status_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "الرجوع",
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
                            colors = listOf(QabasSurfaceDarkElevated, QabasDarkBackground, Color(0xFF030508))
                        )
                    )
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Notice Card: Honest disclaimer explaining simulation/mock nature
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("mock_disclaimer_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x2BFFD54F)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(QabasGoldDark, QabasGold))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = QabasGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تنبيه الشفافية والربط السحابي",
                                color = QabasGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "التوليد الحقيقي للوسائط غير مربوط بعد؛ يتم استخدام محاكاة متكاملة لحالات المعالجة وتجهيز لوحة القصة والمشاهد محليًا.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Main Status & Visual Pulse Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("progress_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF101726)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(
                            listOf(QabasGold.copy(alpha = 0.6f), Color.Transparent, QabasGoldDark.copy(alpha = 0.3f))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Animated Hero Icon with cosmic spiritual glow
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(if (isRunning) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = when {
                                            isCompleted -> listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
                                            isFailed -> listOf(Color(0xFFC62828), Color(0xFF8E0000))
                                            isCancelled -> listOf(Color(0xFFE65100), Color(0xFFB26A00))
                                            else -> listOf(QabasGold.copy(alpha = 0.4f), StudioBlue.copy(alpha = 0.2f))
                                        }
                                    )
                                )
                                .border(
                                    2.dp,
                                    brush = Brush.linearGradient(listOf(QabasGold, QabasGoldLight)),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isCompleted -> Icons.Default.CheckCircle
                                    isFailed -> Icons.Default.Error
                                    isCancelled -> Icons.Default.Close
                                    else -> Icons.Default.AutoAwesome
                                },
                                contentDescription = null,
                                tint = when {
                                    isCompleted -> Color(0xFFA5D6A7)
                                    isFailed -> Color(0xFFFFCDD2)
                                    isCancelled -> Color(0xFFFFE082)
                                    else -> QabasGold
                                },
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stage Title
                        Text(
                            text = stage.titleAr,
                            color = when {
                                isCompleted -> Color(0xFF81C784)
                                isFailed -> Color(0xFFE57373)
                                isCancelled -> Color(0xFFFFB74D)
                                else -> QabasGold
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("stage_title_text")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Stage Subtitle / Description
                        Text(
                            text = stage.descriptionAr,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Bar & Percentage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نسبة الإنجاز",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$progress%",
                                color = QabasGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.testTag("progress_percent_text")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (isCompleted) Color(0xFF4CAF50) else QabasGold,
                            trackColor = Color(0xFF1E283C)
                        )

                        if (job?.message?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = job.message,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Stage Steps Timeline Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("stage_timeline_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF121928)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "مراحل المعالجة والتوليد",
                            color = QabasGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val pipelineStages = listOf(
                            GenerationStage.ANALYZING,
                            GenerationStage.PLANNING,
                            GenerationStage.GENERATING,
                            GenerationStage.RENDERING,
                            GenerationStage.COMPLETED
                        )

                        pipelineStages.forEachIndexed { index, pipelineStage ->
                            val isStageDone = stage.ordinal > pipelineStage.ordinal || (isCompleted && pipelineStage == GenerationStage.COMPLETED)
                            val isStageCurrent = stage == pipelineStage

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isStageDone -> Color(0xFF2E7D32)
                                                isStageCurrent -> QabasGold
                                                else -> Color.White.copy(alpha = 0.1f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isStageDone) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else if (isStageCurrent && isRunning) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = QabasDarkBackground
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            color = if (isStageCurrent) QabasDarkBackground else Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pipelineStage.titleAr,
                                        color = if (isStageCurrent || isStageDone) Color.White else Color.White.copy(alpha = 0.4f),
                                        fontWeight = if (isStageCurrent) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = pipelineStage.descriptionAr,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons (Cancel, Retry, Preview, Editor)
                if (isRunning) {
                    OutlinedButton(
                        onClick = { viewModel.cancelActiveGeneration() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cancel_generation_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF8A80)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إلغاء عملية التوليد", fontWeight = FontWeight.Bold)
                    }
                } else if (isFailed || isCancelled) {
                    Button(
                        onClick = { viewModel.retryActiveGeneration() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("retry_generation_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = QabasGold,
                            contentColor = QabasDarkBackground
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعادة المحاولة", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation to Preview and Editor
                Button(
                    onClick = { navController.navigate(Routes.STUDIO_PREVIEW) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("to_preview_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QabasGold,
                        contentColor = QabasDarkBackground
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("معاينة لوحة المشاهد والمخطط", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { navController.navigate(Routes.STUDIO_EDITOR) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("to_editor_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = QabasGoldLight
                    )
                ) {
                    Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فتح محرر المشاهد وإدارة الموارد", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
