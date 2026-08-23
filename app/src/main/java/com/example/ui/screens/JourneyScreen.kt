package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.components.QabasBottomNavigation
import com.example.ui.components.QabasCard
import com.example.ui.components.QabasTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.NavigationManager
import com.example.ui.navigation.Routes
import com.example.ui.theme.MihrabGreen
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun JourneyScreen(
    navController: NavController? = null,
    onBack: () -> Unit
) {
    val colors = QabasThemeTokens.colors

    Scaffold(
        modifier = Modifier.testTag("screen_journey"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.bottom_nav_journey),
                onBack = onBack
            )
        },
        bottomBar = {
            if (navController != null) {
                QabasBottomNavigation(
                    currentRoute = Routes.JOURNEY,
                    onNavigate = { targetRoute ->
                        NavigationManager.navigateBottomTab(navController, targetRoute)
                    }
                )
            }
        },
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
                    title = stringResource(id = R.string.card_journey_title),
                    subtitle = stringResource(id = R.string.journey_subtitle),
                    textAlign = TextAlign.Center,
                    accentColor = colors.gold,
                    modifier = Modifier.fillMaxWidth()
                )

                // Progress Circle Card
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_journey_progress"),
                    glowAccent = colors.gold,
                    contentPadding = QabasDimens.Space20
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { 0.72f },
                                modifier = Modifier.size(130.dp),
                                color = colors.gold,
                                strokeWidth = 8.dp,
                                trackColor = colors.surfaceBorder
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "72%",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.gold
                                )
                                Text(
                                    text = stringResource(id = R.string.journey_completed_today),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Summary Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
                ) {
                    QabasCard(
                        modifier = Modifier.weight(1f),
                        contentPadding = QabasDimens.Space12
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.journey_streak_days),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(QabasDimens.Space4))
                            Text(
                                text = stringResource(id = R.string.journey_streak_count),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.gold
                            )
                        }
                    }

                    QabasCard(
                        modifier = Modifier.weight(1f),
                        contentPadding = QabasDimens.Space12
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.journey_completed_steps),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(QabasDimens.Space4))
                            Text(
                                text = stringResource(id = R.string.journey_steps_count),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.gold
                            )
                        }
                    }
                }

                // Daily Tasks Cards
                // 1. Dhikr Task
                JourneyTaskCard(
                    title = stringResource(id = R.string.journey_item_dhikr_title),
                    description = stringResource(id = R.string.journey_item_dhikr_desc),
                    icon = Icons.Default.Mosque,
                    isCompleted = true,
                    testTag = "card_task_dhikr"
                )

                // 2. Quran Task
                JourneyTaskCard(
                    title = stringResource(id = R.string.journey_item_quran_title),
                    description = stringResource(id = R.string.journey_item_quran_desc),
                    icon = Icons.Default.AutoStories,
                    isCompleted = true,
                    testTag = "card_task_quran"
                )

                // 3. Prayer Task
                JourneyTaskCard(
                    title = stringResource(id = R.string.journey_item_prayer_title),
                    description = stringResource(id = R.string.journey_item_prayer_desc),
                    icon = Icons.Default.CheckCircle,
                    isCompleted = false,
                    testTag = "card_task_prayer"
                )

                // Demo Notice Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(QabasDimens.Radius12))
                        .background(colors.surfaceElevated)
                        .padding(QabasDimens.Space12),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.journey_demo_notice),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyTaskCard(
    title: String,
    description: String,
    icon: ImageVector,
    isCompleted: Boolean,
    testTag: String
) {
    val colors = QabasThemeTokens.colors

    QabasCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        glowAccent = if (isCompleted) MihrabGreen else colors.gold,
        contentPadding = QabasDimens.Space14
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(QabasDimens.Radius10))
                        .background(colors.surfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCompleted) MihrabGreen else colors.gold,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) MihrabGreen else colors.surfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.background,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
