package com.vocab.core.notifications

import com.vocab.core.domain.repository.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {
    @Binds abstract fun reminders(impl: WorkManagerReminderScheduler): ReminderScheduler
}
