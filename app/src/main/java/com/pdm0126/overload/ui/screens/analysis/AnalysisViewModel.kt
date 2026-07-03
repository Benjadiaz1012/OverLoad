package com.pdm0126.overload.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
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

    val uiState: StateFlow<AnalysisUiState> = combine(
        analysisRepository.getOverallMuscleDistribution(),
        exerciseRepository.getLocalExercises(),
        _selectedExerciseId
    ) { distribution, exercises, selectedId ->
        Triple(distribution, exercises, selectedId)
    }.flatMapLatest { (distribution, exercises, selectedId) ->

        // Si hay un ejercicio seleccionado, consultamos su historial en Room
        val trendFlow = if (selectedId != null) {
            analysisRepository.getVolumeProgressionForExercise(selectedId)
        } else {
            flowOf(emptyList())
        }

        // Mapeamos el resultado final al Estado de la UI
        trendFlow.map { progression ->
            AnalysisUiState(
                isLoading = false,
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

    fun selectExercise(exerciseId: String) {
        _selectedExerciseId.value = exerciseId
    }
    fun unselectExercise() {
        _selectedExerciseId.value = null
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