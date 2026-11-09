package com.vocab.feature.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vocab.core.designsystem.component.Art
import com.vocab.core.designsystem.component.ClayBox
import com.vocab.core.designsystem.component.ClayButton
import com.vocab.core.designsystem.component.ClayCard
import com.vocab.core.designsystem.component.ClayIconButton
import com.vocab.core.designsystem.component.ClayPill
import com.vocab.core.designsystem.component.EnText
import com.vocab.core.designsystem.component.SectionHeader
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.domain.progress.ProgressRules
import com.vocab.core.domain.usecase.StepState
import com.vocab.core.domain.usecase.UnitDetail
import com.vocab.core.model.LessonStep

@Composable
internal fun UnitDetailScreen(
    onStartStep: (String, LessonStep) -> Unit,
    onBack: () -> Unit,
    viewModel: UnitDetailViewModel = hiltViewModel(),
) {
    val detail by viewModel.state.collectAsStateWithLifecycle()
    val current = detail
    if (current == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val colors = VocabTheme.colors
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = Dimens.space4)) {
            item { Hero(current, onBack) }
            item { Intro(current, Modifier.padding(horizontal = Dimens.space4).offset(y = (-32).dp)) }
            item { SectionHeader(stringResource(R.string.course_steps), Modifier.padding(horizontal = Dimens.space4)) }
            items(current.steps, key = { it.step.name }) { step ->
                StepRow(step, Modifier.padding(horizontal = Dimens.space4, vertical = 4.dp)) { onStartStep(current.unit.id, step.step) }
            }
        }
        val next = current.nextStep
        val retake = (current.progress.testScore ?: 0) >= ProgressRules.PASS_SCORE
        ClayButton(
            text = stringResource(if (retake) R.string.course_retake else R.string.course_continue),
            onClick = { onStartStep(current.unit.id, next) },
            modifier = Modifier.fillMaxWidth().padding(Dimens.space4),
            trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
        )
    }
}

@Composable
private fun Hero(detail: UnitDetail, onBack: () -> Unit) {
    val colors = VocabTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .then(Modifier),
    ) {
        ClayBox(Modifier.fillMaxSize(), color = colors.topic(detail.unit.summary.color), radius = 0.dp, shadow = VocabTheme.shadows.sunk) {
            Art(detail.unit.summary.illustration, 150.dp)
        }
        ClayIconButton(
            Icons.AutoMirrored.Rounded.ArrowBack,
            stringResource(R.string.course_back),
            onBack,
            Modifier.align(Alignment.TopStart).padding(Dimens.space4),
        )
    }
}

@Composable
private fun Intro(detail: UnitDetail, modifier: Modifier) {
    val colors = VocabTheme.colors
    val unit = detail.unit.summary
    ClayCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.course_unit_number, unit.order), style = VocabTheme.type.captionFa, color = colors.correct)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space2)) {
                    Text(unit.titleFa, style = VocabTheme.type.titleFa, color = colors.ink)
                    EnText(unit.title, style = VocabTheme.type.wordMd.copy(fontSize = VocabTheme.type.titleFa.fontSize), color = colors.inkMuted)
                }
            }
            if (detail.progress.stars > 0) com.vocab.core.designsystem.component.StarsRow(detail.progress.stars)
        }
        if (detail.unit.descriptionFa.isNotBlank()) {
            Text(detail.unit.descriptionFa, style = VocabTheme.type.labelFa.copy(fontWeight = VocabTheme.type.bodyFa.fontWeight), color = colors.inkMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.space2)) {
            ClayPill(stringResource(R.string.course_words, unit.wordCount), colors.lilacSoft, colors.lilacInk)
            ClayPill("${(detail.progress.mastery * 100).toInt()}%", colors.mintSoft, colors.correct)
        }
    }
}

@Composable
private fun StepRow(state: StepState, modifier: Modifier, onClick: () -> Unit) {
    val colors = VocabTheme.colors
    val passed = (state.score ?: 0) >= ProgressRules.PASS_SCORE
    val current = state.unlocked && !passed
    ClayCard(
        modifier = modifier,
        color = when {
            !state.unlocked -> colors.surfaceSunk
            current -> colors.primarySoft
            else -> colors.surface
        },
        radius = Dimens.radiusMd,
        padding = Dimens.space3,
        shadow = if (state.unlocked) VocabTheme.shadows.raised else VocabTheme.shadows.sunk,
        onClick = if (state.unlocked) onClick else null,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            ClayBox(
                Modifier.size(32.dp),
                color = when {
                    current -> colors.primaryStrong
                    state.unlocked -> colors.mint
                    else -> colors.surface
                },
                radius = 16.dp,
            ) {
                if (state.step == LessonStep.UNIT_TEST) {
                    Art(com.vocab.core.model.Illustration.TROPHY, 20.dp, greyed = !state.unlocked)
                } else {
                    EnText("${state.step.ordinal + 1}", style = VocabTheme.type.labelFa.copy(fontFamily = com.vocab.core.designsystem.theme.Nunito), color = if (current) colors.surface else colors.chipInk)
                }
            }
            Text(stringResource(state.step.label()), style = VocabTheme.type.labelFa.copy(fontSize = VocabTheme.type.bodyFa.fontSize), color = if (state.unlocked) colors.ink else colors.inkMuted, modifier = Modifier.weight(1f))
            state.score?.let {
                EnText(if (state.step == LessonStep.LEARN) stringResource(R.string.course_step_done) else "$it%", style = VocabTheme.type.labelFa.copy(fontFamily = com.vocab.core.designsystem.theme.Nunito), color = if (passed) colors.correct else colors.wrong)
            }
            Icon(
                when {
                    !state.unlocked -> Icons.Rounded.Lock
                    passed -> Icons.Rounded.CheckCircle
                    else -> Icons.Rounded.PlayCircle
                },
                contentDescription = null,
                tint = when {
                    !state.unlocked -> colors.inkMuted
                    passed -> colors.correct
                    else -> colors.primaryText
                },
                modifier = Modifier.size(26.dp),
            )
        }
    }
}
