package com.vocab.feature.today

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object TodayRoute

fun NavGraphBuilder.todayScreen(
    onStartReview: () -> Unit,
    onOpenUnit: (String) -> Unit,
    onOpenCourse: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    composable<TodayRoute> {
        TodayScreen(onStartReview, onOpenUnit, onOpenCourse, onOpenSettings)
    }
}
