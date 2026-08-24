package com.example.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsRepository
import com.example.data.repository.AdminRepository
import com.example.domain.model.AccountStatus
import com.example.domain.model.UserAccount
import com.example.domain.model.UserRole
import com.example.domain.model.admin.AuditLog
import com.example.domain.model.admin.IssueDiagnosis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class DeveloperDashboardState(
    val users: List<UserAccount> = emptyList(),
    val auditLogs: List<AuditLog> = emptyList(),
    val diagnostics: List<IssueDiagnosis> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,

    // Developer API & Service Controls
    val customGeminiApiKey: String = "",
    val aiServicesEnabled: Boolean = true,
    val geminiModel: String = "gemini-1.5-flash",
    val isTestingKey: Boolean = false,
    val testKeySuccess: Boolean? = null,
    val testKeyMessage: String? = null,
    val saveNotice: String? = null
)

class DeveloperDashboardViewModel(
    private val adminRepository: AdminRepository,
    private val settingsRepository: SettingsRepository,
    private val currentUserUid: String,
    private val currentUserRole: UserRole
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeveloperDashboardState())
    val uiState: StateFlow<DeveloperDashboardState> = _uiState.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        loadDashboardData()
        loadDevSettings()
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

    private fun loadDevSettings() {
        viewModelScope.launch {
            launch {
                settingsRepository.devCustomGeminiApiKeyFlow.collect { key ->
                    _uiState.update { it.copy(customGeminiApiKey = key) }
                }
            }
            launch {
                settingsRepository.devAiServicesEnabledFlow.collect { enabled ->
                    _uiState.update { it.copy(aiServicesEnabled = enabled) }
                }
            }
            launch {
                settingsRepository.devGeminiModelFlow.collect { model ->
                    _uiState.update { it.copy(geminiModel = model) }
                }
            }
        }
    }

    fun setAiServicesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDevAiServicesEnabled(enabled)
            _uiState.update {
                it.copy(
                    aiServicesEnabled = enabled,
                    saveNotice = if (enabled) "تم تفعيل خدمات الذكاء الاصطناعي بنجاح" else "تم إيقاف خدمات الذكاء الاصطناعي للتطبيق"
                )
            }
        }
    }

    fun saveCustomApiKey(key: String) {
        viewModelScope.launch {
            val cleanKey = key.trim()
            settingsRepository.saveDevCustomGeminiApiKey(cleanKey)
            _uiState.update {
                it.copy(
                    customGeminiApiKey = cleanKey,
                    saveNotice = if (cleanKey.isBlank()) "تم مسح المفتاح والعودة للافتراضي" else "تم حفظ مفتاح Gemini API بنجاح"
                )
            }
        }
    }

    fun saveGeminiModel(model: String) {
        viewModelScope.launch {
            settingsRepository.saveDevGeminiModel(model)
            _uiState.update {
                it.copy(
                    geminiModel = model,
                    saveNotice = "تم تحديث نموذج الذكاء الاصطناعي إلى $model"
                )
            }
        }
    }

    fun testGeminiApiKey(keyToTest: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val key = keyToTest.trim().ifBlank { _uiState.value.customGeminiApiKey }
            if (key.isBlank()) {
                _uiState.update {
                    it.copy(
                        isTestingKey = false,
                        testKeySuccess = false,
                        testKeyMessage = "المفتاح فارغ، يرجى كتابة أو لصق المفتاح أولاً"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(isTestingKey = true, testKeySuccess = null, testKeyMessage = null)
            }

            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$key"
                val request = Request.Builder().url(url).get().build()
                val response = okHttpClient.newCall(request).execute()
                val isSuccess = response.isSuccessful
                val responseCode = response.code

                _uiState.update {
                    it.copy(
                        isTestingKey = false,
                        testKeySuccess = isSuccess,
                        testKeyMessage = if (isSuccess) {
                            "الاتصال ناجح! المفتاح سليم وصالح للتوليد والعمل."
                        } else {
                            "فشل الاتصال (كود $responseCode): يرجى التأكد من صحة المفتاح والحصة (Quota)."
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTestingKey = false,
                        testKeySuccess = false,
                        testKeyMessage = "خطأ في الاتصال بالشبكة: ${e.localizedMessage ?: e.message}"
                    )
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

    fun dismissSaveNotice() {
        _uiState.update { it.copy(saveNotice = null) }
    }
}
