package com.vocab.feature.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vocab.core.designsystem.component.Art
import com.vocab.core.designsystem.component.ClayBox
import com.vocab.core.designsystem.component.ClayCard
import com.vocab.core.designsystem.component.ClayProgressBar
import com.vocab.core.designsystem.component.EnText
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.domain.usecase.ObserveProgressUseCase
import com.vocab.core.domain.usecase.ProgressSummary
import com.vocab.core.model.Illustration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.math.roundToInt

@Serializable
data object ProgressRoute

fun NavGraphBuilder.progressScreen() {
    composable<ProgressRoute> { ProgressScreen() }
}

@HiltViewModel
class ProgressViewModel @Inject constructor(observeProgress: ObserveProgressUseCase) : ViewModel() {
    val state: StateFlow<ProgressSummary?> = observeProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
internal fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val summary by viewModel.state.collectAsStateWithLifecycle()
    val s = summary ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val colors = VocabTheme.colors
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.space4),
        verticalArrangement = Arrangement.spacedBy(Dimens.space4),
    ) {
        Text(stringResource(R.string.progress_title), style = VocabTheme.type.displayFa, color = colors.ink)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Illustration.OPEN_BOOK, s.learnedWords, stringResource(R.string.progress_learned), colors.lilacSoft, Modifier.weight(1f))
            StatTile(Illustration.CHART, s.masteredWords, stringResource(R.string.progress_mastered), colors.mintSoft, Modifier.weight(1f))
            StatTile(Illustration.FIRE, s.streak, stringResource(R.string.progress_streak), colors.streakSoft, Modifier.weight(1f))
        }
        Calendar(s)
        ClayCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space2)) {
                Art(Illustration.GRADUATION_CAP, 28.dp)
                Text(stringResource(R.string.progress_levels), style = VocabTheme.type.labelFa, color = colors.ink)
            }
            s.levels.forEach { level ->
                val pct = (level.mastery * 100).roundToInt()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row {
                        // LTR text in an RTL row: shrink to the text and sit at the start (right) side.
                        EnText(level.level.title, style = VocabTheme.type.labelFa.copy(fontFamily = com.vocab.core.designsystem.theme.Nunito), modifier = Modifier.weight(1f).wrapContentWidth(Alignment.Start))
                        EnText("$pct%", style = VocabTheme.type.labelFa.copy(fontFamily = com.vocab.core.designsystem.theme.Nunito), color = if (pct >= 90) colors.correct else colors.primaryText)
                    }
                    ClayProgressBar(level.mastery.toFloat(), fill = if (pct >= 90) colors.go else colors.primary, height = 10.dp)
                }
            }
            s.reviewAccuracy?.let { Text(stringResource(R.string.progress_accuracy, (it * 100).roundToInt()), style = VocabTheme.type.captionFa, color = colors.inkMuted) }
            Text(stringResource(R.string.progress_longest, s.longestStreak), style = VocabTheme.type.captionFa, color = colors.inkMuted)
        }
        Text(stringResource(R.string.progress_badges), style = VocabTheme.type.labelFa, color = colors.ink)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Badge(Illustration.TROPHY, stringResource(R.string.badge_first_unit), s.passedUnits >= 1, colors.butterSoft, Modifier.weight(1f))
            Badge(Illustration.FIRE, stringResource(R.string.badge_week), s.longestStreak >= 7, colors.streakSoft, Modifier.weight(1f))
            Badge(Illustration.CALENDAR, stringResource(R.string.badge_month), s.longestStreak >= 30, colors.skySoft, Modifier.weight(1f))
            Badge(Illustration.STAR, stringResource(R.string.badge_100), s.masteredWords >= 100, colors.mintSoft, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(art: Illustration, value: Int, label: String, color: Color, modifier: Modifier) {
    ClayCard(modifier, color = color, radius = Dimens.radiusMd, padding = Dimens.space3, shadow = VocabTheme.shadows.raised) {
        Column(Modifier.padding(0.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Art(art, 36.dp)
            EnText("$value", style = VocabTheme.type.number.copy(fontSize = VocabTheme.type.wordMd.fontSize))
            Text(label, style = VocabTheme.type.captionFa, color = VocabTheme.colors.inkMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun Calendar(s: ProgressSummary) {
    val colors = VocabTheme.colors
    val shades = listOf(colors.surfaceSunk, colors.lilac, colors.primary)
    ClayCard {
        Row {
            Text(stringResource(R.string.progress_calendar), style = VocabTheme.type.labelFa, color = colors.ink, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.progress_calendar_count, s.calendar.count { it > 0 }, s.calendar.size), style = VocabTheme.type.captionFa, color = colors.inkMuted)
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                s.calendar.chunked(7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        week.forEach { level ->
                            ClayBox(
                                Modifier.weight(1f).aspectRatio(1f),
                                color = shades[level],
                                radius = 6.dp,
                                shadow = if (level == 0) VocabTheme.shadows.sunk else VocabTheme.shadows.pill,
                            ) {}
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.progress_less), style = VocabTheme.type.captionFa, color = colors.inkMuted)
            shades.forEach { ClayBox(Modifier.size(12.dp), color = it, radius = 4.dp, shadow = VocabTheme.shadows.pill) {} }
            Text(stringResource(R.string.progress_more), style = VocabTheme.type.captionFa, color = colors.inkMuted)
        }
    }
}

@Composable
private fun Badge(art: Illustration, label: String, earned: Boolean, color: Color, modifier: Modifier) {
    ClayCard(
        modifier,
        color = if (earned) color else VocabTheme.colors.surfaceSunk,
        radius = Dimens.radiusMd,
        padding = 10.dp,
        shadow = if (earned) VocabTheme.shadows.raised else VocabTheme.shadows.sunk,
    ) {
        Column(Modifier.padding(0.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Art(art, 34.dp, greyed = !earned)
            Text(label, style = VocabTheme.type.captionFa.copy(fontSize = VocabTheme.type.captionFa.fontSize * 0.92f), color = if (earned) VocabTheme.colors.ink else VocabTheme.colors.inkMuted, textAlign = TextAlign.Center)
        }
    }
}
