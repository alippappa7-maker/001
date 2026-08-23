package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CosmicStarGold
import com.example.ui.theme.CosmicStarWhite
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasThemeTokens
import kotlin.random.Random

private data class Star(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val isGold: Boolean
)

/**
 * Spiritual cosmic starfield background for QABAS.
 * Renders high-performance deterministic stars with gentle ambient celestial glows.
 */
@Composable
fun StarryBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 60,
    animateTwinkle: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colors = QabasThemeTokens.colors
    val isDark = colors.isDark

    // Deterministic star field so stars stay fixed
    val stars = remember(starCount) {
        val random = Random(1337)
        List(starCount) {
            Star(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                radius = random.nextFloat() * 1.4f + 0.6f,
                baseAlpha = random.nextFloat() * 0.45f + 0.25f,
                isGold = random.nextFloat() > 0.8f
            )
        }
    }

    // Subtle, gentle twinkle that is slow and non-distracting
    val infiniteTransition = rememberInfiniteTransition(label = "twinkle")
    val twinkleFactor by if (animateTwinkle && isDark) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "twinkleFactor"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height * 0.35f)

            if (isDark) {
                // Ambient celestial top glow (Deep Indigo/Cyan)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0F223C).copy(alpha = 0.5f),
                            Color(0xFF071120).copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = width * 0.9f
                    ),
                    radius = width * 0.9f,
                    center = center
                )

                // Ambient subtle gold aura in center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            QabasGold.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = width * 0.55f
                    ),
                    radius = width * 0.55f,
                    center = center
                )

                // Render Star Field
                stars.forEachIndexed { index, star ->
                    val starCenter = Offset(star.xRatio * width, star.yRatio * height)
                    val starTwinkle = if (index % 3 == 0) twinkleFactor else 1f
                    val alpha = (star.baseAlpha * starTwinkle).coerceIn(0.1f, 0.9f)
                    val starColor = if (star.isGold) {
                        CosmicStarGold.copy(alpha = alpha)
                    } else {
                        CosmicStarWhite.copy(alpha = alpha)
                    }

                    drawCircle(
                        color = starColor,
                        radius = star.radius.dp.toPx(),
                        center = starCenter
                    )
                }
            } else {
                // Light mode subtle radial warmth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            QabasGold.copy(alpha = 0.05f),
                            Color(0xFFEEF3FC).copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = width * 0.8f
                    ),
                    radius = width * 0.8f,
                    center = center
                )
            }
        }

        content()
    }
}
