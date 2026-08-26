package com.example.ui.screens.studio.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasSurfaceDark
import com.example.ui.theme.QabasSurfaceDarkElevated
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasGoldDark
import com.example.ui.theme.QabasGoldLight
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

/**
 * نظام تصميم حديث وموحّد لشاشات الاستوديو.
 * مبادئ: مساحات سخية، زوايا دائرية ناعمة، حدود متدرجة ذهبية، تسلسل بصري واضح.
 */

/** خلفية الاستوديو: تدرّج عميق مع توهّج ذهبي ناعم في الأعلى. */
fun studioBackgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        QabasSurfaceDarkElevated,
        QabasSurfaceDark,
        QabasDarkBackground
    )
)

/** توهّج شعاعي ذهبي خفيف يُضاف كطبقة فوق الخلفية لإحساس فاخر. */
fun studioGlowBrush(): Brush = Brush.radialGradient(
    colors = listOf(
        QabasGold.copy(alpha = 0.10f),
        Color.Transparent
    )
)

/**
 * بطاقة زجاجية حديثة: سطح مرفوع بحدّ ذهبي متدرج اختياري وزوايا دائرية واسعة.
 */
@Composable
fun StudioGlassCard(
    modifier: Modifier = Modifier,
    gradientBorder: Boolean = true,
    cornerRadius: Dp = 20.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val base = modifier
        .clip(shape)
        .background(QabasSurfaceDarkElevated)
        .then(
            if (gradientBorder) {
                Modifier.border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(QabasGold.copy(alpha = 0.55f), QabasGoldDark)),
                    shape = shape
                )
            } else {
                Modifier.border(width = 1.dp, color = QabasSurfaceDark, shape = shape)
            }
        )
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(contentPadding)
    Box(modifier = base) { content() }
}

/**
 * شارة حالة ملوّنة صغيرة (Chip) بحواف دائرية كاملة.
 */
@Composable
fun StudioStatusChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * وسم صغير (Pill) لعرض خصائص مثل المدة/الاتجاه/النبرة.
 */
@Composable
fun StudioTagPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(QabasDarkBackground.copy(alpha = 0.55f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = TextSecondaryDark,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * عنوان قسم: نص ذهبي بوزن ثقيل مع اختياري نص فرعي تحته.
 */
@Composable
fun StudioSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = QabasGold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondaryDark,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * رأس شاشة موحّد: زر رجوع + عنوان مركزي + إجراء اختياري.
 */
@Composable
fun StudioTopHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "رجوع",
                tint = QabasGold
            )
        }
        Text(
            text = title,
            color = TextPrimaryDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (action != null) {
            action()
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

/**
 * بطاقة أيقونة دائرية ذهبية تُستخدم كعنصر بصري رائد.
 */
@Composable
fun StudioIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(QabasGold.copy(alpha = 0.14f))
            .border(1.dp, QabasGold.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = QabasGoldLight,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * غلاف يظهر/يختفي بسلاسة للحالات (تحميل/خطأ).
 */
@Composable
fun StudioFadeBox(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) { content() }
}
