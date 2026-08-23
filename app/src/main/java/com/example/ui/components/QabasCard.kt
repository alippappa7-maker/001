package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

/**
 * Standard Glassmorphic Card for QABAS app.
 * Provides luminous border, translucent surface, and tactile spring press feedback.
 */
@Composable
fun QabasCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(QabasDimens.RadiusMedium),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = QabasDimens.BorderThin,
    glowAccent: Color? = null,
    contentPadding: Dp = QabasDimens.Space16,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = QabasThemeTokens.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring scale response on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null && enabled) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "cardScale"
    )

    val surfaceColor = backgroundColor ?: colors.surfaceTranslucent
    val cardBorderColor = borderColor ?: glowAccent?.copy(alpha = 0.45f) ?: colors.surfaceBorder

    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(borderWidth, cardBorderColor)
    ) {
        Box(
            modifier = Modifier
                .padding(contentPadding)
        ) {
            content()
        }
    }
}
