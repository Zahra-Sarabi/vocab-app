package com.vocab.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocab.core.domain.usecase.ObserveTodayUseCase
import com.vocab.core.domain.usecase.TodaySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data class Ready(val summary: TodaySummary) : TodayUiState
}

@HiltViewModel
class TodayViewModel @Inject constructor(observeToday: ObserveTodayUseCase) : ViewModel() {
    val state: StateFlow<TodayUiState> = observeToday()
        .map<TodaySummary, TodayUiState> { TodayUiState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState.Loading)
}
