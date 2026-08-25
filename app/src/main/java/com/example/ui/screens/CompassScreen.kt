package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.ui.screens.compass.CompassViewModel
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import kotlin.math.roundToInt

@Composable
fun CompassScreen(
    navController: NavController? = null,
    onBack: () -> Unit,
    viewModel: CompassViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors
    val context = LocalContext.current
    var showLearnMoreDialog by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (hasFine || hasCoarse) {
            viewModel.onPermissionGranted()
        } else {
            showPermissionRationale = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSensors()
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = stringResource(R.string.permission_location_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.permission_location_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                QabasButton(
                    text = stringResource(R.string.permission_grant),
                    onClick = {
                        showPermissionRationale = false
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    viewModel.onPermissionDenied()
                }) {
                    Text(stringResource(R.string.cancel), color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }

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
                        onClick = { showLearnMoreDialog = true },
                        userRotation = uiState.userAngle,
                        qiblaAngle = uiState.qiblaAngle
                    )
                }

                if (!uiState.isLocationPermissionGranted && !uiState.manualCityMode) {
                    QabasCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_compass_permission"),
                        glowAccent = colors.gold,
                        contentPadding = QabasDimens.Space16
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.gold)
                            Spacer(modifier = Modifier.height(QabasDimens.Space8))
                            Text(
                                text = stringResource(R.string.location_denied_msg),
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(QabasDimens.Space12))
                            QabasButton(
                                text = stringResource(R.string.enter_city_manually),
                                onClick = { viewModel.enableManualCityMode() }
                            )
                        }
                    }
                }

                // رسالة خطأ تهيئة (خدمات Google Play أو المستشعرات غير متوفرة)
                // تُعرض بدلاً من كراش التطبيق عند عدم توفر خدمات الموقع/البوصلة.
                uiState.initializationError?.let { errorMsg ->
                    QabasCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_compass_init_error"),
                        glowAccent = colors.gold,
                        contentPadding = QabasDimens.Space16
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = colors.gold)
                            Spacer(modifier = Modifier.height(QabasDimens.Space8))
                            Text(
                                text = errorMsg,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(QabasDimens.Space12))
                            QabasButton(
                                text = stringResource(R.string.enter_city_manually),
                                onClick = { viewModel.enableManualCityMode() }
                            )
                        }
                    }
                }

                if (uiState.qiblaAngle != null) {
                    QabasCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_compass_info"),
                        glowAccent = colors.gold,
                        contentPadding = QabasDimens.Space16
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.qibla_angle),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.gold
                                )
                                Text(
                                    text = "${uiState.qiblaAngle?.roundToInt()}°",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(QabasDimens.Space8))
                            
                            if (uiState.isCalibrating) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = colors.gold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(QabasDimens.Space8))
                                    Text(
                                        text = stringResource(id = R.string.compass_calibrating),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.gold
                                    )
                                }
                            }
                        }
                    }
                }
                
                Text(
                    text = stringResource(id = R.string.compass_magnetic_warning),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = QabasDimens.Space16)
                )

                // Placeholder for extra elements
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showLearnMoreDialog) {
        AlertDialog(
            onDismissRequest = { showLearnMoreDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.compass_learn_more),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.compass_learn_more_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showLearnMoreDialog = false }) {
                    Text("OK", color = colors.gold, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }
}
