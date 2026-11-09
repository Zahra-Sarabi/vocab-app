package com.vocab.core.domain.usecase

import com.vocab.core.domain.TimeSource
import com.vocab.core.domain.repository.BackupPreview
import com.vocab.core.domain.repository.BackupRepository
import com.vocab.core.domain.repository.DocumentStore
import com.vocab.core.domain.repository.ReminderScheduler
import com.vocab.core.domain.repository.SettingsRepository
import com.vocab.core.model.UserSettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Saves settings and keeps the reminder schedule in step with them. */
class UpdateSettingsUseCase @Inject constructor(
    private val settings: SettingsRepository,
    private val reminders: ReminderScheduler,
) {
    suspend operator fun invoke(transform: (UserSettings) -> UserSettings) {
        settings.update(transform)
        reminders.apply(settings.settings.first())
    }
}

class ExportBackupUseCase @Inject constructor(
    private val backup: BackupRepository,
    private val documents: DocumentStore,
    private val settings: SettingsRepository,
    private val time: TimeSource,
) {
    suspend operator fun invoke(uri: String) {
        documents.write(uri, backup.createBackup())
        val now = time.now()
        settings.update { it.copy(lastBackupAt = now) }
    }
}

/** A backup file the user picked, read and checked but not applied yet. */
data class PendingRestore(val bytes: ByteArray, val preview: BackupPreview)

class InspectBackupUseCase @Inject constructor(
    private val backup: BackupRepository,
    private val documents: DocumentStore,
) {
    suspend operator fun invoke(uri: String): PendingRestore {
        val bytes = documents.read(uri)
        return PendingRestore(bytes, backup.inspect(bytes))
    }
}

class RestoreBackupUseCase @Inject constructor(
    private val backup: BackupRepository,
    private val settings: SettingsRepository,
    private val reminders: ReminderScheduler,
) {
    suspend operator fun invoke(pending: PendingRestore) {
        backup.restore(pending.bytes)
        reminders.apply(settings.settings.first())
    }
}
