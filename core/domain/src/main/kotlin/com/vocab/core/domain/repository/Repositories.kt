package com.vocab.core.domain.repository

import com.vocab.core.model.DailyActivity
import com.vocab.core.model.Level
import com.vocab.core.model.LessonStep
import com.vocab.core.model.Rating
import com.vocab.core.model.StepResult
import com.vocab.core.model.UserSettings
import com.vocab.core.model.VocabUnit
import com.vocab.core.model.Word
import com.vocab.core.model.WordCard
import kotlinx.coroutines.flow.Flow

/** Read-only course content bundled with the app. */
interface ContentRepository {
    suspend fun levels(): List<Level>
    suspend fun unit(unitId: String): VocabUnit
    suspend fun words(wordIds: Collection<String>): Map<String, Word>
}

/** Everything the learner has done: cards, step scores, review history. */
interface ProgressRepository {
    fun observeCards(): Flow<List<WordCard>>
    fun observeStepResults(): Flow<List<StepResult>>
    suspend fun cards(wordIds: Collection<String>): List<WordCard>
    suspend fun dueCards(now: Long, limit: Int): List<WordCard>
    suspend fun upsertCards(cards: List<WordCard>)
    /** Saves [score] for the step, keeping the best score seen so far. */
    suspend fun recordStep(unitId: String, step: LessonStep, score: Int, at: Long)
    suspend fun logReview(wordId: String, rating: Rating, at: Long)
    suspend fun setStarred(wordId: String, starred: Boolean)
}

interface ActivityRepository {
    fun observeActivity(): Flow<List<DailyActivity>>
    suspend fun add(epochDay: Long, newWords: Int = 0, reviews: Int = 0, answered: Int = 0, correct: Int = 0)
}

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun update(transform: (UserSettings) -> UserSettings)
}

/** Serializes all progress and settings so they can move to another phone. */
interface BackupRepository {
    suspend fun createBackup(): ByteArray
    suspend fun inspect(bytes: ByteArray): BackupPreview
    /** Replaces all progress with the backup. A copy of the current state is kept first. */
    suspend fun restore(bytes: ByteArray)
}

data class BackupPreview(
    val exportedAt: Long,
    val learnedWords: Int,
    val passedUnits: Int,
    val streakDays: Int,
)

/** Reads and writes documents the user picked (Storage Access Framework URIs). */
interface DocumentStore {
    suspend fun read(uri: String): ByteArray
    suspend fun write(uri: String, bytes: ByteArray)
}

/** Speaks English words aloud, offline. */
interface Pronouncer {
    fun speak(text: String)
}

/** Keeps the daily reminder in line with the settings. */
interface ReminderScheduler {
    fun apply(settings: UserSettings)
}
