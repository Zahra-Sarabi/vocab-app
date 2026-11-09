package com.vocab.feature.course

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vocab.core.model.LessonStep
import kotlinx.serialization.Serializable

@Serializable
data object CourseRoute

@Serializable
data class UnitRoute(val unitId: String)

fun NavController.navigateToUnit(unitId: String) = navigate(UnitRoute(unitId))

fun NavGraphBuilder.courseScreens(
    onOpenUnit: (String) -> Unit,
    onStartStep: (unitId: String, step: LessonStep) -> Unit,
    onBack: () -> Unit,
) {
    composable<CourseRoute> { CourseScreen(onOpenUnit) }
    composable<UnitRoute> { UnitDetailScreen(onStartStep, onBack) }
}
