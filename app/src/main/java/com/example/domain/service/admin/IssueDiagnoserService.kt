package com.example.domain.service.admin

import com.example.domain.model.admin.DiagnosisStatus
import com.example.domain.model.admin.IssueDiagnosis
import com.example.domain.model.admin.IssueSeverity
import java.util.UUID

interface IssueDiagnoserService {
    suspend fun diagnoseError(errorDetails: String, stackTrace: String?): IssueDiagnosis
}

class IssueDiagnoserServiceImpl : IssueDiagnoserService {
    override suspend fun diagnoseError(errorDetails: String, stackTrace: String?): IssueDiagnosis {
        // In a real app, this would send data to an AI API or backend for diagnosis
        // Here we simulate the process
        val severity = if (errorDetails.contains("Fatal", ignoreCase = true) || errorDetails.contains("NullPointerException")) {
            IssueSeverity.FATAL
        } else if (errorDetails.contains("ANR")) {
            IssueSeverity.CRITICAL
        } else {
            IssueSeverity.MODERATE
        }

        return IssueDiagnosis(
            issueId = UUID.randomUUID().toString(),
            title = "Automated Diagnosis: ${errorDetails.take(20)}...",
            summary = "The app encountered an error: $errorDetails",
            technicalCause = stackTrace ?: "Stack trace not provided",
            affectedComponent = extractComponent(stackTrace),
            severity = severity,
            affectedUsers = 1,
            reproductionSteps = "1. Navigate to the affected screen\n2. Trigger the action that caused the error",
            suggestedFix = "Review the stack trace and implement null checks or exception handling.",
            confidence = 0.85f,
            status = DiagnosisStatus.DIAGNOSED
        )
    }

    private fun extractComponent(stackTrace: String?): String {
        if (stackTrace == null) return "Unknown"
        val match = Regex("com\\.example\\.[a-zA-Z0-9.]+").find(stackTrace)
        return match?.value ?: "Unknown Component"
    }
}
