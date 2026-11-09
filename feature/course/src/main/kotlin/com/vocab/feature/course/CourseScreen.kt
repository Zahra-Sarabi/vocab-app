package com.vocab.feature.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
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
import com.vocab.core.designsystem.component.ClayCard
import com.vocab.core.designsystem.component.ClayPill
import com.vocab.core.designsystem.component.ClayProgressBar
import com.vocab.core.designsystem.component.ClayThumb
import com.vocab.core.designsystem.component.StarsRow
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.model.Illustration
import com.vocab.core.model.LevelProgress
import com.vocab.core.model.UnitProgress
import com.vocab.core.model.UnitStatus
import kotlin.math.roundToInt

@Composable
internal fun CourseScreen(onOpenUnit: (String) -> Unit, viewModel: CourseViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selected = state.selected
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.space4),
        verticalArrangement = Arrangement.spacedBy(Dimens.space3),
    ) {
        item { Text(stringResource(R.string.course_title), style = VocabTheme.type.displayFa, color = VocabTheme.colors.ink) }
        item { LevelSelector(state.levels, selected?.level?.id, viewModel::selectLevel) }
        if (selected != null) {
            item { LevelCard(selected) }
            items(selected.units, key = { it.unit.id }) { unit -> UnitRow(unit) { onOpenUnit(unit.unit.id) } }
        }
    }
}

@Composable
private fun LevelSelector(levels: List<LevelProgress>, selectedId: String?, onSelect: (String) -> Unit) {
    val colors = VocabTheme.colors
    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(vertical = Dimens.space1),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space2),
    ) {
        levels.forEach { level ->
            val passedAll = level.units.isNotEmpty() && level.passedCount == level.units.size
            val (bg, fg) = when {
                level.level.id == selectedId -> colors.primaryStrong to colors.onPrimary
                passedAll -> colors.correctSoft to colors.correct
                !level.unlocked -> colors.surfaceSunk to colors.inkMuted
                else -> colors.surface to colors.ink
            }
            ClayPill(
                text = level.level.title,
                background = bg,
                content = fg,
                modifier = Modifier.clickable { onSelect(level.level.id) },
                leading = when {
                    passedAll && level.level.id != selectedId -> { { Icon(Icons.Rounded.CheckCircle, null, tint = fg, modifier = Modifier.size(16.dp)) } }
                    !level.unlocked -> { { Icon(Icons.Rounded.Lock, null, tint = fg, modifier = Modifier.size(16.dp)) } }
                    else -> null
                },
            )
        }
    }
}

@Composable
private fun LevelCard(level: LevelProgress) {
    val colors = VocabTheme.colors
    ClayCard(color = colors.lilacSoft, padding = Dimens.space4) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Art(Illustration.GRADUATION_CAP, 56.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text(stringResource(R.string.course_level_mastery), style = VocabTheme.type.labelFa, color = colors.ink, modifier = Modifier.weight(1f))
                    Text("${(level.mastery * 100).roundToInt()}%", style = VocabTheme.type.wordMd, color = colors.primaryText)
                }
                ClayProgressBar(level.mastery.toFloat())
                Text(
                    when {
                        level.units.isEmpty() -> stringResource(R.string.course_level_empty)
                        !level.unlocked -> stringResource(R.string.course_level_locked)
                        else -> stringResource(R.string.course_level_passed, level.passedCount, level.units.size)
                    },
                    style = VocabTheme.type.captionFa,
                    color = colors.inkMuted,
                )
            }
        }
    }
}

@Composable
private fun UnitRow(unit: UnitProgress, onClick: () -> Unit) {
    val colors = VocabTheme.colors
    val locked = unit.status == UnitStatus.LOCKED
    val current = unit.status == UnitStatus.IN_PROGRESS
    val mastery = (unit.mastery * 100).roundToInt()
    ClayCard(
        color = when {
            locked -> colors.surfaceSunk
            current -> colors.primarySoft
            else -> colors.surface
        },
        radius = Dimens.radiusMd,
        padding = Dimens.space3,
        shadow = if (locked) VocabTheme.shadows.sunk else VocabTheme.shadows.raised,
        onClick = if (locked) null else onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            ClayThumb(unit.unit.illustration, colors.topic(unit.unit.color), greyed = locked)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.course_unit_title, unit.unit.order, unit.unit.title),
                    style = VocabTheme.type.labelFa.copy(fontSize = VocabTheme.type.bodyFa.fontSize),
                    color = if (locked) colors.inkMuted else colors.ink,
                )
                when (unit.status) {
                    UnitStatus.LOCKED -> Unit // The lock icon says it; its description keeps the reason for screen readers.
                    UnitStatus.IN_PROGRESS -> ClayProgressBar(unit.stepScores.size.toFloat() / com.vocab.core.model.LessonStep.entries.size, height = 10.dp)
                    UnitStatus.NEEDS_REVIEW -> Text(stringResource(R.string.course_unit_needs_review, mastery), style = VocabTheme.type.captionFa, color = colors.butterInk)
                    else -> Text(stringResource(R.string.course_unit_meta, unit.unit.wordCount, mastery), style = VocabTheme.type.captionFa, color = colors.inkMuted)
                }
            }
            if (unit.status == UnitStatus.PASSED || unit.status == UnitStatus.NEEDS_REVIEW) StarsRow(unit.stars)
            Icon(
                when (unit.status) {
                    UnitStatus.LOCKED -> Icons.Rounded.Lock
                    UnitStatus.PASSED -> Icons.Rounded.CheckCircle
                    UnitStatus.NEEDS_REVIEW -> Icons.Rounded.History
                    else -> Icons.Rounded.PlayCircle
                },
                contentDescription = if (unit.status == UnitStatus.LOCKED) stringResource(R.string.course_unit_locked) else null,
                tint = when (unit.status) {
                    UnitStatus.LOCKED -> colors.inkMuted
                    UnitStatus.PASSED -> colors.correct
                    UnitStatus.NEEDS_REVIEW -> colors.butterInk
                    else -> colors.primaryText
                },
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
