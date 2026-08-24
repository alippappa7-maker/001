package com.example.domain.model.admin

import com.example.domain.model.UserRole

data class IssueDiagnosis(
    val issueId: String,
    val title: String,
    val summary: String,
    val technicalCause: String,
    val affectedComponent: String?,
    val severity: IssueSeverity,
    val affectedUsers: Int,
    val reproductionSteps: String,
    val suggestedFix: String,
    val confidence: Float,
    val status: DiagnosisStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class IssueSeverity {
    LOW, MODERATE, HIGH, CRITICAL, FATAL
}

enum class DiagnosisStatus {
    NEW, INVESTIGATING, DIAGNOSED, FIX_PROPOSED, RESOLVED, IGNORED
}

data class AppPatch(
    val patchId: String,
    val issueId: String,
    val branchName: String,
    val diffReport: String,
    val status: PatchStatus,
    val createdAt: Long = System.currentTimeMillis()
)

enum class PatchStatus {
    PROPOSED, PATCH_CREATED, TESTING, TEST_FAILED, TEST_PASSED, WAITING_APPROVAL, APPROVED, REJECTED, MERGED, ROLLED_BACK
}

data class AuditLog(
    val logId: String,
    val actorId: String,
    val actorRole: UserRole,
    val action: String,
    val targetId: String?,
    val reason: String,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
