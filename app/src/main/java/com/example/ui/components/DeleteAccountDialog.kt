package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun DeleteAccountDialog(
    isOpen: Boolean,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return
    val colors = QabasThemeTokens.colors

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("dialog_delete_account"),
            shape = RoundedCornerShape(QabasDimens.Radius24),
            color = colors.surface,
            border = BorderStroke(QabasDimens.BorderThin, colors.statusInactive.copy(alpha = 0.5f)),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(QabasDimens.Space20),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.statusInactive,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                Text(
                    text = stringResource(id = R.string.auth_delete_confirm_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space8))

                Text(
                    text = stringResource(id = R.string.auth_delete_confirm_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space20))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
                ) {
                    QabasButton(
                        text = stringResource(id = R.string.cancel),
                        onClick = onDismiss,
                        enabled = !isLoading,
                        variant = QabasButtonVariant.SecondarySurface,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_cancel_delete_account")
                    )

                    QabasButton(
                        text = stringResource(id = R.string.auth_delete_btn),
                        onClick = onConfirm,
                        enabled = !isLoading,
                        isLoading = isLoading,
                        variant = QabasButtonVariant.PrimaryGold,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_confirm_delete_account")
                    )
                }
            }
        }
    }
}
