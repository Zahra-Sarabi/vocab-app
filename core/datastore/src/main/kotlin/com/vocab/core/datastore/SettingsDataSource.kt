package com.vocab.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vocab.core.model.Accent
import com.vocab.core.model.ThemeMode
import com.vocab.core.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** User settings in Preferences DataStore. Unknown or missing values fall back to defaults. */
@Singleton
class SettingsDataSource @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.settingsStore

    val settings: Flow<UserSettings> = store.data.map { it.toSettings() }

    suspend fun update(transform: (UserSettings) -> UserSettings) {
        store.edit { prefs ->
            val next = transform(prefs.toSettings())
            prefs[Keys.dailyGoal] = next.dailyGoal
            prefs[Keys.reviewCap] = next.reviewCap
            prefs[Keys.reminderEnabled] = next.reminderEnabled
            prefs[Keys.reminderMinute] = next.reminderMinuteOfDay
            prefs[Keys.streakAlert] = next.streakAlertEnabled
            prefs[Keys.accent] = next.accent.name
            prefs[Keys.speechRate] = next.speechRate
            prefs[Keys.theme] = next.themeMode.name
            prefs[Keys.onboardingDone] = next.onboardingDone
            val backup = next.lastBackupAt
            if (backup == null) prefs.remove(Keys.lastBackupAt) else prefs[Keys.lastBackupAt] = backup
        }
    }

    suspend fun current(): UserSettings = settings.first()

    private fun Preferences.toSettings(): UserSettings {
        val defaults = UserSettings()
        return UserSettings(
            dailyGoal = this[Keys.dailyGoal] ?: defaults.dailyGoal,
            reviewCap = this[Keys.reviewCap] ?: defaults.reviewCap,
            reminderEnabled = this[Keys.reminderEnabled] ?: defaults.reminderEnabled,
            reminderMinuteOfDay = this[Keys.reminderMinute] ?: defaults.reminderMinuteOfDay,
            streakAlertEnabled = this[Keys.streakAlert] ?: defaults.streakAlertEnabled,
            accent = this[Keys.accent]?.let { name -> Accent.entries.firstOrNull { it.name == name } } ?: defaults.accent,
            speechRate = this[Keys.speechRate] ?: defaults.speechRate,
            themeMode = this[Keys.theme]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: defaults.themeMode,
            lastBackupAt = this[Keys.lastBackupAt],
            onboardingDone = this[Keys.onboardingDone] ?: defaults.onboardingDone,
        )
    }

    private object Keys {
        val dailyGoal = intPreferencesKey("daily_goal")
        val reviewCap = intPreferencesKey("review_cap")
        val reminderEnabled = booleanPreferencesKey("reminder_enabled")
        val reminderMinute = intPreferencesKey("reminder_minute")
        val streakAlert = booleanPreferencesKey("streak_alert")
        val accent = stringPreferencesKey("accent")
        val speechRate = floatPreferencesKey("speech_rate")
        val theme = stringPreferencesKey("theme")
        val lastBackupAt = longPreferencesKey("last_backup_at")
        val onboardingDone = booleanPreferencesKey("onboarding_done")
    }
}
