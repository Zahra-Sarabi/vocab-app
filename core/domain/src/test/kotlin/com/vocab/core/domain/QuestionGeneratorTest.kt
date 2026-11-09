package com.vocab.core.domain

import com.vocab.core.domain.exercise.QuestionGenerator
import com.vocab.core.model.LessonStep
import com.vocab.core.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuestionGeneratorTest {
    private val generator = QuestionGenerator()
    private val unit = TestContent.unit

    @Test
    fun `meaning questions have four distinct options with the right answer`() {
        val questions = generator.questions(unit, LessonStep.MEANING, Random(1)).filterIsInstance<Question.MeaningChoice>()
        assertEquals(unit.words.size, questions.size)
        questions.forEach { q ->
            assertEquals(QuestionGenerator.OPTION_COUNT, q.options.size)
            assertEquals(q.options.size, q.options.toSet().size)
            val word = unit.words.first { it.id == q.wordId }
            assertEquals(word.meaningFa, q.options[q.correctIndex])
        }
    }

    @Test
    fun `hand-written gaps come first and examples fill the rest`() {
        val gaps = generator.questions(unit, LessonStep.GAP, Random(1)).filterIsInstance<Question.Gap>()
        val sister = gaps.first { it.wordId == "w3" }
        assertEquals("Nazanin is Arash's ___.", sister.sentence)
        assertTrue(gaps.none { it.wordId == "w6" })
        gaps.forEach { assertTrue(it.sentence.contains(QuestionGenerator.BLANK)) }
    }

    @Test
    fun `match questions cover every word once`() {
        val pairs = generator.questions(unit, LessonStep.MATCH, Random(1)).filterIsInstance<Question.Match>().flatMap { it.pairs }
        assertEquals(unit.words.map { it.id }.toSet(), pairs.map { it.wordId }.toSet())
    }

    @Test
    fun `unit test asks every word once`() {
        val questions = generator.questions(unit, LessonStep.UNIT_TEST, Random(1))
        assertEquals(unit.words.map { it.id }.sorted(), questions.map { it.wordId }.sorted())
    }
}
