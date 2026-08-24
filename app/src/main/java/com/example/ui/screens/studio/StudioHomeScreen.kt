package com.example.ui.screens.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoStatus
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.Routes
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import com.example.ui.theme.StudioBlue

@Composable
fun StudioHomeScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors
    val projects by viewModel.projects.collectAsState()

    Scaffold(
        topBar = {
            QabasTopBar(
                title = "الاستوديو",
                onBack = onBack
            )
        },
        containerColor = colors.background,
        modifier = Modifier.testTag("screen_studio"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createNewProject()
                    navController.navigate(Routes.STUDIO_CREATE)
                },
                containerColor = StudioBlue,
                contentColor = colors.surface
            ) {
                Icon(Icons.Default.Add, contentDescription = "أنشئ فيديو")
            }
        }
    ) { padding ->
        StarryBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(QabasDimens.Space16),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "حوّل فكرتك إلى فيديو",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.gold,
                    modifier = Modifier.padding(bottom = QabasDimens.Space24)
                )

                if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = colors.textSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(QabasDimens.Space16))
                            Text(
                                text = "لا توجد مشاريع سابقة",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
                    ) {
                        items(projects) { project ->
                            ProjectCard(
                                project = project,
                                onClick = {
                                    viewModel.loadProject(project.id)
                                    when (project.status) {
                                        VideoStatus.DRAFT -> navController.navigate(Routes.STUDIO_CREATE)
                                        VideoStatus.ANALYZING, VideoStatus.PLANNING -> navController.navigate(Routes.STUDIO_ANALYSIS)
                                        else -> navController.navigate(Routes.STUDIO_PLAN)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCard(project: VideoProject, onClick: () -> Unit) {
    val colors = QabasThemeTokens.colors
    
    val statusColor = when (project.status) {
        VideoStatus.COMPLETED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        VideoStatus.FAILED -> MaterialTheme.colorScheme.error
        VideoStatus.GENERATING -> StudioBlue
        else -> colors.gold
    }

    val statusText = when (project.status) {
        VideoStatus.DRAFT -> "مسودة"
        VideoStatus.ANALYZING -> "جاري التحليل"
        VideoStatus.PLANNING -> "قيد التخطيط"
        VideoStatus.GENERATING -> "جاري المعالجة"
        VideoStatus.COMPLETED -> "مكتمل"
        VideoStatus.FAILED -> "فشل - إعادة المحاولة"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QabasDimens.Radius12))
            .background(colors.surfaceElevated)
            .clickable { onClick() }
            .padding(QabasDimens.Space16)
    ) {
        Column {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(QabasDimens.Space8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.idea.ideaText.take(30) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (project.status == VideoStatus.FAILED) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }
        }
    }
}
