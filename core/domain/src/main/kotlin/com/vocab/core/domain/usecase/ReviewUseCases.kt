package com.vocab.core.domain.usecase

import com.vocab.core.domain.TimeSource
import com.vocab.core.domain.repository.ActivityRepository
import com.vocab.core.domain.repository.ContentRepository
import com.vocab.core.domain.repository.ProgressRepository
import com.vocab.core.domain.repository.SettingsRepository
import com.vocab.core.domain.srs.Fsrs
import com.vocab.core.model.Rating
import com.vocab.core.model.Word
import com.vocab.core.model.WordCard
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class ReviewItem(val card: WordCard, val word: Word, val nextIntervals: Map<Rating, Long>)

/** Today's due words, mixed across units, within the daily review cap. */
class StartReviewUseCase @Inject constructor(
    private val progress: ProgressRepository,
    private val content: ContentRepository,
    private val settings: SettingsRepository,
    private val activity: ActivityRepository,
    private val fsrs: Fsrs,
    private val time: TimeSource,
) {
    suspend operator fun invoke(): List<ReviewItem> {
        val now = time.now()
        val today = time.epochDay(now)
        val doneToday = activity.observeActivity().first().firstOrNull { it.epochDay == today }?.reviews ?: 0
        val limit = (settings.settings.first().reviewCap - doneToday).coerceAtLeast(0)
        val cards = progress.dueCards(now, limit)
        val words = content.words(cards.map { it.wordId })
        return cards.mapNotNull { card ->
            val word = words[card.wordId] ?: return@mapNotNull null
            ReviewItem(card, word, Rating.entries.associateWith { previewDueMillis(card, it, now) })
        }.shuffled()
    }

    private fun previewDueMillis(card: WordCard, rating: Rating, now: Long) = fsrs.next(card, rating, now).dueAt - now
}

class SubmitReviewUseCase @Inject constructor(
    private val progress: ProgressRepository,
    private val activity: ActivityRepository,
    private val fsrs: Fsrs,
    private val time: TimeSource,
) {
    /** Returns the updated card; an AGAIN card comes back in the same session. */
    suspend operator fun invoke(card: WordCard, rating: Rating): WordCard {
        val now = time.now()
        val next = fsrs.next(card, rating, now)
        progress.upsertCards(listOf(next))
        progress.logReview(card.wordId, rating, now)
        activity.add(time.epochDay(now), reviews = 1, answered = 1, correct = if (rating == Rating.AGAIN) 0 else 1)
        return next
    }
}

class ToggleStarUseCase @Inject constructor(private val progress: ProgressRepository) {
    suspend operator fun invoke(wordId: String, starred: Boolean) = progress.setStarred(wordId, starred)
}
