package com.vocab.core.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.vocab.core.database.VocabDatabase
import com.vocab.core.database.entity.DailyActivityEntity
import com.vocab.core.database.entity.ReviewLogEntity
import com.vocab.core.database.entity.StepResultEntity
import com.vocab.core.database.entity.WordCardEntity
import com.vocab.core.datastore.SettingsDataSource
import com.vocab.core.domain.TimeSource
import com.vocab.core.domain.progress.ProgressRules
import com.vocab.core.domain.repository.BackupPreview
import com.vocab.core.domain.repository.BackupRepository
import com.vocab.core.model.Accent
import com.vocab.core.model.LessonStep
import com.vocab.core.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class InvalidBackupException(message: String) : Exception(message)

class JsonBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: VocabDatabase,
    private val settings: SettingsDataSource,
    private val time: TimeSource,
) : BackupRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    override suspend fun createBackup(): ByteArray = withContext(Dispatchers.IO) {
        json.encodeToString(BackupFile.serializer(), snapshot()).toByteArray()
    }

    override suspend fun inspect(bytes: ByteArray): BackupPreview {
        val file = parse(bytes)
        return BackupPreview(
            exportedAt = file.exportedAt,
            learnedWords = file.cards.size,
            passedUnits = file.steps.count { it.step == LessonStep.UNIT_TEST.name && it.bestScore >= ProgressRules.PASS_SCORE },
            streakDays = ProgressRules.streak(
                file.activity.filter { it.newWords + it.reviews + it.answered > 0 }.map { it.epochDay }.toSet(),
                time.epochDay(file.exportedAt),
            ),
        )
    }

    override suspend fun restore(bytes: ByteArray): Unit = withContext(Dispatchers.IO) {
        val file = parse(bytes)
        keepSafetyCopy()
        db.withTransaction {
            db.wordCardDao().clear()
            db.stepResultDao().clear()
            db.reviewLogDao().clear()
            db.dailyActivityDao().clear()
            db.wordCardDao().upsert(file.cards.map {
                WordCardEntity(it.wordId, it.unitId, it.phase, it.stability, it.difficulty, it.dueAt, it.lastReviewedAt, it.reps, it.lapses, it.wrongCount, it.starred, it.note)
            })
            db.stepResultDao().upsert(file.steps.map { StepResultEntity(it.unitId, it.step, it.bestScore, it.completedAt) })
            db.reviewLogDao().insert(file.reviews.map { ReviewLogEntity(wordId = it.wordId, rating = it.rating, reviewedAt = it.reviewedAt) })
            db.dailyActivityDao().upsert(file.activity.map { DailyActivityEntity(it.epochDay, it.newWords, it.reviews, it.answered, it.correct) })
        }
        file.settings?.let { s ->
            settings.update { current ->
                current.copy(
                    dailyGoal = s.dailyGoal ?: current.dailyGoal,
                    reviewCap = s.reviewCap ?: current.reviewCap,
                    reminderEnabled = s.reminderEnabled ?: current.reminderEnabled,
                    reminderMinuteOfDay = s.reminderMinuteOfDay ?: current.reminderMinuteOfDay,
                    streakAlertEnabled = s.streakAlertEnabled ?: current.streakAlertEnabled,
                    accent = Accent.entries.firstOrNull { it.name == s.accent } ?: current.accent,
                    speechRate = s.speechRate ?: current.speechRate,
                    themeMode = ThemeMode.entries.firstOrNull { it.name == s.themeMode } ?: current.themeMode,
                    lastBackupAt = file.exportedAt,
                    onboardingDone = true,
                )
            }
        }
        Unit
    }

    private fun parse(bytes: ByteArray): BackupFile {
        val file = try {
            json.decodeFromString(BackupFile.serializer(), bytes.decodeToString())
        } catch (e: SerializationException) {
            throw InvalidBackupException("Not a backup file")
        } catch (e: IllegalArgumentException) {
            throw InvalidBackupException("Not a backup file")
        }
        if (file.format != BackupFile.FORMAT) throw InvalidBackupException("Not a backup file")
        if (file.schemaVersion > BackupFile.CURRENT_SCHEMA) throw InvalidBackupException("Backup is from a newer version of the app")
        return file
    }

    private suspend fun snapshot(): BackupFile {
        val prefs = settings.current()
        return BackupFile(
            exportedAt = time.now(),
            cards = db.wordCardDao().all().map {
                CardDto(it.wordId, it.unitId, it.phase, it.stability, it.difficulty, it.dueAt, it.lastReviewedAt, it.reps, it.lapses, it.wrongCount, it.starred, it.note)
            },
            steps = db.stepResultDao().all().map { StepDto(it.unitId, it.step, it.bestScore, it.completedAt) },
            reviews = db.reviewLogDao().all().map { ReviewDto(it.wordId, it.rating, it.reviewedAt) },
            activity = db.dailyActivityDao().all().map { ActivityDto(it.epochDay, it.newWords, it.reviews, it.answered, it.correct) },
            settings = SettingsDto(
                prefs.dailyGoal, prefs.reviewCap, prefs.reminderEnabled, prefs.reminderMinuteOfDay,
                prefs.streakAlertEnabled, prefs.accent.name, prefs.speechRate, prefs.themeMode.name,
            ),
        )
    }

    /** Keeps the state from before a restore, in case the wrong file was picked. Last three kept. */
    private suspend fun keepSafetyCopy() {
        val dir = File(context.filesDir, SAFETY_DIR).apply { mkdirs() }
        File(dir, "before-restore-${time.now()}.json").writeText(json.encodeToString(BackupFile.serializer(), snapshot()))
        dir.listFiles()?.sortedByDescending { it.name }?.drop(SAFETY_COPIES)?.forEach { it.delete() }
    }

    private companion object {
        const val SAFETY_DIR = "backups"
        const val SAFETY_COPIES = 3
    }
}
