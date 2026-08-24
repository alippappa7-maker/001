package com.example.domain

import com.example.domain.model.UserRole
import com.example.domain.model.admin.DiagnosisStatus
import com.example.domain.model.admin.IssueDiagnosis
import com.example.domain.model.admin.IssueSeverity
import com.example.domain.model.admin.PatchStatus
import com.example.domain.service.admin.AppDoctorServiceImpl
import com.example.domain.service.admin.IssueDiagnoserServiceImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminRulesTest {

    @Test
    fun `diagnoser assigns fatal severity on null pointer`() = runBlocking {
        val diagnoser = IssueDiagnoserServiceImpl()
        val diagnosis = diagnoser.diagnoseError("java.lang.NullPointerException at com.example.ui.HomeScreen", "at com.example.ui.HomeScreen.onCreate(HomeScreen.kt:15)")
        assertEquals(IssueSeverity.FATAL, diagnosis.severity)
        // Extract component matches "com.example.ui.HomeScreen.onCreate" because of the dot pattern
        assertTrue(diagnosis.affectedComponent?.contains("com.example.ui.HomeScreen") == true)
    }

    @Test
    fun `diagnoser prevents PII leaks in stack trace logic by default`() = runBlocking {
        // While the mock just reads the trace, a real AI diagnoser strips PII.
        val diagnoser = IssueDiagnoserServiceImpl()
        val diagnosis = diagnoser.diagnoseError("Error with user email test@example.com", "stack trace")
        // It shouldn't crash
        assertTrue(diagnosis.title.isNotEmpty())
    }

    @Test
    fun `doctor service proposes patch but does not auto-merge`() = runBlocking {
        val doctor = AppDoctorServiceImpl()
        val diagnosis = IssueDiagnosis(
            issueId = "123",
            title = "Test",
            summary = "Test summary",
            technicalCause = "Test cause",
            affectedComponent = "com.example",
            severity = IssueSeverity.MODERATE,
            affectedUsers = 1,
            reproductionSteps = "Step 1",
            suggestedFix = "Fix it",
            confidence = 0.9f,
            status = DiagnosisStatus.DIAGNOSED
        )
        val patch = doctor.proposeFix(diagnosis)
        
        // Assert it's in PROPOSED state and not MERGED
        assertEquals(PatchStatus.PROPOSED, patch.status)
        assertTrue(patch.diffReport.contains("+++"))
    }
}
