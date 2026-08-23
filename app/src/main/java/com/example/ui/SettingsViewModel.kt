package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val isDarkMode: StateFlow<Boolean?> = repository.isDarkModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val language: StateFlow<String?> = repository.languageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
        
    val hasSeenWelcome: StateFlow<Boolean?> = repository.hasSeenWelcomeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(isDark)
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            repository.setLanguage(languageCode)
        }
    }
    
    fun setHasSeenWelcome() {
        viewModelScope.launch {
            repository.setHasSeenWelcome(true)
        }
    }
}
