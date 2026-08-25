package com.example.ui.screens.studio.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasThemeTokens

/**
 * ترويسة موحّدة لخطوات الاستوديو: عنوان + وصف مختصر + رقم الخطوة.
 * تُستخدم عبر شاشات (الفكرة ← التحليل ← الخطة ← المعاينة) لضمان
 * لون وخط موحّدين وتقليل التكرار.
 */
@Composable
fun StudioStepHeader(
    title: String,
    subtitle: String,
    stepNumber: Int? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        if (stepNumber != null) {
            Text(
                text = "خطوة $stepNumber",
                color = QabasGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = title,
            color = QabasThemeTokens.colors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = QabasThemeTokens.colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
