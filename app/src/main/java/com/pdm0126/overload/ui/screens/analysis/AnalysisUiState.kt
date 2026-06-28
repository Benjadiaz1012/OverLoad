package com.pdm0126.overload.ui.screens.analysis

import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.model.ExerciseVolumeRecord
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.model.MuscleProgressionRecord

data class AnalysisUiState(
    val isLoading: Boolean = true,

    val muscleDistribution: List<MuscleDistribution> = emptyList(),

    // Filtro y Gráfica Micro (Líneas)
    val availableExercises: List<Exercise> = emptyList(), // NUEVO: Para el menú desplegable
    val selectedExerciseId: String? = null,
    val exerciseProgression: List<ExerciseVolumeRecord> = emptyList(),

    // (Opcional para el futuro) Progreso por músculo
    val selectedMuscle: String? = null,
    val muscleProgression: List<MuscleProgressionRecord> = emptyList()
)