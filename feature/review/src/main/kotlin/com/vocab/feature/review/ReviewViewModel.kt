package com.vocab.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocab.core.domain.repository.Pronouncer
import com.vocab.core.domain.usecase.ReviewItem
import com.vocab.core.domain.usecase.StartReviewUseCase
import com.vocab.core.domain.usecase.SubmitReviewUseCase
import com.vocab.core.model.Rating
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReviewUiState {
    data object Loading : ReviewUiState
    data object Empty : ReviewUiState
    data class Reviewing(val item: ReviewItem, val revealed: Boolean, val done: Int, val total: Int) : ReviewUiState
    data class Done(val reviewed: Int) : ReviewUiState
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val startReview: StartReviewUseCase,
    private val submitReview: SubmitReviewUseCase,
    private val pronouncer: Pronouncer,
) : ViewModel() {
    private val _state = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    private val queue = ArrayDeque<ReviewItem>()
    private var total = 0
    private var reviewed = 0

    init {
        viewModelScope.launch {
            queue.addAll(startReview())
            total = queue.size
            showNext()
        }
    }

    fun reveal() {
        val s = _state.value as? ReviewUiState.Reviewing ?: return
        _state.value = s.copy(revealed = true)
        pronouncer.speak(s.item.word.text)
    }

    fun speak(text: String) = pronouncer.speak(text)

    /** Words the learner forgot come back at the end of this session. */
    fun rate(rating: Rating) {
        val s = _state.value as? ReviewUiState.Reviewing ?: return
        viewModelScope.launch {
            val updated = submitReview(s.item.card, rating)
            reviewed++
            if (rating == Rating.AGAIN) queue.addLast(s.item.copy(card = updated))
            showNext()
        }
    }

    private fun showNext() {
        val next = queue.removeFirstOrNull()
        _state.value = when {
            next != null -> ReviewUiState.Reviewing(next, revealed = false, done = reviewed, total = total.coerceAtLeast(reviewed + 1))
            total == 0 -> ReviewUiState.Empty
            else -> ReviewUiState.Done(reviewed)
        }
    }
}
