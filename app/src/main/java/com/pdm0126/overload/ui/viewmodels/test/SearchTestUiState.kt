package com.pdm0126.overload.ui.viewmodels.test

import com.pdm0126.overload.domain.model.Exercise

sealed interface SearchTestUiState {
    object Idle : SearchTestUiState
    object Loading : SearchTestUiState
    data class Success(val exercises: List<Exercise>) : SearchTestUiState
    data class Error(val message: String) : SearchTestUiState
}

