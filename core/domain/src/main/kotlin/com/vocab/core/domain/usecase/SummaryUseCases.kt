package com.vocab.core.domain.usecase

import com.vocab.core.domain.TimeSource
import com.vocab.core.domain.progress.ProgressRules
import com.vocab.core.domain.repository.ActivityRepository
import com.vocab.core.domain.repository.ProgressRepository
import com.vocab.core.domain.repository.SettingsRepository
import com.vocab.core.model.DailyActivity
import com.vocab.core.model.LevelProgress
import com.vocab.core.model.UnitProgress
import com.vocab.core.model.UnitStatus
import com.vocab.core.model.WordState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class TodaySummary(
    val dueCount: Int,
    val newWordsToday: Int,
    val dailyGoal: Int,
    val streak: Int,
    val continueUnit: UnitProgress?,
    val topics: List<UnitProgress>,
    val backupOverdue: Boolean,
    /** False until the first backup, so the reminder can say so. */
    val hasBackup: Boolean,
) {
    val goalReached: Boolean get() = newWordsToday >= dailyGoal
}

class ObserveTodayUseCase @Inject constructor(
    private val observePath: ObservePathUseCase,
    private val progress: ProgressRepository,
    private val activity: ActivityRepository,
    private val settings: SettingsRepository,
    private val time: TimeSource,
) {
    operator fun invoke(): Flow<TodaySummary> = combine(
        observePath(), progress.observeCards(), activity.observeActivity(), settings.settings,
    ) { levels, cards, days, prefs ->
        val now = time.now()
        val today = time.epochDay(now)
        val todayActivity = days.firstOrNull { it.epochDay == today }
        val units = levels.flatMap { it.units }
        val reviewsLeft = (prefs.reviewCap - (todayActivity?.reviews ?: 0)).coerceAtLeast(0)
        val lastBackup = prefs.lastBackupAt
        TodaySummary(
            dueCount = cards.count { it.dueAt <= now }.coerceAtMost(reviewsLeft),
            newWordsToday = todayActivity?.newWords ?: 0,
            dailyGoal = prefs.dailyGoal,
            streak = ProgressRules.streak(days.activeDays(), today),
            continueUnit = units.firstOrNull { it.status == UnitStatus.IN_PROGRESS }
                ?: units.firstOrNull { it.status == UnitStatus.OPEN },
            topics = units.filter { it.status != UnitStatus.LOCKED }.take(TOPIC_COUNT),
            backupOverdue = cards.isNotEmpty() && if (lastBackup == null) {
                // Never backed up: wait a few days of use instead of nagging on day one.
                val firstDay = days.minOfOrNull { it.epochDay } ?: today
                today - firstDay >= FIRST_BACKUP_REMINDER_DAYS
            } else {
                now - lastBackup > BACKUP_REMINDER_DAYS * MILLIS_IN_DAY
            },
            hasBackup = lastBackup != null,
        )
    }

    private companion object {
        const val TOPIC_COUNT = 6
        const val BACKUP_REMINDER_DAYS = 14L
        const val FIRST_BACKUP_REMINDER_DAYS = 3L
        const val MILLIS_IN_DAY = 86_400_000L
    }
}

data class ProgressSummary(
    val learnedWords: Int,
    val masteredWords: Int,
    val streak: Int,
    val longestStreak: Int,
    /** Last [ObserveProgressUseCase.CALENDAR_DAYS] days, oldest first; 0 = none, 1 = some, 2 = goal met. */
    val calendar: List<Int>,
    val levels: List<LevelProgress>,
    val reviewAccuracy: Double?,
    val passedUnits: Int,
)

class ObserveProgressUseCase @Inject constructor(
    private val observePath: ObservePathUseCase,
    private val progress: ProgressRepository,
    private val activity: ActivityRepository,
    private val settings: SettingsRepository,
    private val time: TimeSource,
) {
    operator fun invoke(): Flow<ProgressSummary> = combine(
        observePath(), progress.observeCards(), activity.observeActivity(), settings.settings,
    ) { levels, cards, days, prefs ->
        val today = time.epochDay()
        val byDay = days.associateBy { it.epochDay }
        val active = days.activeDays()
        val recent = days.filter { it.epochDay > today - ACCURACY_DAYS }
        val answered = recent.sumOf { it.answered }
        ProgressSummary(
            learnedWords = cards.size,
            masteredWords = cards.count { ProgressRules.wordState(it) == WordState.MASTERED },
            streak = ProgressRules.streak(active, today),
            longestStreak = longestRun(active),
            calendar = (CALENDAR_DAYS - 1 downTo 0).map { offset ->
                val day = byDay[today - offset]
                when {
                    day == null || !day.isActive() -> 0
                    day.newWords + day.reviews >= prefs.dailyGoal -> 2
                    else -> 1
                }
            },
            levels = levels,
            reviewAccuracy = if (answered == 0) null else recent.sumOf { it.correct }.toDouble() / answered,
            passedUnits = levels.sumOf { it.passedCount },
        )
    }

    private fun longestRun(days: Set<Long>): Int {
        var best = 0
        days.forEach { day ->
            if (day - 1 !in days) {
                var length = 0
                while (day + length in days) length++
                best = maxOf(best, length)
            }
        }
        return best
    }

    companion object {
        const val CALENDAR_DAYS = 35
        private const val ACCURACY_DAYS = 30
    }
}

internal fun DailyActivity.isActive() = newWords + reviews + answered > 0

internal fun List<DailyActivity>.activeDays(): Set<Long> = filter { it.isActive() }.map { it.epochDay }.toSet()
