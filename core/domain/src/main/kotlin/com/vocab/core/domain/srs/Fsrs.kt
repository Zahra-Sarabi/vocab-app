package com.vocab.core.domain.srs

import com.vocab.core.domain.MILLIS_PER_DAY
import com.vocab.core.domain.MILLIS_PER_MINUTE
import com.vocab.core.model.CardPhase
import com.vocab.core.model.Rating
import com.vocab.core.model.WordCard
import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * FSRS-5 spaced-repetition scheduler (the algorithm Anki uses), with its published default weights.
 *
 * Stability S is the number of days after which recall probability falls to 90%;
 * difficulty D runs from 1 (easy) to 10 (hard). With [desiredRetention] = 0.9 the next
 * interval equals S.
 */
class Fsrs @Inject constructor() {
    var desiredRetention: Double = 0.9
    private val w = DEFAULT_WEIGHTS

    /** Schedules a word seen for the first time. */
    fun first(wordId: String, unitId: String, rating: Rating, now: Long): WordCard {
        val stability = initialStability(rating)
        val difficulty = initialDifficulty(rating)
        return WordCard(
            wordId = wordId,
            unitId = unitId,
            phase = if (rating == Rating.AGAIN) CardPhase.LEARNING else CardPhase.REVIEW,
            stability = stability,
            difficulty = difficulty,
            dueAt = due(now, rating, stability),
            lastReviewedAt = now,
            reps = 1,
            lapses = 0,
            wrongCount = if (rating == Rating.AGAIN) 1 else 0,
            starred = false,
            note = null,
        )
    }

    /** Schedules the next review of [card] after the learner rated it [rating] at [now]. */
    fun next(card: WordCard, rating: Rating, now: Long): WordCard {
        val elapsedDays = max(0.0, (now - card.lastReviewedAt).toDouble() / MILLIS_PER_DAY)
        val difficulty = nextDifficulty(card.difficulty, rating)
        val stability = when {
            elapsedDays < 1.0 -> shortTermStability(card.stability, rating)
            rating == Rating.AGAIN -> forgetStability(card.difficulty, card.stability, retrievability(elapsedDays, card.stability))
            else -> recallStability(card.difficulty, card.stability, retrievability(elapsedDays, card.stability), rating)
        }.coerceAtLeast(MIN_STABILITY)
        val lapsed = rating == Rating.AGAIN && card.phase == CardPhase.REVIEW
        return card.copy(
            phase = when {
                rating == Rating.AGAIN && card.phase == CardPhase.REVIEW -> CardPhase.RELEARNING
                rating == Rating.AGAIN -> card.phase
                else -> CardPhase.REVIEW
            },
            stability = stability,
            difficulty = difficulty,
            dueAt = due(now, rating, stability),
            lastReviewedAt = now,
            reps = card.reps + 1,
            lapses = card.lapses + if (lapsed) 1 else 0,
            wrongCount = card.wrongCount + if (rating == Rating.AGAIN) 1 else 0,
        )
    }

    /** Days until the next review for a given stability, at least one. */
    fun intervalDays(stability: Double): Long {
        val days = stability / FACTOR * (desiredRetention.pow(1 / DECAY) - 1)
        return days.roundToLong().coerceIn(1, MAX_INTERVAL_DAYS)
    }

    fun retrievability(elapsedDays: Double, stability: Double): Double =
        (1 + FACTOR * elapsedDays / stability).pow(DECAY)

    private fun due(now: Long, rating: Rating, stability: Double): Long =
        if (rating == Rating.AGAIN) now + RELEARN_DELAY_MINUTES * MILLIS_PER_MINUTE
        else now + intervalDays(stability) * MILLIS_PER_DAY

    private fun initialStability(rating: Rating) = max(w[rating.value - 1], MIN_STABILITY)

    private fun initialDifficulty(rating: Rating) =
        clampDifficulty(w[4] - exp(w[5] * (rating.value - 1)) + 1)

    private fun nextDifficulty(d: Double, rating: Rating): Double {
        val delta = -w[6] * (rating.value - 3)
        val damped = d + delta * (10 - d) / 9
        val reverted = w[7] * initialDifficulty(Rating.EASY) + (1 - w[7]) * damped
        return clampDifficulty(reverted)
    }

    private fun recallStability(d: Double, s: Double, r: Double, rating: Rating): Double {
        val hardPenalty = if (rating == Rating.HARD) w[15] else 1.0
        val easyBonus = if (rating == Rating.EASY) w[16] else 1.0
        return s * (1 + exp(w[8]) * (11 - d) * s.pow(-w[9]) * (exp((1 - r) * w[10]) - 1) * hardPenalty * easyBonus)
    }

    private fun forgetStability(d: Double, s: Double, r: Double): Double {
        val next = w[11] * d.pow(-w[12]) * ((s + 1).pow(w[13]) - 1) * exp((1 - r) * w[14])
        return min(next, s)
    }

    private fun shortTermStability(s: Double, rating: Rating): Double =
        s * exp(w[17] * (rating.value - 3 + w[18]))

    private fun clampDifficulty(d: Double) = d.coerceIn(1.0, 10.0)

    companion object {
        private const val DECAY = -0.5
        private const val FACTOR = 19.0 / 81.0
        private const val MIN_STABILITY = 0.01
        private const val MAX_INTERVAL_DAYS = 36_500L
        const val RELEARN_DELAY_MINUTES = 10L

        val DEFAULT_WEIGHTS = doubleArrayOf(
            0.40255, 1.18385, 3.173, 15.69105, 7.1949, 0.5345, 1.4604, 0.0046, 1.54575, 0.1192,
            1.01925, 1.9395, 0.11, 0.29605, 2.2698, 0.2315, 2.9898, 0.51655, 0.6621,
        )
    }
}
