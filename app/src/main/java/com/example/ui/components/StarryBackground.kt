package com.example.ui.components

import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CosmicStarGold
import com.example.ui.theme.CosmicStarWhite
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasThemeTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Star(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val isGold: Boolean
)

/** موجة شفق واحدة: لون + مركز أساسي + سرعة/طور الحركة. */
private data class AuroraBlob(
    val color: Color,
    val centerX: Float,
    val centerY: Float,
    val driftRadius: Float,
    val phase: Float,
    val speed: Float,
    val baseRadiusRatio: Float
)

/** نيزكة سينمائية: مسار ثابت + توقيت ضمن دورة طويلة. */
private data class MeteorSpec(
    val startX: Float,
    val startY: Float,
    val angle: Float, // بالراديان
    val length: Float,
    val startFraction: Float, // متى تبدأ ضمن الدورة (0..1)
    val streakFraction: Float, // مدة الوميض كنسبة من الدورة
    val color: Color
)

/**
 * خلفية كونية روحانية لتطبيق قبس.
 * نجوم حتمية تتلألأ + توهجات سماوية + (اختياريًا) شفق متحرك ونيازك سينمائية.
 */
@Composable
fun StarryBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 60,
    animateTwinkle: Boolean = true,
    enableCinematicAurora: Boolean = false,
    enableMeteors: Boolean = false,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colors = QabasThemeTokens.colors
    val isDark = colors.isDark

    val context = LocalContext.current
    val reduceMotionEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

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

    // موجات الشفق: مراكز ثابتة تنجرف ببطء حول موضعها الأساسي.
    val auroraBlobs = remember {
        listOf(
            AuroraBlob(Color(0xFF1E3A5F), 0.25f, 0.30f, 0.08f, 0f, 0.6f, 0.55f),     // نيلي عميق
            AuroraBlob(Color(0xFF0F4C5C), 0.75f, 0.22f, 0.10f, 1.2f, 0.45f, 0.60f), // سماوي غامق
            AuroraBlob(Color(0xFF3B2F5F), 0.50f, 0.55f, 0.07f, 2.4f, 0.5f, 0.50f),  // بنفسجي خافت
            AuroraBlob(QabasGold, 0.60f, 0.40f, 0.05f, 3.6f, 0.35f, 0.35f)         // ذهبي خافت
        )
    }

    // نيازك حتمية (٣ فقط) ضمن دورة واحدة طويلة.
    val meteors = remember {
        val random = Random(2024)
        List(3) { i ->
            val startX = random.nextFloat().coerceIn(0.05f, 0.6f)
            val startY = random.nextFloat().coerceIn(0.05f, 0.35f)
            val angle = (PI * 0.18 + random.nextFloat() * 0.12).toFloat() // ميل لطيف نحو الأسفل
            MeteorSpec(
                startX = startX,
                startY = startY,
                angle = angle,
                length = 0.18f + random.nextFloat() * 0.10f,
                startFraction = (i + 1) / 4f + random.nextFloat() * 0.05f,
                streakFraction = 0.12f,
                color = if (random.nextFloat() > 0.5f) CosmicStarGold else CosmicStarWhite
            )
        }
    }

    // Subtle, gentle twinkle that is slow and non-distracting
    val infiniteTransition = rememberInfiniteTransition(label = "twinkle")
    val twinkleFactor by if (animateTwinkle && isDark && !reduceMotionEnabled) {
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

    // طور الشفق المتحرك (بطيء جدًا).
    val auroraPhase by if (enableCinematicAurora && isDark && !reduceMotionEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 24000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "auroraPhase"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    // دورة النيازك (طويلة، كل نيزكة تظهر ضمن نافذتها).
    val meteorCycle by if (enableMeteors && isDark && !reduceMotionEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 11000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "meteorCycle"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
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
                // ===== الشفق السينمائي المتحرك (خلف النجوم) =====
                if (enableCinematicAurora) {
                    auroraBlobs.forEach { blob ->
                        val t = auroraPhase * blob.speed + blob.phase
                        val cx = (blob.centerX + cos(t) * blob.driftRadius) * width
                        val cy = (blob.centerY + sin(t * 0.8f) * blob.driftRadius) * height
                        val r = width * blob.baseRadiusRatio
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    blob.color.copy(alpha = 0.18f),
                                    blob.color.copy(alpha = 0.06f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = r
                            ),
                            radius = r,
                            center = Offset(cx, cy)
                        )
                    }
                }

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

                // ===== النيازك السينمائية =====
                if (enableMeteors && !reduceMotionEnabled) {
                    meteors.forEach { m ->
                        val local = (meteorCycle - m.startFraction) / m.streakFraction
                        if (local in 0f..1f) {
                            val progress = local
                            val headX = m.startX * width
                            val headY = m.startY * height
                            val tailX = headX - cos(m.angle) * m.length * width * progress
                            val tailY = headY - sin(m.angle) * m.length * width * progress
                            val headPoint = Offset(
                                headX + cos(m.angle) * m.length * width * progress,
                                headY + sin(m.angle) * m.length * width * progress
                            )
                            // يتلاشى الذيل مع تقدم النيزكة.
                            val alpha = (1f - progress) * 0.85f
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        m.color.copy(alpha = 0f),
                                        m.color.copy(alpha = alpha * 0.9f),
                                        Color.White.copy(alpha = alpha)
                                    ),
                                    start = Offset(tailX, tailY),
                                    end = headPoint
                                ),
                                start = Offset(tailX, tailY),
                                end = headPoint,
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }

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
