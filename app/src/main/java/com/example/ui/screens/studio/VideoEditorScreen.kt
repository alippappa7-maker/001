package com.example.ui.screens.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.domain.model.studio.AssetLicense
import com.example.domain.model.studio.AssetType
import com.example.domain.model.studio.LicensedAsset
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoScene
import com.example.ui.navigation.Routes
import com.example.ui.screens.studio.components.StudioGlassCard
import com.example.ui.screens.studio.components.studioBackgroundBrush
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasGoldDark
import com.example.ui.theme.QabasGoldLight
import com.example.ui.theme.QabasSurfaceDarkElevated
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel
) {
    val currentProject by viewModel.currentProject.collectAsState()
    val localAvailableResources by viewModel.localAvailableResources.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    val scenes = currentProject?.plan?.scenes ?: emptyList()
    var selectedSceneIndex by remember { mutableIntStateOf(0) }
    val selectedScene = scenes.getOrNull(selectedSceneIndex)

    var showAddResourceDialog by remember { mutableStateOf(false) }
    var showResourcePickerForSceneId by remember { mutableStateOf<String?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("video_editor_screen"),
            containerColor = QabasDarkBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "محرر المشاهد والجدول الزمني",
                            color = QabasGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("editor_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "الرجوع",
                                tint = QabasGold
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Routes.STUDIO_PREVIEW) },
                            modifier = Modifier.testTag("editor_preview_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "المعاينة التفاعلية",
                                tint = QabasGold
                            )
                        }
                        IconButton(
                            onClick = { viewModel.saveCurrentProject() },
                            modifier = Modifier.testTag("editor_save_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "حفظ",
                                tint = QabasGold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = QabasSurfaceDarkElevated
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(studioBackgroundBrush())
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 1. Scene Preview Canvas Card
                val orientation = currentProject?.idea?.orientation ?: VideoOrientation.PORTRAIT
                val previewAspectRatio = when (orientation) {
                    VideoOrientation.PORTRAIT -> 9f / 16f
                    VideoOrientation.LANDSCAPE -> 16f / 9f
                    VideoOrientation.SQUARE -> 1f
                }

                StudioGlassCard(
                    modifier = Modifier
                        .fillMaxWidth(if (orientation == VideoOrientation.PORTRAIT) 0.85f else 1f)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .testTag("editor_preview_card"),
                    gradientBorder = true,
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(previewAspectRatio)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF1E2742), Color(0xFF0B101E), Color(0xFF04060C))
                                )
                            )
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedScene != null) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "المشهد ${selectedSceneIndex + 1} من ${scenes.size}",
                                        color = QabasGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${selectedScene.durationSeconds} ثوانٍ",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = selectedScene.onScreenText.ifBlank { "نص الشاشة للمشهد المحدد" },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.testTag("editor_canvas_text")
                                    )
                                    if (!selectedScene.attachedAssetTitle.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "مورد: ${selectedScene.attachedAssetTitle}",
                                            color = Color(0xFFA5D6A7),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "انتقال: ${selectedScene.transition}",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                            }
                        } else {
                            Text("حدد مشهدًا لتحريره", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                }

                // 2. Visual Timeline Bar
                StudioGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("editor_timeline_card"),
                    gradientBorder = false,
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = QabasGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("الجدول الزمني", color = QabasGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                text = "المدة الإجمالية: ${scenes.sumOf { it.durationSeconds }} ثانية",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF090E18))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            scenes.forEachIndexed { index, sc ->
                                val isSelected = index == selectedSceneIndex
                                Box(
                                    modifier = Modifier
                                        .weight(sc.durationSeconds.toFloat().coerceAtLeast(1f))
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) Brush.horizontalGradient(listOf(QabasGold, QabasGoldLight))
                                            else Brush.horizontalGradient(listOf(Color(0xFF1F293D), Color(0xFF192233)))
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.5.dp,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedSceneIndex = index }
                                        .testTag("timeline_segment_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1} (${sc.durationSeconds}ث)",
                                        color = if (isSelected) QabasDarkBackground else Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Fallback Resource Mode Controller
                val fallbackMode = currentProject?.fallbackMode
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("fallback_resource_section"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10192A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(QabasGoldDark.copy(alpha = 0.5f), QabasGold.copy(alpha = 0.5f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = QabasGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("المسار الاحتياطي للموارد", color = QabasGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("استخدام وسائط ومخطوطات محلية مرخصة", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = fallbackMode?.isEnabled == true,
                                onCheckedChange = { viewModel.toggleFallbackResourceMode(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = QabasGold, checkedTrackColor = QabasGoldDark),
                                modifier = Modifier.testTag("fallback_mode_switch")
                            )
                        }

                        AnimatedVisibility(visible = fallbackMode?.isEnabled == true) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = fallbackMode?.userConfirmedConsent == true,
                                        onCheckedChange = { viewModel.setFallbackConsent(it) },
                                        colors = CheckboxDefaults.colors(checkedColor = QabasGold, checkmarkColor = QabasDarkBackground),
                                        modifier = Modifier.testTag("fallback_consent_checkbox")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "أتعهد بملكية الموارد المرفوعة أو كونها مرخصة ومصرح بها نظاميًا.",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { showAddResourceDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("add_local_resource_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QabasGoldLight)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة مورد محلي جديد مع التحقق من الترخيص", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 4. Scene List & Scene-Level Editor
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("قائمة المشاهد والتعديل التفصيلي", color = QabasGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Button(
                        onClick = { viewModel.addScene() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = QabasGold, contentColor = QabasDarkBackground),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("editor_add_scene_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مشهد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                scenes.forEachIndexed { index, scene ->
                    val isSelected = index == selectedSceneIndex
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("scene_card_$index"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF151F33) else Color(0xFF0F1523)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                if (isSelected) listOf(QabasGold, QabasGoldDark)
                                else listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Scene Header Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) QabasGold else Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = if (isSelected) QabasDarkBackground else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "المشهد ${index + 1}",
                                        color = QabasGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.moveSceneUp(index) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "أعلى", tint = QabasGold, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.moveSceneDown(index) },
                                        enabled = index < scenes.size - 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "أسفل", tint = QabasGold, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteScene(scene.id) },
                                        enabled = scenes.size > 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF8A80), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Duration Stepper & Transition
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Duration
                                OutlinedTextField(
                                    value = scene.durationSeconds.toString(),
                                    onValueChange = { newVal ->
                                        val sec = newVal.filter { it.isDigit() }.toIntOrNull() ?: 1
                                        viewModel.updateScene(scene.copy(durationSeconds = sec.coerceIn(1, 30)))
                                    },
                                    label = { Text("المدة (ثوانٍ)", fontSize = 11.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scene_duration_input_$index"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = QabasGold,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                // Transition
                                OutlinedTextField(
                                    value = scene.transition,
                                    onValueChange = { viewModel.updateScene(scene.copy(transition = it)) },
                                    label = { Text("الانتقال", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1.5f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = QabasGold,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // On-Screen Text
                            OutlinedTextField(
                                value = scene.onScreenText,
                                onValueChange = { viewModel.updateScene(scene.copy(onScreenText = it)) },
                                label = { Text("نص الشاشة", fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scene_text_input_$index"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = QabasGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Voiceover
                            OutlinedTextField(
                                value = scene.voiceoverText,
                                onValueChange = { viewModel.updateScene(scene.copy(voiceoverText = it)) },
                                label = { Text("التعليق الصوتي", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = QabasGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Attached Resource Badge / Attach Button
                            if (!scene.attachedAssetId.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1B382B))
                                        .border(1.dp, Color(0xFF66BB6A), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Attachment, contentDescription = null, tint = Color(0xFFA5D6A7), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "المورد: ${scene.attachedAssetTitle}",
                                            color = Color(0xFFA5D6A7),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.detachResourceFromScene(scene.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "إزالة المورد", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { showResourcePickerForSceneId = scene.id },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("attach_resource_btn_$index"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QabasGold)
                                ) {
                                    Icon(imageVector = Icons.Default.Attachment, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إرفاق مورد محلي بهذا المشهد", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog: Add Local Resource with License Validation
        if (showAddResourceDialog) {
            AddResourceValidationDialog(
                onDismiss = { showAddResourceDialog = false },
                onAdd = { title, path, type, size, source, license, author, url, isUser, consent ->
                    val result = viewModel.validateAndAddExternalResource(
                        title = title,
                        uriOrPath = path,
                        assetType = type,
                        fileSizeBytes = size,
                        source = source,
                        license = license,
                        author = author,
                        sourceUrl = url,
                        isUserProvided = isUser,
                        isConsentGiven = consent
                    )
                    if (result.isSuccess) {
                        showAddResourceDialog = false
                    }
                    result
                }
            )
        }

        // Dialog: Resource Picker to attach to a scene
        showResourcePickerForSceneId?.let { sceneId ->
            ResourcePickerModal(
                availableResources = (currentProject?.licensedAssets ?: emptyList()) + localAvailableResources,
                onDismiss = { showResourcePickerForSceneId = null },
                onSelect = { asset ->
                    viewModel.attachResourceToScene(sceneId, asset)
                    showResourcePickerForSceneId = null
                },
                onAddNew = {
                    showResourcePickerForSceneId = null
                    showAddResourceDialog = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddResourceValidationDialog(
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        path: String,
        type: AssetType,
        size: Long,
        source: String,
        license: AssetLicense,
        author: String,
        url: String,
        isUser: Boolean,
        consent: Boolean
    ) -> Result<LicensedAsset>
) {
    var title by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("content://media/local_sample.jpg") }
    var source by remember { mutableStateOf("مكتبة المستخدم المحلية") }
    var author by remember { mutableStateOf("") }
    var isUserProvided by remember { mutableStateOf(true) }
    var selectedType by remember { mutableStateOf(AssetType.IMAGE) }
    var selectedLicense by remember { mutableStateOf(AssetLicense.USER_OWN_WORK) }
    var isConsentGiven by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var licenseDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إضافة مورد محلي مع التحقق من الترخيص", color = QabasGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMessage != null) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x33C62828))
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = errorMessage ?: "", color = Color(0xFFFFCDD2), fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المورد") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resource_title_input"),
                    singleLine = true
                )

                // Asset Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.titleAr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع المورد") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        AssetType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.titleAr) },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // License Dropdown
                ExposedDropdownMenuBox(
                    expanded = licenseDropdownExpanded,
                    onExpandedChange = { licenseDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedLicense.titleAr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الترخيص") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = licenseDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("license_dropdown_trigger")
                    )
                    ExposedDropdownMenu(
                        expanded = licenseDropdownExpanded,
                        onDismissRequest = { licenseDropdownExpanded = false }
                    ) {
                        AssetLicense.entries.forEach { license ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = license.titleAr,
                                        color = if (license == AssetLicense.UNKNOWN_UNLICENSED) Color(0xFFFF8A80) else Color.White
                                    )
                                },
                                onClick = {
                                    selectedLicense = license
                                    licenseDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("المصدر المعتمد") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resource_source_input"),
                    singleLine = true
                )

                // Consent Checkbox (Mandatory)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isConsentGiven,
                        onCheckedChange = { isConsentGiven = it },
                        colors = CheckboxDefaults.colors(checkedColor = QabasGold, checkmarkColor = QabasDarkBackground),
                        modifier = Modifier.testTag("dialog_consent_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "أقر وأتعهد بأن هذا المورد مرخص وصالح للاستخدام.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = onAdd(
                        title,
                        path,
                        selectedType,
                        512 * 1024L,
                        source,
                        selectedLicense,
                        author,
                        "",
                        isUserProvided,
                        isConsentGiven
                    )
                    if (result.isFailure) {
                        errorMessage = result.exceptionOrNull()?.message ?: "بيانات المورد غير مقبولة"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = QabasGold, contentColor = QabasDarkBackground),
                modifier = Modifier.testTag("dialog_confirm_add_resource")
            ) {
                Text("إضافة وتحقق", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = QabasSurfaceDarkElevated
    )
}

@Composable
private fun ResourcePickerModal(
    availableResources: List<LicensedAsset>,
    onDismiss: () -> Unit,
    onSelect: (LicensedAsset) -> Unit,
    onAddNew: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("اختيار مورد للمشهد", color = QabasGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (availableResources.isEmpty()) {
                    Text("لا توجد موارد محلية مسجلة بعد.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                } else {
                    availableResources.distinctBy { it.id }.forEach { asset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(asset) }
                                .testTag("picker_asset_${asset.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151E30))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = asset.title, color = QabasGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = "${asset.assetType.titleAr} • ${asset.license.titleAr}", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                }
                                Icon(imageVector = Icons.Default.Check, contentDescription = "اختيار", tint = QabasGold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onAddNew,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QabasGoldLight)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مورد جديد", fontSize = 11.sp)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = QabasSurfaceDarkElevated
    )
}
