package com.vocab.feature.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vocab.core.designsystem.component.Art
import com.vocab.core.designsystem.component.ClayButton
import com.vocab.core.designsystem.component.ClayButtonStyle
import com.vocab.core.designsystem.component.ClayCard
import com.vocab.core.designsystem.component.ClayIconButton
import com.vocab.core.designsystem.component.ClayPill
import com.vocab.core.designsystem.component.ClayProgressBar
import com.vocab.core.designsystem.component.EnText
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.domain.MILLIS_PER_DAY
import com.vocab.core.domain.MILLIS_PER_MINUTE
import com.vocab.core.model.Illustration
import com.vocab.core.model.Rating

@Composable
internal fun ReviewScreen(onClose: () -> Unit, onSessionActive: (Boolean) -> Unit, viewModel: ReviewViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val reviewing = state is ReviewUiState.Reviewing
    LaunchedEffect(reviewing) { onSessionActive(reviewing) }
    DisposableEffect(Unit) { onDispose { onSessionActive(false) } }
    when (val s = state) {
        ReviewUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        ReviewUiState.Empty -> EndCard(Illustration.SPARKLES, stringResource(R.string.review_empty_title), stringResource(R.string.review_empty_body), onClose)
        is ReviewUiState.Done -> EndCard(Illustration.TROPHY, stringResource(R.string.review_done_title), stringResource(R.string.review_done_body, s.reviewed), onClose)
        is ReviewUiState.Reviewing -> ReviewContent(s, viewModel, onClose)
    }
}

@Composable
private fun ReviewContent(s: ReviewUiState.Reviewing, vm: ReviewViewModel, onClose: () -> Unit) {
    val colors = VocabTheme.colors
    val word = s.item.word
    Column(Modifier.fillMaxSize().padding(Dimens.space4), verticalArrangement = Arrangement.spacedBy(Dimens.space4)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            ClayIconButton(Icons.Rounded.Close, stringResource(R.string.review_close), onClose)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text(stringResource(R.string.review_title), style = VocabTheme.type.captionFa, color = colors.ink, modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.review_count, s.done + 1, s.total), style = VocabTheme.type.captionFa, color = colors.inkMuted)
                }
                ClayProgressBar(s.done.toFloat() / s.total, fill = colors.butter)
            }
        }
        ClayCard(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.space3),
            ) {
                ClayPill(stringResource(R.string.review_unit, word.unitId.substringAfterLast('-')), colors.butterSoft, colors.butterInk)
                Art(word.illustration ?: Illustration.OPEN_BOOK, 88.dp)
                EnText(word.text, style = VocabTheme.type.wordXl)
                ClayIconButton(Icons.AutoMirrored.Rounded.VolumeUp, stringResource(R.string.review_pronounce), { vm.speak(word.text) }, container = colors.primarySoft, tint = colors.primaryText, size = 48.dp)
                if (s.revealed) {
                    HorizontalDivider(color = colors.surfaceSunk, thickness = 2.dp)
                    Text(word.meaningFa, style = VocabTheme.type.titleFa.copy(fontSize = VocabTheme.type.displayFa.fontSize), color = colors.ink, textAlign = TextAlign.Center)
                    EnText(word.example, style = VocabTheme.type.exampleEn.copy(textAlign = TextAlign.Center), color = colors.inkMuted, modifier = Modifier.fillMaxWidth())
                } else {
                    Text(stringResource(R.string.review_think), style = VocabTheme.type.captionFa, color = colors.inkMuted)
                }
            }
        }
        if (s.revealed) {
            Text(stringResource(R.string.review_how), style = VocabTheme.type.labelFa, color = colors.inkMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
                RatingButton(Rating.AGAIN, s, vm, colors.wrongSoft, colors.wrong, Modifier.weight(1f))
                RatingButton(Rating.HARD, s, vm, colors.peachSoft, colors.peachInk, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
                RatingButton(Rating.GOOD, s, vm, colors.go, colors.onGo, Modifier.weight(1f))
                RatingButton(Rating.EASY, s, vm, colors.primarySoft, colors.primaryText, Modifier.weight(1f))
            }
        } else {
            ClayButton(stringResource(R.string.review_show_answer), vm::reveal, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RatingButton(rating: Rating, s: ReviewUiState.Reviewing, vm: ReviewViewModel, bg: Color, fg: Color, modifier: Modifier) {
    val label = stringResource(
        when (rating) {
            Rating.AGAIN -> R.string.rating_again
            Rating.HARD -> R.string.rating_hard
            Rating.GOOD -> R.string.rating_good
            Rating.EASY -> R.string.rating_easy
        },
    )
    val millis = s.item.nextIntervals[rating] ?: 0
    val interval = if (millis < MILLIS_PER_DAY) {
        stringResource(R.string.interval_minutes, (millis / MILLIS_PER_MINUTE).toInt().coerceAtLeast(1))
    } else {
        stringResource(R.string.interval_days, (millis / MILLIS_PER_DAY).toInt())
    }
    ClayCard(
        modifier,
        color = bg,
        radius = Dimens.radiusMd,
        padding = Dimens.space3,
        shadow = if (rating == Rating.GOOD) VocabTheme.shadows.buttonGo else VocabTheme.shadows.buttonSoft,
        onClick = { vm.rate(rating) },
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = VocabTheme.type.labelFa.copy(fontSize = VocabTheme.type.bodyFa.fontSize), color = fg)
            Text(interval, style = VocabTheme.type.captionFa, color = fg)
        }
    }
}

@Composable
private fun EndCard(art: Illustration, title: String, body: String, onClose: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(Dimens.space4),
        verticalArrangement = Arrangement.spacedBy(Dimens.space4, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Art(art, 120.dp)
        Text(title, style = VocabTheme.type.titleFa, color = VocabTheme.colors.ink, textAlign = TextAlign.Center)
        Text(body, style = VocabTheme.type.bodyFa, color = VocabTheme.colors.inkMuted, textAlign = TextAlign.Center)
        ClayButton(stringResource(R.string.review_back), onClose, Modifier.fillMaxWidth(), style = ClayButtonStyle.SECONDARY)
    }
}
