package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AccountStatus
import com.example.domain.model.UserRole
import com.example.ui.components.QabasTopBar
import com.example.ui.theme.LocalExtendedQabasColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperDashboardScreen(
    viewModel: DeveloperDashboardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalExtendedQabasColors.current
    var selectedTab by remember { mutableIntStateOf(1) } // Default to API & Services tab for quick access
    val tabs = listOf("نظرة عامة", "الخدمات والذكاء الاصطناعي", "المستخدمين", "التشخيص", "سجل التدقيق")

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveNotice) {
        uiState.saveNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSaveNotice()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("تنبيه: $it")
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            QabasTopBar(
                title = "لوحة تحكم المطور والمدير",
                onBack = onBack
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.surface,
                contentColor = colors.gold,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) colors.gold else colors.textSecondary
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading && uiState.users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.gold)
                }
            } else {
                when (selectedTab) {
                    0 -> OverviewTab(uiState, onNavigateToServices = { selectedTab = 1 })
                    1 -> ApiAndServicesTab(uiState, viewModel)
                    2 -> UserManagementTab(uiState, viewModel)
                    3 -> ErrorCenterTab(uiState)
                    4 -> AuditLogTab(uiState)
                }
            }
        }
    }
}

@Composable
fun OverviewTab(
    uiState: DeveloperDashboardState,
    onNavigateToServices: () -> Unit
) {
    val colors = LocalExtendedQabasColors.current
    val totalUsers = uiState.users.size
    val activeUsers = uiState.users.count { it.status == AccountStatus.ACTIVE }
    val developers = uiState.users.count { it.role == UserRole.DEVELOPER || it.role == UserRole.SUPER_ADMIN }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = null, tint = colors.gold)
                        Text(
                            text = "ملخص حالة المنظومة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي المستخدمين المسجلين:", color = colors.textSecondary)
                        Text("$totalUsers", fontWeight = FontWeight.Bold, color = colors.gold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المستخدمون النشطون:", color = colors.textSecondary)
                        Text("$activeUsers", fontWeight = FontWeight.Bold, color = colors.statusActive)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المطورون والمسؤولون:", color = colors.textSecondary)
                        Text("$developers", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (uiState.aiServicesEnabled) colors.statusActive else colors.statusInactive
                        )
                        Text(
                            text = "حالة خدمات الذكاء الاصطناعي والاستوديو",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (uiState.aiServicesEnabled)
                            "الخدمات الذكية (مفعلة وتعمل) - جاهزة لاستقبال الطلبات وتوليد المحتوى."
                        else
                            "الخدمات الذكية (متوقفة مؤقتاً) - تم إيقافها بواسطة المطور لحماية الحساب أو الصيانة.",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onNavigateToServices,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الانتقال إلى لوحة التحكم بالخدمات والمفاتيح")
                    }
                }
            }
        }
    }
}

@Composable
fun ApiAndServicesTab(
    uiState: DeveloperDashboardState,
    viewModel: DeveloperDashboardViewModel
) {
    val colors = LocalExtendedQabasColors.current
    val focusManager = LocalFocusManager.current
    var apiKeyInput by remember(uiState.customGeminiApiKey) { mutableStateOf(uiState.customGeminiApiKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val models = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master AI Services Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (uiState.aiServicesEnabled) colors.statusActive.copy(alpha = 0.15f)
                                        else colors.statusInactive.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.aiServicesEnabled) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = if (uiState.aiServicesEnabled) colors.statusActive else colors.statusInactive
                                )
                            }
                            Column {
                                Text(
                                    text = "تشغيل / إيقاف خدمات الذكاء الاصطناعي",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (uiState.aiServicesEnabled) "الخدمة مفعلة للتطبيق حالياً" else "الخدمة متوقفة مؤقتاً للتطبيق",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (uiState.aiServicesEnabled) colors.statusActive else colors.statusInactive
                                )
                            }
                        }
                        Switch(
                            checked = uiState.aiServicesEnabled,
                            onCheckedChange = { viewModel.setAiServicesEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.gold,
                                checkedTrackColor = colors.gold.copy(alpha = 0.4f)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يتيح هذا الزر للمطور فقط تفعيل أو تعطيل خدمات رفيق قبس ومصنع الفيديو الذكي بالكامل فوراً للتحكم بالاستهلاك أو أثناء فترات الصيانة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Gemini API Key Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = colors.gold)
                        Text(
                            text = "مفتاح Google Gemini API للمطور",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يمكنك إضافة أو تغيير مفتاح الذكاء الاصطناعي الخاص بك من هنا مباشرة، وسيعمل التطبيق به فوراً دون الحاجة لإعادة البناء.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (apiKeyInput.isNotBlank()) {
                                    IconButton(onClick = { apiKeyInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textMuted)
                                    }
                                }
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = colors.gold
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.gold,
                            unfocusedBorderColor = colors.surfaceBorder,
                            focusedLabelColor = colors.gold
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.saveCustomApiKey(apiKeyInput)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.gold)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ وتطبيق", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.testGeminiApiKey(apiKeyInput)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isTestingKey
                        ) {
                            if (uiState.isTestingKey) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.gold)
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("فحص الاتصال")
                            }
                        }
                    }

                    // Test Result Display
                    if (uiState.testKeyMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val isSuccess = uiState.testKeySuccess == true
                        val badgeBg = if (isSuccess) colors.statusActive.copy(alpha = 0.15f) else colors.statusInactive.copy(alpha = 0.15f)
                        val badgeColor = if (isSuccess) colors.statusActive else colors.statusInactive

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = uiState.testKeyMessage.orEmpty(),
                                color = badgeColor,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Gemini Model Selection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = colors.gold)
                        Text(
                            text = "نموذج الذكاء الاصطناعي (Gemini Model)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "اختر النموذج المناسب للأداء وسرعة الاستجابة:",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    models.forEach { modelName ->
                        val isSelected = uiState.geminiModel == modelName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.gold.copy(alpha = 0.12f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) colors.gold else colors.surfaceBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = modelName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.gold else colors.textPrimary
                                )
                                Text(
                                    text = when (modelName) {
                                        "gemini-1.5-flash" -> "أسرع وأعلى كفاءة في استهلاك الحصة (موصى به)"
                                        "gemini-1.5-pro" -> "قدرات تفكير متقدمة للمهام العميقة"
                                        "gemini-2.0-flash" -> "الجيل الثاني فائق السرعة والاستجابة"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.saveGeminiModel(modelName) },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.gold)
                            )
                        }
                    }
                }
            }
        }

        // Firebase Integration Guide & Security Badge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = colors.statusActive)
                        Text(
                            text = "حماية وأمان لوحة المطور",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• هذه اللوحة وقسم التحكم بالخدمات محمي بالكامل ومتاح حصرياً لرتبتي (SUPER_ADMIN و DEVELOPER).\n• لا يمكن للمستخدم العادي الوصول إلى هذه الإعدادات أو رؤية المفاتيح.\n• ملف قواعد الحماية Firestore Rules مجهز تلقائياً لمنع أي تلاعب بالصلاحيات.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun UserManagementTab(uiState: DeveloperDashboardState, viewModel: DeveloperDashboardViewModel) {
    val colors = LocalExtendedQabasColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.users) { user ->
            var showDialog by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.displayName ?: user.email ?: "مستخدم بدون اسم",
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (user.role == UserRole.SUPER_ADMIN || user.role == UserRole.DEVELOPER)
                                        colors.gold.copy(alpha = 0.2f)
                                    else colors.surfaceElevated
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = user.role.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (user.role == UserRole.SUPER_ADMIN || user.role == UserRole.DEVELOPER) colors.gold else colors.textSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("البريد: ${user.email ?: "غير مسجل"}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Text("الحالة: ${user.status.name}", style = MaterialTheme.typography.bodySmall, color = if (user.status == AccountStatus.ACTIVE) colors.statusActive else colors.statusInactive)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إدارة الحساب والصلاحيات")
                    }
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("إدارة حساب: ${user.displayName ?: user.email}") },
                    text = {
                        Column {
                            Text("اختر الإجراء الإداري المطلوب تطبيقه:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("الحالة الحالية: ${user.status.name} | الرتبة: ${user.role.name}")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val nextStatus = if (user.status == AccountStatus.ACTIVE) AccountStatus.FROZEN else AccountStatus.ACTIVE
                                viewModel.updateUserStatus(user.uid, nextStatus, "إجراء إداري من لوحة المطور")
                                showDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (user.status == AccountStatus.ACTIVE) colors.statusInactive else colors.statusActive)
                        ) {
                            Text(if (user.status == AccountStatus.ACTIVE) "تجميد الحساب" else "تفعيل الحساب")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("إلغاء") }
                    }
                )
            }
        }
    }
}

@Composable
fun ErrorCenterTab(uiState: DeveloperDashboardState) {
    val colors = LocalExtendedQabasColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (uiState.diagnostics.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.statusActive, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد أخطاء مسجلة، التطبيق يعمل بكفاءة تامة.", color = colors.textSecondary)
                    }
                }
            }
        }
        items(uiState.diagnostics) { diag ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("معرف: ${diag.issueId}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Text("العنوان: ${diag.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("الخطورة: ${diag.severity.name}", color = colors.statusWarning)
                    Text("الحالة: ${diag.status.name}", color = colors.textSecondary)
                }
            }
        }
    }
}

@Composable
fun AuditLogTab(uiState: DeveloperDashboardState) {
    val colors = LocalExtendedQabasColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (uiState.auditLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد عمليات تدقيق مسجلة حتى الآن.", color = colors.textSecondary)
                }
            }
        }
        items(uiState.auditLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("الإجراء: ${log.action}", fontWeight = FontWeight.Bold, color = colors.gold)
                    Text("المنفذ: ${log.actorId} (${log.actorRole.name})", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Text("السبب: ${log.reason}", style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
                }
            }
        }
    }
}

