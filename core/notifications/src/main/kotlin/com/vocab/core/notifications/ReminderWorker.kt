package com.vocab.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vocab.core.domain.usecase.ObserveTodayUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Shows the reminder only when it helps: nothing if the learner already practiced today,
 * and the streak alert only for streaks longer than three days.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val observeToday: ObserveTodayUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = observeToday().first()
        val practicedToday = today.newWordsToday > 0 || today.goalReached
        if (practicedToday) return Result.success()
        val kind = inputData.getString(KEY_KIND) ?: KIND_DAILY
        val (title, body) = when (kind) {
            KIND_STREAK -> {
                if (today.streak < MIN_STREAK_FOR_ALERT) return Result.success()
                applicationContext.getString(R.string.streak_title) to applicationContext.getString(R.string.streak_body, today.streak)
            }
            else -> applicationContext.getString(R.string.reminder_title) to
                if (today.dueCount > 0) applicationContext.getString(R.string.reminder_due, today.dueCount)
                else applicationContext.getString(R.string.reminder_no_due)
        }
        notify(title, body, if (kind == KIND_STREAK) STREAK_ID else DAILY_ID)
        return Result.success()
    }

    private fun notify(title: String, body: String, id: Int) {
        val context = applicationContext
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            android.os.Build.VERSION.SDK_INT >= 33
        ) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_DEFAULT),
        )
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = launch?.let {
            android.app.PendingIntent.getActivity(context, 0, it, android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    companion object {
        const val KEY_KIND = "kind"
        const val KIND_DAILY = "daily"
        const val KIND_STREAK = "streak"
        private const val CHANNEL_ID = "reminders"
        private const val DAILY_ID = 1
        private const val STREAK_ID = 2
        private const val MIN_STREAK_FOR_ALERT = 3
    }
}
