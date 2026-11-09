package com.vocab.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.CompositionLocalProvider
import com.vocab.core.designsystem.component.AnswerOption
import com.vocab.core.designsystem.component.Art
import com.vocab.core.designsystem.component.ClayBox
import com.vocab.core.designsystem.component.ClayButton
import com.vocab.core.designsystem.component.ClayButtonStyle
import com.vocab.core.designsystem.component.ClayCard
import com.vocab.core.designsystem.component.ClayIconButton
import com.vocab.core.designsystem.component.ClayPill
import com.vocab.core.designsystem.component.ClayProgressBar
import com.vocab.core.designsystem.component.EnText
import com.vocab.core.designsystem.component.OptionState
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.model.AnswerVerdict
import com.vocab.core.model.Illustration
import com.vocab.core.model.LessonStep
import com.vocab.core.model.Question

@Composable
internal fun LessonScreen(
    onClose: () -> Unit,
    onNextStep: (String, LessonStep) -> Unit,
    viewModel: LessonViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        LessonUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is LessonUiState.Learning -> LearnContent(s, viewModel, onClose)
        is LessonUiState.Practicing -> PracticeContent(s, viewModel, onClose)
        is LessonUiState.Finished -> ResultContent(s, onClose, onNextStep)
    }
}

@Composable
private fun TopBar(progress: Float, label: String, onClose: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
        ClayIconButton(Icons.Rounded.Close, stringResource(R.string.lesson_close), onClose)
        ClayProgressBar(progress, Modifier.weight(1f), height = 14.dp)
        EnText(label, style = VocabTheme.type.labelFa.copy(fontFamily = com.vocab.core.designsystem.theme.Nunito), color = VocabTheme.colors.inkMuted)
    }
}

@Composable
private fun LearnContent(s: LessonUiState.Learning, vm: LessonViewModel, onClose: () -> Unit) {
    val colors = VocabTheme.colors
    val word = s.word
    Column(Modifier.fillMaxSize().padding(Dimens.space4), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        TopBar((s.index + 1f) / s.unit.words.size, "${s.index + 1}/${s.unit.words.size}", onClose)
        ClayCard(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.space3),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ClayPill(stringResource(R.string.lesson_new_word), colors.skySoft, colors.skyInk)
                    Box(Modifier.weight(1f))
                    val starred = word.id in s.starred
                    ClayIconButton(
                        if (starred) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        stringResource(R.string.lesson_star),
                        { vm.toggleStar(word.id) },
                        container = colors.butterSoft,
                        tint = colors.star,
                        size = 40.dp,
                    )
                }
                ClayBox(Modifier.size(150.dp), color = colors.lilacSoft, radius = 75.dp, shadow = VocabTheme.shadows.sunk) {
                    Art(word.illustration ?: s.unit.summary.illustration, 110.dp)
                }
                EnText(word.text, style = VocabTheme.type.wordXl)
                listOfNotNull(word.phonetic, word.partOfSpeech).takeIf { it.isNotEmpty() }?.let {
                    EnText(it.joinToString(" · "), style = VocabTheme.type.labelFa.copy(fontFamily = com.vocab.core.designsystem.theme.Nunito), color = colors.inkMuted)
                }
                ClayIconButton(Icons.AutoMirrored.Rounded.VolumeUp, stringResource(R.string.lesson_pronounce), { vm.speak(word.text) }, container = colors.primarySoft, tint = colors.primaryText, size = 52.dp)
                Text(word.meaningFa, style = VocabTheme.type.titleFa.copy(fontSize = VocabTheme.type.displayFa.fontSize), color = colors.ink, textAlign = TextAlign.Center)
                EnText(word.example, style = VocabTheme.type.exampleEn.copy(textAlign = TextAlign.Center), color = colors.inkMuted, modifier = Modifier.fillMaxWidth())
                word.tipFa?.let { tip ->
                    ClayBox(Modifier.fillMaxWidth(), color = colors.butterSoft, radius = Dimens.radiusSm, shadow = VocabTheme.shadows.pill) {
                        Row(Modifier.padding(Dimens.space3), horizontalArrangement = Arrangement.spacedBy(Dimens.space2)) {
                            Art(Illustration.LIGHT_BULB, 26.dp)
                            Text(tip, style = VocabTheme.type.labelFa.copy(fontWeight = VocabTheme.type.bodyFa.fontWeight), color = colors.butterInk, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            ClayButton(stringResource(R.string.lesson_previous), vm::previousWord, Modifier.weight(1f), ClayButtonStyle.SECONDARY, enabled = s.index > 0, leadingIcon = Icons.AutoMirrored.Rounded.ArrowBack)
            ClayButton(
                stringResource(if (s.isLast) R.string.lesson_finish_learn else R.string.lesson_next),
                vm::nextWord,
                Modifier.weight(2f),
                trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
            )
        }
    }
}

@Composable
private fun PracticeContent(s: LessonUiState.Practicing, vm: LessonViewModel, onClose: () -> Unit) {
    val colors = VocabTheme.colors
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Dimens.space4),
            verticalArrangement = Arrangement.spacedBy(Dimens.space4),
        ) {
            TopBar(s.index.toFloat() / s.questions.size, "${s.index + 1}/${s.questions.size}", onClose)
            Text(stringResource(R.string.question_count, s.index + 1, s.questions.size), style = VocabTheme.type.captionFa, color = colors.inkMuted)
            when (val q = s.question) {
                is Question.MeaningChoice -> ChoiceQuestion(stringResource(R.string.q_meaning), q.word, true, q.options, q.correctIndex, s, vm)
                is Question.WordChoice -> ChoiceQuestion(stringResource(R.string.q_word), q.meaningFa, false, q.options, q.correctIndex, s, vm)
                is Question.Gap -> TypingQuestion(s, vm, prompt = stringResource(R.string.q_gap), sentence = q.sentence, hint = q.hintFa, answer = q.answer)
                is Question.Listen -> TypingQuestion(s, vm, prompt = stringResource(R.string.q_listen), sentence = null, hint = null, answer = q.answer, listen = true)
                is Question.Match -> MatchQuestion(q, s, vm)
            }
        }
        val feedback = s.feedback
        if (feedback != null && s.showsFeedback) {
            FeedbackBar(feedback, vm::continueAfterFeedback)
        } else if (s.question !is Question.Match && (s.showsFeedback || s.question is Question.Gap || s.question is Question.Listen)) {
            ClayButton(
                stringResource(R.string.q_check),
                vm::check,
                Modifier.fillMaxWidth().padding(Dimens.space4),
                enabled = s.selectedOption != null || s.question is Question.Gap || s.question is Question.Listen,
            )
        }
    }
}

@Composable
private fun ChoiceQuestion(
    prompt: String,
    subject: String,
    subjectIsEnglish: Boolean,
    options: List<String>,
    correctIndex: Int,
    s: LessonUiState.Practicing,
    vm: LessonViewModel,
) {
    val colors = VocabTheme.colors
    ClayCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            Column(Modifier.weight(1f)) {
                Text(prompt, style = VocabTheme.type.labelFa.copy(fontWeight = VocabTheme.type.bodyFa.fontWeight), color = colors.inkMuted)
                if (subjectIsEnglish) EnText(subject, style = VocabTheme.type.wordXl.copy(fontSize = VocabTheme.type.number.fontSize))
                else Text(subject, style = VocabTheme.type.titleFa, color = colors.ink)
            }
            if (subjectIsEnglish) {
                ClayIconButton(Icons.AutoMirrored.Rounded.VolumeUp, stringResource(R.string.lesson_pronounce), { vm.speak(subject) }, container = colors.primarySoft, tint = colors.primaryText, size = 48.dp)
            }
            Art(s.unit.summary.illustration, 64.dp)
        }
    }
    val letters = listOf("A", "B", "C", "D")
    val badgeColors = listOf(colors.mint, colors.butter, colors.periwinkle, colors.pink)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEachIndexed { i, option ->
            val state = when {
                s.feedback != null && s.showsFeedback && i == correctIndex -> OptionState.CORRECT
                s.feedback != null && s.showsFeedback && i == s.selectedOption -> OptionState.WRONG
                i == s.selectedOption -> OptionState.SELECTED
                else -> OptionState.REST
            }
            val content: @Composable () -> Unit = {
                AnswerOption(
                    letter = letters.getOrNull(i),
                    text = option,
                    state = state,
                    onClick = { vm.selectOption(i) },
                    letterColor = badgeColors[i % badgeColors.size],
                    trailing = if (state == OptionState.CORRECT) {
                        { Icon(Icons.Rounded.CheckCircle, null, tint = colors.correct) }
                    } else null,
                )
            }
            if (subjectIsEnglish) content() else CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) { content() }
        }
    }
}

@Composable
private fun TypingQuestion(
    s: LessonUiState.Practicing,
    vm: LessonViewModel,
    prompt: String,
    sentence: String?,
    hint: String?,
    answer: String,
    listen: Boolean = false,
) {
    val colors = VocabTheme.colors
    if (sentence != null && s.unit.referenceRows.isNotEmpty()) {
        ClayCard(color = colors.lilacSoft, padding = Dimens.space4) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space2)) {
                Art(s.unit.summary.illustration, 28.dp)
                Text(stringResource(R.string.q_reference), style = VocabTheme.type.labelFa, color = colors.lilacInk)
            }
            s.unit.referenceRows.forEach { row ->
                EnText(row, style = VocabTheme.type.labelFa.copy(fontFamily = com.vocab.core.designsystem.theme.Nunito), modifier = Modifier.fillMaxWidth(), color = colors.ink)
            }
        }
    }
    ClayCard {
        Text(prompt, style = VocabTheme.type.labelFa.copy(fontWeight = VocabTheme.type.bodyFa.fontWeight), color = colors.inkMuted)
        if (listen) {
            ClayButton(stringResource(R.string.q_play_again), { vm.speak(answer) }, style = ClayButtonStyle.SECONDARY, leadingIcon = Icons.AutoMirrored.Rounded.VolumeUp)
        }
        sentence?.let { EnText(it, style = VocabTheme.type.wordMd.copy(fontSize = VocabTheme.type.titleFa.fontSize), modifier = Modifier.fillMaxWidth()) }
        hint?.let { Text(it, style = VocabTheme.type.captionFa, color = colors.inkMuted) }
    }
    val verdict = s.feedback?.verdict.takeIf { s.showsFeedback }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        OutlinedTextField(
            value = s.typed,
            onValueChange = vm::type,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.q_answer_label), style = VocabTheme.type.captionFa) },
            singleLine = true,
            isError = s.emptyError || verdict == AnswerVerdict.WRONG,
            textStyle = VocabTheme.type.wordMd,
            supportingText = if (s.emptyError) {
                { Text(stringResource(R.string.q_empty_answer), style = VocabTheme.type.captionFa, color = colors.wrong) }
            } else null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { vm.check() }),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.radiusMd),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceSunk,
                unfocusedContainerColor = colors.surfaceSunk,
                focusedBorderColor = when (verdict) { AnswerVerdict.CORRECT -> colors.correct; AnswerVerdict.WRONG -> colors.wrong; else -> colors.focus },
                unfocusedBorderColor = when (verdict) { AnswerVerdict.CORRECT -> colors.correct; AnswerVerdict.WRONG -> colors.wrong; else -> colors.line },
            ),
        )
    }
}

@Composable
private fun MatchQuestion(q: Question.Match, s: LessonUiState.Practicing, vm: LessonViewModel) {
    val colors = VocabTheme.colors
    Text(stringResource(R.string.q_match), style = VocabTheme.type.labelFa, color = colors.inkMuted)
    val meanings = androidx.compose.runtime.remember(q) { q.pairs.shuffled() }
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.space2)) {
            meanings.forEach { pair ->
                AnswerOption(
                    letter = null,
                    text = pair.meaningFa,
                    state = when {
                        pair.wordId in s.match.solved -> OptionState.CORRECT
                        pair.wordId == s.match.lastWrongMeaning -> OptionState.WRONG
                        else -> OptionState.REST
                    },
                    onClick = { vm.selectMatchMeaning(pair.wordId) },
                )
            }
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.space2)) {
                q.pairs.forEach { pair ->
                    AnswerOption(
                        letter = null,
                        text = pair.word,
                        state = when {
                            pair.wordId in s.match.solved -> OptionState.CORRECT
                            pair.wordId == s.match.selectedWordId -> OptionState.SELECTED
                            else -> OptionState.REST
                        },
                        onClick = { vm.selectMatchWord(pair.wordId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackBar(feedback: Feedback, onContinue: () -> Unit) {
    val colors = VocabTheme.colors
    val correct = feedback.verdict == AnswerVerdict.CORRECT
    val (bg, fg) = if (correct) colors.correctSoft to colors.correct else colors.wrongSoft to colors.wrong
    ClayCard(
        Modifier.fillMaxWidth(),
        color = bg,
        radius = Dimens.radiusLg,
        shadow = VocabTheme.shadows.card,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            Art(if (correct) Illustration.PARTY else Illustration.BRAIN, 52.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(
                        when (feedback.verdict) {
                            AnswerVerdict.CORRECT -> R.string.feedback_correct
                            AnswerVerdict.ALMOST -> R.string.feedback_almost
                            AnswerVerdict.WRONG -> R.string.feedback_wrong
                        },
                    ),
                    style = VocabTheme.type.titleFa,
                    color = fg,
                )
                val line = if (correct) feedback.detail else feedback.correctAnswer.takeIf { it.isNotEmpty() }?.let { stringResource(R.string.feedback_answer, it) }
                line?.let { Text(it, style = VocabTheme.type.labelFa.copy(fontWeight = VocabTheme.type.bodyFa.fontWeight), color = fg) }
            }
        }
        ClayButton(
            stringResource(if (correct) R.string.feedback_next else R.string.feedback_got_it),
            onContinue,
            Modifier.fillMaxWidth(),
            style = if (correct) ClayButtonStyle.GO else ClayButtonStyle.PRIMARY,
            trailingIcon = if (correct) Icons.AutoMirrored.Rounded.ArrowForward else null,
        )
    }
}
