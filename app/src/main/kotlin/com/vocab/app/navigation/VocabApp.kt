package com.vocab.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.vocab.app.R
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.designsystem.theme.clay
import com.vocab.feature.course.CourseRoute
import com.vocab.feature.course.UnitRoute
import com.vocab.feature.course.courseScreens
import com.vocab.feature.course.navigateToUnit
import com.vocab.feature.lesson.LessonRoute
import com.vocab.feature.lesson.lessonScreen
import com.vocab.feature.lesson.navigateToLesson
import com.vocab.feature.progress.ProgressRoute
import com.vocab.feature.progress.progressScreen
import com.vocab.feature.review.ReviewRoute
import com.vocab.feature.review.reviewScreen
import com.vocab.feature.settings.SettingsRoute
import com.vocab.feature.settings.settingsScreen
import com.vocab.feature.today.TodayRoute
import com.vocab.feature.today.todayScreen
import kotlin.reflect.KClass

private enum class TopLevel(val route: Any, val routeClass: KClass<*>, val icon: ImageVector, val label: Int) {
    TODAY(TodayRoute, TodayRoute::class, Icons.Rounded.Home, R.string.nav_today),
    COURSE(CourseRoute, CourseRoute::class, Icons.Rounded.Map, R.string.nav_course),
    REVIEW(ReviewRoute, ReviewRoute::class, Icons.Rounded.Style, R.string.nav_review),
    PROGRESS(ProgressRoute, ProgressRoute::class, Icons.Rounded.BarChart, R.string.nav_progress),
}

/** The app shell: one navigation graph and the bottom dock on top-level screens. */
@Composable
fun VocabApp() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination
    val current = TopLevel.entries.firstOrNull { tab -> destination?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true }
    var reviewSessionActive by remember { mutableStateOf(false) }
    val showDock = current != null && !(current == TopLevel.REVIEW && reviewSessionActive)

    Column(Modifier.fillMaxSize().background(VocabTheme.colors.surfaceAlt).statusBarsPadding()) {
        // Without the dock, screens keep clear of the system navigation bar themselves.
        Box(Modifier.weight(1f).then(if (showDock) Modifier else Modifier.navigationBarsPadding())) {
            NavHost(nav, startDestination = TodayRoute) {
                todayScreen(
                    onStartReview = { nav.navigate(ReviewRoute) },
                    onOpenUnit = nav::navigateToUnit,
                    onOpenCourse = { nav.navigateTopLevel(TopLevel.COURSE) },
                    onOpenSettings = { nav.navigate(SettingsRoute) },
                )
                courseScreens(
                    onOpenUnit = nav::navigateToUnit,
                    onStartStep = { unitId, step -> nav.navigateToLesson(unitId, step) },
                    onBack = nav::popBackStack,
                )
                lessonScreen(
                    onClose = nav::popBackStack,
                    onNextStep = { unitId, step ->
                        nav.navigateToLesson(unitId, step, navOptions { popUpTo<LessonRoute> { inclusive = true } })
                    },
                )
                reviewScreen(onClose = nav::popBackStack, onSessionActive = { reviewSessionActive = it })
                progressScreen()
                settingsScreen(onBack = nav::popBackStack)
            }
        }
        if (showDock) Dock(current) { nav.navigateTopLevel(it) }
    }
}

private fun NavHostController.navigateTopLevel(tab: TopLevel) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun Dock(current: TopLevel?, onSelect: (TopLevel) -> Unit) {
    val colors = VocabTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = Dimens.space3, end = Dimens.space3, bottom = Dimens.space3)
            .clay(colors.surface, Dimens.radiusLg, VocabTheme.shadows.card)
            .padding(Dimens.space2),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        TopLevel.entries.forEach { tab ->
            val selected = tab == current
            Column(
                Modifier
                    .widthIn(min = 68.dp)
                    .heightIn(min = Dimens.tapMin)
                    .then(if (selected) Modifier.clay(colors.primarySoft, Dimens.radiusMd, VocabTheme.shadows.raised) else Modifier)
                    .clickable(role = Role.Tab) { onSelect(tab) }
                    .padding(horizontal = Dimens.space2, vertical = Dimens.space1),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(tab.icon, null, tint = if (selected) colors.primaryText else colors.inkMuted, modifier = Modifier.size(24.dp))
                Text(
                    stringResource(tab.label),
                    style = if (selected) VocabTheme.type.captionFa.copy(fontWeight = VocabTheme.type.labelFa.fontWeight) else VocabTheme.type.captionFa,
                    color = if (selected) colors.primaryText else colors.inkMuted,
                )
            }
        }
    }
}
