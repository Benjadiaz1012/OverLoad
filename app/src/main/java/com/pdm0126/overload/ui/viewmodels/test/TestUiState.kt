package com.pdm0126.overload.ui.viewmodels.test

import com.pdm0126.overload.domain.model.Exercise

sealed interface TestUiState {
    object Loading : TestUiState
    data class Success(val exercises: List<Exercise>) : TestUiState
    data class Error(val message: String) : TestUiState
}

