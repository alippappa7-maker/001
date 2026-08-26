package com.example.ui.screens.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.R
import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.VideoDuration
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoLanguage
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoTone
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
import com.example.ui.navigation.Routes
import com.example.ui.screens.studio.components.StudioGlassCard
import com.example.ui.screens.studio.components.StudioSectionHeader
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import com.example.ui.theme.StudioBlue

private val VERSE_KEY_REGEX = Regex("^\\d{1,3}:\\d{1,3}$")

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
    var audience by remember { mutableStateOf(project?.idea?.audience ?: "") }
    var language by remember { mutableStateOf(project?.idea?.language ?: VideoLanguage.ARABIC) }
    var orientation by remember { mutableStateOf(project?.idea?.orientation ?: VideoOrientation.PORTRAIT) }
    var duration by remember { mutableStateOf(project?.idea?.duration ?: VideoDuration.SHORT) }
    var tone by remember { mutableStateOf(project?.idea?.tone ?: VideoTone.INSPIRING) }
    var editingStyle by remember { mutableStateOf(project?.idea?.editingStyle ?: EditingStyle.CINEMATIC) }
    var hasVoiceover by remember { mutableStateOf(project?.idea?.hasVoiceover ?: true) }
    var hasOnScreenText by remember { mutableStateOf(project?.idea?.hasOnScreenText ?: true) }
    var hasMusicOrEffects by remember { mutableStateOf(project?.idea?.hasMusicOrEffects ?: true) }
    var verseKey by remember { mutableStateOf(project?.idea?.verseKey ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    Scaffold(
        topBar = {
            QabasTopBar(
                title = stringResource(R.string.studio_create_title),
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
        modifier = Modifier.testTag("screen_studio_create"),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space12)
            ) {
                QabasButton(
                    text = stringResource(R.string.studio_btn_analyze_idea),
                    onClick = {
                        val isQuran = editingStyle == EditingStyle.QURAN_RECITATION
                        val verseKeyTrim = verseKey.trim()

                        if (isQuran && !VERSE_KEY_REGEX.matches(verseKeyTrim)) {
                            errorMessage = "أدخل مفتاح آية صحيحًا (مثال: 2:255)"
                            return@QabasButton
                        }
                        if (!isQuran && ideaText.trim().isBlank()) {
                            errorMessage = "يرجى كتابة فكرة الفيديو أولاً للمتابعة"
                            return@QabasButton
                        }

                        val resolvedIdeaText = if (isQuran && ideaText.trim().isBlank()) {
                            "تلاوة قرآنية: $verseKeyTrim"
                        } else {
                            ideaText.trim()
                        }

                        val updatedIdea = VideoIdea(
                            ideaText = resolvedIdeaText,
                            language = language,
                            orientation = orientation,
                            duration = duration,
                            audience = audience.trim(),
                            tone = tone,
                            editingStyle = editingStyle,
                            hasVoiceover = hasVoiceover,
                            hasOnScreenText = hasOnScreenText,
                            hasMusicOrEffects = hasMusicOrEffects,
                            verseKey = if (isQuran) verseKeyTrim else ""
                        )
                        viewModel.updateIdea(updatedIdea)
                        viewModel.analyzeIdea()
                        navController.navigate(Routes.STUDIO_ANALYSIS)
                    },
                    variant = QabasButtonVariant.PrimaryGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_analyze_idea")
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
                    .padding(horizontal = QabasDimens.Space16, vertical = QabasDimens.Space12),
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space20)
            ) {
                // 1. Large Idea Input Field
                SectionCard(title = stringResource(R.string.studio_idea_label), required = true) {
                    OutlinedTextField(
                        value = ideaText,
                        onValueChange = { ideaText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("input_idea_text"),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.studio_idea_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.gold,
                            unfocusedBorderColor = colors.surfaceElevated,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = colors.gold,
                            focusedContainerColor = colors.surfaceElevated.copy(alpha = 0.4f),
                            unfocusedContainerColor = colors.surfaceElevated.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(QabasDimens.Radius12),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }

                // 2. Target Audience Input Field
                SectionCard(title = stringResource(R.string.studio_audience_label)) {
                    OutlinedTextField(
                        value = audience,
                        onValueChange = { audience = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_audience"),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.studio_audience_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.gold,
                            unfocusedBorderColor = colors.surfaceElevated,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = colors.gold,
                            focusedContainerColor = colors.surfaceElevated.copy(alpha = 0.4f),
                            unfocusedContainerColor = colors.surfaceElevated.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(QabasDimens.Radius12),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }

                // 3. Language Selector
                SectionCard(title = stringResource(R.string.studio_language_label)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                    ) {
                        VideoLanguage.values().forEach { lang ->
                            OptionChip(
                                label = lang.titleAr,
                                selected = language == lang,
                                onClick = { language = lang },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. Orientation Selector (9:16, 16:9, 1:1)
                SectionCard(title = stringResource(R.string.studio_orientation_label)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                    ) {
                        OrientationChip(
                            title = "عمودي 9:16",
                            subtitle = "ريلز وستوري",
                            icon = Icons.Default.CropPortrait,
                            selected = orientation == VideoOrientation.PORTRAIT,
                            onClick = { orientation = VideoOrientation.PORTRAIT },
                            modifier = Modifier.weight(1f)
                        )
                        OrientationChip(
                            title = "أفقي 16:9",
                            subtitle = "يوتيوب وشاشات",
                            icon = Icons.Default.Tv,
                            selected = orientation == VideoOrientation.LANDSCAPE,
                            onClick = { orientation = VideoOrientation.LANDSCAPE },
                            modifier = Modifier.weight(1f)
                        )
                        OrientationChip(
                            title = "مربع 1:1",
                            subtitle = "منشورات عامة",
                            icon = Icons.Default.CropSquare,
                            selected = orientation == VideoOrientation.SQUARE,
                            onClick = { orientation = VideoOrientation.SQUARE },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 5. Duration Selector (15, 30, 60s)
                SectionCard(title = stringResource(R.string.studio_duration_label)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space8)
                    ) {
                        VideoDuration.values().forEach { dur ->
                            OptionChip(
                                label = dur.labelAr,
                                selected = duration == dur,
                                onClick = { duration = dur },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 6. Video Tone Selector
                SectionCard(title = stringResource(R.string.studio_tone_label)) {
                    FlowGridSelection(
                        items = VideoTone.values().toList(),
                        selectedItem = tone,
                        itemLabel = { it.titleAr },
                        onSelect = { tone = it }
                    )
                }

                // 7. Editing Style Selector
                SectionCard(title = stringResource(R.string.studio_editing_style_label)) {
                    FlowGridSelection(
                        items = EditingStyle.values().toList(),
                        selectedItem = editingStyle,
                        itemLabel = { it.titleAr },
                        onSelect = { editingStyle = it }
                    )
                }

                // 7b. Quran verse key (يظهر فقط لنمط التلاوة القرآنية)
                if (editingStyle == EditingStyle.QURAN_RECITATION) {
                    SectionCard(title = "مفتاح الآية", required = true) {
                        OutlinedTextField(
                            value = verseKey,
                            onValueChange = { verseKey = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_verse_key"),
                            placeholder = {
                                Text(
                                    text = "مثال: 2:255 (السورة:رقم الآية)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary.copy(alpha = 0.6f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.gold,
                                unfocusedBorderColor = colors.surfaceElevated,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                cursorColor = colors.gold,
                                focusedContainerColor = colors.surfaceElevated.copy(alpha = 0.4f),
                                unfocusedContainerColor = colors.surfaceElevated.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(QabasDimens.Radius12),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 8. Toggles (Voiceover, On-screen text, Music/Effects)
                SectionCard(title = stringResource(R.string.studio_options_title)) {
                    Column(verticalArrangement = Arrangement.spacedBy(QabasDimens.Space8)) {
                        ToggleRow(
                            title = stringResource(R.string.studio_option_voiceover),
                            icon = Icons.Default.Mic,
                            checked = hasVoiceover,
                            onCheckedChange = { hasVoiceover = it }
                        )
                        ToggleRow(
                            title = stringResource(R.string.studio_option_on_screen_text),
                            icon = Icons.Default.Subtitles,
                            checked = hasOnScreenText,
                            onCheckedChange = { hasOnScreenText = it }
                        )
                        ToggleRow(
                            title = stringResource(R.string.studio_option_music_effects),
                            icon = Icons.Default.GraphicEq,
                            checked = hasMusicOrEffects,
                            onCheckedChange = { hasMusicOrEffects = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space24))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    required: Boolean = false,
    content: @Composable () -> Unit
) {
    StudioGlassCard(
        modifier = Modifier.fillMaxWidth(),
        gradientBorder = false,
        cornerRadius = QabasDimens.Radius16,
        contentPadding = PaddingValues(QabasDimens.Space16)
    ) {
        StudioSectionHeader(
            title = if (required) "$title *" else title
        )
        Spacer(modifier = Modifier.height(QabasDimens.Space12))
        content()
    }
}

@Composable
private fun OptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QabasThemeTokens.colors
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(RoundedCornerShape(QabasDimens.Radius8))
            .background(if (selected) colors.gold.copy(alpha = 0.2f) else colors.background)
            .border(
                1.dp,
                if (selected) colors.gold else colors.surfaceElevated,
                RoundedCornerShape(QabasDimens.Radius8)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) colors.gold else colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OrientationChip(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QabasThemeTokens.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(QabasDimens.Radius10))
            .background(if (selected) colors.gold.copy(alpha = 0.18f) else colors.background)
            .border(
                1.dp,
                if (selected) colors.gold else colors.surfaceElevated,
                RoundedCornerShape(QabasDimens.Radius10)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) colors.gold else colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (selected) colors.gold else colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun <T> FlowGridSelection(
    items: List<T>,
    selectedItem: T,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    val chunked = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    OptionChip(
                        label = itemLabel(item),
                        selected = item == selectedItem,
                        onClick = { onSelect(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remainder of row if incomplete
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = QabasThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QabasDimens.Radius8))
            .background(colors.background)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (checked) colors.gold else colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.surface,
                checkedTrackColor = colors.gold,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.surfaceElevated
            )
        )
    }
}
