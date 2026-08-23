package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasGoldLight
import com.example.ui.theme.QabasThemeTokens

/**
 * Reusable Golden Celestial Compass for QABAS.
 */
@Composable
fun GoldenCompass(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    compassSize: Dp = QabasDimens.CompassSizeDefault,
    showAuraGlow: Boolean = true,
    showOrbitalRing: Boolean = true,
    userRotation: Float = 0f,
    qiblaAngle: Float? = null
) {
    val colors = QabasThemeTokens.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "compassScale"
    )

    // Smooth rotation animation for the compass background
    val animatedRotation by animateFloatAsState(
        targetValue = -userRotation,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "compassRotation"
    )

    Box(
        modifier = modifier
            .size(compassSize * 1.35f)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Celestial Rings & Glow Aura Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val compassRadiusPx = (compassSize.toPx()) / 2f

            if (showAuraGlow) {
                // Soft radial golden ambient glow behind compass
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.gold.copy(alpha = 0.22f),
                            Color(0xFF0E223D).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = compassRadiusPx * 1.5f
                    ),
                    radius = compassRadiusPx * 1.5f,
                    center = center
                )
            }

            if (showOrbitalRing) {
                // Outer dashed astronomical orbit
                drawCircle(
                    color = colors.gold.copy(alpha = 0.28f),
                    radius = compassRadiusPx * 1.25f,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f), 0f)
                    )
                )
            }

            // Outer compass border halo
            drawCircle(
                color = colors.gold.copy(alpha = 0.35f),
                radius = compassRadiusPx + 3.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            
            // Qibla Arrow
            if (qiblaAngle != null) {
                val qiblaRelativeAngle = qiblaAngle - userRotation
                val angleRad = Math.toRadians((qiblaRelativeAngle - 90).toDouble())
                
                // Draw Qibla pointer on the orbital ring
                val pointerRadius = compassRadiusPx * 1.25f
                val pointerX = center.x + (pointerRadius * kotlin.math.cos(angleRad)).toFloat()
                val pointerY = center.y + (pointerRadius * kotlin.math.sin(angleRad)).toFloat()
                
                drawCircle(
                    color = colors.gold,
                    radius = 6.dp.toPx(),
                    center = Offset(pointerX, pointerY)
                )
            }
        }

        // Compass Center Container
        Box(
            modifier = Modifier
                .size(compassSize)
                .clip(CircleShape)
                .background(colors.surfaceElevated)
                .border(
                    BorderStroke(
                        width = QabasDimens.BorderRegular,
                        brush = Brush.sweepGradient(
                            listOf(
                                colors.gold,
                                QabasGoldLight,
                                colors.gold,
                                colors.goldDark,
                                colors.gold
                            )
                        )
                    ),
                    shape = CircleShape
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.qabas_compass_v2_1787516547189),
                contentDescription = stringResource(id = R.string.compass_title),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedRotation)
            )
        }
    }
}
