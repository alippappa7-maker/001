package com.example.ui.screens.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.Routes
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
                title = "تحليل الفكرة",
                onBack = onBack
            )
        },
        containerColor = colors.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(QabasDimens.Space16),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QabasButton(
                    text = "إنشاء مخطط الفيديو",
                    onClick = {
                        viewModel.generatePlan()
                        navController.navigate(Routes.STUDIO_PLAN) {
                            popUpTo(Routes.STUDIO_ANALYSIS) { inclusive = true }
                        }
                    },
                    variant = QabasButtonVariant.PrimaryGold,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = plan?.missingQuestions?.isEmpty() == true
                )
                QabasButton(
                    text = "تعديل الفكرة",
                    onClick = {
                        navController.navigate(Routes.STUDIO_CREATE) {
                            popUpTo(Routes.STUDIO_ANALYSIS) { inclusive = true }
                        }
                    },
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier.fillMaxWidth()
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
                    .padding(QabasDimens.Space16),
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space16)
            ) {
                if (plan != null) {
                    AnalysisSection("الملخص", plan.summary)
                    AnalysisSection("الهدف", plan.goal)
                    AnalysisSection("الجمهور", plan.targetAudience)
                    AnalysisSection("أسلوب المونتاج المقترح", plan.suggestedEditingStyle)

                    if (plan.requiredResources.isNotEmpty()) {
                        AnalysisListSection("الموارد المطلوبة", plan.requiredResources)
                    }

                    if (plan.suggestedTexts.isNotEmpty()) {
                        AnalysisListSection("نصوص مقترحة", plan.suggestedTexts)
                    }

                    if (plan.missingQuestions.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "معلومات ناقصة",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "يرجى تعديل الفكرة للإجابة على التساؤلات التالية لتتمكن من إنشاء المخطط:",
                                    color = colors.textPrimary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                plan.missingQuestions.forEach { q ->
                                    Text("• $q", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                } else {
                    Text("لا توجد بيانات للتحليل", color = colors.textSecondary)
                }

                Spacer(modifier = Modifier.height(140.dp)) // Padding for bottom bar
            }
        }
    }
}

@Composable
fun AnalysisSection(title: String, content: String) {
    val colors = QabasThemeTokens.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceElevated)
            .padding(16.dp)
    ) {
        Text(title, color = StudioBlue, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AnalysisListSection(title: String, items: List<String>) {
    val colors = QabasThemeTokens.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceElevated)
            .padding(16.dp)
    ) {
        Text(title, color = StudioBlue, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            Text("• $item", color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
