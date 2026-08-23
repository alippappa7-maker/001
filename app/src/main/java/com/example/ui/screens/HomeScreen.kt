package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.components.GlowingOrb
import com.example.ui.components.GoldenCompass
import com.example.ui.components.QabasBottomNavigation
import com.example.ui.components.QabasCard
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.NavigationManager
import com.example.ui.navigation.Routes
import com.example.ui.theme.CompanionPurple
import com.example.ui.theme.ImpactOliveGold
import com.example.ui.theme.KnowledgeTurquoise
import com.example.ui.theme.MihrabGreen
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasThemeTokens
import com.example.ui.theme.StudioBlue
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val colors = QabasThemeTokens.colors

    Scaffold(
        modifier = Modifier.testTag("screen_home"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HomeTopBar(
                onNotificationClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.notifications_later))
                    }
                },
                onProfileClick = {
                    NavigationManager.navigateSingleTop(navController, Routes.PROFILE)
                }
            )
        },
        bottomBar = {
            QabasBottomNavigation(
                currentRoute = Routes.HOME,
                onNavigate = { targetRoute ->
                    NavigationManager.navigateBottomTab(navController, targetRoute)
                }
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        StarryBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = QabasDimens.Space24)
            ) {
                item {
                    Spacer(modifier = Modifier.height(QabasDimens.Space6))
                    HomeHeaderGreeting()
                    Spacer(modifier = Modifier.height(QabasDimens.Space16))
                }

                item {
                    OrbitalSystem(navController = navController)
                    Spacer(modifier = Modifier.height(QabasDimens.Space24))
                }

                item {
                    BottomCardsSection(navController = navController)
                    Spacer(modifier = Modifier.height(QabasDimens.Space16))
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val colors = QabasThemeTokens.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space8),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Notification Button
        Surface(
            onClick = onNotificationClick,
            shape = CircleShape,
            color = colors.surfaceElevated.copy(alpha = 0.85f),
            border = BorderStroke(QabasDimens.BorderThin, colors.surfaceBorder),
            modifier = Modifier
                .size(44.dp)
                .testTag("btn_notifications")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(id = R.string.notifications_title),
                    tint = colors.gold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Center Brand Identity
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = colors.gold
            )
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                colors.gold,
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Profile Button
        Surface(
            onClick = onProfileClick,
            shape = CircleShape,
            color = colors.surfaceElevated.copy(alpha = 0.85f),
            border = BorderStroke(QabasDimens.BorderThin, colors.surfaceBorder),
            modifier = Modifier
                .size(44.dp)
                .testTag("btn_profile")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(id = R.string.bottom_nav_profile),
                    tint = colors.gold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeHeaderGreeting() {
    val colors = QabasThemeTokens.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = QabasDimens.Space24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.home_greeting_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(QabasDimens.Space2))
        Text(
            text = stringResource(id = R.string.home_greeting_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OrbitalSystem(navController: NavController) {
    val colors = QabasThemeTokens.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = QabasDimens.Space8),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val containerSize = availableWidth.coerceAtMost(380.dp)
        val compassSize = (containerSize * 0.32f).coerceIn(100.dp, 126.dp)
        val orbitRadius = (containerSize * 0.37f).coerceIn(115.dp, 142.dp)
        val orbSize = (containerSize * 0.155f).coerceIn(52.dp, 60.dp)

        val infiniteTransition = rememberInfiniteTransition(label = "orbital")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(90000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        Box(
            modifier = Modifier
                .size(containerSize)
                .padding(QabasDimens.Space4),
            contentAlignment = Alignment.Center
        ) {
            // Astronomical Orbits Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val orbitRadiusPx = orbitRadius.toPx()

                // Ambient Radial Glow behind compass
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.gold.copy(alpha = 0.18f),
                            Color(0xFF0F2238).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = orbitRadiusPx * 1.35f
                    ),
                    radius = orbitRadiusPx * 1.35f,
                    center = center
                )

                // Outer decorative dashed orbit
                drawCircle(
                    color = colors.gold.copy(alpha = 0.18f),
                    radius = orbitRadiusPx * 1.15f,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 16f), 0f)
                    )
                )

                // Main Orbital Path (where the 5 planets travel)
                drawCircle(
                    color = colors.gold.copy(alpha = 0.35f),
                    radius = orbitRadiusPx,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Inner orbit
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.22f),
                    radius = orbitRadiusPx * 0.72f,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f)
                    )
                )
            }

            // Central Fixed Golden Compass
            GoldenCompass(
                onClick = {
                    NavigationManager.navigateSingleTop(navController, Routes.COMPASS)
                },
                compassSize = compassSize,
                showAuraGlow = false,
                showOrbitalRing = false
            )

            // The 5 Feature Orbs
            val orbs = listOf(
                OrbData("01", R.string.feature_studio_title, R.drawable.qabas_orb_studio_1787516791381, StudioBlue, Routes.STUDIO, "orb_studio"),
                OrbData("02", R.string.feature_companion_title, R.drawable.qabas_orb_companion_1787516801929, CompanionPurple, Routes.COMPANION, "orb_companion"),
                OrbData("03", R.string.feature_mihrab_title, R.drawable.qabas_orb_mihrab_1787516818194, MihrabGreen, Routes.MIHRAB, "orb_mihrab"),
                OrbData("04", R.string.feature_knowledge_title, R.drawable.qabas_orb_knowledge_1787516830952, KnowledgeTurquoise, Routes.KNOWLEDGE, "orb_knowledge"),
                OrbData("05", R.string.feature_impact_title, R.drawable.qabas_orb_impact_1787516843202, ImpactOliveGold, Routes.IMPACT, "orb_impact")
            )

            orbs.forEachIndexed { index, orbData ->
                val angle = (index * (360f / orbs.size)) + rotation - 90f
                val angleRad = Math.toRadians(angle.toDouble())

                val offsetX = (orbitRadius.value * cos(angleRad)).toFloat()
                val offsetY = (orbitRadius.value * sin(angleRad)).toFloat()

                Box(
                    modifier = Modifier
                        .offset(x = offsetX.dp, y = offsetY.dp)
                        .testTag(orbData.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    GlowingOrb(
                        title = stringResource(id = orbData.titleRes),
                        imagePainter = painterResource(id = orbData.imageRes),
                        glowColor = orbData.color,
                        numberBadge = orbData.number,
                        orbSize = orbSize,
                        onClick = {
                            NavigationManager.navigateSingleTop(navController, orbData.route)
                        }
                    )
                }
            }
        }
    }
}

data class OrbData(
    val number: String,
    val titleRes: Int,
    val imageRes: Int,
    val color: Color,
    val route: String,
    val testTag: String
)

@Composable
fun BottomCardsSection(navController: NavController) {
    val colors = QabasThemeTokens.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = QabasDimens.Space16),
        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
    ) {
        // 1. Compass Status Card
        InfoCard(
            modifier = Modifier
                .weight(1f)
                .testTag("card_compass_quick"),
            label = stringResource(id = R.string.compass_title),
            value = stringResource(id = R.string.card_compass_status),
            accentColor = colors.gold,
            onClick = {
                NavigationManager.navigateSingleTop(navController, Routes.COMPASS)
            }
        )

        // 2. Journey Progress Card
        InfoCard(
            modifier = Modifier
                .weight(1f)
                .testTag("card_journey_quick"),
            label = stringResource(id = R.string.card_journey_title),
            value = "72%",
            showProgressBar = true,
            progress = 0.72f,
            accentColor = colors.gold,
            onClick = {
                NavigationManager.navigateSingleTop(navController, Routes.JOURNEY)
            }
        )

        // 3. Dhikr Card
        InfoCard(
            modifier = Modifier
                .weight(1f)
                .testTag("card_mihrab_quick"),
            label = stringResource(id = R.string.card_dhikr_title),
            value = stringResource(id = R.string.card_dhikr_content),
            accentColor = colors.gold,
            onClick = {
                NavigationManager.navigateSingleTop(navController, Routes.MIHRAB)
            }
        )
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    showProgressBar: Boolean = false,
    progress: Float = 0f,
    accentColor: Color = QabasGold,
    onClick: () -> Unit
) {
    val colors = QabasThemeTokens.colors

    QabasCard(
        modifier = modifier.defaultMinSize(minHeight = 94.dp),
        onClick = onClick,
        shape = RoundedCornerShape(QabasDimens.RadiusMedium),
        contentPadding = QabasDimens.Space8
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (showProgressBar) {
                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(QabasDimens.Space4))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(QabasDimens.RadiusXS)),
                        color = accentColor,
                        trackColor = colors.surfaceBorder
                    )
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
