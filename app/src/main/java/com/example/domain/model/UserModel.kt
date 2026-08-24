package com.example.domain.model

data class UserAccount(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

data class UserDataSync(
    val favoriteZikrIds: List<String> = emptyList(),
    val dailyCompletedTasks: Int = 0,
    val dailyTotalTasks: Int = 5,
    val isDarkMode: Boolean? = true,
    val language: String? = "ar",
    val selectedCityId: String? = null,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    ERROR
}

data class AuthUiState(
    val user: UserAccount? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val isFirebaseConfigured: Boolean = true
)
