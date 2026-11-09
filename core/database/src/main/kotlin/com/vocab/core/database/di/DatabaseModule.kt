package com.vocab.core.database.di

import android.content.Context
import androidx.room.Room
import com.vocab.core.database.VocabDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): VocabDatabase =
        Room.databaseBuilder(context, VocabDatabase::class.java, VocabDatabase.NAME).build()

    @Provides fun wordCards(db: VocabDatabase) = db.wordCardDao()
    @Provides fun stepResults(db: VocabDatabase) = db.stepResultDao()
    @Provides fun reviewLog(db: VocabDatabase) = db.reviewLogDao()
    @Provides fun dailyActivity(db: VocabDatabase) = db.dailyActivityDao()
}
