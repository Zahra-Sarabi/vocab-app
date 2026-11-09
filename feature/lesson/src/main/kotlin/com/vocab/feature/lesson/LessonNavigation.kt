package com.vocab.feature.lesson

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.vocab.core.model.LessonStep
import kotlinx.serialization.Serializable

@Serializable
data class LessonRoute(val unitId: String, val step: String)

fun NavController.navigateToLesson(unitId: String, step: LessonStep, options: NavOptions? = null) =
    navigate(LessonRoute(unitId, step.name), options)

fun NavGraphBuilder.lessonScreen(
    onClose: () -> Unit,
    onNextStep: (unitId: String, step: LessonStep) -> Unit,
) {
    composable<LessonRoute> { LessonScreen(onClose, onNextStep) }
}
