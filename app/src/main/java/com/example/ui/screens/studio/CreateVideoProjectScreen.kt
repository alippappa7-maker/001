package com.example.ui.screens.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.VideoDuration
import com.example.domain.model.studio.VideoLanguage
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoTone
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.Routes
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import com.example.ui.theme.StudioBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVideoProjectScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors
    val project by viewModel.currentProject.collectAsState()
    
    var ideaText by remember { mutableStateOf(project?.idea?.ideaText ?: "") }
    var language by remember { mutableStateOf(project?.idea?.language ?: VideoLanguage.ARABIC) }
    var orientation by remember { mutableStateOf(project?.idea?.orientation ?: VideoOrientation.PORTRAIT) }
    var duration by remember { mutableStateOf(project?.idea?.duration ?: VideoDuration.SHORT) }
    var audience by remember { mutableStateOf(project?.idea?.audience ?: "") }
    var tone by remember { mutableStateOf(project?.idea?.tone ?: VideoTone.INSPIRING) }
    var editingStyle by remember { mutableStateOf(project?.idea?.editingStyle ?: EditingStyle.CINEMATIC) }
    var voiceover by remember { mutableStateOf(project?.idea?.hasVoiceover ?: true) }
    var onScreenText by remember { mutableStateOf(project?.idea?.hasOnScreenText ?: true) }
    var music by remember { mutableStateOf(project?.idea?.hasMusicOrEffects ?: true) }

    Scaffold(
        topBar = {
            QabasTopBar(
                title = "فكرة الفيديو",
                onBack = onBack
            )
        },
        containerColor = colors.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(QabasDimens.Space16)
            ) {
                QabasButton(
                    text = "حلّل الفكرة",
                    onClick = {
                        project?.let {
                            val updatedIdea = it.idea.copy(
                                ideaText = ideaText,
                                language = language,
                                orientation = orientation,
                                duration = duration,
                                audience = audience,
                                tone = tone,
                                editingStyle = editingStyle,
                                hasVoiceover = voiceover,
                                hasOnScreenText = onScreenText,
                                hasMusicOrEffects = music
                            )
                            viewModel.updateIdea(updatedIdea)
                            viewModel.analyzeIdea()
                            navController.navigate(Routes.STUDIO_ANALYSIS) {
                                popUpTo(Routes.STUDIO_CREATE) { inclusive = true }
                            }
                        }
                    },
                    variant = QabasButtonVariant.PrimaryGold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { padding ->
        StarryBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(QabasDimens.Space16),
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space24)
            ) {
                // Idea Text Field
                Column {
                    Text("ما هي فكرة الفيديو؟", color = colors.gold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ideaText,
                        onValueChange = { ideaText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = { Text("اكتب فكرتك هنا بالتفصيل...") },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = StudioBlue,
                            unfocusedIndicatorColor = colors.surfaceElevated,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = StudioBlue,
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Audience
                Column {
                    Text("الجمهور المستهدف", color = colors.gold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = audience,
                        onValueChange = { audience = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("مثال: الشباب، الطلاب، الأمهات...") },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = StudioBlue,
                            unfocusedIndicatorColor = colors.surfaceElevated,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = StudioBlue,
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Settings Group
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SelectionRow("اللغة", VideoLanguage.values().toList(), language) { language = it }
                    SelectionRow("الاتجاه", VideoOrientation.values().toList(), orientation) { orientation = it }
                    SelectionRow("المدة", VideoDuration.values().toList(), duration) { duration = it }
                    SelectionRow("نبرة الفيديو", VideoTone.values().toList(), tone) { tone = it }
                    SelectionRow("أسلوب المونتاج", EditingStyle.values().toList(), editingStyle) { editingStyle = it }
                }

                // Toggles
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("عناصر الفيديو", color = colors.gold, style = MaterialTheme.typography.titleMedium)
                    ToggleRow("تعليق صوتي", voiceover) { voiceover = it }
                    ToggleRow("نصوص على الشاشة", onScreenText) { onScreenText = it }
                    ToggleRow("موسيقى أو مؤثرات", music) { music = it }
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
            }
        }
    }
}

@Composable
fun <T: Enum<T>> SelectionRow(title: String, options: List<T>, selected: T, onSelect: (T) -> Unit) {
    val colors = QabasThemeTokens.colors
    Column {
        Text(title, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selected == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) StudioBlue else colors.surfaceElevated)
                        .clickable { onSelect(option) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) colors.surface else colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = QabasThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.surface,
                checkedTrackColor = StudioBlue,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.surfaceElevated
            )
        )
    }
}
