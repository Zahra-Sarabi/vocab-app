package com.vocab.core.data.di

import com.vocab.core.data.backup.JsonBackupRepository
import com.vocab.core.data.content.AssetContentRepository
import com.vocab.core.data.device.SafDocumentStore
import com.vocab.core.data.device.TtsPronouncer
import com.vocab.core.data.repository.DataStoreSettingsRepository
import com.vocab.core.data.repository.RoomActivityRepository
import com.vocab.core.data.repository.RoomProgressRepository
import com.vocab.core.domain.SystemTimeSource
import com.vocab.core.domain.TimeSource
import com.vocab.core.domain.repository.ActivityRepository
import com.vocab.core.domain.repository.BackupRepository
import com.vocab.core.domain.repository.ContentRepository
import com.vocab.core.domain.repository.DocumentStore
import com.vocab.core.domain.repository.ProgressRepository
import com.vocab.core.domain.repository.Pronouncer
import com.vocab.core.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @Singleton abstract fun content(impl: AssetContentRepository): ContentRepository
    @Binds @Singleton abstract fun progress(impl: RoomProgressRepository): ProgressRepository
    @Binds @Singleton abstract fun activity(impl: RoomActivityRepository): ActivityRepository
    @Binds @Singleton abstract fun settings(impl: DataStoreSettingsRepository): SettingsRepository
    @Binds @Singleton abstract fun backup(impl: JsonBackupRepository): BackupRepository
    @Binds abstract fun documents(impl: SafDocumentStore): DocumentStore
    @Binds @Singleton abstract fun pronouncer(impl: TtsPronouncer): Pronouncer
    @Binds @Singleton abstract fun time(impl: SystemTimeSource): TimeSource
}
