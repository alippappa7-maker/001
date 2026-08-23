package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val colors = QabasThemeTokens.colors

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
                // Section 1: Appearance
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
                                },
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
            }
        }
    }
}
