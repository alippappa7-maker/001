package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun AuthDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String?) -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val colors = QabasThemeTokens.colors
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = QabasDimens.Space16)
                .testTag("dialog_auth"),
            shape = RoundedCornerShape(QabasDimens.Radius24),
            color = colors.surface,
            border = BorderStroke(QabasDimens.BorderThin, colors.gold.copy(alpha = 0.35f)),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(QabasDimens.Space20),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row with Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.auth_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.textPrimary
                    )

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.testTag("btn_auth_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space12))

                // Tab Switch: Sign In vs Sign Up
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, RoundedCornerShape(QabasDimens.Radius12))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QabasButton(
                        text = stringResource(id = R.string.auth_login_tab),
                        onClick = { isRegisterMode = false },
                        variant = if (!isRegisterMode) QabasButtonVariant.PrimaryGold else QabasButtonVariant.SecondarySurface,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_auth_login")
                    )

                    QabasButton(
                        text = stringResource(id = R.string.auth_register_tab),
                        onClick = { isRegisterMode = true },
                        variant = if (isRegisterMode) QabasButtonVariant.PrimaryGold else QabasButtonVariant.SecondarySurface,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_auth_register")
                    )
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space16))

                // Error Message Banner if present
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = QabasDimens.Space12)
                            .testTag("banner_auth_error"),
                        shape = RoundedCornerShape(QabasDimens.Radius8),
                        color = colors.statusInactive.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, colors.statusInactive.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.statusInactive,
                            modifier = Modifier.padding(QabasDimens.Space10)
                        )
                    }
                }

                // Name field (Sign Up mode only)
                if (isRegisterMode) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text(stringResource(id = R.string.auth_name_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.gold
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = getTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_auth_name")
                    )
                    Spacer(modifier = Modifier.height(QabasDimens.Space10))
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(id = R.string.auth_email_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = colors.gold
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = getTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_email")
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space10))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(id = R.string.auth_password_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.gold
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = colors.textSecondary
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (isRegisterMode) {
                            onSignUp(email, password, displayName.ifBlank { null })
                        } else {
                            onSignIn(email, password)
                        }
                    }),
                    colors = getTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_password")
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space16))

                // Primary Submit Button (Login or Register)
                QabasButton(
                    text = if (isRegisterMode) {
                        stringResource(id = R.string.auth_submit_register)
                    } else {
                        stringResource(id = R.string.auth_submit_login)
                    },
                    onClick = {
                        focusManager.clearFocus()
                        if (isRegisterMode) {
                            onSignUp(email, password, displayName.ifBlank { null })
                        } else {
                            onSignIn(email, password)
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    isLoading = isLoading,
                    variant = QabasButtonVariant.PrimaryGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_auth_submit")
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space10))

                // Google Sign In Option
                QabasButton(
                    text = stringResource(id = R.string.auth_google_sign_in),
                    onClick = onGoogleSignIn,
                    enabled = !isLoading,
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_auth_google")
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space10))

                // Continue as Guest button
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    modifier = Modifier.testTag("btn_auth_continue_guest")
                ) {
                    Text(
                        text = stringResource(id = R.string.auth_guest_continue),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun getTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = QabasThemeTokens.colors.gold,
    unfocusedBorderColor = QabasThemeTokens.colors.surfaceBorder,
    focusedLabelColor = QabasThemeTokens.colors.gold,
    unfocusedLabelColor = QabasThemeTokens.colors.textSecondary,
    focusedTextColor = QabasThemeTokens.colors.textPrimary,
    unfocusedTextColor = QabasThemeTokens.colors.textPrimary,
    cursorColor = QabasThemeTokens.colors.gold
)
