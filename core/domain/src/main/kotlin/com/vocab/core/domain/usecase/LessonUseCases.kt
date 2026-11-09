package com.vocab.core.domain.usecase

import com.vocab.core.domain.TimeSource
import com.vocab.core.domain.exercise.QuestionGenerator
import com.vocab.core.domain.progress.ProgressRules
import com.vocab.core.domain.repository.ActivityRepository
import com.vocab.core.domain.repository.ContentRepository
import com.vocab.core.domain.repository.ProgressRepository
import com.vocab.core.domain.srs.Fsrs
import com.vocab.core.model.AnswerRecord
import com.vocab.core.model.AnswerVerdict
import com.vocab.core.model.LessonStep
import com.vocab.core.model.Question
import com.vocab.core.model.Rating
import com.vocab.core.model.UnitSummary
import com.vocab.core.model.VocabUnit
import com.vocab.core.model.Word
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.roundToInt

data class StepSession(val unit: VocabUnit, val step: LessonStep, val questions: List<Question>)

class StartStepUseCase @Inject constructor(
    private val content: ContentRepository,
    private val generator: QuestionGenerator,
) {
    suspend operator fun invoke(unitId: String, step: LessonStep): StepSession {
        val unit = content.unit(unitId)
        return StepSession(unit, step, generator.questions(unit, step))
    }
}

/**
 * Finishing the learn step puts the unit's words into spaced repetition (first review tomorrow)
 * and counts them toward today's goal.
 */
class FinishLearnStepUseCase @Inject constructor(
    private val progress: ProgressRepository,
    private val activity: ActivityRepository,
    private val fsrs: Fsrs,
    private val time: TimeSource,
) {
    suspend operator fun invoke(unit: VocabUnit) {
        val now = time.now()
        val existing = progress.cards(unit.words.map { it.id }).associateBy { it.wordId }
        val newCards = unit.words.filter { it.id !in existing }.map { fsrs.first(it.id, unit.id, Rating.GOOD, now) }
        progress.upsertCards(newCards)
        progress.recordStep(unit.id, LessonStep.LEARN, 100, now)
        if (newCards.isNotEmpty()) activity.add(time.epochDay(now), newWords = newCards.size)
    }
}

data class StepOutcome(
    val step: LessonStep,
    val score: Int,
    val passed: Boolean,
    val stars: Int,
    val correctCount: Int,
    val total: Int,
    val missedWords: List<Word>,
    /** Set when passing this unit test opened the next unit. */
    val unlockedUnit: UnitSummary?,
)

class FinishExerciseStepUseCase @Inject constructor(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val activity: ActivityRepository,
    private val observePath: ObservePathUseCase,
    private val time: TimeSource,
) {
    suspend operator fun invoke(unit: VocabUnit, step: LessonStep, answers: List<AnswerRecord>): StepOutcome {
        val now = time.now()
        val total = answers.size.coerceAtLeast(1)
        val score = (answers.sumOf { it.verdict.credit } / total * 100).roundToInt()
        val passedBefore = ProgressRules.isPassed(currentScores(unit.id))

        val missedIds = answers.filter { it.verdict == AnswerVerdict.WRONG }.flatMap { missedWordIds(it.question) }.toSet()
        if (missedIds.isNotEmpty()) {
            val cards = progress.cards(missedIds)
            progress.upsertCards(cards.map { it.copy(wrongCount = it.wrongCount + 1) })
        }
        progress.recordStep(unit.id, step, score, now)
        activity.add(
            time.epochDay(now),
            answered = answers.size,
            correct = answers.count { it.verdict == AnswerVerdict.CORRECT },
        )

        val passed = score >= ProgressRules.PASS_SCORE
        val unlocked = if (step == LessonStep.UNIT_TEST && passed && !passedBefore) nextUnit(unit.id) else null
        return StepOutcome(
            step = step,
            score = score,
            passed = passed,
            stars = if (step == LessonStep.UNIT_TEST) ProgressRules.stars(score) else 0,
            correctCount = answers.count { it.verdict == AnswerVerdict.CORRECT },
            total = answers.size,
            missedWords = content.words(missedIds).values.toList(),
            unlockedUnit = unlocked,
        )
    }

    private suspend fun currentScores(unitId: String): Map<LessonStep, Int> =
        observePath().first().flatMap { it.units }.firstOrNull { it.unit.id == unitId }?.stepScores.orEmpty()

    private suspend fun nextUnit(unitId: String): UnitSummary? {
        val all = observePath().first().flatMap { it.units }.map { it.unit }
        val index = all.indexOfFirst { it.id == unitId }
        return all.getOrNull(index + 1)
    }

    private fun missedWordIds(question: Question): List<String> = when (question) {
        is Question.Match -> question.pairs.map { it.wordId }
        else -> listOf(question.wordId)
    }
}
