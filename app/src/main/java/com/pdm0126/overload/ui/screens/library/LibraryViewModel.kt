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

class LibraryViewModel(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _selectedMuscles = MutableStateFlow<List<String>>(emptyList())
    private val _selectedMechanic = MutableStateFlow<String?>(null)
    private val _query = MutableStateFlow("")
    private val _remoteState = MutableStateFlow(RemoteState())

    private val _filtersFlow = combine(_selectedMuscles, _selectedMechanic) { muscle, mechanic ->
        Pair(muscle, mechanic)
    }
    val uiState: StateFlow<LibraryUiState> = combine(
        exerciseRepository.getLocalExercises(),           // Flow<List<Exercise>>
        _selectedTabIndex,                               // MutableStateFlow<Int>
        _filtersFlow,                                 // MutableStateFlow<String?>
        _query,                                          // MutableStateFlow<String>
        _remoteState                                     // MutableStateFlow<RemoteState>
    ) { local, tabIndex, filters, query, remote ->

        val (muscles, mechanic) = filters
        val filteredLocal = local.filter { exercise ->
            val matchesMuscles = muscles.isEmpty() || muscles.any { muscle -> exercise.muscleGroup.equals(muscle, ignoreCase = true) }
            val matchesMechanic = mechanic == null || exercise.mechanic.equals(mechanic, ignoreCase = true)
            val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
            matchesMuscles && matchesMechanic && matchesQuery
        }

        val filteredRemote = remote.results.filter { exercise ->
            val matchesMuscles = muscles.isEmpty() || muscles.any { muscles -> exercise.muscleGroup.equals(muscles, ignoreCase = true) }
            val matchesMechanic = mechanic == null || exercise.mechanic.equals(mechanic, ignoreCase = true)
            val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
            matchesMuscles && matchesMechanic && matchesQuery
        }

        val localExercisesIds = filteredLocal.map { ex -> ex.id }.toSet()

        LibraryUiState(
            selectedTabIndex = tabIndex,
            localExercises = filteredLocal,
            localExercisesIds = localExercisesIds,
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

    fun onTabSelected(index: Int) {
        if (_selectedTabIndex.value != index) {
            _selectedTabIndex.value = index
        }
    }

    fun onSearchQueryChanged(query: String) {
        _query.value = query
        if (query.isBlank() && _selectedTabIndex.value == 1) {
            _remoteState.value = RemoteState() // Limpiar resultados de búsqueda
        }
    }

    fun onMuscleFilterSelected(muscle: String?) {
        if (muscle == null) {
            _selectedMuscles.value = emptyList()
        } else {
            val currentMuscles = _selectedMuscles.value.toMutableList()
            if (currentMuscles.contains(muscle)) {
                currentMuscles.remove(muscle)
            } else {
                currentMuscles.add(muscle)
            }
            _selectedMuscles.value = currentMuscles
        }
    }
    fun onMechanicFilterSelected(mechanic: String?) {
        _selectedMechanic.value = if (_selectedMechanic.value == mechanic) null else mechanic
    }

    fun searchRemoteExercises() {
        if (_query.value.isBlank() || _selectedTabIndex.value != 1)
            return
        viewModelScope.launch {
            _remoteState.value = RemoteState(isLoading = true, errorMessage = null)
            exerciseRepository.getRemoteExercises(_query.value)
                .onSuccess { remoteExercises ->
                    if (remoteExercises.isEmpty()) {
                        _remoteState.value = RemoteState(
                            errorMessage = "No se encontraron resultados",
                            isLoading = false
                        )
                    } else {
                        _remoteState.value = RemoteState(
                            results = remoteExercises,
                            errorMessage = null,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _remoteState.value = RemoteState(
                        errorMessage = error.message ?: "Error desconocido",
                        isLoading = false
                    )
                }
        }
    }

    // Para el selection mode
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