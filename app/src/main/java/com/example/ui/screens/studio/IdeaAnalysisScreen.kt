package com.example.ui.screens.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.R
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.Routes
import com.example.ui.screens.studio.components.StudioGlassCard
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import com.example.ui.theme.StudioBlue

@Composable
fun IdeaAnalysisScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors
    val project by viewModel.currentProject.collectAsState()
    val plan = project?.plan

    Scaffold(
        topBar = {
            QabasTopBar(
                title = stringResource(R.string.studio_analysis_title),
                onBack = onBack
            )
        },
        containerColor = colors.background,
        modifier = Modifier.testTag("screen_studio_analysis"),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space12),
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
            ) {
                QabasButton(
                    text = stringResource(R.string.studio_btn_create_plan),
                    onClick = {
                        viewModel.generatePlan()
                        navController.navigate(Routes.STUDIO_PLAN)
                    },
                    variant = QabasButtonVariant.PrimaryGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_create_video_plan")
                )

                QabasButton(
                    text = stringResource(R.string.studio_btn_edit_idea),
                    onClick = {
                        navController.popBackStack()
                    },
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_edit_idea")
                )
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space12),
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space16)
            ) {
                if (plan != null) {
                    // 1. Idea Summary
                    AnalysisCard(
                        title = stringResource(R.string.studio_analysis_summary_label),
                        icon = Icons.Default.FormatQuote
                    ) {
                        Text(
                            text = plan.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            lineHeight = 22.sp
                        )
                    }

                    // 2. Goal and Impact
                    AnalysisCard(
                        title = stringResource(R.string.studio_analysis_goal_label),
                        icon = Icons.Default.Lightbulb
                    ) {
                        Text(
                            text = plan.goal,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            lineHeight = 22.sp
                        )
                    }

                    // 3. Key Specifications Grid (Audience, Format/Duration, Style, Scene Count)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                    ) {
                        SpecMiniCard(
                            label = stringResource(R.string.studio_analysis_audience_label),
                            value = plan.targetAudience,
                            modifier = Modifier.weight(1f)
                        )
                        SpecMiniCard(
                            label = stringResource(R.string.studio_analysis_format_label),
                            value = "${plan.durationSeconds}ث • ${plan.orientation.aspectRatioText}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                    ) {
                        SpecMiniCard(
                            label = stringResource(R.string.studio_analysis_style_label),
                            value = plan.suggestedEditingStyle,
                            modifier = Modifier.weight(1f)
                        )
                        SpecMiniCard(
                            label = stringResource(R.string.studio_analysis_scenes_count_label),
                            value = "${plan.sceneCount} مشاهد",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 4. Required Resources
                    if (plan.requiredResources.isNotEmpty()) {
                        AnalysisCard(
                            title = stringResource(R.string.studio_analysis_resources_label),
                            icon = Icons.Default.MovieCreation
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                plan.requiredResources.forEach { resource ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(colors.gold)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = resource,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Suggested On-Screen Texts
                    if (plan.suggestedTexts.isNotEmpty()) {
                        AnalysisCard(
                            title = stringResource(R.string.studio_analysis_suggested_texts_label),
                            icon = Icons.Default.FormatQuote
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                plan.suggestedTexts.forEach { textItem ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(QabasDimens.Radius8))
                                            .background(colors.background.copy(alpha = 0.5f))
                                            .border(1.dp, StudioBlue.copy(alpha = 0.2f), RoundedCornerShape(QabasDimens.Radius8))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = textItem,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = StudioBlue
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6. Missing Questions / Clarifications Section
                    if (plan.missingQuestions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QabasDimens.Radius12))
                                .background(colors.surfaceElevated)
                                .border(1.dp, colors.gold.copy(alpha = 0.6f), RoundedCornerShape(QabasDimens.Radius12))
                                .padding(QabasDimens.Space16)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = null,
                                        tint = colors.gold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.studio_analysis_missing_questions_label),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.gold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = stringResource(R.string.studio_analysis_incomplete_notice),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                plan.missingQuestions.forEach { question ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "• ",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.gold
                                        )
                                        Text(
                                            text = question,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Complete Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QabasDimens.Radius12))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f), RoundedCornerShape(QabasDimens.Radius12))
                                .padding(QabasDimens.Space12),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.studio_analysis_complete_badge),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(QabasDimens.Space24))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(QabasDimens.Space32),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "يرجى كتابة فكرة الفيديو وتحليلها للمتابعة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    StudioGlassCard(
        modifier = Modifier.fillMaxWidth(),
        gradientBorder = false,
        cornerRadius = QabasDimens.Radius12,
        contentPadding = PaddingValues(QabasDimens.Space16)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = StudioBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = StudioBlue
            )
        }
        Spacer(modifier = Modifier.height(QabasDimens.Space10))
        content()
    }
}

@Composable
private fun SpecMiniCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = QabasThemeTokens.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(QabasDimens.Radius10))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.gold.copy(alpha = 0.12f), RoundedCornerShape(QabasDimens.Radius10))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = colors.gold,
                maxLines = 1
            )
        }
    }
}
