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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun MihrabScreen(
    navController: NavController? = null,
    onBack: () -> Unit
) {
    val colors = QabasThemeTokens.colors

    Scaffold(
        modifier = Modifier.testTag("screen_mihrab"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.feature_mihrab_title),
                onBack = onBack
            )
        },
        bottomBar = {
            if (navController != null) {
                QabasBottomNavigation(
                    currentRoute = Routes.MIHRAB,
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
                    title = stringResource(id = R.string.feature_mihrab_title),
                    subtitle = stringResource(id = R.string.feature_mihrab_desc),
                    accentColor = MihrabGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Demo Data Badge Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(QabasDimens.RadiusFull))
                        .background(MihrabGreen.copy(alpha = 0.15f))
                        .padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space6)
                ) {
                    Text(
                        text = stringResource(id = R.string.demo_data_badge),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MihrabGreen
                    )
                }

                // 1. Quran Card (Gold & Green Accent)
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_mihrab_quran"),
                    glowAccent = colors.gold,
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(id = R.string.mihrab_quran_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.gold
                                )
                                Text(
                                    text = stringResource(id = R.string.mihrab_quran_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(QabasDimens.Space12))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QabasDimens.Radius12))
                                .background(colors.surfaceElevated)
                                .padding(QabasDimens.Space12),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(id = R.string.mihrab_quran_sample_verse),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 28.sp
                                    ),
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(QabasDimens.Space4))
                                Text(
                                    text = stringResource(id = R.string.mihrab_quran_sample_source),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.gold
                                )
                            }
                        }
                    }
                }

                // 2. Dhikr Card (Green Theme)
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_mihrab_dhikr"),
                    glowAccent = MihrabGreen,
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = null,
                                tint = MihrabGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.mihrab_dhikr_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MihrabGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(QabasDimens.Space12))

                        Text(
                            text = stringResource(id = R.string.mihrab_dhikr_sample),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 3. Prayer Times Card
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_mihrab_prayer_times"),
                    glowAccent = colors.gold,
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.mihrab_prayer_times_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.gold
                            )
                        }

                        Spacer(modifier = Modifier.height(QabasDimens.Space12))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                        ) {
                            PrayerPill(
                                text = stringResource(id = R.string.mihrab_fajr),
                                modifier = Modifier.weight(1f)
                            )
                            PrayerPill(
                                text = stringResource(id = R.string.mihrab_dhuhr),
                                modifier = Modifier.weight(1f)
                            )
                            PrayerPill(
                                text = stringResource(id = R.string.mihrab_asr),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(QabasDimens.Space8))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                        ) {
                            PrayerPill(
                                text = stringResource(id = R.string.mihrab_maghrib),
                                modifier = Modifier.weight(1f)
                            )
                            PrayerPill(
                                text = stringResource(id = R.string.mihrab_isha),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. Qibla Card
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_mihrab_qibla"),
                    glowAccent = MihrabGreen,
                    contentPadding = QabasDimens.Space16
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MihrabGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(id = R.string.mihrab_qibla_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MihrabGreen
                            )
                            Text(
                                text = stringResource(id = R.string.mihrab_qibla_status),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerPill(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = QabasThemeTokens.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(QabasDimens.Radius8))
            .background(colors.surfaceElevated)
            .padding(horizontal = QabasDimens.Space8, vertical = QabasDimens.Space6),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
