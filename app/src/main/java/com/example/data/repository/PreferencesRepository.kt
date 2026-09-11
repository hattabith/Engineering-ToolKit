package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage {
    SYSTEM,
    UKRAINIAN,
    ENGLISH
}

class PreferencesRepository(private val context: Context) {
    private val THEME_KEY = stringPreferencesKey("app_theme_mode")
    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            AppThemeMode.LIGHT.name -> AppThemeMode.LIGHT
            AppThemeMode.DARK.name -> AppThemeMode.DARK
            else -> AppThemeMode.SYSTEM
        }
    }

    val language: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        when (prefs[LANGUAGE_KEY]) {
            AppLanguage.UKRAINIAN.name -> AppLanguage.UKRAINIAN
            AppLanguage.ENGLISH.name -> AppLanguage.ENGLISH
            else -> AppLanguage.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }

    suspend fun setLanguage(lang: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = lang.name
        }
    }
}
