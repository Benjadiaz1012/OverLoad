package com.pdm0126.overload.ui.viewmodels.test

import com.pdm0126.overload.domain.model.Exercise

sealed interface InitTestUiState {
    object Loading : InitTestUiState
    data class Success(val exercises: List<Exercise>) : InitTestUiState
    data class Error(val message: String) : InitTestUiState
}

