package com.pdm0126.overload.domain.model

// Representa un punto en la gráfica de un ejercicio específico
data class ExerciseVolumeRecord(
    val timestamp: Long,
    val totalVolume: Float
)

// Representa un punto en la gráfica de un grupo muscular
data class MuscleProgressionRecord(
    val timestamp: Long,
    val muscleGroup: String,
    val effectiveVolume: Float
)

// Opcional: Para una gráfica de pastel (Distribución de volumen por músculo en general)
data class MuscleDistribution(
    val muscleGroup: String,
    val totalEffectiveVolume: Float
)