package com.vocab.core.domain

import com.vocab.core.domain.progress.ProgressRules
import com.vocab.core.model.CardPhase
import com.vocab.core.model.LessonStep
import com.vocab.core.model.StepResult
import com.vocab.core.model.UnitStatus
import com.vocab.core.model.WordCard
import com.vocab.core.model.WordState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressRulesTest {

    private fun card(unit: String, stability: Double, reps: Int = 3, phase: CardPhase = CardPhase.REVIEW) =
        WordCard("$unit-$stability-${Math.random()}", unit, phase, stability, 5.0, 0, 0, reps, 0, 0, false, null)

    @Test
    fun `stars follow the pass marks`() {
        assertEquals(0, ProgressRules.stars(69))
        assertEquals(1, ProgressRules.stars(70))
        assertEquals(2, ProgressRules.stars(85))
        assertEquals(3, ProgressRules.stars(95))
    }

    @Test
    fun `word state comes from the card`() {
        assertEquals(WordState.NEW, ProgressRules.wordState(null))
        assertEquals(WordState.LEARNING, ProgressRules.wordState(card("u", 3.0, reps = 1)))
        assertEquals(WordState.REVIEW, ProgressRules.wordState(card("u", 5.0)))
        assertEquals(WordState.MASTERED, ProgressRules.wordState(card("u", 30.0)))
    }

    @Test
    fun `steps unlock in order at 70 percent`() {
        assertTrue(ProgressRules.isStepUnlocked(LessonStep.LEARN, emptyMap()))
        assertFalse(ProgressRules.isStepUnlocked(LessonStep.MEANING, emptyMap()))
        assertTrue(ProgressRules.isStepUnlocked(LessonStep.MEANING, mapOf(LessonStep.LEARN to 100)))
        assertFalse(ProgressRules.isStepUnlocked(LessonStep.MATCH, mapOf(LessonStep.LEARN to 100, LessonStep.MEANING to 60)))
    }

    @Test
    fun `units open one after another across levels`() {
        val fresh = ProgressRules.levelProgress(TestContent.levels, emptyList(), emptyList())
        assertEquals(UnitStatus.OPEN, fresh[0].units[0].status)
        assertEquals(UnitStatus.LOCKED, fresh[0].units[1].status)
        assertFalse(fresh[1].unlocked)

        val passed = listOf(StepResult("u1", LessonStep.UNIT_TEST, 90, 0), StepResult("u2", LessonStep.UNIT_TEST, 80, 0))
        val cards = List(4) { card("u1", 30.0) } + List(4) { card("u2", 30.0) }
        val later = ProgressRules.levelProgress(TestContent.levels, cards, passed)
        assertEquals(UnitStatus.PASSED, later[0].units[0].status)
        assertEquals(2, later[0].units[0].stars)
        assertTrue(later[1].unlocked)
        assertEquals(UnitStatus.OPEN, later[1].units[0].status)
    }

    @Test
    fun `passed unit with fading memory needs review`() {
        val passed = listOf(StepResult("u1", LessonStep.UNIT_TEST, 90, 0))
        val weak = List(4) { card("u1", 2.0, reps = 1, phase = CardPhase.RELEARNING) }
        val progress = ProgressRules.levelProgress(TestContent.levels, weak, passed)
        assertEquals(UnitStatus.NEEDS_REVIEW, progress[0].units[0].status)
    }

    @Test
    fun `streak counts back from today or yesterday`() {
        assertEquals(3, ProgressRules.streak(setOf(10, 9, 8, 5), today = 10))
        assertEquals(3, ProgressRules.streak(setOf(9, 8, 7), today = 10))
        assertEquals(0, ProgressRules.streak(setOf(7), today = 10))
    }
}
