package com.vocab.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.vocab.core.domain.repository.ReminderScheduler
import com.vocab.core.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Schedules the daily reminder and the evening streak alert as periodic WorkManager jobs. */
class WorkManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderScheduler {

    override fun apply(settings: UserSettings) {
        val work = WorkManager.getInstance(context)
        schedule(work, DAILY_WORK, settings.reminderEnabled, settings.reminderMinuteOfDay, ReminderWorker.KIND_DAILY)
        schedule(work, STREAK_WORK, settings.streakAlertEnabled, STREAK_ALERT_MINUTE, ReminderWorker.KIND_STREAK)
    }

    private fun schedule(work: WorkManager, name: String, enabled: Boolean, minuteOfDay: Int, kind: String) {
        if (!enabled) {
            work.cancelUniqueWork(name)
            return
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(minuteOfDay).toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_KIND to kind))
            .build()
        work.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun delayUntil(minuteOfDay: Int): Duration {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(LocalTime.of(minuteOfDay / 60, minuteOfDay % 60))
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next)
    }

    private companion object {
        const val DAILY_WORK = "daily-reminder"
        const val STREAK_WORK = "streak-alert"
        const val STREAK_ALERT_MINUTE = 22 * 60
    }
}
