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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasCard
import com.example.ui.components.QabasTopBar
import com.example.ui.components.SectionTitle
import com.example.ui.components.StarryBackground
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import com.example.ui.theme.StudioBlue

@Composable
fun StudioScreen(onBack: () -> Unit) {
    val colors = QabasThemeTokens.colors

    Scaffold(
        modifier = Modifier.testTag("screen_studio"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.feature_studio_title),
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
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space16)
            ) {
                SectionTitle(
                    title = stringResource(id = R.string.feature_studio_title),
                    subtitle = stringResource(id = R.string.feature_studio_desc),
                    accentColor = StudioBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Introductory Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(QabasDimens.Radius12))
                        .background(StudioBlue.copy(alpha = 0.12f))
                        .padding(QabasDimens.Space12),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.studio_intro_banner),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = StudioBlue,
                        textAlign = TextAlign.Center
                    )
                }

                // 1. Design Card
                StudioToolCard(
                    title = stringResource(id = R.string.studio_card_design_title),
                    description = stringResource(id = R.string.studio_card_design_desc),
                    icon = Icons.Default.Create,
                    testTag = "card_studio_design"
                )

                // 2. Sound Card
                StudioToolCard(
                    title = stringResource(id = R.string.studio_card_sound_title),
                    description = stringResource(id = R.string.studio_card_sound_desc),
                    icon = Icons.Default.GraphicEq,
                    testTag = "card_studio_sound"
                )

                // 3. Meditation Card
                StudioToolCard(
                    title = stringResource(id = R.string.studio_card_meditation_title),
                    description = stringResource(id = R.string.studio_card_meditation_desc),
                    icon = Icons.Default.SelfImprovement,
                    testTag = "card_studio_meditation"
                )

                // 4. Notes Card
                StudioToolCard(
                    title = stringResource(id = R.string.studio_card_notes_title),
                    description = stringResource(id = R.string.studio_card_notes_desc),
                    icon = Icons.Default.StickyNote2,
                    testTag = "card_studio_notes"
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space8))

                // Return to home button
                QabasButton(
                    text = stringResource(id = R.string.btn_return_home),
                    onClick = onBack,
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_studio_return_home")
                )
            }
        }
    }
}

@Composable
private fun StudioToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    testTag: String
) {
    val colors = QabasThemeTokens.colors

    QabasCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        glowAccent = StudioBlue,
        contentPadding = QabasDimens.Space16
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(QabasDimens.Radius10))
                        .background(colors.surfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = StudioBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(QabasDimens.RadiusFull))
                    .background(colors.surfaceElevated)
                    .padding(horizontal = QabasDimens.Space10, vertical = QabasDimens.Space4)
            ) {
                Text(
                    text = stringResource(id = R.string.tool_coming_soon),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.gold
                )
            }
        }
    }
}
