package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasCard
import com.example.ui.components.QabasTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StarryBackground
import com.example.ui.theme.KnowledgeTurquoise
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import kotlinx.coroutines.launch

@Composable
fun KnowledgeScreen(onBack: () -> Unit) {
    val colors = QabasThemeTokens.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val inactiveSearchMsg = stringResource(id = R.string.knowledge_search_inactive_msg)

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val categories = listOf(
        stringResource(id = R.string.knowledge_cat_all),
        stringResource(id = R.string.knowledge_cat_tafsir),
        stringResource(id = R.string.knowledge_cat_heart),
        stringResource(id = R.string.knowledge_cat_ethics),
        stringResource(id = R.string.knowledge_cat_history)
    )

    Scaffold(
        modifier = Modifier.testTag("screen_knowledge"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.feature_knowledge_title),
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background
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
                    .padding(QabasDimens.Space20),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space16)
            ) {
                SectionTitle(
                    title = stringResource(id = R.string.feature_knowledge_title),
                    subtitle = stringResource(id = R.string.feature_knowledge_desc),
                    accentColor = KnowledgeTurquoise,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Visual Search Bar (Clickable preview)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(QabasDimens.Radius12))
                        .clickable {
                            scope.launch {
                                snackbarHostState.showSnackbar(inactiveSearchMsg)
                            }
                        }
                        .testTag("search_bar_knowledge"),
                    shape = RoundedCornerShape(QabasDimens.Radius12),
                    color = colors.surfaceElevated,
                    border = BorderStroke(QabasDimens.BorderThin, colors.surfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space14),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = KnowledgeTurquoise,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.knowledge_search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }

                // Categories Row (Horizontal scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                ) {
                    categories.forEachIndexed { index, categoryTitle ->
                        val isSelected = selectedCategoryIndex == index
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { selectedCategoryIndex = index }
                                .testTag("cat_chip_$index"),
                            shape = CircleShape,
                            color = if (isSelected) KnowledgeTurquoise else colors.surfaceElevated,
                            border = BorderStroke(
                                QabasDimens.BorderThin,
                                if (isSelected) KnowledgeTurquoise else colors.surfaceBorder
                            )
                        ) {
                            Text(
                                text = categoryTitle,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) colors.background else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space8)
                            )
                        }
                    }
                }

                // Demo Lesson 1
                KnowledgeLessonCard(
                    title = stringResource(id = R.string.knowledge_lesson1_title),
                    description = stringResource(id = R.string.knowledge_lesson1_desc),
                    readTime = stringResource(id = R.string.knowledge_lesson1_time),
                    testTag = "card_lesson_1"
                )

                // Demo Lesson 2
                KnowledgeLessonCard(
                    title = stringResource(id = R.string.knowledge_lesson2_title),
                    description = stringResource(id = R.string.knowledge_lesson2_desc),
                    readTime = stringResource(id = R.string.knowledge_lesson2_time),
                    testTag = "card_lesson_2"
                )

                // Demo Lesson 3
                KnowledgeLessonCard(
                    title = stringResource(id = R.string.knowledge_lesson3_title),
                    description = stringResource(id = R.string.knowledge_lesson3_desc),
                    readTime = stringResource(id = R.string.knowledge_lesson3_time),
                    testTag = "card_lesson_3"
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space8))

                QabasButton(
                    text = stringResource(id = R.string.btn_return_home),
                    onClick = onBack,
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_knowledge_return_home")
                )
            }
        }
    }
}

@Composable
private fun KnowledgeLessonCard(
    title: String,
    description: String,
    readTime: String,
    testTag: String
) {
    val colors = QabasThemeTokens.colors

    QabasCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        glowAccent = KnowledgeTurquoise,
        contentPadding = QabasDimens.Space16
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = KnowledgeTurquoise,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = readTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space8))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}
