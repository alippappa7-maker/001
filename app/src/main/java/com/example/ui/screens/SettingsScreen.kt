package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
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
import com.example.domain.model.AVAILABLE_CALCULATION_METHODS
import com.example.domain.model.COMMON_TIMEZONES
import com.example.domain.model.CityModel
import com.example.domain.model.POPULAR_CITIES
import com.example.ui.SettingsViewModel
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasCard
import com.example.ui.components.QabasTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StarryBackground
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val prayerConfig by viewModel.prayerConfig.collectAsState()
    val notificationSettings by viewModel.notificationSettings.collectAsState()
    val colors = QabasThemeTokens.colors

    var showCityDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var showTimezoneDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.testTag("screen_settings"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.settings_title),
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
                // Section 1: Notifications
                SectionTitle(
                    title = stringResource(id = R.string.notifications_title),
                    showAccentDot = true
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToNotifications() }
                        .testTag("card_settings_notifications"),
                    contentPadding = QabasDimens.Space16
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.notif_section_prayers),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (notificationSettings.masterEnabled) {
                                    stringResource(id = R.string.notif_permission_granted)
                                } else {
                                    stringResource(id = R.string.cancel)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        QabasButton(
                            text = stringResource(id = R.string.compass_learn_more),
                            onClick = onNavigateToNotifications,
                            variant = QabasButtonVariant.SecondarySurface,
                            modifier = Modifier.testTag("btn_goto_notifications")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space24))

                // Section 2: Appearance
                SectionTitle(
                    title = stringResource(id = R.string.settings_theme),
                    showAccentDot = true
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                QabasCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = QabasDimens.Space16
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
                                } + " Mode",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (isDarkMode != false) {
                                    stringResource(id = R.string.settings_theme_dark_desc)
                                } else {
                                    stringResource(id = R.string.settings_theme_light_desc)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Switch(
                            checked = isDarkMode ?: true,
                            onCheckedChange = { viewModel.setDarkMode(it) },
                            modifier = Modifier.testTag("switch_dark_mode"),
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

                // Section 2: Language
                SectionTitle(
                    title = stringResource(id = R.string.settings_language),
                    showAccentDot = true
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                QabasCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.settings_language_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(QabasDimens.Space16))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
                        ) {
                            val isArabicSelected = language == "ar" || language == null
                            QabasButton(
                                text = "العربية",
                                onClick = { viewModel.setLanguage("ar") },
                                variant = if (isArabicSelected) QabasButtonVariant.PrimaryGold else QabasButtonVariant.SecondarySurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_lang_ar")
                            )

                            val isEnglishSelected = language == "en"
                            QabasButton(
                                text = "English",
                                onClick = { viewModel.setLanguage("en") },
                                variant = if (isEnglishSelected) QabasButtonVariant.PrimaryGold else QabasButtonVariant.SecondarySurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_lang_en")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space24))

                // Section 3: Prayer Settings
                SectionTitle(
                    title = stringResource(id = R.string.prayer_settings_title),
                    showAccentDot = true
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                // City / Location selector Card
                QabasCard(
                    modifier = Modifier.fillMaxWidth(),
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
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(QabasDimens.Space8))
                                Column {
                                    Text(
                                        text = stringResource(id = R.string.prayer_select_city),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = prayerConfig.selectedCity.fullDisplayNameAr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.gold
                                    )
                                }
                            }
                            QabasButton(
                                text = stringResource(id = R.string.prayer_location_manual),
                                onClick = { showCityDialog = true },
                                variant = QabasButtonVariant.SecondarySurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                // Calculation Method & Asr Madhab Card
                QabasCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(QabasDimens.Space16)
                    ) {
                        // Calculation Method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMethodDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.prayer_method_title),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                val currentMethodName = AVAILABLE_CALCULATION_METHODS.find { it.id == prayerConfig.calculationMethod }?.nameAr
                                    ?: "جامعة أم القرى - مكة المكرمة"
                                Text(
                                    text = currentMethodName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.gold
                                )
                            }
                            QabasButton(
                                text = "تغيير",
                                onClick = { showMethodDialog = true },
                                variant = QabasButtonVariant.SecondarySurface
                            )
                        }

                        // Asr Madhab
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(id = R.string.prayer_madhab_title),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(QabasDimens.Space8))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                            ) {
                                QabasButton(
                                    text = stringResource(id = R.string.prayer_madhab_standard),
                                    onClick = { viewModel.setAsrMadhab(0) },
                                    variant = if (prayerConfig.asrMadhab == 0) QabasButtonVariant.PrimaryGold else QabasButtonVariant.SecondarySurface,
                                    modifier = Modifier.weight(1f)
                                )
                                QabasButton(
                                    text = stringResource(id = R.string.prayer_madhab_hanafi),
                                    onClick = { viewModel.setAsrMadhab(1) },
                                    variant = if (prayerConfig.asrMadhab == 1) QabasButtonVariant.PrimaryGold else QabasButtonVariant.SecondarySurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Timezone
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTimezoneDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.prayer_timezone_title),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = prayerConfig.timezone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.gold
                                )
                            }
                            QabasButton(
                                text = "تغيير",
                                onClick = { showTimezoneDialog = true },
                                variant = QabasButtonVariant.SecondarySurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                // Section 4: Minute Adjustments Card
                QabasCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.prayer_adjustments_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = stringResource(id = R.string.prayer_adjustments_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.resetPrayerAdjustments() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = stringResource(id = R.string.prayer_reset_adjustments),
                                    tint = colors.gold
                                )
                            }
                        }

                        // Adjustment items
                        MinuteAdjustItem(
                            label = stringResource(id = R.string.prayer_fajr),
                            minutes = prayerConfig.fajrAdjustment,
                            onMinus = { viewModel.setPrayerAdjustment("fajr", prayerConfig.fajrAdjustment - 1) },
                            onPlus = { viewModel.setPrayerAdjustment("fajr", prayerConfig.fajrAdjustment + 1) }
                        )
                        MinuteAdjustItem(
                            label = stringResource(id = R.string.prayer_sunrise),
                            minutes = prayerConfig.sunriseAdjustment,
                            onMinus = { viewModel.setPrayerAdjustment("sunrise", prayerConfig.sunriseAdjustment - 1) },
                            onPlus = { viewModel.setPrayerAdjustment("sunrise", prayerConfig.sunriseAdjustment + 1) }
                        )
                        MinuteAdjustItem(
                            label = stringResource(id = R.string.prayer_dhuhr),
                            minutes = prayerConfig.dhuhrAdjustment,
                            onMinus = { viewModel.setPrayerAdjustment("dhuhr", prayerConfig.dhuhrAdjustment - 1) },
                            onPlus = { viewModel.setPrayerAdjustment("dhuhr", prayerConfig.dhuhrAdjustment + 1) }
                        )
                        MinuteAdjustItem(
                            label = stringResource(id = R.string.prayer_asr),
                            minutes = prayerConfig.asrAdjustment,
                            onMinus = { viewModel.setPrayerAdjustment("asr", prayerConfig.asrAdjustment - 1) },
                            onPlus = { viewModel.setPrayerAdjustment("asr", prayerConfig.asrAdjustment + 1) }
                        )
                        MinuteAdjustItem(
                            label = stringResource(id = R.string.prayer_maghrib),
                            minutes = prayerConfig.maghribAdjustment,
                            onMinus = { viewModel.setPrayerAdjustment("maghrib", prayerConfig.maghribAdjustment - 1) },
                            onPlus = { viewModel.setPrayerAdjustment("maghrib", prayerConfig.maghribAdjustment + 1) }
                        )
                        MinuteAdjustItem(
                            label = stringResource(id = R.string.prayer_isha),
                            minutes = prayerConfig.ishaAdjustment,
                            onMinus = { viewModel.setPrayerAdjustment("isha", prayerConfig.ishaAdjustment - 1) },
                            onPlus = { viewModel.setPrayerAdjustment("isha", prayerConfig.ishaAdjustment + 1) }
                        )
                    }
                }
            }
        }
    }

    // City Selection Dialog
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.prayer_select_city),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.gold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                ) {
                    POPULAR_CITIES.forEach { city ->
                        val isSelected = city.id == prayerConfig.selectedCity.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QabasDimens.Radius8))
                                .background(if (isSelected) colors.gold.copy(alpha = 0.15f) else colors.surfaceElevated)
                                .clickable {
                                    viewModel.setSelectedCity(city)
                                    showCityDialog = false
                                }
                                .padding(QabasDimens.Space12),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.nameAr,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) colors.gold else colors.textPrimary
                                )
                                Text(
                                    text = city.countryAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text(stringResource(id = R.string.cancel), color = colors.gold)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }

    // Calculation Method Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.prayer_method_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.gold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                ) {
                    AVAILABLE_CALCULATION_METHODS.forEach { method ->
                        val isSelected = method.id == prayerConfig.calculationMethod
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QabasDimens.Radius8))
                                .background(if (isSelected) colors.gold.copy(alpha = 0.15f) else colors.surfaceElevated)
                                .clickable {
                                    viewModel.setCalculationMethod(method.id)
                                    showMethodDialog = false
                                }
                                .padding(QabasDimens.Space12),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = method.nameAr,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) colors.gold else colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text(stringResource(id = R.string.cancel), color = colors.gold)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }

    // Timezone Dialog
    if (showTimezoneDialog) {
        AlertDialog(
            onDismissRequest = { showTimezoneDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.prayer_timezone_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.gold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                ) {
                    COMMON_TIMEZONES.forEach { tz ->
                        val isSelected = tz == prayerConfig.timezone
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QabasDimens.Radius8))
                                .background(if (isSelected) colors.gold.copy(alpha = 0.15f) else colors.surfaceElevated)
                                .clickable {
                                    viewModel.setTimezone(tz)
                                    showTimezoneDialog = false
                                }
                                .padding(QabasDimens.Space12),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tz,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) colors.gold else colors.textPrimary
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimezoneDialog = false }) {
                    Text(stringResource(id = R.string.cancel), color = colors.gold)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }
}

@Composable
private fun MinuteAdjustItem(
    label: String,
    minutes: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    val colors = QabasThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QabasDimens.Radius8))
            .background(colors.surfaceElevated)
            .padding(horizontal = QabasDimens.Space12, vertical = QabasDimens.Space6),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onMinus,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.background)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Minus",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .padding(horizontal = QabasDimens.Space4),
                contentAlignment = Alignment.Center
            ) {
                val signStr = if (minutes > 0) "+$minutes" else "$minutes"
                Text(
                    text = "$signStr د",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (minutes != 0) colors.gold else colors.textSecondary
                )
            }

            IconButton(
                onClick = onPlus,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.background)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Plus",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
