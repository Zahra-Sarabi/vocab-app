package com.vocab.core.domain.exercise

import com.vocab.core.model.AnswerVerdict
import java.text.Normalizer
import javax.inject.Inject
import kotlin.math.min

/** Grades typed answers: case and extra spaces don't matter, one typo counts as almost right. */
class AnswerChecker @Inject constructor() {

    fun check(given: String, expected: String): AnswerVerdict {
        val a = normalize(given)
        val b = normalize(expected)
        return when {
            a.isEmpty() -> AnswerVerdict.WRONG
            a == b -> AnswerVerdict.CORRECT
            b.length > MIN_LENGTH_FOR_TYPO && editDistance(a, b) <= 1 -> AnswerVerdict.ALMOST
            else -> AnswerVerdict.WRONG
        }
    }

    // Sentence punctuation and accents aren't part of the answer: "Well done!" matches "well done", "cafe" matches "café".
    private fun normalize(text: String) =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
            .replace(Regex("[.,!?]"), " ").trim().replace(Regex("\\s+"), " ").replace('’', '\'')

    private fun editDistance(a: String, b: String): Int {
        var previous = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val current = IntArray(b.length + 1)
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = min(min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost)
            }
            previous = current
        }
        return previous[b.length]
    }

    private companion object {
        const val MIN_LENGTH_FOR_TYPO = 3
    }
}
