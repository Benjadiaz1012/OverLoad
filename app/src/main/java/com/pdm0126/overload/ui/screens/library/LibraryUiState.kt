package com.pdm0126.overload.ui.screens.library

import com.pdm0126.overload.domain.model.Exercise

data class LibraryUiState(
    val selectedTabIndex: Int = 0,
    val localExercises: List<Exercise> = emptyList(),
    val savedExercisesIds: Set<String> = emptySet(),
    val selectedMuscle: String? = null,
    val searchQuery: String = "",
    val remoteState: RemoteState = RemoteState()
)

data class RemoteState(
    val isSearching: Boolean = false,
    val results: List<Exercise> = emptyList(),
    val errorMessage: String? = null
)


