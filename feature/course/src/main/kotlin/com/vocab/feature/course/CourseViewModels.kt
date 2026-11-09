package com.vocab.feature.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.vocab.core.domain.usecase.ObservePathUseCase
import com.vocab.core.domain.usecase.ObserveUnitDetailUseCase
import com.vocab.core.domain.usecase.UnitDetail
import com.vocab.core.model.LevelProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CourseUiState(val levels: List<LevelProgress> = emptyList(), val selectedLevelId: String? = null) {
    val selected: LevelProgress?
        get() = levels.firstOrNull { it.level.id == selectedLevelId }
            ?: levels.lastOrNull { it.unlocked && it.units.isNotEmpty() }
            ?: levels.firstOrNull()
}

@HiltViewModel
class CourseViewModel @Inject constructor(observePath: ObservePathUseCase) : ViewModel() {
    private val selected = MutableStateFlow<String?>(null)

    val state: StateFlow<CourseUiState> = combine(observePath(), selected.asStateFlow()) { levels, id -> CourseUiState(levels, id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseUiState())

    fun selectLevel(levelId: String) {
        selected.value = levelId
    }
}

@HiltViewModel
class UnitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeUnit: ObserveUnitDetailUseCase,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<UnitRoute>()

    val state: StateFlow<UnitDetail?> = observeUnit(route.unitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
