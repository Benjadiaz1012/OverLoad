package com.pdm0126.overload.Interfaz.screens.activeWorkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val activeSession: WorkoutSession? = null,
    val activeDay: RoutineDay? = null,
    val sessionSets: List<WorkoutSet> = emptyList(),
    val lastSets: Map<String, List<WorkoutSet>> = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveWorkoutViewModel(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val uiState: StateFlow<ActiveWorkoutUiState> = workoutRepository.getActiveSession()
        .flatMapLatest { session ->
            if (session == null) {
                flowOf(ActiveWorkoutUiState(isLoading = false, activeSession = null))
            } else {
                combine(
                    routineRepository.getRoutineDay(session.dayId),
                    workoutRepository.getSetsForSession(session.sessionId)
                ) { day, sets ->
                    val historicalSets = day?.slots?.associate { slot ->
                        slot.exercise.id to workoutRepository.getLastSetsForExercise(slot.exercise.id)
                    } ?: emptyMap()

                    ActiveWorkoutUiState(
                        isLoading = false,
                        activeSession = session,
                        activeDay = day,
                        sessionSets = sets,
                        lastSets = historicalSets
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ActiveWorkoutUiState()
        )

    private val _isWorkoutFinished = MutableStateFlow(false)
    val isWorkoutFinished: StateFlow<Boolean> = _isWorkoutFinished.asStateFlow()

    fun logSet(request: LogSetRequest) {
        val sessionId = uiState.value.activeSession?.sessionId ?: return
        viewModelScope.launch {
            workoutRepository.logSet(
                sessionId = sessionId,
                slotId = request.slotId,
                exerciseId = request.exerciseId,
                setNumber = request.setNumber,
                weightKg = request.weightKg,
                reps = request.reps,
                rir = request.rir,
                isRirEnabled = request.isRirEnabled
            )
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteSet(setId)
        }
    }

    fun endWorkout() {
        val sessionId = uiState.value.activeSession?.sessionId ?: return
        val loggedSets = uiState.value.sessionSets

        viewModelScope.launch {
            if (loggedSets.isEmpty()) {
                workoutRepository.cancelSession(sessionId)
            } else {
                workoutRepository.endSession(sessionId)
            }
            _isWorkoutFinished.value = true
        }
    }

    fun cancelWorkout() {
        val sessionId = uiState.value.activeSession?.sessionId ?: return
        viewModelScope.launch {
            workoutRepository.cancelSession(sessionId)
            _isWorkoutFinished.value = true
        }
    }

    fun resetNavigation() {
        _isWorkoutFinished.value = false
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                ActiveWorkoutViewModel(
                    routineRepository = app.overloadProvider.provideRoutineRepository(),
                    workoutRepository = app.overloadProvider.provideWorkoutRepository()
                )
            }
        }
    }
}