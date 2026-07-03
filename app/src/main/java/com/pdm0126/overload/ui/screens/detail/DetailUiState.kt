package com.pdm0126.overload.ui.screens.detail

import com.pdm0126.overload.domain.model.Exercise

data class DetailUiState(
    val isLoading: Boolean = true,
    val exercise: Exercise? = null,
    val isBookmarked: Boolean = false,
    val errorMessage: String? = null
)
