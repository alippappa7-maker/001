package com.example.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.CompanionConfig
import com.example.domain.model.CompanionMessage
import com.example.domain.model.CompanionStatus
import com.example.data.repository.CompanionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class CompanionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var fakeRepository: FakeCompanionRepository
    private lateinit var viewModel: CompanionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        fakeRepository = FakeCompanionRepository()
        viewModel = CompanionViewModel(application, fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage adds user message and generates assistant response`() = runTest(testDispatcher) {
        viewModel.onInputChanged("ما فضل أذكار الصباح؟")
        viewModel.sendMessage()

        // Before coroutine finishes, status is generating
        assertEquals(1, viewModel.uiState.value.messages.size)
        assertEquals("ما فضل أذكار الصباح؟", viewModel.uiState.value.messages[0].text)
        assertTrue(viewModel.uiState.value.messages[0].isUser)

        advanceUntilIdle()

        // After completion
        assertEquals(2, viewModel.uiState.value.messages.size)
        assertFalse(viewModel.uiState.value.messages[1].isUser)
        assertEquals(CompanionStatus.IDLE, viewModel.uiState.value.status)
        assertEquals("رد رفيق قبس التجريبي", viewModel.uiState.value.messages[1].text)
    }

    @Test
    fun `sendMessage respects max characters limit`() = runTest(testDispatcher) {
        val longMessage = "أ".repeat(501)
        viewModel.onInputChanged(longMessage)
        viewModel.sendMessage()

        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.messages.size)
    }

    @Test
    fun `clearChat empties messages and stops generation`() = runTest(testDispatcher) {
        viewModel.sendMessage(customPrompt = "سؤال")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.messages.size)

        viewModel.clearChat()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.messages.size)
        assertEquals(CompanionStatus.IDLE, viewModel.uiState.value.status)
    }

    @Test
    fun `reportMessage marks message as reported`() = runTest(testDispatcher) {
        viewModel.sendMessage(customPrompt = "سؤال")
        advanceUntilIdle()

        val assistantMessage = viewModel.uiState.value.messages.first { !it.isUser }
        viewModel.showReportDialog(assistantMessage.id)
        viewModel.confirmReport("Safety review")
        advanceUntilIdle()

        val reportedMessage = viewModel.uiState.value.messages.first { it.id == assistantMessage.id }
        assertTrue(reportedMessage.isReported)
        assertTrue(viewModel.uiState.value.reportSuccess)
    }
}

class FakeCompanionRepository : CompanionRepository {
    private val saveConsent = MutableStateFlow(false)
    private val savedMessages = MutableStateFlow<List<CompanionMessage>>(emptyList())
    private val config = CompanionConfig(maxInputChars = 500)

    override suspend fun generateResponse(
        prompt: String,
        history: List<CompanionMessage>,
        language: String
    ): Result<String> {
        return Result.success("رد رفيق قبس التجريبي")
    }

    override fun isSaveHistoryEnabled(): Flow<Boolean> = saveConsent

    override suspend fun setSaveHistoryEnabled(enabled: Boolean) {
        saveConsent.value = enabled
        if (!enabled) savedMessages.value = emptyList()
    }

    override fun getSavedHistory(): Flow<List<CompanionMessage>> = savedMessages

    override suspend fun saveHistory(messages: List<CompanionMessage>) {
        if (saveConsent.value) {
            savedMessages.value = messages
        }
    }

    override suspend fun clearHistory() {
        savedMessages.value = emptyList()
    }

    override suspend fun reportMessage(messageId: String, reason: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun getConfig(): CompanionConfig = config
}
