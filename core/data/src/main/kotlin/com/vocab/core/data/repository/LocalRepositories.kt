package com.vocab.core.data.repository

import com.vocab.core.database.dao.DailyActivityDao
import com.vocab.core.database.dao.ReviewLogDao
import com.vocab.core.database.dao.StepResultDao
import com.vocab.core.database.dao.WordCardDao
import com.vocab.core.database.entity.ReviewLogEntity
import com.vocab.core.database.entity.StepResultEntity
import com.vocab.core.database.toEntity
import com.vocab.core.database.toModel
import com.vocab.core.database.toModelOrNull
import com.vocab.core.datastore.SettingsDataSource
import com.vocab.core.domain.repository.ActivityRepository
import com.vocab.core.domain.repository.ProgressRepository
import com.vocab.core.domain.repository.SettingsRepository
import com.vocab.core.model.DailyActivity
import com.vocab.core.model.LessonStep
import com.vocab.core.model.Rating
import com.vocab.core.model.StepResult
import com.vocab.core.model.UserSettings
import com.vocab.core.model.WordCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomProgressRepository @Inject constructor(
    private val cards: WordCardDao,
    private val steps: StepResultDao,
    private val reviews: ReviewLogDao,
) : ProgressRepository {
    override fun observeCards(): Flow<List<WordCard>> = cards.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeStepResults(): Flow<List<StepResult>> =
        steps.observeAll().map { list -> list.mapNotNull { it.toModelOrNull() } }

    override suspend fun cards(wordIds: Collection<String>): List<WordCard> =
        if (wordIds.isEmpty()) emptyList() else cards.byIds(wordIds).map { it.toModel() }

    override suspend fun dueCards(now: Long, limit: Int): List<WordCard> =
        if (limit <= 0) emptyList() else cards.due(now, limit).map { it.toModel() }

    override suspend fun upsertCards(cards: List<WordCard>) {
        if (cards.isNotEmpty()) this.cards.upsert(cards.map { it.toEntity() })
    }

    override suspend fun recordStep(unitId: String, step: LessonStep, score: Int, at: Long) =
        steps.recordBest(StepResultEntity(unitId, step.name, score.coerceIn(0, 100), at))

    override suspend fun logReview(wordId: String, rating: Rating, at: Long) =
        reviews.insert(listOf(ReviewLogEntity(wordId = wordId, rating = rating.value, reviewedAt = at)))

    override suspend fun setStarred(wordId: String, starred: Boolean) = cards.setStarred(wordId, starred)
}

class RoomActivityRepository @Inject constructor(private val dao: DailyActivityDao) : ActivityRepository {
    override fun observeActivity(): Flow<List<DailyActivity>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun add(epochDay: Long, newWords: Int, reviews: Int, answered: Int, correct: Int) =
        dao.add(epochDay, newWords, reviews, answered, correct)
}

class DataStoreSettingsRepository @Inject constructor(private val source: SettingsDataSource) : SettingsRepository {
    override val settings: Flow<UserSettings> = source.settings
    override suspend fun update(transform: (UserSettings) -> UserSettings) = source.update(transform)
}
