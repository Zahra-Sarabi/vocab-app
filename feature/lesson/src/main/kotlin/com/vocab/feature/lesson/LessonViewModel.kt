package com.vocab.feature.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.vocab.core.domain.exercise.AnswerChecker
import com.vocab.core.domain.repository.Pronouncer
import com.vocab.core.domain.usecase.FinishExerciseStepUseCase
import com.vocab.core.domain.usecase.FinishLearnStepUseCase
import com.vocab.core.domain.usecase.StartStepUseCase
import com.vocab.core.domain.usecase.StepOutcome
import com.vocab.core.domain.usecase.ToggleStarUseCase
import com.vocab.core.model.AnswerRecord
import com.vocab.core.model.AnswerVerdict
import com.vocab.core.model.LessonStep
import com.vocab.core.model.Question
import com.vocab.core.model.VocabUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Feedback(val verdict: AnswerVerdict, val correctAnswer: String, val detail: String?)

/** Progress through a match question: chosen word on the left, pairs already solved. */
data class MatchState(val selectedWordId: String? = null, val solved: Set<String> = emptySet(), val mistakes: Int = 0, val lastWrongMeaning: String? = null)

sealed interface LessonUiState {
    data object Loading : LessonUiState

    data class Learning(val unit: VocabUnit, val index: Int, val starred: Set<String>) : LessonUiState {
        val word get() = unit.words[index]
        val isLast get() = index == unit.words.lastIndex
    }

    data class Practicing(
        val unit: VocabUnit,
        val step: LessonStep,
        val questions: List<Question>,
        val index: Int,
        val selectedOption: Int? = null,
        val typed: String = "",
        val emptyError: Boolean = false,
        val match: MatchState = MatchState(),
        val feedback: Feedback? = null,
        /** Questions answered wrong the first time, asked once more at the end. */
        val retryCount: Int = 0,
    ) : LessonUiState {
        val question get() = questions[index]
        /** The unit test shows no answers until the end. */
        val showsFeedback get() = step != LessonStep.UNIT_TEST
    }

    data class Finished(val unit: VocabUnit, val outcome: StepOutcome) : LessonUiState
}

@HiltViewModel
class LessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startStep: StartStepUseCase,
    private val finishLearn: FinishLearnStepUseCase,
    private val finishExercise: FinishExerciseStepUseCase,
    private val toggleStar: ToggleStarUseCase,
    private val checker: AnswerChecker,
    private val pronouncer: Pronouncer,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<LessonRoute>()
    private val step = LessonStep.valueOf(route.step)
    private val _state = MutableStateFlow<LessonUiState>(LessonUiState.Loading)
    val state: StateFlow<LessonUiState> = _state.asStateFlow()

    /** First-attempt answers, which are what the score counts. */
    private val answers = mutableListOf<AnswerRecord>()
    private val answeredIds = mutableSetOf<Int>()

    init {
        viewModelScope.launch {
            val session = startStep(route.unitId, step)
            _state.value = if (step == LessonStep.LEARN) {
                LessonUiState.Learning(session.unit, 0, emptySet())
            } else {
                LessonUiState.Practicing(session.unit, step, session.questions, 0)
            }
            speakIfListening()
        }
    }

    fun speak(text: String) = pronouncer.speak(text)

    // Learn step

    fun nextWord() {
        val s = _state.value as? LessonUiState.Learning ?: return
        if (s.isLast) {
            viewModelScope.launch {
                finishLearn(s.unit)
                _state.value = LessonUiState.Finished(
                    s.unit,
                    StepOutcome(LessonStep.LEARN, 100, true, 0, s.unit.words.size, s.unit.words.size, emptyList(), null),
                )
            }
        } else {
            _state.value = s.copy(index = s.index + 1)
        }
    }

    fun previousWord() {
        _state.update { s -> if (s is LessonUiState.Learning && s.index > 0) s.copy(index = s.index - 1) else s }
    }

    fun toggleStar(wordId: String) {
        val s = _state.value as? LessonUiState.Learning ?: return
        val starred = wordId !in s.starred
        _state.value = s.copy(starred = if (starred) s.starred + wordId else s.starred - wordId)
        viewModelScope.launch { toggleStar(wordId, starred) }
    }

    // Exercises

    fun selectOption(index: Int) {
        _state.update { s -> if (s is LessonUiState.Practicing && s.feedback == null) s.copy(selectedOption = index) else s }
        val s = _state.value as? LessonUiState.Practicing ?: return
        if (!s.showsFeedback) check()
    }

    fun type(text: String) {
        _state.update { s -> if (s is LessonUiState.Practicing && s.feedback == null) s.copy(typed = text, emptyError = false) else s }
    }

    /** Grades a choice or typed answer. Match questions grade themselves pair by pair. */
    fun check() {
        val s = _state.value as? LessonUiState.Practicing ?: return
        if (s.feedback != null) return
        val (verdict, given, expected) = when (val q = s.question) {
            is Question.MeaningChoice -> {
                val chosen = s.selectedOption ?: return
                Triple(if (chosen == q.correctIndex) AnswerVerdict.CORRECT else AnswerVerdict.WRONG, q.options[chosen], q.options[q.correctIndex])
            }
            is Question.WordChoice -> {
                val chosen = s.selectedOption ?: return
                Triple(if (chosen == q.correctIndex) AnswerVerdict.CORRECT else AnswerVerdict.WRONG, q.options[chosen], q.options[q.correctIndex])
            }
            is Question.Gap -> typedVerdict(s, q.answer) ?: return
            is Question.Listen -> typedVerdict(s, q.answer) ?: return
            is Question.Match -> return
        }
        record(s, verdict, given)
        val feedback = Feedback(verdict, expected, detailFor(s.unit, s.question))
        if (s.showsFeedback) _state.value = s.copy(feedback = feedback) else advance(s.copy(feedback = feedback))
    }

    fun selectMatchWord(wordId: String) {
        _state.update { s ->
            if (s is LessonUiState.Practicing && wordId !in s.match.solved) s.copy(match = s.match.copy(selectedWordId = wordId, lastWrongMeaning = null)) else s
        }
    }

    fun selectMatchMeaning(meaningWordId: String) {
        val s = _state.value as? LessonUiState.Practicing ?: return
        val q = s.question as? Question.Match ?: return
        val selected = s.match.selectedWordId ?: return
        val match = if (selected == meaningWordId) {
            s.match.copy(selectedWordId = null, solved = s.match.solved + selected, lastWrongMeaning = null)
        } else {
            s.match.copy(selectedWordId = null, mistakes = s.match.mistakes + 1, lastWrongMeaning = meaningWordId)
        }
        val next = s.copy(match = match)
        if (match.solved.size == q.pairs.size) {
            val verdict = when (match.mistakes) {
                0 -> AnswerVerdict.CORRECT
                in 1..2 -> AnswerVerdict.ALMOST
                else -> AnswerVerdict.WRONG
            }
            record(s, verdict, "")
            val feedback = Feedback(verdict, "", null)
            if (s.showsFeedback) _state.value = next.copy(feedback = feedback) else advance(next.copy(feedback = feedback))
        } else {
            _state.value = next
        }
    }

    fun continueAfterFeedback() {
        val s = _state.value as? LessonUiState.Practicing ?: return
        advance(s)
    }

    private fun advance(s: LessonUiState.Practicing) {
        val missed = s.feedback?.verdict == AnswerVerdict.WRONG
        var questions = s.questions
        var retries = s.retryCount
        if (missed && s.showsFeedback && s.index !in retriedIndexes) {
            questions = questions + s.question
            retriedIndexes += questions.lastIndex
            retries++
        }
        val nextIndex = s.index + 1
        if (nextIndex >= questions.size) {
            finish(s.unit)
        } else {
            _state.value = s.copy(questions = questions, index = nextIndex, selectedOption = null, typed = "", emptyError = false, match = MatchState(), feedback = null, retryCount = retries)
            speakIfListening()
        }
    }

    private val retriedIndexes = mutableSetOf<Int>()

    private fun finish(unit: VocabUnit) {
        viewModelScope.launch {
            _state.value = LessonUiState.Finished(unit, finishExercise(unit, step, answers.toList()))
        }
    }

    private fun record(s: LessonUiState.Practicing, verdict: AnswerVerdict, given: String) {
        if (s.index in retriedIndexes || !answeredIds.add(s.index)) return
        answers += AnswerRecord(s.question, verdict, given)
    }

    private fun typedVerdict(s: LessonUiState.Practicing, expected: String): Triple<AnswerVerdict, String, String>? {
        if (s.typed.isBlank()) {
            _state.value = s.copy(emptyError = true)
            return null
        }
        return Triple(checker.check(s.typed, expected), s.typed, expected)
    }

    private fun detailFor(unit: VocabUnit, question: Question): String? {
        val word = unit.words.firstOrNull { it.id == question.wordId } ?: return null
        return "${word.text} = ${word.meaningFa}"
    }

    private fun speakIfListening() {
        val s = _state.value as? LessonUiState.Practicing ?: return
        (s.question as? Question.Listen)?.let { pronouncer.speak(it.answer) }
    }
}
