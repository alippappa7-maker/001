package com.example.ui.screens.studio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasThemeTokens

/**
 * شارة تُظهر حالة مورد المشهد: هل لديه صورة/فيديو حقيقي، أم لون احتياطي؟
 * تُعرض في شاشة الخطة والمعاينة لإعلام المستخدم بحالة كل مشهد.
 */
@Composable
fun SceneResourceBadge(
    hasRealAsset: Boolean,
    isVideo: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor: Color
    val icon = if (hasRealAsset) {
        bgColor = QabasGold.copy(alpha = 0.18f)
        if (isVideo) Icons.Default.VideoFile else Icons.Default.Image
    } else {
        bgColor = QabasThemeTokens.colors.textSecondary.copy(alpha = 0.12f)
        Icons.Default.CheckCircle
    }
    val label = when {
        hasRealAsset && isVideo -> "فيديو"
        hasRealAsset -> "صورة"
        else -> "لون تلقائي"
    }
    val tint = if (hasRealAsset) QabasGold else QabasThemeTokens.colors.textSecondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp
        )
    }
}
