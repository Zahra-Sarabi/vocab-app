package com.vocab.core.domain.exercise

import com.vocab.core.model.LessonStep
import com.vocab.core.model.MatchPair
import com.vocab.core.model.Question
import com.vocab.core.model.VocabUnit
import com.vocab.core.model.Word
import javax.inject.Inject
import kotlin.random.Random

/**
 * Builds the questions of each exercise step from a unit's words, so content files only
 * list words (plus optional hand-written gap questions).
 */
class QuestionGenerator @Inject constructor() {

    fun questions(unit: VocabUnit, step: LessonStep, random: Random = Random.Default): List<Question> {
        val words = unit.words.shuffled(random)
        return when (step) {
            LessonStep.LEARN -> emptyList()
            LessonStep.MEANING -> words.map { meaningChoice(it, unit.words, random) }
            LessonStep.MATCH -> words.chunked(MATCH_SIZE).filter { it.size >= 2 }.map { match(it) }
            LessonStep.GAP -> gapQuestions(unit, random)
            LessonStep.LISTEN -> words.map { Question.Listen(it.id, it.text) }
            LessonStep.UNIT_TEST -> unitTest(unit, words, random)
        }
    }

    private fun unitTest(unit: VocabUnit, words: List<Word>, random: Random): List<Question> {
        val gaps = gapsByWord(unit)
        return words.mapIndexed { index, word ->
            when (index % 4) {
                0 -> meaningChoice(word, unit.words, random)
                1 -> wordChoice(word, unit.words, random)
                2 -> gaps[word.id] ?: meaningChoice(word, unit.words, random)
                else -> Question.Listen(word.id, word.text)
            }
        }
    }

    private fun meaningChoice(word: Word, pool: List<Word>, random: Random): Question.MeaningChoice {
        val options = (distractors(word, pool, random).map { it.meaningFa } + word.meaningFa).shuffled(random)
        return Question.MeaningChoice(word.id, word.text, options, options.indexOf(word.meaningFa))
    }

    private fun wordChoice(word: Word, pool: List<Word>, random: Random): Question.WordChoice {
        val options = (distractors(word, pool, random).map { it.text } + word.text).shuffled(random)
        return Question.WordChoice(word.id, word.meaningFa, options, options.indexOf(word.text))
    }

    private fun distractors(word: Word, pool: List<Word>, random: Random): List<Word> =
        pool.filter { it.id != word.id && it.meaningFa != word.meaningFa }
            .distinctBy { it.meaningFa }
            .shuffled(random)
            .take(OPTION_COUNT - 1)

    private fun match(words: List<Word>) =
        Question.Match(words.first().id, words.map { MatchPair(it.id, it.text, it.meaningFa) })

    private fun gapQuestions(unit: VocabUnit, random: Random): List<Question> =
        gapsByWord(unit).values.shuffled(random)

    /** Hand-written gap questions first, then gaps cut from example sentences. */
    private fun gapsByWord(unit: VocabUnit): Map<String, Question.Gap> {
        val result = linkedMapOf<String, Question.Gap>()
        val byText = unit.words.associateBy { it.text.lowercase() }
        unit.gapQuestions.forEach { gap ->
            val word = byText[gap.answer.lowercase()] ?: return@forEach
            result.putIfAbsent(word.id, Question.Gap(word.id, gap.sentence, gap.answer, gap.hintFa))
        }
        unit.words.forEach { word ->
            if (word.id in result) return@forEach
            val pattern = Regex("\\b${Regex.escape(word.text)}\\b", RegexOption.IGNORE_CASE)
            val match = pattern.find(word.example) ?: return@forEach
            val sentence = word.example.replaceRange(match.range, BLANK)
            result[word.id] = Question.Gap(word.id, sentence, match.value, word.meaningFa)
        }
        return result
    }

    companion object {
        const val BLANK = "___"
        const val OPTION_COUNT = 4
        const val MATCH_SIZE = 4
    }
}
