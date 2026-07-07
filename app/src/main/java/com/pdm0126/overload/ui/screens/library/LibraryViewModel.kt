package com.pdm0126.overload.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RemoteState(
    val isLoading: Boolean = false,
    val results: List<Exercise> = emptyList(),
    val errorMessage: String? = null
)

data class LibraryUiState(
    val selectedTabIndex: Int = 0,
    val localExercises: List<Exercise> = emptyList(),
    val localExercisesIds: Set<String> = emptySet(),
    val selectedMuscles: List<String> = emptyList(),
    val selectedMechanic: String? = null,
    val query: String = "",
    val remoteState: RemoteState = RemoteState()
)

class LibraryViewModel(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _selectedMuscles = MutableStateFlow<List<String>>(emptyList())
    private val _selectedMechanic = MutableStateFlow<String?>(null)
    private val _query = MutableStateFlow("")
    private val _remoteState = MutableStateFlow(RemoteState())

    private val _filtersFlow = combine(_selectedMuscles, _selectedMechanic) { muscles, mechanic ->
        muscles to mechanic
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        exerciseRepository.getLocalExercises(),
        _selectedTabIndex,
        _filtersFlow,
        _query,
        _remoteState
    ) { local, tabIndex, filters, query, remote ->

        val (muscles, mechanic) = filters

        val filteredLocal = local.filter { exercise ->
            matchesFilters(exercise, muscles, mechanic, query)
        }

        val filteredRemote = remote.results.filter { exercise ->
            matchesFilters(exercise, muscles, mechanic, query)
        }

        LibraryUiState(
            selectedTabIndex = tabIndex,
            localExercises = filteredLocal,
            localExercisesIds = filteredLocal.map { it.id }.toSet(),
            selectedMuscles = muscles,
            selectedMechanic = mechanic,
            query = query,
            remoteState = remote.copy(results = filteredRemote)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState()
    )

    private fun matchesFilters(
        exercise: Exercise,
        muscles: List<String>,
        mechanic: String?,
        query: String
    ): Boolean {
        val matchesMuscles = muscles.isEmpty() || muscles.any { muscle ->
            exercise.muscleGroup.equals(muscle, ignoreCase = true)
        }
        val matchesMechanic = mechanic == null || exercise.mechanic.equals(mechanic, ignoreCase = true)
        val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
        return matchesMuscles && matchesMechanic && matchesQuery
    }

    fun onTabSelected(index: Int) {
        if (_selectedTabIndex.value != index) {
            _selectedTabIndex.value = index
            if (index == 1 && _query.value.isNotBlank()) {
                searchRemoteExercises()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _query.value = query
        if (query.isBlank() && _selectedTabIndex.value == 1) {
            _remoteState.value = RemoteState()
        }
    }

    fun onMuscleFilterSelected(muscle: String?) {
        if (muscle == null) {
            _selectedMuscles.value = emptyList()
            return
        }
        val current = _selectedMuscles.value.toMutableList()
        if (current.contains(muscle)) current.remove(muscle) else current.add(muscle)
        _selectedMuscles.value = current
    }

    fun onMechanicFilterSelected(mechanic: String?) {
        _selectedMechanic.value = if (_selectedMechanic.value == mechanic) null else mechanic
    }

    fun searchRemoteExercises() {
        if (_query.value.isBlank() || _selectedTabIndex.value != 1) return

        viewModelScope.launch {
            _remoteState.value = RemoteState(isLoading = true)

            exerciseRepository.getRemoteExercises(_query.value)
                .onSuccess { remoteExercises ->
                    _remoteState.value = if (remoteExercises.isEmpty()) {
                        RemoteState(errorMessage = "No se encontraron resultados")
                    } else {
                        RemoteState(results = remoteExercises)
                    }
                }
                .onFailure { error ->
                    val friendlyMessage = if (error is java.io.IOException) {
                        "Sin conexión a internet. Revisa tu red e intenta de nuevo."
                    } else {
                        "Ocurrió un error al buscar. Intenta de nuevo."
                    }
                    _remoteState.value = RemoteState(errorMessage = friendlyMessage)
                }
        }
    }

    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            if (!uiState.value.localExercisesIds.contains(exercise.id)) {
                exerciseRepository.saveRemoteExerciseToLocal(exercise)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                LibraryViewModel(app.overloadProvider.provideExerciseRepository())
            }
        }
    }
}