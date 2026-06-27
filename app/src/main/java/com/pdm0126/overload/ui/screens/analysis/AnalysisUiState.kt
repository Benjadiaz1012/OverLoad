package com.pdm0126.overload.ui.screens.analysis

import com.pdm0126.overload.domain.model.ExerciseVolumeRecord
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.model.MuscleProgressionRecord

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val muscleDistribution: List<MuscleDistribution> = emptyList(),
    val selectedMuscle: String? = null,
    val muscleProgression: List<MuscleProgressionRecord> = emptyList(),
    val selectedExerciseId: String? = null,
    val exerciseProgression: List<ExerciseVolumeRecord> = emptyList()
)
