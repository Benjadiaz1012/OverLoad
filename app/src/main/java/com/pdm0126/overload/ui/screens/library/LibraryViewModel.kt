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

/*private data class SearchState(
    val query: String = "",
    val isSearching: Boolean = false,
    val remoteResults: List<Exercise> = emptyList(),
    val errorMessage: String? = null
)*/

class LibraryViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _selectedMuscle = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)
    private val _remoteResults = MutableStateFlow<List<Exercise>>(emptyList())
    private val _errorMessage = MutableStateFlow<String?>(null)


    val uiState: StateFlow<LibraryUiState> = combine(
        repository.getAllExercises(),
        _selectedMuscle,
        combine(
            _searchQuery, _isSearching, _remoteResults, _errorMessage
        ) { query, searching, remote, error ->
            SearchState(query, searching, remote, error)
        }
    ) { local, muscle, searchState ->

        val filteredLocal = if (muscle == null) local else local.filter {
            it.muscleGroup.equals(muscle, ignoreCase = true)
        }

        LibraryUiState(
            localExercises = filteredLocal,
            selectedMuscle = muscle,
            searchState = searchState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _remoteResults.value = emptyList()
            _errorMessage.value = null
        }
    }

    fun onMuscleFilterSelected(muscle: String) {
        _selectedMuscle.value = if (_selectedMuscle.value == muscle) null else muscle
    }

    fun searchRemoteExercises() {
        if (_searchQuery.value.isBlank()) return

        viewModelScope.launch {
            _isSearching.value = true
            _errorMessage.value = null
            try {
                val results = repository.searchRemoteExercises(_searchQuery.value)
                _remoteResults.value = results
                if (results.isEmpty()) _errorMessage.value = "No se encontraron ejercicios con ese nombre."
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión. Verifica tu internet."
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun saveExerciseToLocal(exercise: Exercise) {
        viewModelScope.launch {
            repository.saveRemoteExerciseToLocal(exercise)
            _remoteResults.value = _remoteResults.value.filterNot { it.id == exercise.id }
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