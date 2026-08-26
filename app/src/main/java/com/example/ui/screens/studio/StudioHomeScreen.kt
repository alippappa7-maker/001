package com.example.ui.screens.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
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
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoRenderStatus
import com.example.domain.model.studio.VideoStatus
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.Routes
import com.example.ui.screens.studio.components.StudioStatusChip
import com.example.ui.screens.studio.components.StudioTagPill
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import com.example.ui.theme.StudioBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioHomeScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors
    val allProjects by viewModel.projects.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    var projectToDelete by remember { mutableStateOf<VideoProject?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    val filteredProjects = remember(allProjects, selectedFilter) {
        when (selectedFilter) {
            StudioFilter.ALL -> allProjects
            StudioFilter.PROCESSING -> allProjects.filter {
                it.status == VideoStatus.GENERATING || it.renderStatus == VideoRenderStatus.PROCESSING || it.renderStatus == VideoRenderStatus.QUEUED
            }
            StudioFilter.COMPLETED -> allProjects.filter {
                it.status == VideoStatus.COMPLETED || it.renderStatus == VideoRenderStatus.COMPLETED
            }
            StudioFilter.DRAFTS_FAILED -> allProjects.filter {
                it.status == VideoStatus.DRAFT || it.status == VideoStatus.PLANNING || it.status == VideoStatus.FAILED || it.renderStatus == VideoRenderStatus.FAILED
            }
        }
    }

    Scaffold(
        topBar = {
            QabasTopBar(
                title = stringResource(R.string.feature_studio_title),
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Routes.STUDIO_STYLE_REFERENCE) },
                        modifier = Modifier.testTag("btn_style_reference")
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "أسلوب من فيديو يعجبك",
                            tint = colors.gold
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
        modifier = Modifier.testTag("screen_studio"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createNewProject()
                    navController.navigate(Routes.STUDIO_CREATE)
                },
                containerColor = colors.gold,
                contentColor = colors.surface,
                modifier = Modifier.testTag("btn_fab_create_project")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.studio_create_video_btn))
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
                contentPadding = PaddingValues(top = QabasDimens.Space16, bottom = 80.dp)
            ) {
                // Header Hero Section
                item {
                    StudioHeroHeader(
                        onCreateClick = {
                            viewModel.createNewProject()
                            navController.navigate(Routes.STUDIO_CREATE)
                        }
                    )
                }

                // Filter Tabs
                item {
                    StudioFilterTabs(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { viewModel.setFilter(it) },
                        allCount = allProjects.size,
                        processingCount = allProjects.count { it.status == VideoStatus.GENERATING || it.renderStatus == VideoRenderStatus.PROCESSING },
                        completedCount = allProjects.count { it.status == VideoStatus.COMPLETED },
                        draftsCount = allProjects.count { it.status == VideoStatus.DRAFT || it.status == VideoStatus.PLANNING || it.status == VideoStatus.FAILED }
                    )
                }

                // Content List or Empty State
                if (filteredProjects.isEmpty()) {
                    item {
                        StudioEmptyState(
                            filter = selectedFilter,
                            onCreateClick = {
                                viewModel.createNewProject()
                                navController.navigate(Routes.STUDIO_CREATE)
                            }
                        )
                    }
                } else {
                    items(filteredProjects, key = { it.id }) { project ->
                        StudioProjectCard(
                            project = project,
                            onClick = {
                                viewModel.loadProject(project.id)
                                when (project.status) {
                                    VideoStatus.DRAFT -> navController.navigate(Routes.STUDIO_CREATE)
                                    VideoStatus.ANALYZING -> navController.navigate(Routes.STUDIO_ANALYSIS)
                                    VideoStatus.PLANNING -> navController.navigate(Routes.STUDIO_PLAN)
                                    VideoStatus.GENERATING -> navController.navigate(Routes.STUDIO_PREVIEW)
                                    VideoStatus.COMPLETED -> navController.navigate(Routes.STUDIO_PREVIEW)
                                    else -> navController.navigate(Routes.STUDIO_PLAN)
                                }
                            },
                            onRetry = {
                                viewModel.retryProject(project.id)
                            },
                            onDelete = {
                                projectToDelete = project
                            }
                        )
                    }
                }

                // Bottom Notice
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = QabasDimens.Space8)
                            .clip(RoundedCornerShape(QabasDimens.Radius8))
                            .background(colors.surfaceElevated.copy(alpha = 0.5f))
                            .padding(QabasDimens.Space12),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.studio_local_storage_notice),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        projectToDelete?.let { project ->
            AlertDialog(
                onDismissRequest = { projectToDelete = null },
                title = {
                    Text(
                        text = stringResource(R.string.studio_delete_confirm_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.studio_delete_confirm_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteProject(project.id)
                            projectToDelete = null
                        }
                    ) {
                        Text(
                            text = "تأكيد الحذف",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { projectToDelete = null }) {
                        Text(text = stringResource(R.string.cancel), color = colors.textPrimary)
                    }
                },
                containerColor = colors.surfaceElevated,
                shape = RoundedCornerShape(QabasDimens.Radius16)
            )
        }
    }
}

@Composable
private fun StudioHeroHeader(onCreateClick: () -> Unit) {
    val colors = QabasThemeTokens.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QabasDimens.Radius16))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.gold.copy(alpha = 0.3f), RoundedCornerShape(QabasDimens.Radius16))
            .padding(QabasDimens.Space20)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(StudioBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Videocam,
                        contentDescription = null,
                        tint = StudioBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(QabasDimens.Space12))
                Column {
                    Text(
                        text = stringResource(R.string.feature_studio_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.gold
                    )
                    Text(
                        text = stringResource(R.string.feature_studio_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space16))

            QabasButton(
                text = stringResource(R.string.studio_create_video_btn),
                onClick = onCreateClick,
                variant = QabasButtonVariant.PrimaryGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_create_video_hero")
            )
        }
    }
}

@Composable
private fun StudioFilterTabs(
    selectedFilter: StudioFilter,
    onFilterSelected: (StudioFilter) -> Unit,
    allCount: Int,
    processingCount: Int,
    completedCount: Int,
    draftsCount: Int
) {
    val colors = QabasThemeTokens.colors

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
    ) {
        FilterTabChip(
            label = "${stringResource(R.string.studio_tab_all)} ($allCount)",
            selected = selectedFilter == StudioFilter.ALL,
            onClick = { onFilterSelected(StudioFilter.ALL) },
            modifier = Modifier.weight(1f).testTag("tab_filter_all")
        )
        FilterTabChip(
            label = "${stringResource(R.string.studio_tab_processing)} ($processingCount)",
            selected = selectedFilter == StudioFilter.PROCESSING,
            onClick = { onFilterSelected(StudioFilter.PROCESSING) },
            modifier = Modifier.weight(1.1f).testTag("tab_filter_processing")
        )
        FilterTabChip(
            label = "${stringResource(R.string.studio_tab_completed)} ($completedCount)",
            selected = selectedFilter == StudioFilter.COMPLETED,
            onClick = { onFilterSelected(StudioFilter.COMPLETED) },
            modifier = Modifier.weight(1f).testTag("tab_filter_completed")
        )
        FilterTabChip(
            label = "${stringResource(R.string.studio_tab_drafts)} ($draftsCount)",
            selected = selectedFilter == StudioFilter.DRAFTS_FAILED,
            onClick = { onFilterSelected(StudioFilter.DRAFTS_FAILED) },
            modifier = Modifier.weight(1.2f).testTag("tab_filter_drafts")
        )
    }
}

@Composable
private fun FilterTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QabasThemeTokens.colors
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clip(RoundedCornerShape(QabasDimens.Radius20))
            .background(if (selected) colors.gold.copy(alpha = 0.2f) else colors.surfaceElevated)
            .border(
                1.dp,
                if (selected) colors.gold else colors.surfaceElevated,
                RoundedCornerShape(QabasDimens.Radius20)
            )
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (selected) colors.gold else colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StudioProjectCard(
    project: VideoProject,
    onClick: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = QabasThemeTokens.colors

    val statusColor = when (project.status) {
        VideoStatus.COMPLETED -> Color(0xFF4CAF50)
        VideoStatus.FAILED -> MaterialTheme.colorScheme.error
        VideoStatus.CANCELLED -> Color(0xFFFF9800)
        VideoStatus.GENERATING -> StudioBlue
        VideoStatus.PLANNING -> colors.gold
        VideoStatus.ANALYZING -> colors.gold
        VideoStatus.DRAFT -> colors.textSecondary
    }

    val statusLabel = when (project.status) {
        VideoStatus.DRAFT -> stringResource(R.string.studio_status_draft)
        VideoStatus.ANALYZING -> "تحليل الفكرة"
        VideoStatus.PLANNING -> stringResource(R.string.studio_status_planning)
        VideoStatus.GENERATING -> stringResource(R.string.studio_status_processing)
        VideoStatus.COMPLETED -> stringResource(R.string.studio_status_completed)
        VideoStatus.FAILED -> stringResource(R.string.studio_status_failed)
        VideoStatus.CANCELLED -> "ملغي"
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QabasDimens.Radius12))
            .background(colors.surfaceElevated)
            .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(QabasDimens.Radius12))
            .clickable { onClick() }
            .padding(QabasDimens.Space16)
            .testTag("card_project_${project.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                StudioStatusChip(
                    label = statusLabel,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space8))

            if (project.idea.ideaText.isNotBlank()) {
                Text(
                    text = project.idea.ideaText,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(QabasDimens.Space8))
            }

            // Tags row: Duration, Orientation, Tone, Editing style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioTagPill(text = "${project.idea.duration.seconds}ث")
                StudioTagPill(text = project.idea.orientation.aspectRatioText)
                StudioTagPill(text = project.idea.tone.titleAr)
                StudioTagPill(text = project.idea.editingStyle.titleAr)

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف المشروع",
                        tint = colors.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // If Failed or In Progress with engine notice
            if (project.status == VideoStatus.FAILED) {
                Spacer(modifier = Modifier.height(QabasDimens.Space8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = project.errorMessage ?: "حدث خطأ أثناء المعالجة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    TextButton(
                        onClick = onRetry,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = colors.gold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.studio_retry_btn),
                            color = colors.gold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else if (project.status == VideoStatus.GENERATING) {
                Spacer(modifier = Modifier.height(QabasDimens.Space8))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioBlue.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = StudioBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.studio_engine_notice),
                        style = MaterialTheme.typography.labelSmall,
                        color = StudioBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioEmptyState(
    filter: StudioFilter,
    onCreateClick: () -> Unit
) {
    val colors = QabasThemeTokens.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QabasDimens.Radius16))
            .background(colors.surfaceElevated)
            .padding(QabasDimens.Space24),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.gold.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = colors.gold,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space16))

            Text(
                text = when (filter) {
                    StudioFilter.ALL -> stringResource(R.string.studio_empty_title)
                    StudioFilter.PROCESSING -> "لا توجد مشاريع قيد المعالجة"
                    StudioFilter.COMPLETED -> "لا توجد مشاريع مكتملة بعد"
                    StudioFilter.DRAFTS_FAILED -> "لا توجد مسودات أو مشاريع فاشلة"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(QabasDimens.Space8))

            Text(
                text = stringResource(R.string.studio_empty_desc),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = QabasDimens.Space8)
            )

            Spacer(modifier = Modifier.height(QabasDimens.Space16))

            QabasButton(
                text = stringResource(R.string.studio_empty_action),
                onClick = onCreateClick,
                variant = QabasButtonVariant.OutlineGold
            )
        }
    }
}
