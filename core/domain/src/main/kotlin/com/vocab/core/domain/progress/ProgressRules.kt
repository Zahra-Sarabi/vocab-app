package com.vocab.core.domain.progress

import com.vocab.core.model.CardPhase
import com.vocab.core.model.LessonStep
import com.vocab.core.model.Level
import com.vocab.core.model.LevelProgress
import com.vocab.core.model.StepResult
import com.vocab.core.model.UnitProgress
import com.vocab.core.model.UnitStatus
import com.vocab.core.model.WordCard
import com.vocab.core.model.WordState

/** The rules from the product design: pass marks, stars, mastery and what unlocks what. */
object ProgressRules {
    const val PASS_SCORE = 70
    const val LEVEL_PASS_SCORE = 75
    const val NEEDS_REVIEW_MASTERY = 0.5
    const val MASTERED_STABILITY_DAYS = 21.0

    fun stars(score: Int?): Int = when {
        score == null || score < PASS_SCORE -> 0
        score < 85 -> 1
        score < 95 -> 2
        else -> 3
    }

    fun wordState(card: WordCard?): WordState = when {
        card == null -> WordState.NEW
        card.phase != CardPhase.REVIEW || card.reps <= 1 -> WordState.LEARNING
        card.stability >= MASTERED_STABILITY_DAYS -> WordState.MASTERED
        else -> WordState.REVIEW
    }

    /** Mastered words count fully, words in review 60%, words being learned 30%. */
    fun mastery(states: List<WordState>, totalWords: Int): Double {
        if (totalWords == 0) return 0.0
        val points = states.sumOf {
            when (it) {
                WordState.MASTERED -> 1.0
                WordState.REVIEW -> 0.6
                WordState.LEARNING -> 0.3
                WordState.NEW -> 0.0
            }
        }
        return (points / totalWords).coerceIn(0.0, 1.0)
    }

    /** A step opens once the step before it was passed. */
    fun isStepUnlocked(step: LessonStep, scores: Map<LessonStep, Int>): Boolean {
        val index = step.ordinal
        if (index == 0) return true
        val previous = LessonStep.entries[index - 1]
        return (scores[previous] ?: 0) >= PASS_SCORE
    }

    fun isPassed(scores: Map<LessonStep, Int>) = (scores[LessonStep.UNIT_TEST] ?: 0) >= PASS_SCORE

    /**
     * Builds the path: the first unit of the first level is open; every other unit opens when
     * the unit before it (across levels, in order) is passed.
     */
    fun levelProgress(levels: List<Level>, cards: List<WordCard>, steps: List<StepResult>): List<LevelProgress> {
        val cardsByUnit = cards.groupBy { it.unitId }
        val scoresByUnit = steps.groupBy { it.unitId }.mapValues { (_, list) -> list.associate { it.step to it.bestScore } }
        var previousPassed = true
        return levels.sortedBy { it.order }.map { level ->
            val levelUnlocked = previousPassed
            val units = level.units.sortedBy { it.order }.map { unit ->
                val scores = scoresByUnit[unit.id].orEmpty()
                val states = cardsByUnit[unit.id].orEmpty().map { wordState(it) }
                val mastery = mastery(states, unit.wordCount)
                val passed = isPassed(scores)
                val status = when {
                    passed && mastery < NEEDS_REVIEW_MASTERY -> UnitStatus.NEEDS_REVIEW
                    passed -> UnitStatus.PASSED
                    !previousPassed -> UnitStatus.LOCKED
                    scores.isNotEmpty() -> UnitStatus.IN_PROGRESS
                    else -> UnitStatus.OPEN
                }
                previousPassed = passed
                UnitProgress(unit, status, scores, stars(scores[LessonStep.UNIT_TEST]), mastery)
            }
            LevelProgress(level, units, levelUnlocked)
        }
    }

    /** Days in a row with any activity, ending today (or yesterday, if today is still empty). */
    fun streak(activeDays: Set<Long>, today: Long): Int {
        var day = if (today in activeDays) today else today - 1
        var count = 0
        while (day in activeDays) {
            count++
            day--
        }
        return count
    }
}
