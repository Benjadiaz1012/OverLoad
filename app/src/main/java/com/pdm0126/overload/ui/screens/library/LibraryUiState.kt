package com.pdm0126.overload.ui.screens.library

import com.pdm0126.overload.domain.model.Exercise

data class LibraryUiState(
    // Local
    val localExercises: List<Exercise> = emptyList(),
    val selectedMuscle: String? = null,
    // Remoto
    /*val searchQuery: String = "",
    val isSearching: Boolean = false,
    val remoteResults: List<Exercise> = emptyList(),
    val errorMessage: String? = null*/
    val searchState: SearchState = SearchState()
)

data class SearchState(
    val query: String = "",
    val isSearching: Boolean = false,
    val remoteResults: List<Exercise> = emptyList(),
    val errorMessage: String? = null
)


