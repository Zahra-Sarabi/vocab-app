package com.vocab.core.data.backup

import kotlinx.serialization.Serializable

/**
 * The backup file. Content is not included (it ships with the app), only the learner's data.
 * Add fields with defaults so older files keep loading; bump [BackupFile.CURRENT_SCHEMA]
 * only for a change older apps can't read.
 */
@Serializable
data class BackupFile(
    val format: String = FORMAT,
    val schemaVersion: Int = CURRENT_SCHEMA,
    val exportedAt: Long,
    val cards: List<CardDto> = emptyList(),
    val steps: List<StepDto> = emptyList(),
    val reviews: List<ReviewDto> = emptyList(),
    val activity: List<ActivityDto> = emptyList(),
    val settings: SettingsDto? = null,
) {
    companion object {
        const val FORMAT = "vocab-backup"
        const val CURRENT_SCHEMA = 1
    }
}

@Serializable
data class CardDto(
    val wordId: String,
    val unitId: String,
    val phase: String,
    val stability: Double,
    val difficulty: Double,
    val dueAt: Long,
    val lastReviewedAt: Long,
    val reps: Int,
    val lapses: Int,
    val wrongCount: Int = 0,
    val starred: Boolean = false,
    val note: String? = null,
)

@Serializable
data class StepDto(val unitId: String, val step: String, val bestScore: Int, val completedAt: Long)

@Serializable
data class ReviewDto(val wordId: String, val rating: Int, val reviewedAt: Long)

@Serializable
data class ActivityDto(val epochDay: Long, val newWords: Int, val reviews: Int, val answered: Int, val correct: Int)

@Serializable
data class SettingsDto(
    val dailyGoal: Int? = null,
    val reviewCap: Int? = null,
    val reminderEnabled: Boolean? = null,
    val reminderMinuteOfDay: Int? = null,
    val streakAlertEnabled: Boolean? = null,
    val accent: String? = null,
    val speechRate: Float? = null,
    val themeMode: String? = null,
)
