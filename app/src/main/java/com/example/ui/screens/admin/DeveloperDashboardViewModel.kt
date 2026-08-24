package com.example.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AdminRepository
import com.example.domain.model.AccountStatus
import com.example.domain.model.UserAccount
import com.example.domain.model.UserRole
import com.example.domain.model.admin.AuditLog
import com.example.domain.model.admin.IssueDiagnosis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeveloperDashboardState(
    val users: List<UserAccount> = emptyList(),
    val auditLogs: List<AuditLog> = emptyList(),
    val diagnostics: List<IssueDiagnosis> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class DeveloperDashboardViewModel(
    private val adminRepository: AdminRepository,
    private val currentUserUid: String,
    private val currentUserRole: UserRole
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeveloperDashboardState())
    val uiState: StateFlow<DeveloperDashboardState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            launch {
                adminRepository.observeAllUsers()
                    .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                    .collect { users ->
                        _uiState.update { it.copy(users = users, isLoading = false) }
                    }
            }
            launch {
                adminRepository.observeAuditLogs()
                    .catch { e -> _uiState.update { it.copy(error = e.message) } }
                    .collect { logs ->
                        _uiState.update { it.copy(auditLogs = logs) }
                    }
            }
            launch {
                adminRepository.observeDiagnostics()
                    .catch { e -> _uiState.update { it.copy(error = e.message) } }
                    .collect { diagnostics ->
                        _uiState.update { it.copy(diagnostics = diagnostics) }
                    }
            }
        }
    }

    fun updateUserStatus(targetUid: String, newStatus: AccountStatus, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = adminRepository.updateUserStatus(targetUid, newStatus, reason, currentUserUid, currentUserRole)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message, isLoading = false) }
            }
        }
    }

    fun updateUserRole(targetUid: String, newRole: UserRole, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = adminRepository.updateUserRole(targetUid, newRole, reason, currentUserUid, currentUserRole)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message, isLoading = false) }
            }
        }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
