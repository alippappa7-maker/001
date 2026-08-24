package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.SettingsRepository
import com.example.data.repository.MihrabRepository
import com.example.data.repository.MihrabRepositoryImpl
import com.example.data.repository.UserRepository
import com.example.data.repository.UserRepositoryImpl
import com.example.domain.model.AuthUiState
import com.example.domain.model.SyncStatus
import com.example.domain.model.UserAccount
import com.example.domain.model.UserDataSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel @JvmOverloads constructor(
    application: Application,
    private val userRepository: UserRepository = UserRepositoryImpl(application),
    private val settingsRepository: SettingsRepository = SettingsRepository(application),
    private val mihrabRepository: MihrabRepository = MihrabRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isFirebaseConfigured = userRepository.isFirebaseAvailable
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _uiState.update { 
                    it.copy(
                        user = user, 
                        isFirebaseConfigured = userRepository.isFirebaseAvailable,
                        syncStatus = if (user == null) SyncStatus.IDLE else it.syncStatus
                    ) 
                }
                if (user != null) {
                    performSync()
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "يرجى إدخال بريد إلكتروني صالح") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "يجب أن تكون كلمة المرور 6 أحرف على الأقل") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.signInWithEmail(email, password)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        successMessage = "تم تسجيل الدخول بنجاح"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "فشل تسجيل الدخول. يرجى التحقق من البيانات والاتصال."
                    )
                }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String?) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "يرجى إدخال بريد إلكتروني صالح") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "يجب أن تكون كلمة المرور 6 أحرف على الأقل") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.signUpWithEmail(email, password, displayName)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        successMessage = "تم إنشاء الحساب بنجاح"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "فشل إنشاء الحساب. يرجى المحاولة مرة أخرى."
                    )
                }
            }
        }
    }

    fun launchGoogleSignIn(context: Context) {
        val serverClientId = try {
            // Check if web client ID is defined in BuildConfig or resource
            val field = BuildConfig::class.java.getField("GOOGLE_WEB_CLIENT_ID")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }

        if (serverClientId.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "يتطلب تسجيل الدخول عبر Google إعداد OAuth Web Client ID في Google Cloud."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.launchGoogleSignIn(context, serverClientId)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        successMessage = "تم تسجيل الدخول عبر Google بنجاح"
                    )
                }
                performSync()
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "فشل تسجيل الدخول عبر Google."
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.signOut()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = null,
                    syncStatus = SyncStatus.IDLE,
                    successMessage = "تم تسجيل الخروج بنجاح"
                )
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.deleteAccount()
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = null,
                        syncStatus = SyncStatus.IDLE,
                        successMessage = "تم حذف الحساب والبيانات السحابية بنجاح"
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "فشل حذف الحساب. قد يتطلب إعادة تسجيل الدخول للتحقق."
                    )
                }
            }
        }
    }

    fun performSync() {
        if (_uiState.value.user == null) {
            _uiState.update { it.copy(syncStatus = SyncStatus.IDLE) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(syncStatus = SyncStatus.SYNCING) }

            try {
                // Collect current local state
                val favorites = mihrabRepository.getFavorites().firstOrNull() ?: emptyList()
                val favIds = favorites.map { it.id }
                val progress = mihrabRepository.getDailyProgress().firstOrNull()
                val isDark = settingsRepository.isDarkModeFlow.firstOrNull()
                val lang = settingsRepository.languageFlow.firstOrNull()
                val prayerConfig = settingsRepository.prayerConfigFlow.firstOrNull()

                // Fetch cloud data first to check for merging
                val cloudDataResult = userRepository.fetchCloudUserData()
                val cloudData = cloudDataResult.getOrNull()

                // Merge and write to Cloud
                val mergedFavs = (favIds + (cloudData?.favoriteZikrIds ?: emptyList())).distinct()
                val completedTasks = maxOf(progress?.completedTasks ?: 0, cloudData?.dailyCompletedTasks ?: 0)
                val totalTasks = maxOf(progress?.totalTasks ?: 5, cloudData?.dailyTotalTasks ?: 5)

                val syncPayload = UserDataSync(
                    favoriteZikrIds = mergedFavs,
                    dailyCompletedTasks = completedTasks,
                    dailyTotalTasks = totalTasks,
                    isDarkMode = isDark,
                    language = lang,
                    selectedCityId = prayerConfig?.selectedCity?.id,
                    lastSyncedAt = System.currentTimeMillis()
                )

                val uploadResult = userRepository.syncUserData(syncPayload)
                if (uploadResult.isSuccess) {
                    // Update local progress if cloud had higher
                    if (cloudData != null && cloudData.dailyCompletedTasks > (progress?.completedTasks ?: 0)) {
                        mihrabRepository.updateDailyProgress(completedTasks, totalTasks)
                    }
                    _uiState.update { it.copy(syncStatus = SyncStatus.SYNCED) }
                } else {
                    _uiState.update { it.copy(syncStatus = SyncStatus.ERROR) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(syncStatus = SyncStatus.ERROR) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
