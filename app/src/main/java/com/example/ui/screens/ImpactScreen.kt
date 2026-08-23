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
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
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
import com.example.ui.theme.ImpactOliveGold
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun ImpactScreen(onBack: () -> Unit) {
    val colors = QabasThemeTokens.colors

    Scaffold(
        modifier = Modifier.testTag("screen_impact"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.feature_impact_title),
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
                    title = stringResource(id = R.string.feature_impact_title),
                    subtitle = stringResource(id = R.string.feature_impact_desc),
                    accentColor = ImpactOliveGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Educational Concept Card
                QabasCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_impact_intro"),
                    glowAccent = ImpactOliveGold,
                    contentPadding = QabasDimens.Space16
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.impact_intro_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ImpactOliveGold
                        )
                        Spacer(modifier = Modifier.height(QabasDimens.Space8))
                        Text(
                            text = stringResource(id = R.string.impact_intro_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    }
                }

                // Pillar 1: Beneficial Knowledge
                ImpactPillarCard(
                    title = stringResource(id = R.string.impact_pillar1_title),
                    description = stringResource(id = R.string.impact_pillar1_desc),
                    icon = Icons.Default.School,
                    testTag = "card_impact_pillar_1"
                )

                // Pillar 2: Kind Words & Solidarity
                ImpactPillarCard(
                    title = stringResource(id = R.string.impact_pillar2_title),
                    description = stringResource(id = R.string.impact_pillar2_desc),
                    icon = Icons.Default.Diversity1,
                    testTag = "card_impact_pillar_2"
                )

                // Pillar 3: Sustainability & Lasting Good
                ImpactPillarCard(
                    title = stringResource(id = R.string.impact_pillar3_title),
                    description = stringResource(id = R.string.impact_pillar3_desc),
                    icon = Icons.Default.Eco,
                    testTag = "card_impact_pillar_3"
                )

                // Non-financial Awareness Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(QabasDimens.Radius12))
                        .background(colors.surfaceElevated)
                        .padding(QabasDimens.Space12),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.impact_badge_no_payment),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space4))

                QabasButton(
                    text = stringResource(id = R.string.btn_return_home),
                    onClick = onBack,
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_impact_return_home")
                )
            }
        }
    }
}

@Composable
private fun ImpactPillarCard(
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
        glowAccent = ImpactOliveGold,
        contentPadding = QabasDimens.Space16
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    tint = ImpactOliveGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
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
    }
}
