package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.R
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QabasTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StarryBackground
import com.example.ui.theme.CompanionPurple
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun CompanionScreen(onBack: () -> Unit) {
    val colors = QabasThemeTokens.colors

    Scaffold(
        modifier = Modifier.testTag("screen_companion"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.feature_companion_title),
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
                    .padding(QabasDimens.Space20),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                SectionTitle(
                    title = stringResource(id = R.string.feature_companion_title),
                    subtitle = stringResource(id = R.string.feature_companion_desc),
                    accentColor = CompanionPurple,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space20))

                EmptyStateCard(
                    title = stringResource(id = R.string.feature_companion_title),
                    description = stringResource(id = R.string.feature_companion_later),
                    icon = Icons.Default.AutoAwesome,
                    accentColor = CompanionPurple,
                    actionButtonText = stringResource(id = R.string.btn_return_home),
                    onActionClick = onBack
                )
            }
        }
    }
}
