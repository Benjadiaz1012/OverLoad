package com.pdm0126.overload.ui.screens.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.Routine
import com.pdm0126.overload.domain.model.PlannedExercise
import com.pdm0126.overload.domain.model.WorkoutSet
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExerciseDisplayItem(
    val plannedExercise: PlannedExercise,
    val lastSets: List<WorkoutSet>
)

data class TrainingUiState(
    val isLoading: Boolean = true,
    val activeRoutine: Routine? = null,
    val day: RoutineDay? = null,
    val exercises: List<ExerciseDisplayItem> = emptyList(),
    val activeSessionDayId: Long? = null
)

class TrainingViewModel(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val _selectedDayId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TrainingUiState> = combine(
        _selectedDayId,
        routineRepository.getActiveRoutine(),
        workoutRepository.getActiveSession()
    ) { selectedDayId, routine, activeSession ->
        Triple(selectedDayId, routine, activeSession)
    }.map { (selectedDayId, routine, activeSession) ->
        val day = routine?.days?.find { it.dayId == selectedDayId }
            ?: routine?.days?.firstOrNull()
        val exercises = day?.plannedExercises?.map { plannedExercise ->
            ExerciseDisplayItem(
                plannedExercise = plannedExercise,
                lastSets = workoutRepository.getLastSetsForExercise(plannedExercise.exercise.id)
            )
        } ?: emptyList()

        TrainingUiState(
            isLoading = false,
            activeRoutine = routine,
            day = day,
            exercises = exercises,
            activeSessionDayId = activeSession?.dayId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingUiState()
    )

    fun selectDay(dayId: Long) {
        _selectedDayId.value = dayId
    }

    fun startWorkout(onSessionStarted: () -> Unit) {
        val dayId = uiState.value.day?.dayId ?: return
        viewModelScope.launch {
            workoutRepository.startSession(dayId)
            onSessionStarted()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                TrainingViewModel(
                    routineRepository = app.overloadProvider.provideRoutineRepository(),
                    workoutRepository = app.overloadProvider.provideWorkoutRepository()
                )
            }
        }
    }
}