package com.example.ui.screens.studio

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.domain.model.studio.StyleSignature
import com.example.ui.components.QabasButton
import com.example.ui.components.QabasButtonVariant
import com.example.ui.components.QabasTopBar
import com.example.ui.components.StarryBackground
import com.example.ui.screens.studio.components.StudioGlassCard
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens
import com.example.ui.theme.StudioBlue

@Composable
fun StyleReferenceScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: StudioViewModel = viewModel()
) {
    val colors = QabasThemeTokens.colors
    val analysisState by viewModel.styleAnalysisState.collectAsState()
    val savedStyles by viewModel.styleReferences.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()

    var pendingLabel by remember { mutableStateOf("") }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val label = pendingLabel.ifBlank { "فيديو مرجعي" }
            viewModel.analyzeStyleFromVideo(uri, label)
        }
    }

    Scaffold(
        topBar = {
            QabasTopBar(
                title = "أسلوب من فيديو يعجبك",
                subtitle = "التطبيق يفهم الأسلوب فقط، ولا يولّد أو ينسخ الفيديو نفسه",
                onBack = onBack
            )
        },
        containerColor = colors.background,
        modifier = Modifier.testTag("screen_style_reference")
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
                verticalArrangement = Arrangement.spacedBy(QabasDimens.Space16)
            ) {
                // --- Upload section ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(QabasDimens.Radius12))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.gold.copy(alpha = 0.15f), RoundedCornerShape(QabasDimens.Radius12))
                        .padding(QabasDimens.Space16)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ارفع فيديو أعجبك أسلوبه",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = StudioBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(QabasDimens.Space8))
                    Text(
                        text = "سيقوم التطبيق بتحليل الألوان، ومعالجة النص، وبنية السرد، والإيقاع — دون تحليل أو حفظ محتواه الكلامي أو الديني.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(QabasDimens.Space12))

                    OutlinedTextField(
                        value = pendingLabel,
                        onValueChange = { pendingLabel = it },
                        label = { Text("سمِّ هذا الأسلوب (مثال: قصص برسوم كرتونية)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(QabasDimens.Space12))

                    QabasButton(
                        text = "اختيار فيديو من الجهاز",
                        onClick = { videoPickerLauncher.launch("video/*") },
                        variant = QabasButtonVariant.PrimaryGold,
                        icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                        enabled = analysisState !is StyleAnalysisUiState.Analyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_pick_style_video")
                    )
                }

                // --- Analysis state ---
                when (val state = analysisState) {
                    is StyleAnalysisUiState.Analyzing -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QabasDimens.Radius12))
                                .background(colors.surfaceElevated)
                                .padding(QabasDimens.Space16),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = colors.gold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "جارٍ تحليل أسلوب الفيديو (قد يستغرق بضع ثوانٍ)...",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary
                            )
                        }
                    }
                    is StyleAnalysisUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QabasDimens.Radius12))
                                .background(Color(0xFFB00020).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFFB00020).copy(alpha = 0.3f), RoundedCornerShape(QabasDimens.Radius12))
                                .padding(QabasDimens.Space16)
                        ) {
                            Text(
                                text = "تعذر التحليل",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFB00020)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary
                            )
                        }
                    }
                    is StyleAnalysisUiState.Success -> {
                        StyleSignatureCard(
                            signature = state.signature,
                            showApplyButton = currentProject != null,
                            onApply = {
                                viewModel.applyStyleSignatureToCurrentProject(state.signature)
                                navController.popBackStack()
                            }
                        )
                    }
                    StyleAnalysisUiState.Idle -> {}
                }

                // --- Previously saved styles ---
                if (savedStyles.isNotEmpty()) {
                    Text(
                        text = "أساليب محفوظة سابقًا",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    savedStyles.forEach { signature ->
                        StyleSignatureCard(
                            signature = signature,
                            showApplyButton = currentProject != null,
                            onApply = {
                                viewModel.applyStyleSignatureToCurrentProject(signature)
                                navController.popBackStack()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QabasDimens.Space24))
            }
        }
    }
}

@Composable
private fun StyleSignatureCard(
    signature: StyleSignature,
    showApplyButton: Boolean,
    onApply: () -> Unit
) {
    StudioGlassCard(
        modifier = Modifier.fillMaxWidth(),
        gradientBorder = true,
        cornerRadius = QabasDimens.Radius12,
        contentPadding = PaddingValues(QabasDimens.Space16)
    ) {
        val colors = QabasThemeTokens.colors
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = signature.sourceLabel,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.gold
            )
        }

        Spacer(modifier = Modifier.height(QabasDimens.Space8))
        Text(text = signature.summary, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)

        Spacer(modifier = Modifier.height(QabasDimens.Space12))
        StyleAttributeRow("نوع المعالجة البصرية", signature.visualMedium.titleAr)
        StyleAttributeRow("بنية السرد", signature.narrativeStructure.titleAr)
        StyleAttributeRow("لوحة الألوان", signature.colorPalette)
        StyleAttributeRow("معالجة النص", signature.textTreatment)
        StyleAttributeRow("الانتقالات", signature.transitionStyle)
        if (signature.signatureMotif.isNotBlank()) {
            StyleAttributeRow("عنصر التوقيع الثابت", signature.signatureMotif)
        }
        StyleAttributeRow("الإيقاع", signature.paceDescription)

        if (showApplyButton) {
            Spacer(modifier = Modifier.height(QabasDimens.Space12))
            QabasButton(
                text = "تطبيق هذا الأسلوب على المشروع الحالي",
                onClick = onApply,
                variant = QabasButtonVariant.OutlineGold,
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StyleAttributeRow(label: String, value: String) {
    val colors = QabasThemeTokens.colors
    if (value.isBlank()) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
    }
}
