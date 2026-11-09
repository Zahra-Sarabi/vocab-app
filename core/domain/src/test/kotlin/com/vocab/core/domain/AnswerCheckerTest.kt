package com.vocab.core.domain

import com.vocab.core.domain.exercise.AnswerChecker
import com.vocab.core.model.AnswerVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerCheckerTest {
    private val checker = AnswerChecker()

    @Test fun `case and spaces are ignored`() = assertEquals(AnswerVerdict.CORRECT, checker.check("  Only   Child ", "only child"))

    @Test fun `sentence punctuation is ignored`() = assertEquals(AnswerVerdict.CORRECT, checker.check("Well done!", "well done"))

    @Test fun `accents are ignored`() = assertEquals(AnswerVerdict.CORRECT, checker.check("cafe", "café"))

    @Test fun `apostrophes still count`() = assertEquals(AnswerVerdict.CORRECT, checker.check("Don’t like", "don't like"))

    @Test fun `one typo is almost right`() = assertEquals(AnswerVerdict.ALMOST, checker.check("nephiw", "nephew"))

    @Test fun `two typos are wrong`() = assertEquals(AnswerVerdict.WRONG, checker.check("nefw", "nephew"))

    @Test fun `short words need to be exact`() = assertEquals(AnswerVerdict.WRONG, checker.check("sin", "son"))

    @Test fun `empty is wrong`() = assertEquals(AnswerVerdict.WRONG, checker.check("  ", "aunt"))
}
