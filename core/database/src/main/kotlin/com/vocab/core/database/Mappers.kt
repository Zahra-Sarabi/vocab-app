package com.vocab.core.database

import com.vocab.core.database.entity.DailyActivityEntity
import com.vocab.core.database.entity.StepResultEntity
import com.vocab.core.database.entity.WordCardEntity
import com.vocab.core.model.CardPhase
import com.vocab.core.model.DailyActivity
import com.vocab.core.model.LessonStep
import com.vocab.core.model.StepResult
import com.vocab.core.model.WordCard

fun WordCardEntity.toModel() = WordCard(
    wordId, unitId, CardPhase.valueOf(phase), stability, difficulty, dueAt, lastReviewedAt, reps, lapses, wrongCount, starred, note,
)

fun WordCard.toEntity() = WordCardEntity(
    wordId, unitId, phase.name, stability, difficulty, dueAt, lastReviewedAt, reps, lapses, wrongCount, starred, note,
)

/** Rows written by a future step this version doesn't know are skipped, not crashed on. */
fun StepResultEntity.toModelOrNull() =
    LessonStep.entries.firstOrNull { it.name == step }?.let { StepResult(unitId, it, bestScore, completedAt) }

fun DailyActivityEntity.toModel() = DailyActivity(epochDay, newWords, reviews, answered, correct)
