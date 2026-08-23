package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasGoldDark
import com.example.ui.theme.QabasGoldLight
import com.example.ui.theme.QabasThemeTokens

enum class QabasButtonVariant {
    PrimaryGold,
    OutlineGold,
    SecondarySurface,
    TextOnly
}

/**
 * Standard button component for QABAS app.
 * Provides rich gold styling, smooth press feedback, and accessibility compliance.
 */
@Composable
fun QabasButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: QabasButtonVariant = QabasButtonVariant.PrimaryGold,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(QabasDimens.RadiusMedium),
    height: Dp = QabasDimens.ButtonHeight,
    accentColor: Color? = null
) {
    val colors = QabasThemeTokens.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "btnScale"
    )

    val effectiveAccent = accentColor ?: colors.gold

    val backgroundModifier = when (variant) {
        QabasButtonVariant.PrimaryGold -> {
            if (enabled) {
                Modifier.background(
                    Brush.horizontalGradient(
                        listOf(
                            effectiveAccent,
                            effectiveAccent.copy(alpha = 0.88f)
                        )
                    )
                )
            } else {
                Modifier.background(colors.surfaceElevated)
            }
        }
        QabasButtonVariant.OutlineGold -> {
            Modifier.background(Color.Transparent)
        }
        QabasButtonVariant.SecondarySurface -> {
            Modifier.background(colors.surfaceElevated)
        }
        QabasButtonVariant.TextOnly -> {
            Modifier.background(Color.Transparent)
        }
    }

    val border = when (variant) {
        QabasButtonVariant.OutlineGold -> {
            BorderStroke(
                width = QabasDimens.BorderRegular,
                color = if (enabled) effectiveAccent.copy(alpha = 0.8f) else colors.surfaceBorder
            )
        }
        QabasButtonVariant.SecondarySurface -> {
            BorderStroke(
                width = QabasDimens.BorderThin,
                color = colors.surfaceBorder
            )
        }
        else -> null
    }

    val contentColor = when (variant) {
        QabasButtonVariant.PrimaryGold -> if (enabled) Color(0xFF0F1420) else colors.textMuted
        QabasButtonVariant.OutlineGold -> if (enabled) effectiveAccent else colors.textMuted
        QabasButtonVariant.SecondarySurface -> if (enabled) colors.textPrimary else colors.textMuted
        QabasButtonVariant.TextOnly -> if (enabled) effectiveAccent else colors.textMuted
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .height(height)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        shape = shape,
        color = Color.Transparent,
        border = border
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .then(backgroundModifier)
                .padding(horizontal = QabasDimens.Space20),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = contentColor,
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        icon()
                        Spacer(modifier = Modifier.width(QabasDimens.Space8))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}
