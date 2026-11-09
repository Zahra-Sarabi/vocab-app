package com.vocab.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.vocab.core.database.entity.DailyActivityEntity
import com.vocab.core.database.entity.ReviewLogEntity
import com.vocab.core.database.entity.StepResultEntity
import com.vocab.core.database.entity.WordCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCardDao {
    @Query("SELECT * FROM word_cards")
    fun observeAll(): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards")
    suspend fun all(): List<WordCardEntity>

    @Query("SELECT * FROM word_cards WHERE word_id IN (:wordIds)")
    suspend fun byIds(wordIds: Collection<String>): List<WordCardEntity>

    @Query("SELECT * FROM word_cards WHERE due_at <= :now ORDER BY due_at LIMIT :limit")
    suspend fun due(now: Long, limit: Int): List<WordCardEntity>

    @Query("SELECT COUNT(*) FROM word_cards WHERE due_at <= :now")
    suspend fun dueCount(now: Long): Int

    @Upsert
    suspend fun upsert(cards: List<WordCardEntity>)

    @Query("UPDATE word_cards SET starred = :starred WHERE word_id = :wordId")
    suspend fun setStarred(wordId: String, starred: Boolean)

    @Query("DELETE FROM word_cards")
    suspend fun clear()
}

@Dao
interface StepResultDao {
    @Query("SELECT * FROM step_results")
    fun observeAll(): Flow<List<StepResultEntity>>

    @Query("SELECT * FROM step_results")
    suspend fun all(): List<StepResultEntity>

    @Query("SELECT * FROM step_results WHERE unit_id = :unitId AND step = :step")
    suspend fun get(unitId: String, step: String): StepResultEntity?

    @Upsert
    suspend fun upsert(results: List<StepResultEntity>)

    /** Keeps the best score: a lower score never overwrites a higher one. */
    @Transaction
    suspend fun recordBest(result: StepResultEntity) {
        val current = get(result.unitId, result.step)
        if (current == null || result.bestScore > current.bestScore) upsert(listOf(result))
    }

    @Query("DELETE FROM step_results")
    suspend fun clear()
}

@Dao
interface ReviewLogDao {
    @Insert
    suspend fun insert(entries: List<ReviewLogEntity>)

    @Query("SELECT * FROM review_log ORDER BY reviewed_at")
    suspend fun all(): List<ReviewLogEntity>

    @Query("DELETE FROM review_log")
    suspend fun clear()
}

@Dao
interface DailyActivityDao {
    @Query("SELECT * FROM daily_activity ORDER BY epoch_day")
    fun observeAll(): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity ORDER BY epoch_day")
    suspend fun all(): List<DailyActivityEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: DailyActivityEntity)

    @Query(
        """UPDATE daily_activity SET new_words = new_words + :newWords, reviews = reviews + :reviews,
           answered = answered + :answered, correct = correct + :correct WHERE epoch_day = :epochDay""",
    )
    suspend fun increment(epochDay: Long, newWords: Int, reviews: Int, answered: Int, correct: Int)

    @Transaction
    suspend fun add(epochDay: Long, newWords: Int, reviews: Int, answered: Int, correct: Int) {
        insertIfMissing(DailyActivityEntity(epochDay, 0, 0, 0, 0))
        increment(epochDay, newWords, reviews, answered, correct)
    }

    @Upsert
    suspend fun upsert(days: List<DailyActivityEntity>)

    @Query("DELETE FROM daily_activity")
    suspend fun clear()
}
