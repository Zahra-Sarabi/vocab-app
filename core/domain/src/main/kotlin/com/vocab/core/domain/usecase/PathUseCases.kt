package com.vocab.core.domain.usecase

import com.vocab.core.domain.progress.ProgressRules
import com.vocab.core.domain.repository.ContentRepository
import com.vocab.core.domain.repository.ProgressRepository
import com.vocab.core.model.LessonStep
import com.vocab.core.model.LevelProgress
import com.vocab.core.model.UnitProgress
import com.vocab.core.model.VocabUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/** Levels and units with their live status, stars and mastery. */
class ObservePathUseCase @Inject constructor(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
) {
    operator fun invoke(): Flow<List<LevelProgress>> {
        val levels = flow { emit(content.levels()) }
        return combine(levels, progress.observeCards(), progress.observeStepResults()) { lv, cards, steps ->
            ProgressRules.levelProgress(lv, cards, steps)
        }
    }
}

data class StepState(val step: LessonStep, val score: Int?, val unlocked: Boolean)

data class UnitDetail(
    val unit: VocabUnit,
    val progress: UnitProgress,
    val steps: List<StepState>,
) {
    /** The first unlocked step that hasn't been passed yet, or the unit test to retake. */
    val nextStep: LessonStep
        get() = steps.firstOrNull { it.unlocked && (it.score ?: 0) < ProgressRules.PASS_SCORE }?.step
            ?: LessonStep.UNIT_TEST
}

class ObserveUnitDetailUseCase @Inject constructor(
    private val content: ContentRepository,
    private val observePath: ObservePathUseCase,
) {
    operator fun invoke(unitId: String): Flow<UnitDetail> {
        val unit = flow { emit(content.unit(unitId)) }
        return combine(unit, observePath()) { u, levels ->
            val progress = levels.flatMap { it.units }.first { it.unit.id == unitId }
            val steps = LessonStep.entries.map {
                StepState(it, progress.stepScores[it], ProgressRules.isStepUnlocked(it, progress.stepScores))
            }
            UnitDetail(u, progress, steps)
        }
    }
}
