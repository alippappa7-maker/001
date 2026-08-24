package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.AccountStatus
import com.example.domain.model.UserRole
import com.example.ui.components.QabasTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperDashboardScreen(
    viewModel: DeveloperDashboardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Errors", "Users", "Content", "Studio")

    Scaffold(
        topBar = {
            QabasTopBar(
                title = "Developer Dashboard",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> OverviewTab(uiState)
                    1 -> ErrorCenterTab(uiState)
                    2 -> UserManagementTab(uiState, viewModel)
                    3 -> ContentModerationTab()
                    4 -> StudioMonitoringTab()
                }
            }
        }
    }
}

@Composable
fun OverviewTab(uiState: DeveloperDashboardState) {
    val totalUsers = uiState.users.size
    val activeUsers = uiState.users.count { it.status == AccountStatus.ACTIVE }
    val newUsers = uiState.users.count { it.createdAt > System.currentTimeMillis() - 86400000 }
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("User Statistics", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Users: $totalUsers")
                    Text("Active Users: $activeUsers")
                    Text("New Users (24h): $newUsers")
                }
            }
        }
    }
}

@Composable
fun ErrorCenterTab(uiState: DeveloperDashboardState) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (uiState.diagnostics.isEmpty()) {
            item { Text("No diagnostics found.") }
        }
        items(uiState.diagnostics) { diag ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID: ${diag.issueId}", style = MaterialTheme.typography.bodySmall)
                    Text("Title: ${diag.title}", style = MaterialTheme.typography.titleMedium)
                    Text("Severity: ${diag.severity.name}")
                    Text("Status: ${diag.status.name}")
                }
            }
        }
    }
}

@Composable
fun UserManagementTab(uiState: DeveloperDashboardState, viewModel: DeveloperDashboardViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(uiState.users) { user ->
            var showDialog by remember { mutableStateOf(false) }
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Name: ${user.displayName ?: "Unknown"}")
                    Text("Role: ${user.role.name}")
                    Text("Status: ${user.status.name}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showDialog = true }) {
                        Text("Manage User")
                    }
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Manage User: ${user.displayName}") },
                    text = { Text("Select new status for this user.") },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.updateUserStatus(user.uid, AccountStatus.FROZEN, "Admin freeze")
                            showDialog = false
                        }) { Text("Freeze") }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
fun ContentModerationTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Content Moderation: Coming Soon")
    }
}

@Composable
fun StudioMonitoringTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Studio Monitoring: Coming Soon")
    }
}
