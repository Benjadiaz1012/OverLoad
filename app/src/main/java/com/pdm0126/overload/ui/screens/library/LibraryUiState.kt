package com.pdm0126.overload.ui.screens.library

import com.pdm0126.overload.domain.model.Exercise

data class LibraryUiState(
    val selectedTabIndex: Int = 0,
    val localExercises: List<Exercise> = emptyList(),
    val localExercisesIds: Set<String> = emptySet(),
    val selectedMuscle: String? = null,
    val query: String = "",
    val remoteState: RemoteState = RemoteState()
)

data class RemoteState(
    val isLoading: Boolean = false,
    val results: List<Exercise> = emptyList(),
    val errorMessage: String? = null
)


