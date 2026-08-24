package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.SettingsViewModel
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasCard
import com.example.ui.components.QabasTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StarryBackground
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import java.util.Locale

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val notificationSettings by viewModel.notificationSettings.collectAsState()
    val colors = QabasThemeTokens.colors
    var hasPermission by remember { mutableStateOf(viewModel.hasNotificationPermission()) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    Scaffold(
        modifier = Modifier.testTag("screen_notification_settings"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.notifications_title),
                onBack = onBack
            )
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
                    .padding(QabasDimens.Space20)
            ) {
                // 1. Permission status card if not granted on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                    QabasCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = QabasDimens.Space16)
                            .testTag("card_notification_permission_banner"),
                        glowAccent = colors.statusWarning,
                        contentPadding = QabasDimens.Space16
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = colors.statusWarning,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(QabasDimens.Space12))
                                Text(
                                    text = stringResource(id = R.string.notif_permission_banner_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(QabasDimens.Space8))
                            Text(
                                text = stringResource(id = R.string.notif_permission_banner_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(QabasDimens.Space12))
                            QabasButton(
                                text = stringResource(id = R.string.notif_permission_btn),
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_grant_notif_permission"),
                                variant = QabasButtonVariant.PrimaryGold
                            )
                        }
                    }
                }

                // 2. Master switch
                SectionTitle(
                    title = stringResource(id = R.string.notifications_title),
                    subtitle = stringResource(id = R.string.notif_master_switch_desc),
                    showAccentDot = true
                )
                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_master_notifications"),
                    contentPadding = QabasDimens.Space16
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (notificationSettings.masterEnabled) colors.gold.copy(alpha = 0.15f) else colors.surfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (notificationSettings.masterEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (notificationSettings.masterEnabled) colors.gold else colors.textMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(QabasDimens.Space16))
                            Column {
                                Text(
                                    text = stringResource(id = R.string.notif_master_switch),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (notificationSettings.masterEnabled) stringResource(id = R.string.notif_permission_granted) else stringResource(id = R.string.cancel),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            }
                        }
                        Switch(
                            checked = notificationSettings.masterEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.setNotificationMaster(isChecked)
                            },
                            modifier = Modifier.testTag("switch_master_notifications"),
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

                Spacer(modifier = Modifier.height(QabasDimens.Space24))

                // 3. Per-Prayer Notifications
                SectionTitle(
                    title = stringResource(id = R.string.notif_section_prayers),
                    subtitle = stringResource(id = R.string.notif_section_prayers_desc),
                    showAccentDot = true
                )
                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_prayers_notifications"),
                    contentPadding = QabasDimens.Space8
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        PrayerToggleRow(
                            label = stringResource(id = R.string.prayer_fajr),
                            isChecked = notificationSettings.fajrEnabled,
                            isEnabled = notificationSettings.masterEnabled,
                            onToggle = { viewModel.setPrayerNotificationEnabled("fajr", it) },
                            testTag = "toggle_notif_fajr"
                        )
                        PrayerToggleRow(
                            label = stringResource(id = R.string.prayer_sunrise),
                            isChecked = notificationSettings.sunriseEnabled,
                            isEnabled = notificationSettings.masterEnabled,
                            onToggle = { viewModel.setPrayerNotificationEnabled("sunrise", it) },
                            testTag = "toggle_notif_sunrise"
                        )
                        PrayerToggleRow(
                            label = stringResource(id = R.string.prayer_dhuhr),
                            isChecked = notificationSettings.dhuhrEnabled,
                            isEnabled = notificationSettings.masterEnabled,
                            onToggle = { viewModel.setPrayerNotificationEnabled("dhuhr", it) },
                            testTag = "toggle_notif_dhuhr"
                        )
                        PrayerToggleRow(
                            label = stringResource(id = R.string.prayer_asr),
                            isChecked = notificationSettings.asrEnabled,
                            isEnabled = notificationSettings.masterEnabled,
                            onToggle = { viewModel.setPrayerNotificationEnabled("asr", it) },
                            testTag = "toggle_notif_asr"
                        )
                        PrayerToggleRow(
                            label = stringResource(id = R.string.prayer_maghrib),
                            isChecked = notificationSettings.maghribEnabled,
                            isEnabled = notificationSettings.masterEnabled,
                            onToggle = { viewModel.setPrayerNotificationEnabled("maghrib", it) },
                            testTag = "toggle_notif_maghrib"
                        )
                        PrayerToggleRow(
                            label = stringResource(id = R.string.prayer_isha),
                            isChecked = notificationSettings.ishaEnabled,
                            isEnabled = notificationSettings.masterEnabled,
                            onToggle = { viewModel.setPrayerNotificationEnabled("isha", it) },
                            testTag = "toggle_notif_isha"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space24))

                // 4. Daily Dhikr Reminder
                SectionTitle(
                    title = stringResource(id = R.string.notif_section_dhikr),
                    subtitle = stringResource(id = R.string.journey_item_dhikr_desc),
                    showAccentDot = true
                )
                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_dhikr_notifications"),
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.notif_dhikr_toggle),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(
                                        id = R.string.notif_dhikr_time,
                                        notificationSettings.dhikrReminderHour,
                                        notificationSettings.dhikrReminderMinute
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            }
                            Switch(
                                checked = notificationSettings.dhikrReminderEnabled && notificationSettings.masterEnabled,
                                enabled = notificationSettings.masterEnabled,
                                onCheckedChange = { viewModel.setDhikrReminderEnabled(it) },
                                modifier = Modifier.testTag("switch_dhikr_reminder"),
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

                        if (notificationSettings.dhikrReminderEnabled && notificationSettings.masterEnabled) {
                            Spacer(modifier = Modifier.height(QabasDimens.Space12))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceElevated)
                                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                                    .clickable { showTimePickerDialog = true }
                                    .padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space12)
                                    .testTag("btn_pick_dhikr_time"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = colors.gold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(QabasDimens.Space12))
                                    Text(
                                        text = stringResource(id = R.string.notif_dhikr_time_label),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textPrimary
                                    )
                                }
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%02d:%02d",
                                        notificationSettings.dhikrReminderHour,
                                        notificationSettings.dhikrReminderMinute
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.gold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space24))

                // 5. Sound & Vibration
                SectionTitle(
                    title = stringResource(id = R.string.notif_section_sound),
                    subtitle = stringResource(id = R.string.notif_sound_desc),
                    showAccentDot = true
                )
                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_sound_vibration"),
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
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = if (notificationSettings.soundEnabled) colors.gold else colors.textMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(QabasDimens.Space12))
                                Column {
                                    Text(
                                        text = stringResource(id = R.string.notif_sound_toggle),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = stringResource(id = R.string.notif_sound_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted
                                    )
                                }
                            }
                            Switch(
                                checked = notificationSettings.soundEnabled,
                                onCheckedChange = { viewModel.setNotificationSoundEnabled(it) },
                                modifier = Modifier.testTag("switch_notif_sound"),
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

                        Spacer(modifier = Modifier.height(QabasDimens.Space16))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = if (notificationSettings.vibrateEnabled) colors.gold else colors.textMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(QabasDimens.Space12))
                                Column {
                                    Text(
                                        text = stringResource(id = R.string.notif_vibrate_toggle),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = stringResource(id = R.string.notif_vibrate_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted
                                    )
                                }
                            }
                            Switch(
                                checked = notificationSettings.vibrateEnabled,
                                onCheckedChange = { viewModel.setNotificationVibrateEnabled(it) },
                                modifier = Modifier.testTag("switch_notif_vibrate"),
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
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space24))

                // 6. Test Notification Button
                QabasButton(
                    text = stringResource(id = R.string.notif_test_button),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.sendTestNotification()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_send_test_notification"),
                    variant = QabasButtonVariant.OutlineGold
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space40))
            }
        }
    }

    // Time Picker Dialog for Dhikr Reminder
    if (showTimePickerDialog) {
        DhikrTimePickerDialog(
            currentHour = notificationSettings.dhikrReminderHour,
            currentMinute = notificationSettings.dhikrReminderMinute,
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { hour, min ->
                viewModel.setDhikrReminderTime(hour, min)
                showTimePickerDialog = false
            }
        )
    }
}

@Composable
private fun PrayerToggleRow(
    label: String,
    isChecked: Boolean,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    testTag: String
) {
    val colors = QabasThemeTokens.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = QabasDimens.Space12, vertical = QabasDimens.Space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isEnabled) colors.textPrimary else colors.textMuted,
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = isChecked && isEnabled,
            enabled = isEnabled,
            onCheckedChange = onToggle,
            modifier = Modifier.testTag(testTag),
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

@Composable
private fun DhikrTimePickerDialog(
    currentHour: Int,
    currentMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val colors = QabasThemeTokens.colors
    var hour by remember { mutableIntStateOf(currentHour) }
    var minute by remember { mutableIntStateOf(currentMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.notif_dhikr_time_label),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = QabasDimens.Space16),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { hour = (hour + 1) % 24 }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = colors.gold)
                        }
                        Text(
                            text = String.format(Locale.US, "%02d", hour),
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { hour = if (hour == 0) 23 else hour - 1 }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = colors.gold)
                        }
                    }

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.gold,
                        modifier = Modifier.padding(horizontal = QabasDimens.Space16)
                    )

                    // Minute picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { minute = (minute + 5) % 60 }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = colors.gold)
                        }
                        Text(
                            text = String.format(Locale.US, "%02d", minute),
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { minute = if (minute < 5) 55 else minute - 5 }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = colors.gold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hour, minute) }) {
                Text(
                    text = stringResource(id = R.string.start_now),
                    color = colors.gold,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = colors.textMuted
                )
            }
        },
        containerColor = colors.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
