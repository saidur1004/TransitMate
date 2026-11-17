package com.transitmate.bd.domain.repository

import com.transitmate.bd.domain.model.AppLanguage
import com.transitmate.bd.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val hasCompletedOnboarding: Boolean = false
)

interface PreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setThemeMode(themeMode: AppThemeMode)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
