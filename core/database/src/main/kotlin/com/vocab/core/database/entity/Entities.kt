package com.vocab.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "word_cards", indices = [Index("unit_id"), Index("due_at")])
data class WordCardEntity(
    @PrimaryKey @ColumnInfo(name = "word_id") val wordId: String,
    @ColumnInfo(name = "unit_id") val unitId: String,
    val phase: String,
    val stability: Double,
    val difficulty: Double,
    @ColumnInfo(name = "due_at") val dueAt: Long,
    @ColumnInfo(name = "last_reviewed_at") val lastReviewedAt: Long,
    val reps: Int,
    val lapses: Int,
    @ColumnInfo(name = "wrong_count") val wrongCount: Int,
    val starred: Boolean,
    val note: String?,
)

@Entity(tableName = "step_results", primaryKeys = ["unit_id", "step"])
data class StepResultEntity(
    @ColumnInfo(name = "unit_id") val unitId: String,
    val step: String,
    @ColumnInfo(name = "best_score") val bestScore: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Long,
)

@Entity(tableName = "review_log", indices = [Index("word_id")])
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "word_id") val wordId: String,
    val rating: Int,
    @ColumnInfo(name = "reviewed_at") val reviewedAt: Long,
)

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey @ColumnInfo(name = "epoch_day") val epochDay: Long,
    @ColumnInfo(name = "new_words") val newWords: Int,
    val reviews: Int,
    val answered: Int,
    val correct: Int,
)
