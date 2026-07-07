package com.pdm0126.overload.Interfaz.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.TechnicalDictionary
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.repository.AnalysisRepository
import com.pdm0126.overload.domain.repository.ExerciseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class ProgressionPoint(
    val timestamp: Long,
    val volume: Float
)

enum class EvolutionMode { EXERCISE, MUSCLE_GROUP }

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val selectedTabIndex: Int = 0,

    val muscleDistribution: List<MuscleDistribution> = emptyList(),

    val evolutionMode: EvolutionMode = EvolutionMode.EXERCISE,
    val availableExercises: List<Exercise> = emptyList(),
    val availableMuscleGroups: List<String> = emptyList(),
    val selectedExerciseId: String? = null,
    val selectedMuscleGroup: String? = null,
    val evolutionProgression: List<ProgressionPoint> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModel(
    private val analysisRepository: AnalysisRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _evolutionMode = MutableStateFlow(EvolutionMode.EXERCISE)
    private val _selectedExerciseId = MutableStateFlow<String?>(null)
    private val _selectedMuscleGroup = MutableStateFlow<String?>(null)

    private data class AnalysisStateData(
        val tabIndex: Int,
        val evolutionMode: EvolutionMode,
        val distribution: List<MuscleDistribution>,
        val exercises: List<Exercise>,
        val selectedExerciseId: String?,
        val selectedMuscleGroup: String?
    )

    val uiState: StateFlow<AnalysisUiState> = combine(
        _selectedTabIndex,
        _evolutionMode,
        analysisRepository.getOverallMuscleDistribution(),
        exerciseRepository.getLocalExercises(),
        combine(_selectedExerciseId, _selectedMuscleGroup) { exerciseId, muscleGroup ->
            exerciseId to muscleGroup
        }
    ) { tabIndex, evolutionMode, distribution, exercises, (exerciseId, muscleGroup) ->
        AnalysisStateData(tabIndex, evolutionMode, distribution, exercises, exerciseId, muscleGroup)
    }.flatMapLatest { data ->

        val progressionFlow: Flow<List<ProgressionPoint>> = when {
            data.evolutionMode == EvolutionMode.EXERCISE && data.selectedExerciseId != null -> {
                analysisRepository.getVolumeProgressionForExercise(data.selectedExerciseId)
                    .map { records ->
                        records.map {
                            ProgressionPoint(
                                it.timestamp,
                                it.totalVolume
                            )
                        }
                    }
            }

            data.evolutionMode == EvolutionMode.MUSCLE_GROUP && data.selectedMuscleGroup != null -> {
                analysisRepository.getEffectiveVolumeProgressionForMuscle(data.selectedMuscleGroup)
                    .map { records ->
                        records.map {
                            ProgressionPoint(
                                it.timestamp,
                                it.effectiveVolume
                            )
                        }
                    }
            }

            else -> flowOf(emptyList())
        }

        progressionFlow.map { progression ->
            AnalysisUiState(
                isLoading = false,
                selectedTabIndex = data.tabIndex,
                muscleDistribution = data.distribution,
                evolutionMode = data.evolutionMode,
                availableExercises = data.exercises,
                availableMuscleGroups = TechnicalDictionary.mainMuscleGroupsList,
                selectedExerciseId = data.selectedExerciseId,
                selectedMuscleGroup = data.selectedMuscleGroup,
                evolutionProgression = progression
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

    fun onEvolutionModeChanged(mode: EvolutionMode) {
        _evolutionMode.value = mode
    }

    fun selectExercise(exerciseId: String?) {
        _selectedExerciseId.value = exerciseId
    }

    fun selectMuscleGroup(muscleGroup: String?) {
        _selectedMuscleGroup.value = muscleGroup
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