package com.pdm0126.overload.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.repository.AnalysisRepository
import com.pdm0126.overload.domain.repository.ExerciseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModel(
    private val analysisRepository: AnalysisRepository,
    private val exerciseRepository: ExerciseRepository // Inyección del repositorio correcto
) : ViewModel() {

    private val _selectedExerciseId = MutableStateFlow<String?>(null)
    private val _selectedTabIndex = MutableStateFlow(0)

    private data class AnalysisStateData(
        val tabIndex: Int,
        val distribution: List<MuscleDistribution>,
        val exercises: List<Exercise>,
        val selectedId: String?
    )

    val uiState: StateFlow<AnalysisUiState> = combine(
        _selectedTabIndex,
        analysisRepository.getOverallMuscleDistribution(),
        exerciseRepository.getLocalExercises(),
        _selectedExerciseId
    ) { tabIndex, distribution, exercises, selectedId ->
        AnalysisStateData(tabIndex, distribution, exercises, selectedId)
    }.flatMapLatest { ( tabIndex, distribution, exercises, selectedId) ->

        // Si hay un ejercicio seleccionado, consultamos su historial en Room
        val trendFlow = if (selectedId != null) {
            analysisRepository.getVolumeProgressionForExercise(selectedId)
        } else {
            flowOf(emptyList())
        }

        trendFlow.map { progression ->
            AnalysisUiState(
                isLoading = false,
                selectedTabIndex = tabIndex,
                muscleDistribution = distribution,
                availableExercises = exercises,
                selectedExerciseId = selectedId,
                exerciseProgression = progression
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalysisUiState()
    )
    fun onTabSelected(index: Int) {
        if (_selectedTabIndex.value != index) {
            _selectedTabIndex.value = index
        }
    }
    fun selectExercise(exerciseId: String?) {
        if (exerciseId == null) {
            _selectedExerciseId.value = null
            return
        }
        _selectedExerciseId.value = exerciseId
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                AnalysisViewModel(
                    analysisRepository = app.overloadProvider.provideAnalysisRepository(),
                    exerciseRepository = app.overloadProvider.provideExerciseRepository()
                )
            }
        }
    }
}