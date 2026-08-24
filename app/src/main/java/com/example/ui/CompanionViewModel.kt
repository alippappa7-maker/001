package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CompanionRepository
import com.example.data.repository.CompanionRepositoryImpl
import com.example.domain.model.CompanionMessage
import com.example.domain.model.CompanionStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CompanionUiState(
    val messages: List<CompanionMessage> = emptyList(),
    val inputText: String = "",
    val status: CompanionStatus = CompanionStatus.IDLE,
    val errorMessage: String? = null,
    val isSaveHistoryEnabled: Boolean = false,
    val modelName: String = "gemini-2.5-flash",
    val maxInputChars: Int = 500,
    val showClearDialog: Boolean = false,
    val showReportDialog: Boolean = false,
    val reportingMessageId: String? = null,
    val reportSuccess: Boolean = false
)

class CompanionViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: CompanionRepository = CompanionRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        CompanionUiState(
            modelName = repository.getConfig().modelName,
            maxInputChars = repository.getConfig().maxInputChars
        )
    )
    val uiState: StateFlow<CompanionUiState> = _uiState.asStateFlow()

    private var activeGenerationJob: Job? = null

    init {
        viewModelScope.launch {
            repository.isSaveHistoryEnabled().collect { isEnabled ->
                _uiState.update { it.copy(isSaveHistoryEnabled = isEnabled) }
                if (isEnabled) {
                    repository.getSavedHistory().collect { savedMessages ->
                        if (savedMessages.isNotEmpty() && _uiState.value.messages.isEmpty()) {
                            _uiState.update { it.copy(messages = savedMessages) }
                        }
                    }
                }
            }
        }
    }

    fun onInputChanged(text: String) {
        if (text.length <= _uiState.value.maxInputChars) {
            _uiState.update { it.copy(inputText = text, errorMessage = null) }
        }
    }

    fun sendMessage(customPrompt: String? = null, language: String = "ar") {
        val promptToSend = (customPrompt ?: _uiState.value.inputText).trim()
        if (promptToSend.isBlank()) return

        if (promptToSend.length > _uiState.value.maxInputChars) {
            _uiState.update { it.copy(errorMessage = "تتجاوز الرسالة الحد المسموح به (500 حرف)") }
            return
        }

        val userMessage = CompanionMessage(
            id = UUID.randomUUID().toString(),
            text = promptToSend,
            isUser = true
        )

        val updatedMessages = _uiState.value.messages + userMessage

        _uiState.update {
            it.copy(
                messages = updatedMessages,
                inputText = "",
                status = CompanionStatus.GENERATING,
                errorMessage = null
            )
        }

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            val result = repository.generateResponse(promptToSend, updatedMessages, language)

            result.onSuccess { responseText ->
                val assistantMessage = CompanionMessage(
                    id = UUID.randomUUID().toString(),
                    text = responseText,
                    isUser = false
                )
                val finalMessages = _uiState.value.messages + assistantMessage
                _uiState.update {
                    it.copy(
                        messages = finalMessages,
                        status = CompanionStatus.IDLE
                    )
                }

                if (_uiState.value.isSaveHistoryEnabled) {
                    repository.saveHistory(finalMessages)
                }
            }.onFailure { error ->
                val errorText = when {
                    error.message == "RATE_LIMIT_EXCEEDED" -> "تم تجاوز الحد المسموح من الطلبات حاليًا. يرجى الانتظار قليلاً والمحاولة مجددًا."
                    error is java.net.UnknownHostException || error is java.net.SocketTimeoutException -> "لا يوجد اتصال بالإنترنت. يرجى التحقق من الشبكة والمحاولة مجددًا."
                    else -> error.localizedMessage ?: "تعذر استلام رد. يرجى المحاولة لاحقاً."
                }

                val errorMessageObj = CompanionMessage(
                    id = UUID.randomUUID().toString(),
                    text = errorText,
                    isUser = false,
                    isError = true
                )

                _uiState.update {
                    it.copy(
                        messages = _uiState.value.messages + errorMessageObj,
                        status = CompanionStatus.ERROR,
                        errorMessage = errorText
                    )
                }
            }
        }
    }

    fun stopGenerating() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _uiState.update { it.copy(status = CompanionStatus.IDLE) }
    }

    fun retryLastMessage(language: String = "ar") {
        val lastUserMessage = _uiState.value.messages.lastOrNull { it.isUser }
        if (lastUserMessage != null) {
            sendMessage(customPrompt = lastUserMessage.text, language = language)
        }
    }

    fun clearChat() {
        stopGenerating()
        _uiState.update {
            it.copy(
                messages = emptyList(),
                status = CompanionStatus.IDLE,
                errorMessage = null,
                showClearDialog = false
            )
        }
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun toggleSaveHistory(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSaveHistoryEnabled(enabled)
            if (enabled) {
                repository.saveHistory(_uiState.value.messages)
            }
        }
    }

    fun showClearDialog(show: Boolean) {
        _uiState.update { it.copy(showClearDialog = show) }
    }

    fun showReportDialog(messageId: String?) {
        _uiState.update {
            it.copy(
                showReportDialog = messageId != null,
                reportingMessageId = messageId,
                reportSuccess = false
            )
        }
    }

    fun confirmReport(reason: String = "Content safety & accuracy review") {
        val msgId = _uiState.value.reportingMessageId ?: return
        viewModelScope.launch {
            repository.reportMessage(msgId, reason)
            val updated = _uiState.value.messages.map {
                if (it.id == msgId) it.copy(isReported = true) else it
            }
            _uiState.update {
                it.copy(
                    messages = updated,
                    showReportDialog = false,
                    reportSuccess = true,
                    reportingMessageId = null
                )
            }
        }
    }

    fun dismissReportDialog() {
        _uiState.update { it.copy(showReportDialog = false, reportingMessageId = null) }
    }
}
