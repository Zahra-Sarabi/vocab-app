package com.vocab.core.domain

import com.vocab.core.domain.srs.Fsrs
import com.vocab.core.model.CardPhase
import com.vocab.core.model.Rating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FsrsTest {
    private val fsrs = Fsrs()
    private val t0 = 1_700_000_000_000L

    private fun days(card: com.vocab.core.model.WordCard, from: Long) = (card.dueAt - from) / MILLIS_PER_DAY

    @Test
    fun `first rating sets initial stability from the weights`() {
        val good = fsrs.first("w", "u", Rating.GOOD, t0)
        assertEquals(3.173, good.stability, 1e-9)
        assertEquals(3L, days(good, t0))
        assertEquals(CardPhase.REVIEW, good.phase)
    }

    @Test
    fun `again comes back in minutes and counts as wrong`() {
        val again = fsrs.first("w", "u", Rating.AGAIN, t0)
        assertEquals(t0 + Fsrs.RELEARN_DELAY_MINUTES * MILLIS_PER_MINUTE, again.dueAt)
        assertEquals(CardPhase.LEARNING, again.phase)
        assertEquals(1, again.wrongCount)
    }

    @Test
    fun `easier ratings give longer intervals`() {
        val hard = fsrs.first("w", "u", Rating.HARD, t0)
        val good = fsrs.first("w", "u", Rating.GOOD, t0)
        val easy = fsrs.first("w", "u", Rating.EASY, t0)
        assertTrue(hard.dueAt <= good.dueAt)
        assertTrue(good.dueAt < easy.dueAt)
        assertTrue(easy.difficulty < good.difficulty)
    }

    @Test
    fun `reviewing on time with good grows the interval`() {
        var card = fsrs.first("w", "u", Rating.GOOD, t0)
        var previous = days(card, t0)
        repeat(4) {
            val now = card.dueAt
            card = fsrs.next(card, Rating.GOOD, now)
            val interval = days(card, now)
            assertTrue("interval $interval should exceed $previous", interval > previous)
            previous = interval
        }
    }

    @Test
    fun `forgetting a review card lowers stability and counts a lapse`() {
        val learned = fsrs.first("w", "u", Rating.GOOD, t0)
        val lapsed = fsrs.next(learned, Rating.AGAIN, learned.dueAt)
        assertTrue(lapsed.stability < learned.stability)
        assertEquals(1, lapsed.lapses)
        assertEquals(CardPhase.RELEARNING, lapsed.phase)
    }

    @Test
    fun `difficulty stays between 1 and 10`() {
        var card = fsrs.first("w", "u", Rating.AGAIN, t0)
        repeat(20) { card = fsrs.next(card, Rating.AGAIN, card.dueAt) }
        assertTrue(card.difficulty in 1.0..10.0)
        repeat(20) { card = fsrs.next(card, Rating.EASY, card.dueAt) }
        assertTrue(card.difficulty in 1.0..10.0)
    }
}
