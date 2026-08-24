package com.example.domain.model

data class CompanionMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val isReported: Boolean = false
)

enum class CompanionStatus {
    IDLE,
    LOADING,
    GENERATING,
    ERROR
}

data class CompanionConfig(
    val modelName: String = "gemini-2.5-flash",
    val maxInputChars: Int = 500,
    val maxHistoryTurns: Int = 10,
    val requestsPerWindow: Int = 15,
    val windowMinutes: Long = 10L
)
