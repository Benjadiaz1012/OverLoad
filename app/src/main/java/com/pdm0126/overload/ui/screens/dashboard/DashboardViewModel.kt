package com.pdm0126.overload.ui.screens.dashboard

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
class DashboardViewModel(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                routineRepository.getActiveMicrocycle(),
                workoutRepository.getActiveSession()
            ) { microcycle, session ->
                Pair(microcycle, session)
            }
                .flatMapLatest { (microcycle, session) ->
                    if (session != null) {
                        combine(
                            routineRepository.getRoutineDay(session.dayId),
                            workoutRepository.getSetsForSession(session.sessionId)
                        ) { day, sets ->

                            val historicalSets = mutableMapOf<Long, List<WorkoutSet>>()
                            day?.slots?.forEach { slot ->
                                historicalSets[slot.slotId] = workoutRepository.getLastSetsForSlot(
                                    slotId = slot.slotId,
                                    dayId = day.dayId,
                                    targetSets = slot.targetSets
                                )
                            }

                            DashboardUiState(
                                isLoading = false,
                                activeMicrocycle = microcycle,
                                activeSession = session,
                                activeDay = day,
                                sessionSets = sets,
                                lastSets = historicalSets
                            )
                        }
                    } else {
                        flowOf(
                            DashboardUiState(
                                isLoading = false,
                                activeMicrocycle = microcycle,
                                activeSession = null,
                                activeDay = null,
                                sessionSets = emptyList(),
                                lastSets = emptyMap()
                            )
                        )
                    }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun startWorkout(dayId: Long) {
        viewModelScope.launch { workoutRepository.startSession(dayId) }
    }

    fun endWorkout() {
        val sessionId = _uiState.value.activeSession?.sessionId ?: return
        viewModelScope.launch { workoutRepository.endSession(sessionId) }
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
                DashboardViewModel(
                    routineRepository = app.overloadProvider.provideRoutineRepository(),
                    workoutRepository = app.overloadProvider.provideWorkoutRepository()
                )
            }
        }
    }
}