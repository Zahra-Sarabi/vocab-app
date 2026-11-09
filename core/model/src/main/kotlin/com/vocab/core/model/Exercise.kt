package com.vocab.core.model

/** One question in an exercise or test. */
sealed interface Question {
    val wordId: String

    /** English word → choose the Persian meaning. */
    data class MeaningChoice(override val wordId: String, val word: String, val options: List<String>, val correctIndex: Int) : Question

    /** Persian meaning → choose the English word. */
    data class WordChoice(override val wordId: String, val meaningFa: String, val options: List<String>, val correctIndex: Int) : Question

    /** Type the missing word in a sentence containing `___`. */
    data class Gap(override val wordId: String, val sentence: String, val answer: String, val hintFa: String?) : Question

    /** Hear the word and type it. */
    data class Listen(override val wordId: String, val answer: String) : Question

    /** Pair English words with their meanings. [wordId] is the first pair's word. */
    data class Match(override val wordId: String, val pairs: List<MatchPair>) : Question
}

data class MatchPair(val wordId: String, val word: String, val meaningFa: String)

enum class AnswerVerdict(val credit: Double) { CORRECT(1.0), ALMOST(0.5), WRONG(0.0) }

data class AnswerRecord(val question: Question, val verdict: AnswerVerdict, val given: String)
