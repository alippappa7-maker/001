package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.SettingsViewModel
import com.example.ui.components.GoldenCompass
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.StarryBackground
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun WelcomeScreen(
    onNavigateToHome: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors

    val handleNavigate = {
        viewModel.setHasSeenWelcome()
        onNavigateToHome()
    }

    StarryBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_welcome")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = QabasDimens.Space24, vertical = QabasDimens.Space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(QabasDimens.Space16))

            // Center Cosmic Visual Identity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                GoldenCompass(
                    compassSize = 130.dp,
                    showAuraGlow = true,
                    showOrbitalRing = true
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space24))

                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    ),
                    color = colors.gold
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space8))

                Text(
                    text = stringResource(id = R.string.welcome_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = QabasDimens.Space16)
                )
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space32))

            // Bottom Call to Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QabasButton(
                    text = stringResource(id = R.string.start_now),
                    onClick = handleNavigate,
                    variant = QabasButtonVariant.PrimaryGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_welcome_start")
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                QabasButton(
                    text = stringResource(id = R.string.skip),
                    onClick = handleNavigate,
                    variant = QabasButtonVariant.TextOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_welcome_skip")
                )
            }
        }
    }
}
