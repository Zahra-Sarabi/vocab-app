package com.vocab.core.model

/** The steps of a unit, in the order they unlock. */
enum class LessonStep { LEARN, MEANING, MATCH, GAP, LISTEN, UNIT_TEST }

/** Where a spaced-repetition card is in its life. */
enum class CardPhase { LEARNING, REVIEW, RELEARNING }

/** How the learner rates their recall in a review. */
enum class Rating(val value: Int) { AGAIN(1), HARD(2), GOOD(3), EASY(4) }

/** The learner-facing state of a word, shown as a pill. */
enum class WordState { NEW, LEARNING, REVIEW, MASTERED }

enum class UnitStatus { LOCKED, OPEN, IN_PROGRESS, PASSED, NEEDS_REVIEW }

/** Spaced-repetition memory of one word. Times are epoch milliseconds. */
data class WordCard(
    val wordId: String,
    val unitId: String,
    val phase: CardPhase,
    val stability: Double,
    val difficulty: Double,
    val dueAt: Long,
    val lastReviewedAt: Long,
    val reps: Int,
    val lapses: Int,
    val wrongCount: Int,
    val starred: Boolean,
    val note: String?,
) {
    val isHard: Boolean get() = starred || wrongCount >= HARD_THRESHOLD

    companion object {
        const val HARD_THRESHOLD = 3
    }
}

/** Best score (0–100) the learner reached on a step of a unit. */
data class StepResult(val unitId: String, val step: LessonStep, val bestScore: Int, val completedAt: Long)

/** One day of activity. [epochDay] is days since 1970-01-01 in the device time zone. */
data class DailyActivity(
    val epochDay: Long,
    val newWords: Int,
    val reviews: Int,
    val answered: Int,
    val correct: Int,
)

data class UnitProgress(
    val unit: UnitSummary,
    val status: UnitStatus,
    val stepScores: Map<LessonStep, Int>,
    val stars: Int,
    /** 0.0–1.0, how much of the unit is in memory right now. */
    val mastery: Double,
) {
    val testScore: Int? get() = stepScores[LessonStep.UNIT_TEST]
}

data class LevelProgress(
    val level: Level,
    val units: List<UnitProgress>,
    val unlocked: Boolean,
) {
    val mastery: Double get() = if (units.isEmpty()) 0.0 else units.sumOf { it.mastery } / units.size
    val passedCount: Int get() = units.count { it.status == UnitStatus.PASSED || it.status == UnitStatus.NEEDS_REVIEW }
}
