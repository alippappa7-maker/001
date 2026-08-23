package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// QABAS CORE BRAND COLORS (الذهبي والداكن)
// ==========================================

// Gold Palette (لون الهوية الرئيسي)
val QabasGold = Color(0xFFD4AF37)
val QabasGoldLight = Color(0xFFF1D57A)
val QabasGoldGlow = Color(0xFFFFE898)
val QabasGoldDark = Color(0xFF9A7B1C)
val QabasGoldMuted = Color(0x33D4AF37)

// Dark Palette (أسود مزرق عميق وكحلي داكن شفاف)
val QabasDarkBackground = Color(0xFF04060A)
val QabasDarkBackgroundSecondary = Color(0xFF080D16)
val QabasSurfaceDark = Color(0xFF0F1524)
val QabasSurfaceDarkTranslucent = Color(0xD90E1524)
val QabasSurfaceDarkElevated = Color(0xFF162035)
val QabasSurfaceDarkBorder = Color(0xFF1E2B40)
val QabasSurfaceDarkBorderGlow = Color(0x66D4AF37)

// Light Palette (الوضع الفاتح الروحاني الهادئ)
val QabasLightBackground = Color(0xFFF4F6FB)
val QabasLightBackgroundSecondary = Color(0xFFE9EEF7)
val QabasSurfaceLight = Color(0xFFFFFFFF)
val QabasSurfaceLightTranslucent = Color(0xF2FFFFFF)
val QabasSurfaceLightElevated = Color(0xFFF0F4FA)
val QabasSurfaceLightBorder = Color(0xFFDDE3EE)
val QabasGoldLightMode = Color(0xFFB88E18)

// ==========================================
// FIVE CORE PILLARS COLORS (ألوان الأركان الخمسة)
// ==========================================

// 1. Studio (الاستوديو - أزرق)
val StudioBlue = Color(0xFF2563EB)
val StudioBlueLight = Color(0xFF60A5FA)
val StudioBlueGlow = Color(0xFF93C5FD)
val StudioBlueBg = Color(0x262563EB)

// 2. Smart Companion (الرفيق الذكي - بنفسجي)
val CompanionPurple = Color(0xFF8B5CF6)
val CompanionPurpleLight = Color(0xFFA78BFA)
val CompanionPurpleGlow = Color(0xFFC4B5FD)
val CompanionPurpleBg = Color(0x268B5CF6)

// 3. Mihrab (المحراب - أخضر)
val MihrabGreen = Color(0xFF10B981)
val MihrabGreenLight = Color(0xFF34D399)
val MihrabGreenGlow = Color(0xFF6EE7B7)
val MihrabGreenBg = Color(0x2610B981)

// 4. Seeking Knowledge (طلب العلم - تركوازي)
val KnowledgeTurquoise = Color(0xFF06B6D4)
val KnowledgeTurquoiseLight = Color(0xFF22D3EE)
val KnowledgeTurquoiseGlow = Color(0xFF67E8F9)
val KnowledgeTurquoiseBg = Color(0x2606B6D4)

// 5. Impact (الأثر - ذهبي زيتي)
val ImpactOliveGold = Color(0xFFA38A00)
val ImpactOliveGoldLight = Color(0xFFD4B838)
val ImpactOliveGoldGlow = Color(0xFFF3D866)
val ImpactOliveGoldBg = Color(0x26A38A00)

// ==========================================
// TEXT & UTILITY COLORS
// ==========================================
val TextPrimaryDark = Color(0xFFF1F5F9)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextMutedDark = Color(0xFF64748B)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextMutedLight = Color(0xFF94A3B8)

// Cosmic Ambient Glows
val CosmicStarWhite = Color(0xFFE2E8F0)
val CosmicStarGold = Color(0xFFFFDF7D)
val CosmicAmbientBlue = Color(0xFF0B1B33)

// Gradient Brushes
val GoldGradient = Brush.linearGradient(
    colors = listOf(QabasGoldLight, QabasGold, QabasGoldDark)
)

val DarkSurfaceGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF131B2C).copy(alpha = 0.9f),
        Color(0xFF0A0F1A).copy(alpha = 0.95f)
    )
)
