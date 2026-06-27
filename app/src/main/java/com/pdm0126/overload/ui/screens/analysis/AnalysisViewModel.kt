package com.pdm0126.overload.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.repository.AnalysisRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModel(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _selectedMuscle = MutableStateFlow<String?>(null)
    private val _selectedExerciseId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AnalysisUiState> = combine(
        analysisRepository.getOverallMuscleDistribution(),
        _selectedMuscle.flatMapLatest { muscle ->
            if (muscle != null) analysisRepository.getEffectiveVolumeProgressionForMuscle(muscle)
            else flowOf(emptyList())
        },
        _selectedExerciseId.flatMapLatest { exerciseId ->
            if (exerciseId != null) analysisRepository.getVolumeProgressionForExercise(exerciseId)
            else flowOf(emptyList())
        },
        _selectedMuscle,
        _selectedExerciseId
    ) { distribution, muscleProgression, exerciseProgression, selectedMuscle, selectedExerciseId ->
        AnalysisUiState(
            isLoading = false,
            muscleDistribution = distribution,
            muscleProgression = muscleProgression,
            exerciseProgression = exerciseProgression,
            selectedMuscle = selectedMuscle,
            selectedExerciseId = selectedExerciseId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalysisUiState()
    )

    fun selectMuscle(muscleGroup: String) {
        _selectedMuscle.value = muscleGroup
    }

    fun selectExercise(exerciseId: String) {
        _selectedExerciseId.value = exerciseId
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                AnalysisViewModel(
                    analysisRepository = app.overloadProvider.provideAnalysisRepository()
                )
            }
        }
    }
}
