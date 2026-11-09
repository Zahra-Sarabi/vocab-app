package com.vocab.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vocab.core.designsystem.component.Art
import com.vocab.core.designsystem.component.ClayButton
import com.vocab.core.designsystem.component.ClayButtonStyle
import com.vocab.core.designsystem.component.ClayCard
import com.vocab.core.designsystem.component.ClayThumb
import com.vocab.core.designsystem.component.EnText
import com.vocab.core.designsystem.component.StarsRow
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.model.Illustration
import com.vocab.core.model.LessonStep

@Composable
internal fun ResultContent(
    s: LessonUiState.Finished,
    onClose: () -> Unit,
    onNextStep: (String, LessonStep) -> Unit,
) {
    val colors = VocabTheme.colors
    val outcome = s.outcome
    val isLearn = outcome.step == LessonStep.LEARN
    val isTest = outcome.step == LessonStep.UNIT_TEST
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.space4),
        verticalArrangement = Arrangement.spacedBy(Dimens.space4),
    ) {
        ClayCard(Modifier.fillMaxWidth(), color = if (outcome.passed) colors.primaryStrong else colors.surface) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Art(
                    when {
                        isLearn -> Illustration.OPEN_BOOK
                        outcome.passed && isTest -> Illustration.TROPHY
                        outcome.passed -> Illustration.PARTY
                        else -> Illustration.BRAIN
                    },
                    110.dp,
                )
                val onCard = if (outcome.passed) colors.onPrimary else colors.ink
                if (isTest && outcome.passed) StarsRow(outcome.stars, size = 36.dp)
                if (isLearn) {
                    Text(stringResource(R.string.result_learned, outcome.total), style = VocabTheme.type.titleFa, color = onCard, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.result_learned_hint), style = VocabTheme.type.labelFa, color = onCard, textAlign = TextAlign.Center)
                } else {
                    EnText("${outcome.score}%", style = VocabTheme.type.number.copy(fontSize = VocabTheme.type.wordXl.fontSize), color = onCard)
                    Text(
                        stringResource(if (outcome.passed) R.string.result_passed else R.string.result_failed, outcome.correctCount, outcome.total),
                        style = VocabTheme.type.labelFa,
                        color = onCard,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        outcome.unlockedUnit?.let { unit ->
            ClayCard(color = colors.correctSoft, radius = Dimens.radiusMd, padding = Dimens.space4, shadow = VocabTheme.shadows.raised) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
                    ClayThumb(unit.illustration, colors.topic(unit.color), size = 48.dp)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.result_unlocked, unit.titleFa), style = VocabTheme.type.labelFa, color = colors.correct)
                        Text(stringResource(R.string.result_unlocked_hint), style = VocabTheme.type.captionFa, color = colors.correct)
                    }
                }
            }
        }

        if (outcome.missedWords.isNotEmpty()) {
            ClayCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space2)) {
                    Art(Illustration.BRAIN, 28.dp)
                    Text(stringResource(R.string.result_missed), style = VocabTheme.type.labelFa, color = colors.ink)
                }
                outcome.missedWords.forEach { word ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EnText(word.text, style = VocabTheme.type.wordMd.copy(fontSize = VocabTheme.type.bodyFa.fontSize))
                        Text(word.meaningFa, style = VocabTheme.type.labelFa.copy(fontWeight = VocabTheme.type.bodyFa.fontWeight), color = colors.inkMuted, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        val next = LessonStep.entries.getOrNull(outcome.step.ordinal + 1)
        when {
            !outcome.passed -> ClayButton(stringResource(R.string.result_retry), { onNextStep(s.unit.id, outcome.step) }, Modifier.fillMaxWidth())
            next != null -> ClayButton(stringResource(R.string.result_next_step), { onNextStep(s.unit.id, next) }, Modifier.fillMaxWidth(), trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward)
        }
        ClayButton(stringResource(R.string.result_back_to_unit), onClose, Modifier.fillMaxWidth(), style = ClayButtonStyle.SECONDARY)
    }
}
