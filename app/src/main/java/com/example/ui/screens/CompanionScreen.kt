package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.model.CompanionMessage
import com.example.domain.model.CompanionStatus
import com.example.ui.CompanionViewModel
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
import com.example.ui.theme.CompanionPurple
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

@Composable
fun CompanionScreen(
    onBack: () -> Unit,
    companionViewModel: CompanionViewModel = viewModel()
) {
    val uiState by companionViewModel.uiState.collectAsState()
    val colors = QabasThemeTokens.colors
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(uiState.messages.size, uiState.status) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_companion"),
        topBar = {
            QabasTopBar(
                title = stringResource(id = R.string.feature_companion_title),
                onBack = onBack,
                actions = {
                    if (uiState.messages.isNotEmpty()) {
                        IconButton(
                            onClick = { companionViewModel.showClearDialog(true) },
                            modifier = Modifier.testTag("btn_clear_companion_chat")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = stringResource(id = R.string.companion_clear),
                                tint = colors.textSecondary
                            )
                        }
                    }
                }
            )
        },
        containerColor = colors.background
    ) { padding ->
        StarryBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = QabasDimens.Space16)
            ) {
                Spacer(modifier = Modifier.height(QabasDimens.Space8))

                // Prominent Legal/Religious Disclaimer Banner
                CompanionDisclaimerCard()

                Spacer(modifier = Modifier.height(QabasDimens.Space8))

                // Chat Messages or Empty Suggested Prompts
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (uiState.messages.isEmpty()) {
                        CompanionEmptyState(
                            isSaveHistoryEnabled = uiState.isSaveHistoryEnabled,
                            onToggleSaveHistory = { companionViewModel.toggleSaveHistory(it) },
                            onSelectPrompt = { companionViewModel.sendMessage(customPrompt = it) }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("companion_messages_list"),
                            verticalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
                        ) {
                            items(
                                items = uiState.messages,
                                key = { it.id }
                            ) { message ->
                                MessageBubble(
                                    message = message,
                                    onReport = { companionViewModel.showReportDialog(message.id) },
                                    onRetry = { companionViewModel.retryLastMessage() }
                                )
                            }

                            if (uiState.status == CompanionStatus.GENERATING) {
                                item {
                                    GeneratingIndicatorBubble(
                                        onStop = { companionViewModel.stopGenerating() }
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(QabasDimens.Space12))
                            }
                        }
                    }
                }

                // Bottom Input Area
                CompanionInputBar(
                    inputText = uiState.inputText,
                    maxChars = uiState.maxInputChars,
                    isGenerating = uiState.status == CompanionStatus.GENERATING,
                    onTextChanged = { companionViewModel.onInputChanged(it) },
                    onSend = { companionViewModel.sendMessage() },
                    onStop = { companionViewModel.stopGenerating() },
                    modifier = Modifier.navigationBarsPadding()
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space8))
            }
        }
    }

    // Clear Confirmation Dialog
    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { companionViewModel.showClearDialog(false) },
            title = {
                Text(
                    text = stringResource(id = R.string.companion_clear_confirm_title),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.companion_clear_confirm_desc),
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { companionViewModel.clearChat() },
                    modifier = Modifier.testTag("btn_confirm_clear_chat")
                ) {
                    Text(
                        text = stringResource(id = R.string.companion_clear),
                        color = colors.statusInactive,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { companionViewModel.showClearDialog(false) }) {
                    Text(text = stringResource(id = R.string.cancel), color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(QabasDimens.Radius20)
        )
    }

    // Report Message Dialog
    if (uiState.showReportDialog) {
        AlertDialog(
            onDismissRequest = { companionViewModel.dismissReportDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = colors.gold,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(id = R.string.companion_report_dialog_title),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.companion_report_dialog_desc),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        companionViewModel.confirmReport()
                        Toast.makeText(context, context.getString(R.string.companion_report_dialog_desc), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("btn_confirm_report")
                ) {
                    Text(
                        text = stringResource(id = R.string.companion_report_dismiss),
                        color = colors.gold,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(QabasDimens.Radius20)
        )
    }
}

@Composable
fun CompanionDisclaimerCard() {
    val colors = QabasThemeTokens.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_companion_disclaimer"),
        shape = RoundedCornerShape(QabasDimens.Radius12),
        colors = CardDefaults.cardColors(
            containerColor = colors.gold.copy(alpha = 0.08f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = colors.gold.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = QabasDimens.Space12, vertical = QabasDimens.Space8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = colors.gold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(QabasDimens.Space8))
            Text(
                text = stringResource(id = R.string.companion_disclaimer),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = colors.gold,
                lineHeight = 15.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompanionEmptyState(
    isSaveHistoryEnabled: Boolean,
    onToggleSaveHistory: (Boolean) -> Unit,
    onSelectPrompt: (String) -> Unit
) {
    val colors = QabasThemeTokens.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = QabasDimens.Space16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Assistant Avatar & Welcome
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(CompanionPurple.copy(alpha = 0.3f), colors.surface)
                        )
                    )
                    .border(1.5.dp, CompanionPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.gold,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space12))

            Text(
                text = stringResource(id = R.string.feature_companion_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(QabasDimens.Space4))

            Text(
                text = stringResource(id = R.string.companion_welcome_message),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = QabasDimens.Space16)
            )

            Spacer(modifier = Modifier.height(QabasDimens.Space16))

            // Suggested Topics Header
            Text(
                text = stringResource(id = R.string.companion_suggested_prompts_title),
                style = MaterialTheme.typography.labelMedium,
                color = colors.gold,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(QabasDimens.Space8))

            val prompts = listOf(
                stringResource(id = R.string.companion_prompt_1),
                stringResource(id = R.string.companion_prompt_2),
                stringResource(id = R.string.companion_prompt_3),
                stringResource(id = R.string.companion_prompt_4)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
            ) {
                prompts.forEach { prompt ->
                    SuggestedPromptChip(prompt = prompt, onClick = { onSelectPrompt(prompt) })
                }
            }
        }

        // Save Chat Consent Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = QabasDimens.Space16),
            shape = RoundedCornerShape(QabasDimens.Radius16),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(0.5.dp, colors.surfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = QabasDimens.Space12, vertical = QabasDimens.Space10),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.companion_save_history_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = stringResource(id = R.string.companion_save_history_desc),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.width(QabasDimens.Space8))

                Switch(
                    checked = isSaveHistoryEnabled,
                    onCheckedChange = onToggleSaveHistory,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.gold,
                        checkedTrackColor = colors.gold.copy(alpha = 0.3f),
                        uncheckedThumbColor = colors.textMuted,
                        uncheckedTrackColor = colors.surface
                    ),
                    modifier = Modifier.testTag("switch_save_chat_history")
                )
            }
        }
    }
}

@Composable
fun SuggestedPromptChip(
    prompt: String,
    onClick: () -> Unit
) {
    val colors = QabasThemeTokens.colors

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(QabasDimens.Radius20))
            .background(colors.surface)
            .border(1.dp, colors.gold.copy(alpha = 0.3f), RoundedCornerShape(QabasDimens.Radius20))
            .clickable(onClick = onClick)
            .padding(horizontal = QabasDimens.Space12, vertical = QabasDimens.Space8)
            .testTag("chip_prompt")
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MessageBubble(
    message: CompanionMessage,
    onReport: () -> Unit,
    onRetry: () -> Unit
) {
    val colors = QabasThemeTokens.colors
    val context = LocalContext.current

    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bubbleBackground = when {
        message.isError -> colors.statusInactive.copy(alpha = 0.12f)
        message.isUser -> colors.gold.copy(alpha = 0.15f)
        else -> colors.surface
    }

    val borderColor = when {
        message.isError -> colors.statusInactive.copy(alpha = 0.4f)
        message.isUser -> colors.gold.copy(alpha = 0.4f)
        else -> colors.surfaceBorder
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CompanionPurple.copy(alpha = 0.2f))
                        .border(1.dp, CompanionPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.gold,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(QabasDimens.Space8))
            }

            Column(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleBackground)
                    .border(1.dp, borderColor, bubbleShape)
                    .padding(horizontal = QabasDimens.Space12, vertical = QabasDimens.Space10)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isError) colors.statusInactive else colors.textPrimary,
                    lineHeight = 20.sp
                )

                if (!message.isUser && !message.isError) {
                    Spacer(modifier = Modifier.height(QabasDimens.Space6))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Copy Button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Qabas Companion", message.text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ النص", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = colors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(QabasDimens.Space8))

                        // Report Button
                        IconButton(
                            onClick = onReport,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("btn_report_message")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = stringResource(id = R.string.companion_report_button),
                                tint = if (message.isReported) colors.gold else colors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                if (message.isError) {
                    Spacer(modifier = Modifier.height(QabasDimens.Space6))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onRetry,
                            modifier = Modifier.testTag("btn_retry_companion")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(id = R.string.companion_retry),
                                color = colors.gold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratingIndicatorBubble(onStop: () -> Unit) {
    val colors = QabasThemeTokens.colors
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = QabasDimens.Space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(CompanionPurple.copy(alpha = 0.2f))
                .border(1.dp, CompanionPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = colors.gold,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(QabasDimens.Space8))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = QabasDimens.Space12, vertical = QabasDimens.Space8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = colors.gold,
                strokeWidth = 2.dp
            )

            Spacer(modifier = Modifier.width(QabasDimens.Space8))

            Text(
                text = "جاري التفكير والتوليد...",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary.copy(alpha = alpha)
            )

            Spacer(modifier = Modifier.width(QabasDimens.Space8))

            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("btn_stop_generating")
            ) {
                Icon(
                    imageVector = Icons.Default.StopCircle,
                    contentDescription = stringResource(id = R.string.companion_stop),
                    tint = colors.statusInactive,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CompanionInputBar(
    inputText: String,
    maxChars: Int,
    isGenerating: Boolean,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QabasThemeTokens.colors

    Column(modifier = modifier.fillMaxWidth()) {
        // Character counter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = QabasDimens.Space4, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.companion_model_badge),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = colors.textMuted
            )

            Text(
                text = stringResource(id = R.string.companion_chars_count, inputText.length, maxChars),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (inputText.length > maxChars * 0.9) colors.gold else colors.textMuted
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.companion_input_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                },
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (!isGenerating && inputText.isNotBlank()) onSend() }),
                shape = RoundedCornerShape(QabasDimens.Radius20),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.gold,
                    unfocusedBorderColor = colors.surfaceBorder,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_companion_prompt")
            )

            Spacer(modifier = Modifier.width(QabasDimens.Space8))

            if (isGenerating) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.statusInactive.copy(alpha = 0.15f))
                        .border(1.dp, colors.statusInactive, CircleShape)
                        .testTag("btn_companion_stop")
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = stringResource(id = R.string.companion_stop),
                        tint = colors.statusInactive,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) colors.gold else colors.surface
                        )
                        .border(1.dp, if (inputText.isNotBlank()) colors.gold else colors.surfaceBorder, CircleShape)
                        .testTag("btn_companion_send")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(id = R.string.companion_send),
                        tint = if (inputText.isNotBlank()) colors.background else colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
