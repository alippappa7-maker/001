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

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import com.example.domain.model.DailyPrayerTimes

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.R
import com.example.domain.model.Ayah
import com.example.domain.model.PrayerTime
import com.example.domain.model.Zikr
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QabasBottomNavigation
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasCard
import com.example.ui.components.QabasTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.NavigationManager
import com.example.ui.navigation.Routes
import com.example.ui.screens.mihrab.MihrabViewModel
import com.example.ui.theme.MihrabGreen
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MihrabScreen(
    navController: NavController? = null,
    onBack: () -> Unit,
    viewModel: MihrabViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MihrabGreen)
                }
            } else {
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

                    // Daily Progress
                    uiState.dailyProgress?.let { progress ->
                        QabasCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowAccent = colors.gold,
                            contentPadding = QabasDimens.Space16
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = R.string.daily_progress),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.gold
                                )
                                Spacer(modifier = Modifier.height(QabasDimens.Space8))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${stringResource(id = R.string.tasks_completed)}: ${progress.completedTasks}/${progress.totalTasks}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textPrimary
                                    )
                                    QabasButton(
                                        text = stringResource(id = R.string.complete_task),
                                        onClick = { viewModel.completeTask() },
                                        enabled = progress.completedTasks < progress.totalTasks
                                    )
                                }
                            }
                        }
                    }

                    // 1. Quran Card (Gold & Green Accent)
                    uiState.dailyAyah?.let { ayah ->
                        QuranCard(ayah = ayah)
                    }

                    // Search Azkar and Favorites Filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(id = R.string.search_azkar), color = colors.textSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MihrabGreen,
                                unfocusedBorderColor = colors.surfaceElevated,
                                focusedContainerColor = colors.surfaceElevated,
                                unfocusedContainerColor = colors.surfaceElevated,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(QabasDimens.Radius12),
                            singleLine = true
                        )
                        
                        IconButton(
                            onClick = { viewModel.toggleShowFavoritesOnly() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(QabasDimens.Radius12))
                                .background(if (uiState.showFavoritesOnly) colors.gold.copy(alpha = 0.2f) else colors.surfaceElevated)
                        ) {
                            Icon(
                                imageVector = if (uiState.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(id = R.string.favorites),
                                tint = if (uiState.showFavoritesOnly) colors.gold else colors.textSecondary
                            )
                        }
                    }

                    // 2. Azkar Cards
                    if (uiState.azkar.isEmpty()) {
                        EmptyStateCard(
                            title = stringResource(id = R.string.no_azkar_found),
                            description = "",
                            icon = Icons.Default.Search,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        uiState.azkar.forEach { zikr ->
                            ZikrCard(
                                zikr = zikr,
                                onFavoriteToggle = { viewModel.toggleFavorite(zikr.id) }
                            )
                        }
                    }

                    // 3. Prayer Times Card
                    if (uiState.dailyPrayerTimes != null) {
                        PrayerTimesCard(prayerTimes = uiState.dailyPrayerTimes?.toList() ?: emptyList(), nextPrayerName = uiState.nextPrayerName, nextPrayerCountdown = uiState.nextPrayerCountdown, isStale = uiState.dailyPrayerTimes?.isStale == true, isFetching = uiState.isFetchingPrayers, onRefresh = { viewModel.refreshPrayerTimes() })
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranCard(ayah: Ayah) {
    val colors = QabasThemeTokens.colors
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
                        text = ayah.textAr,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        ),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(QabasDimens.Space4))
                    Text(
                        text = "${ayah.surahNameAr} - ${ayah.ayahNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.gold
                    )
                }
            }
        }
    }
}

@Composable
private fun ZikrCard(zikr: Zikr, onFavoriteToggle: () -> Unit) {
    val colors = QabasThemeTokens.colors
    QabasCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_mihrab_dhikr_${zikr.id}"),
        glowAccent = MihrabGreen,
        contentPadding = QabasDimens.Space16
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                        text = zikr.category,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MihrabGreen
                    )
                }
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (zikr.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (zikr.isFavorite) colors.gold else colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space8))

            Text(
                text = zikr.textAr,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(QabasDimens.Space8))
            
            Text(
                text = zikr.sourceAr,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PrayerTimesCard(
    prayerTimes: List<PrayerTime>,
    nextPrayerName: String = "",
    nextPrayerCountdown: String = "",
    isStale: Boolean = false,
    isFetching: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val colors = QabasThemeTokens.colors
    QabasCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_mihrab_prayer_times"),
        glowAccent = colors.gold,
        contentPadding = QabasDimens.Space16
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = colors.gold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(QabasDimens.Space8))
                    Text(
                        text = stringResource(id = R.string.feature_mihrab_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFetching) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colors.gold,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(QabasDimens.Space8))
                    }
                    if (isStale) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Stale Data",
                            tint = androidx.compose.ui.graphics.Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(QabasDimens.Space4))
                    }
                    androidx.compose.material3.IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space16))

            if (nextPrayerName.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next: $nextPrayerName",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.gold
                    )
                    Text(
                        text = nextPrayerCountdown,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(QabasDimens.Space12))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                prayerTimes.forEach { prayer ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = prayer.nameAr,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (prayer.nameAr == nextPrayerName) colors.gold else colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(QabasDimens.Space4))
                        Text(
                            text = prayer.timeStr.split(" ")[0],
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (prayer.nameAr == nextPrayerName) colors.gold else colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerPill(
    text: String,
    isNext: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = QabasThemeTokens.colors
    val bgColor = if (isNext) colors.gold.copy(alpha = 0.2f) else colors.surfaceElevated
    val textColor = if (isNext) colors.gold else colors.textPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(QabasDimens.Radius8))
            .background(bgColor)
            .padding(horizontal = QabasDimens.Space4, vertical = QabasDimens.Space6),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
