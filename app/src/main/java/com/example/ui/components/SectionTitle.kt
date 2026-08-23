package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

/**
 * Clean Section Header Component with brand styling.
 */
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color? = null,
    textAlign: TextAlign = TextAlign.Start,
    showAccentDot: Boolean = true
) {
    val colors = QabasThemeTokens.colors
    val effectiveAccent = accentColor ?: colors.gold

    Column(
        modifier = modifier,
        horizontalAlignment = when (textAlign) {
            TextAlign.Center -> Alignment.CenterHorizontally
            TextAlign.End -> Alignment.End
            else -> Alignment.Start
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showAccentDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(effectiveAccent)
                )
                Spacer(modifier = Modifier.width(QabasDimens.Space8))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colors.textPrimary,
                textAlign = textAlign
            )
        }

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(QabasDimens.Space4))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = textAlign,
                modifier = if (showAccentDot && textAlign != TextAlign.Center) {
                    Modifier.padding(start = 14.dp)
                } else Modifier
            )
        }
    }
}
