package com.transitmate.bd.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.transitmate.bd.domain.model.AppLanguage
import com.transitmate.bd.domain.model.AppThemeMode
import com.transitmate.bd.domain.repository.PreferencesRepository
import com.transitmate.bd.domain.repository.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.transitMateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "transitmate_preferences"
)

class DataStorePreferencesRepository(
    private val context: Context
) : PreferencesRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override val preferences: Flow<UserPreferences> = context.transitMateDataStore.data.map { prefs ->
        val theme = runCatching {
            AppThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name)
        }.getOrDefault(AppThemeMode.SYSTEM)
        val lang = AppLanguage.fromTag(prefs[Keys.LANGUAGE_TAG])
        val onboarded = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        UserPreferences(
            themeMode = theme,
            language = lang,
            hasCompletedOnboarding = onboarded
        )
    }

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.transitMateDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode.name
        }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        context.transitMateDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_TAG] = language.tag
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.transitMateDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }
}
