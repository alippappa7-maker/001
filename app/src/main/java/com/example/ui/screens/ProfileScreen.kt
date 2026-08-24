package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.model.SyncStatus
import com.example.ui.AuthViewModel
import com.example.ui.SettingsViewModel
import com.example.ui.components.AuthDialog
import com.example.ui.components.DeleteAccountDialog
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasCard
import com.example.ui.components.QabasTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StarryBackground
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()

    val authState by authViewModel.uiState.collectAsState()
    var isAuthDialogOpen by remember { mutableStateOf(false) }
    var isDeleteDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(authState.errorMessage, authState.successMessage) {
        authState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
        }
        authState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
            isAuthDialogOpen = false
        }
    }

    Scaffold(
        modifier = Modifier.testTag("screen_profile"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.bottom_nav_profile),
                onBack = onBack,
                actions = {
                    Surface(
                        onClick = onNavigateToSettings,
                        shape = CircleShape,
                        color = colors.surfaceElevated.copy(alpha = 0.85f),
                        border = BorderStroke(QabasDimens.BorderThin, colors.surfaceBorder),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("btn_profile_settings_icon")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.profile_settings_title),
                                tint = colors.gold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
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
                // Profile Avatar & Account Info
                val user = authState.user
                val isAuthenticated = user != null

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .border(BorderStroke(QabasDimens.BorderRegular, colors.gold), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAuthenticated && !user.displayName.isNullOrBlank()) {
                            Text(
                                text = user.displayName.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.gold
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.avatar_description),
                                tint = colors.gold,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(QabasDimens.Space10))

                    Text(
                        text = if (isAuthenticated) {
                            user.displayName?.ifBlank { null } ?: user.email ?: stringResource(id = R.string.profile_guest_name)
                        } else {
                            stringResource(id = R.string.profile_guest_name)
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(QabasDimens.Space4))

                    Text(
                        text = if (isAuthenticated) {
                            user.email ?: stringResource(id = R.string.auth_sync_status_connected)
                        } else {
                            stringResource(id = R.string.auth_guest_badge)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }

                // Cloud Sync Card (Active sync when logged in / Informational when guest)
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_cloud_sync"),
                    glowAccent = if (isAuthenticated) colors.gold else null,
                    contentPadding = QabasDimens.Space14
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10),
                            modifier = Modifier.weight(1f)
                        ) {
                            val icon = when {
                                !isAuthenticated -> Icons.Default.CloudOff
                                authState.syncStatus == SyncStatus.SYNCING -> Icons.Default.CloudSync
                                authState.syncStatus == SyncStatus.SYNCED -> Icons.Default.CloudDone
                                else -> Icons.Default.CloudSync
                            }

                            val iconTint = when {
                                !isAuthenticated -> colors.textMuted
                                authState.syncStatus == SyncStatus.SYNCED -> colors.statusActive
                                else -> colors.gold
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )

                            Column {
                                Text(
                                    text = stringResource(id = R.string.auth_sync_status_label),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = when {
                                        !isAuthenticated -> stringResource(id = R.string.auth_sync_status_offline)
                                        authState.syncStatus == SyncStatus.SYNCING -> stringResource(id = R.string.auth_syncing)
                                        authState.syncStatus == SyncStatus.SYNCED -> stringResource(id = R.string.auth_synced_success)
                                        authState.syncStatus == SyncStatus.ERROR -> stringResource(id = R.string.auth_sync_error)
                                        else -> stringResource(id = R.string.auth_sync_status_connected)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        if (isAuthenticated) {
                            QabasButton(
                                text = stringResource(id = R.string.auth_sync_now),
                                onClick = { authViewModel.performSync() },
                                enabled = authState.syncStatus != SyncStatus.SYNCING,
                                isLoading = authState.syncStatus == SyncStatus.SYNCING,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = colors.gold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                variant = QabasButtonVariant.SecondarySurface,
                                modifier = Modifier.testTag("btn_sync_now")
                            )
                        }
                    }
                }

                // Profile Summary Stats Card
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_profile_stats"),
                    glowAccent = colors.gold,
                    contentPadding = QabasDimens.Space16
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(id = R.string.profile_stat_journeys),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = stringResource(id = R.string.profile_stat_journeys_val),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.gold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(id = R.string.profile_stat_impact),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = stringResource(id = R.string.profile_stat_impact_val),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.gold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(id = R.string.profile_stat_level),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = stringResource(id = R.string.profile_stat_level_val),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.gold
                            )
                        }
                    }
                }

                // Auth CTA Card (If guest -> Sign In / Create Account button, If logged in -> Logout & Delete Account)
                if (!isAuthenticated) {
                    Surface(
                        onClick = { isAuthDialogOpen = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_profile_login"),
                        shape = RoundedCornerShape(QabasDimens.Radius12),
                        color = colors.surfaceElevated,
                        border = BorderStroke(QabasDimens.BorderThin, colors.gold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(QabasDimens.Space14),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.profile_login_btn),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = stringResource(id = R.string.profile_login_notice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
                    ) {
                        QabasButton(
                            text = stringResource(id = R.string.auth_logout),
                            onClick = { authViewModel.signOut() },
                            variant = QabasButtonVariant.SecondarySurface,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_profile_logout")
                        )

                        QabasButton(
                            text = stringResource(id = R.string.auth_delete_account),
                            onClick = { isDeleteDialogOpen = true },
                            variant = QabasButtonVariant.OutlineGold,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = colors.statusInactive,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_profile_delete_account")
                        )
                    }
                }

                // Integrated Theme and Language Controls
                SectionTitle(
                    title = stringResource(id = R.string.profile_quick_settings_title),
                    showAccentDot = true
                )

                // Theme Quick Toggle Card
                QabasCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = QabasDimens.Space14
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isDarkMode != false) {
                                    stringResource(id = R.string.settings_theme_dark)
                                } else {
                                    stringResource(id = R.string.settings_theme_light)
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = stringResource(id = R.string.settings_theme),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }

                        Switch(
                            checked = isDarkMode ?: true,
                            onCheckedChange = { settingsViewModel.setDarkMode(it) },
                            modifier = Modifier.testTag("profile_switch_dark_mode"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.gold,
                                checkedTrackColor = colors.surfaceElevated,
                                checkedBorderColor = colors.gold,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surfaceElevated,
                                uncheckedBorderColor = colors.surfaceBorder
                            )
                        )
                    }
                }

                // Language Quick Select Card
                QabasCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = QabasDimens.Space14
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.settings_language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(QabasDimens.Space10))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
                        ) {
                            val isArabicSelected = language == "ar" || language == null
                            QabasButton(
                                text = "العربية",
                                onClick = { settingsViewModel.setLanguage("ar") },
                                variant = if (isArabicSelected) QabasButtonVariant.PrimaryGold else QabasButtonVariant.SecondarySurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_btn_lang_ar")
                            )

                            val isEnglishSelected = language == "en"
                            QabasButton(
                                text = "English",
                                onClick = { settingsViewModel.setLanguage("en") },
                                variant = if (isEnglishSelected) QabasButtonVariant.PrimaryGold else QabasButtonVariant.SecondarySurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_btn_lang_en")
                            )
                        }
                    }
                }

                // Full Settings Button
                QabasButton(
                    text = stringResource(id = R.string.settings_title),
                    onClick = onNavigateToSettings,
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_profile_settings")
                )
            }
        }
    }

    // Auth Dialog (Sign in / Sign up / Google / Guest)
    AuthDialog(
        isOpen = isAuthDialogOpen,
        onDismiss = { isAuthDialogOpen = false },
        isLoading = authState.isLoading,
        errorMessage = authState.errorMessage,
        onSignIn = { email, password ->
            authViewModel.signInWithEmail(email, password)
        },
        onSignUp = { email, password, name ->
            authViewModel.signUpWithEmail(email, password, name)
        },
        onGoogleSignIn = {
            authViewModel.launchGoogleSignIn(context)
        }
    )

    // Delete Account Confirmation Dialog
    DeleteAccountDialog(
        isOpen = isDeleteDialogOpen,
        isLoading = authState.isLoading,
        onConfirm = {
            authViewModel.deleteAccount()
            isDeleteDialogOpen = false
        },
        onDismiss = { isDeleteDialogOpen = false }
    )
}
