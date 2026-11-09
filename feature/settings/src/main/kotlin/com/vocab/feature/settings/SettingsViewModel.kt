package com.vocab.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocab.core.domain.TimeSource
import com.vocab.core.domain.repository.SettingsRepository
import com.vocab.core.domain.usecase.ExportBackupUseCase
import com.vocab.core.domain.usecase.InspectBackupUseCase
import com.vocab.core.domain.usecase.PendingRestore
import com.vocab.core.domain.usecase.RestoreBackupUseCase
import com.vocab.core.domain.usecase.UpdateSettingsUseCase
import com.vocab.core.model.Accent
import com.vocab.core.model.ThemeMode
import com.vocab.core.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

enum class SettingsMessage { BACKUP_SAVED, BACKUP_FAILED, RESTORE_DONE, RESTORE_INVALID }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val updateSettings: UpdateSettingsUseCase,
    private val exportBackup: ExportBackupUseCase,
    private val inspectBackup: InspectBackupUseCase,
    private val restoreBackup: RestoreBackupUseCase,
    private val time: TimeSource,
) : ViewModel() {

    val settings: StateFlow<UserSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _pending = MutableStateFlow<PendingRestore?>(null)
    val pendingRestore: StateFlow<PendingRestore?> = _pending.asStateFlow()

    private val _message = MutableStateFlow<SettingsMessage?>(null)
    val message: StateFlow<SettingsMessage?> = _message.asStateFlow()

    fun backupFileName(): String = "vocab-backup-${LocalDate.now(time.zone())}.json"

    fun daysSince(millis: Long): Long =
        LocalDate.now(time.zone()).toEpochDay() - Instant.ofEpochMilli(millis).atZone(time.zone()).toLocalDate().toEpochDay()

    fun setDailyGoal(goal: Int) = update { it.copy(dailyGoal = goal) }
    fun setReminder(enabled: Boolean) = update { it.copy(reminderEnabled = enabled) }
    fun setReminderTime(hour: Int, minute: Int) = update { it.copy(reminderMinuteOfDay = hour * 60 + minute) }
    fun setStreakAlert(enabled: Boolean) = update { it.copy(streakAlertEnabled = enabled) }
    fun setAccent(accent: Accent) = update { it.copy(accent = accent) }
    fun setTheme(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    fun export(uri: String) = viewModelScope.launch {
        _message.value = runCatching { exportBackup(uri) }.fold({ SettingsMessage.BACKUP_SAVED }, { SettingsMessage.BACKUP_FAILED })
    }

    fun inspect(uri: String) = viewModelScope.launch {
        runCatching { inspectBackup(uri) }
            .onSuccess { _pending.value = it }
            .onFailure { _message.value = SettingsMessage.RESTORE_INVALID }
    }

    fun confirmRestore() {
        val pending = _pending.value ?: return
        _pending.value = null
        viewModelScope.launch {
            _message.value = runCatching { restoreBackup(pending) }.fold({ SettingsMessage.RESTORE_DONE }, { SettingsMessage.RESTORE_INVALID })
        }
    }

    fun cancelRestore() {
        _pending.value = null
    }

    fun messageShown() {
        _message.value = null
    }

    private fun update(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch { updateSettings(transform) }
    }
}
