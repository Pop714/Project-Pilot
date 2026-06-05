package net.pop.projectpilot.domain.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.pop.projectpilot.presentation.ui.theme.ThemeMode
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val THEME_KEY = stringPreferencesKey("app_theme_mode")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val savedTheme = preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(savedTheme)
    }

    suspend fun saveTheme(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }

}