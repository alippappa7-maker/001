package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.components.GoldenCompass
import com.example.ui.components.QabasBottomNavigation
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasCard
import com.example.ui.components.QabasTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.NavigationManager
import com.example.ui.navigation.Routes
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun CompassScreen(
    navController: NavController? = null,
    onBack: () -> Unit
) {
    val colors = QabasThemeTokens.colors
    var showLearnMoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.testTag("screen_compass"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.compass_title),
                onBack = onBack,
                actions = {
                    Surface(
                        onClick = { showLearnMoreDialog = true },
                        shape = CircleShape,
                        color = colors.surfaceElevated.copy(alpha = 0.85f),
                        border = BorderStroke(QabasDimens.BorderThin, colors.surfaceBorder),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("btn_compass_info_icon")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.compass_learn_more),
                                tint = colors.gold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (navController != null) {
                QabasBottomNavigation(
                    currentRoute = Routes.COMPASS,
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
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space20)
            ) {
                SectionTitle(
                    title = stringResource(id = R.string.compass_title),
                    subtitle = stringResource(id = R.string.compass_subtitle),
                    textAlign = TextAlign.Center,
                    accentColor = colors.gold,
                    modifier = Modifier.fillMaxWidth()
                )

                // Big Golden Compass Component
                Box(
                    modifier = Modifier.padding(vertical = QabasDimens.Space8),
                    contentAlignment = Alignment.Center
                ) {
                    GoldenCompass(
                        compassSize = 180.dp,
                        onClick = { showLearnMoreDialog = true }
                    )
                }

                // Explicit Calibrating notice
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_compass_experimental"),
                    glowAccent = colors.gold,
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.compass_experimental),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Sensor and location status card
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_compass_sensors_status"),
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SensorsOff,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(20.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.compass_sensors_status_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.gold
                            )
                        }

                        Spacer(modifier = Modifier.height(QabasDimens.Space8))

                        Text(
                            text = stringResource(id = R.string.compass_sensors_status_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                // Learn More Button
                QabasButton(
                    text = stringResource(id = R.string.compass_learn_more),
                    onClick = { showLearnMoreDialog = true },
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_compass_learn_more")
                )
            }
        }

        if (showLearnMoreDialog) {
            AlertDialog(
                onDismissRequest = { showLearnMoreDialog = false },
                title = {
                    Text(
                        text = stringResource(id = R.string.compass_dialog_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.gold
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.compass_dialog_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showLearnMoreDialog = false },
                        modifier = Modifier.testTag("btn_dialog_close")
                    ) {
                        Text(
                            text = stringResource(id = R.string.compass_dialog_close),
                            color = colors.gold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = colors.surfaceElevated,
                textContentColor = colors.textPrimary
            )
        }
    }
}
