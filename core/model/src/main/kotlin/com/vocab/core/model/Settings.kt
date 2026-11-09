package com.vocab.core.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class Accent(val localeTag: String) { US("en-US"), UK("en-GB") }

data class UserSettings(
    val dailyGoal: Int = DEFAULT_DAILY_GOAL,
    val reviewCap: Int = DEFAULT_REVIEW_CAP,
    val reminderEnabled: Boolean = true,
    /** Minutes after midnight, local time. */
    val reminderMinuteOfDay: Int = 20 * 60,
    val streakAlertEnabled: Boolean = true,
    val accent: Accent = Accent.US,
    val speechRate: Float = 1.0f,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastBackupAt: Long? = null,
    val onboardingDone: Boolean = false,
) {
    companion object {
        const val DEFAULT_DAILY_GOAL = 10
        const val DEFAULT_REVIEW_CAP = 100
        val DAILY_GOAL_OPTIONS = listOf(5, 10, 15, 20)
    }
}
