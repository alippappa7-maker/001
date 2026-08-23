package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

/**
 * Spiritual Empty / Placeholder State Card for QABAS.
 */
@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = QabasThemeTokens.colors
    val effectiveAccent = accentColor ?: colors.gold

    QabasCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("empty_state_card"),
        shape = RoundedCornerShape(QabasDimens.RadiusLarge),
        borderWidth = QabasDimens.BorderThin,
        glowAccent = effectiveAccent,
        contentPadding = QabasDimens.Space24
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Luminous Icon Sphere
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(effectiveAccent.copy(alpha = 0.12f))
                    .border(
                        BorderStroke(
                            QabasDimens.BorderRegular,
                            Brush.radialGradient(
                                listOf(
                                    effectiveAccent.copy(alpha = 0.8f),
                                    effectiveAccent.copy(alpha = 0.2f)
                                )
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = effectiveAccent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space16))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(QabasDimens.Space8))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            if (actionButtonText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(QabasDimens.Space20))
                QabasButton(
                    text = actionButtonText,
                    onClick = onActionClick,
                    accentColor = effectiveAccent,
                    variant = QabasButtonVariant.OutlineGold,
                    height = 44.dp
                )
            }
        }
    }
}
