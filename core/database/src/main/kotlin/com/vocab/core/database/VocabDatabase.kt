package com.vocab.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vocab.core.database.dao.DailyActivityDao
import com.vocab.core.database.dao.ReviewLogDao
import com.vocab.core.database.dao.StepResultDao
import com.vocab.core.database.dao.WordCardDao
import com.vocab.core.database.entity.DailyActivityEntity
import com.vocab.core.database.entity.ReviewLogEntity
import com.vocab.core.database.entity.StepResultEntity
import com.vocab.core.database.entity.WordCardEntity

/**
 * Learner progress. Course content is not stored here; it ships as JSON in the app.
 * Bump [version] and add a migration (or an AutoMigration) for every schema change;
 * exported schemas live in `core/database/schemas`.
 */
@Database(
    entities = [WordCardEntity::class, StepResultEntity::class, ReviewLogEntity::class, DailyActivityEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class VocabDatabase : RoomDatabase() {
    abstract fun wordCardDao(): WordCardDao
    abstract fun stepResultDao(): StepResultDao
    abstract fun reviewLogDao(): ReviewLogDao
    abstract fun dailyActivityDao(): DailyActivityDao

    companion object {
        const val NAME = "vocab.db"
    }
}
