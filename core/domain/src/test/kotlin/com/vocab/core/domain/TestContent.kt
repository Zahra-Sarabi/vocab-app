package com.vocab.core.domain

import com.vocab.core.model.GapQuestion
import com.vocab.core.model.Illustration
import com.vocab.core.model.Level
import com.vocab.core.model.TopicColor
import com.vocab.core.model.UnitSummary
import com.vocab.core.model.VocabUnit
import com.vocab.core.model.Word

internal object TestContent {
    fun word(id: String, text: String, meaning: String, example: String = "This is my $text.") =
        Word(id, "u1", text, meaning, null, null, example, null, null, emptyList(), emptyList())

    fun summary(id: String, order: Int, levelId: String = "l1", words: Int = 4) =
        UnitSummary(id, levelId, order, id, id, Illustration.FAMILY, TopicColor.PINK, words)

    val unit = VocabUnit(
        summary = summary("u1", 1, words = 6),
        descriptionFa = "",
        words = listOf(
            word("w1", "mother", "مادر"),
            word("w2", "father", "پدر"),
            word("w3", "sister", "خواهر"),
            word("w4", "brother", "برادر"),
            word("w5", "aunt", "عمه"),
            word("w6", "uncle", "عمو", example = "No match here."),
        ),
        gapQuestions = listOf(GapQuestion("g1", "Nazanin is Arash's ___.", "sister", null)),
        referenceRows = emptyList(),
    )

    val levels = listOf(
        Level("l1", 1, "Elementary", "", listOf(summary("u1", 1), summary("u2", 2))),
        Level("l2", 2, "Pre-int", "", listOf(summary("u3", 1, "l2"))),
    )
}
