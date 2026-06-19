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
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _selectedMuscle = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)
    private val _remoteState = MutableStateFlow(RemoteState())


    val uiState: StateFlow<LibraryUiState> = combine(
        repository.getAllExercises(),           // Flow<List<Exercise>>
        _selectedTabIndex,                     // MutableStateFlow<Int>
        _selectedMuscle,                       // MutableStateFlow<String?>
        _searchQuery,                          // MutableStateFlow<String>
        _remoteState                           // MutableStateFlow<RemoteState>
    ) { local, tabIndex, muscle, query, remote ->

        val filteredLocal = local.filter { exercise ->
            val matchesMuscle = muscle == null || exercise.muscleGroup.equals(muscle, ignoreCase = true)
            val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
            matchesMuscle && matchesQuery
        }

        val filteredRemote = remote.results.filter { exercise ->
            muscle == null || exercise.muscleGroup.equals(muscle, ignoreCase = true)
        }

        LibraryUiState(
            selectedTabIndex = tabIndex,
            localExercises = filteredLocal,
            savedExercisesIds = local.map { ex -> ex.id }.toSet(),
            selectedMuscle = muscle,
            searchQuery = query,
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
            _selectedMuscle.value = ""
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank() && _selectedTabIndex.value == 1) {
            _remoteState.value = RemoteState() // Limpiar resultados de búsqueda
        }
    }

    fun onMuscleFilterSelected(muscle: String) {
        _selectedMuscle.value = if (_selectedMuscle.value == muscle) null else muscle
    }

    fun searchRemoteExercises() {
        if (_searchQuery.value.isBlank() || _selectedTabIndex.value != 1)
            return
        viewModelScope.launch {
            _remoteState.value = RemoteState(isSearching = true, errorMessage = null)
            try {
                val results = repository.searchRemoteExercises(_searchQuery.value)
                if (results.isEmpty()) {
                    _remoteState.value = RemoteState(errorMessage = "No se encontraron resultados.")
                } else {
                    _remoteState.value = RemoteState(results = results)
                }
            } catch (e: Exception) {
                _remoteState.value = RemoteState(errorMessage = "Error de conexión. Verifica tu internet")
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun saveExerciseToLocal(exercise: Exercise) {
        viewModelScope.launch {
            repository.saveRemoteExerciseToLocal(exercise)
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