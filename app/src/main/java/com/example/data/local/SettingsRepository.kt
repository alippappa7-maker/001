package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val HAS_SEEN_WELCOME = booleanPreferencesKey("has_seen_welcome")
    }

    val isDarkModeFlow: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE]
    }

    val languageFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LANGUAGE]
    }
    
    val hasSeenWelcomeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAS_SEEN_WELCOME] ?: false
    }

    suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE] = languageCode
        }
    }
    
    suspend fun setHasSeenWelcome(hasSeen: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_WELCOME] = hasSeen
        }
    }
}
