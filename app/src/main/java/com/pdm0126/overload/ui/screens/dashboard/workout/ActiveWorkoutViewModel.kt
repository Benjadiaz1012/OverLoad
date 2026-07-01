package com.pdm0126.overload.ui.screens.dashboard.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.WorkoutSet
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveWorkoutViewModel(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private val _isWorkoutFinished = MutableStateFlow(false)
    val isWorkoutFinished = _isWorkoutFinished.asStateFlow()

    init {
        viewModelScope.launch {
            workoutRepository.getActiveSession()
                .flatMapLatest { session ->
                    if (session != null) {
                        combine(
                            routineRepository.getRoutineDay(session.dayId),
                            workoutRepository.getSetsForSession(session.sessionId)
                        ) { day, sets ->
                            val historicalSets = mutableMapOf<String, List<WorkoutSet>>()
                            day?.slots?.forEach { slot ->
                                historicalSets[slot.exercise.id] = workoutRepository.getLastSetsForExercise(
                                    exerciseId = slot.exercise.id
                                )
                            }
                            ActiveWorkoutUiState(
                                isLoading = false,
                                activeSession = session,
                                activeDay = day,
                                sessionSets = sets,
                                lastSets = historicalSets
                            )
                        }
                    } else {
                        flowOf(ActiveWorkoutUiState(isLoading = false, activeSession = null))
                    }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun endWorkout() {
        val sessionId = _uiState.value.activeSession?.sessionId ?: return
        viewModelScope.launch {
            workoutRepository.endSession(sessionId)
            _isWorkoutFinished.value = true
        }
    }

    fun resetNavigation() {
        _isWorkoutFinished.value = false
    }

    fun logSet(slotId: Long, exerciseId: String, setNumber: Int, weightKg: Float, reps: Int, rir: Int?, isRirEnabled: Boolean) {
        val sessionId = _uiState.value.activeSession?.sessionId ?: return
        viewModelScope.launch {
            workoutRepository.logSet(sessionId, slotId, exerciseId, setNumber, weightKg, reps, rir, isRirEnabled)
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch { workoutRepository.deleteSet(setId) }
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