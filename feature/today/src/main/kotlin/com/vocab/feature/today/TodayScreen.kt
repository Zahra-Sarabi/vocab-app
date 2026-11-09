package com.vocab.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vocab.core.designsystem.component.Art
import com.vocab.core.designsystem.component.ClayButton
import com.vocab.core.designsystem.component.ClayCard
import com.vocab.core.designsystem.component.ClayIconButton
import com.vocab.core.designsystem.component.ClayPill
import com.vocab.core.designsystem.component.ClayProgressBar
import com.vocab.core.designsystem.component.ClayThumb
import com.vocab.core.designsystem.component.SectionHeader
import com.vocab.core.designsystem.component.TopicTile
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.domain.usecase.TodaySummary
import com.vocab.core.model.Illustration
import com.vocab.core.model.UnitProgress
import kotlin.math.ceil

@Composable
internal fun TodayScreen(
    onStartReview: () -> Unit,
    onOpenUnit: (String) -> Unit,
    onOpenCourse: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        TodayUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is TodayUiState.Ready -> TodayContent(s.summary, onStartReview, onOpenUnit, onOpenCourse, onOpenSettings)
    }
}

@Composable
private fun TodayContent(
    summary: TodaySummary,
    onStartReview: () -> Unit,
    onOpenUnit: (String) -> Unit,
    onOpenCourse: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = VocabTheme.colors
    val type = VocabTheme.type
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = Dimens.space4, end = Dimens.space4, top = Dimens.space5, bottom = Dimens.space6)),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.today_greeting), style = type.displayFa, color = colors.ink)
                Text(stringResource(R.string.today_subtitle), style = type.labelFa.copy(fontWeight = type.bodyFa.fontWeight), color = colors.inkMuted)
            }
            ClayPill(
                text = stringResource(R.string.today_streak, summary.streak),
                background = colors.streakSoft,
                content = colors.streakInk,
                leading = { Art(Illustration.FIRE, 22.dp) },
            )
            ClayIconButton(Icons.Rounded.Settings, stringResource(R.string.today_settings), onOpenSettings)
        }

        GoalHero(summary)

        if (summary.backupOverdue) {
            ClayCard(color = colors.butterSoft, padding = Dimens.space4) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
                    Art(Illustration.BACKUP, 36.dp)
                    Text(stringResource(if (summary.hasBackup) R.string.today_backup_banner else R.string.today_backup_first), style = type.captionFa, color = colors.butterInk, modifier = Modifier.weight(1f))
                }
                ClayButton(stringResource(R.string.today_backup_action), onOpenSettings, style = com.vocab.core.designsystem.component.ClayButtonStyle.SECONDARY)
            }
        }

        ReviewCard(summary, onStartReview)

        summary.continueUnit?.let { unit ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(stringResource(R.string.today_continue), action = stringResource(R.string.today_all_units), onAction = onOpenCourse)
                ContinueRow(unit) { onOpenUnit(unit.unit.id) }
            }
        }

        if (summary.topics.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(stringResource(R.string.today_topics))
                summary.topics.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
                        row.forEach { unit ->
                            TopicTile(unit.unit.titleFa, unit.unit.illustration, unit.unit.color, { onOpenUnit(unit.unit.id) }, Modifier.weight(1f))
                        }
                        repeat(3 - row.size) { Box(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalHero(summary: TodaySummary) {
    val colors = VocabTheme.colors
    ClayCard(color = colors.primaryStrong, padding = Dimens.space5) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.today_goal), style = VocabTheme.type.titleFa, color = colors.onPrimary)
                Text(
                    if (summary.goalReached) stringResource(R.string.today_goal_done)
                    else stringResource(R.string.today_goal_progress, summary.newWordsToday, summary.dailyGoal),
                    style = VocabTheme.type.labelFa.copy(fontWeight = VocabTheme.type.bodyFa.fontWeight),
                    color = colors.onPrimary,
                )
                ClayProgressBar(
                    progress = summary.newWordsToday.toFloat() / summary.dailyGoal.coerceAtLeast(1),
                    fill = colors.butter,
                    track = colors.heroTrack,
                    sunk = false,
                )
            }
            Art(Illustration.STUDENT, 88.dp)
        }
    }
}

@Composable
private fun ReviewCard(summary: TodaySummary, onStartReview: () -> Unit) {
    val colors = VocabTheme.colors
    ClayCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            Art(Illustration.CALENDAR, 52.dp)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.today_review), style = VocabTheme.type.titleFa, color = colors.ink)
                Text(
                    if (summary.dueCount == 0) stringResource(R.string.today_review_done)
                    else stringResource(R.string.today_review_due, summary.dueCount, ceil(summary.dueCount * SECONDS_PER_REVIEW / 60.0).toInt().coerceAtLeast(1)),
                    style = VocabTheme.type.captionFa,
                    color = colors.inkMuted,
                )
            }
        }
        if (summary.dueCount > 0) {
            ClayButton(stringResource(R.string.today_review_start), onStartReview, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ContinueRow(unit: UnitProgress, onClick: () -> Unit) {
    val colors = VocabTheme.colors
    ClayCard(color = colors.primarySoft, radius = Dimens.radiusMd, padding = Dimens.space3, shadow = VocabTheme.shadows.raised, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            ClayThumb(unit.unit.illustration, colors.topic(unit.unit.color))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.today_unit_title, unit.unit.order, unit.unit.title), style = VocabTheme.type.labelFa.copy(fontSize = VocabTheme.type.bodyFa.fontSize), color = colors.ink)
                ClayProgressBar(unit.stepScores.size.toFloat() / com.vocab.core.model.LessonStep.entries.size, height = 10.dp)
                Text(stringResource(R.string.today_unit_words, unit.unit.wordCount), style = VocabTheme.type.captionFa, color = colors.inkMuted)
            }
            Icon(Icons.Rounded.PlayCircle, null, tint = colors.primaryText, modifier = Modifier.size(32.dp))
        }
    }
}

private const val SECONDS_PER_REVIEW = 12
