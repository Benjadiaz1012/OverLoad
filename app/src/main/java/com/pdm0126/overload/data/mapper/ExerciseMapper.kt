package com.pdm0126.overload.data.mapper

import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.data.remote.dto.ExerciseDto
import com.pdm0126.overload.domain.model.Exercise

fun ExerciseEntity.toDomainModel() : Exercise {
    return Exercise(
        id = exerciseId,
        name = name,
        muscleGroup = mainMuscleGroup,
        mechanic = mechanic,
        targetMuscles = targetMuscles,
        secondaryMuscles = secondaryMuscles,
        equipments = equipments,
        instructions = instructions,
        remoteImages = remoteImagesUrls
    )
}

// Para guardar ejercicios externos
fun Exercise.toEntity() : ExerciseEntity {
    return ExerciseEntity(
        exerciseId = id,
        name = name,
        mainMuscleGroup = muscleGroup,
        mechanic = mechanic,
        targetMuscles = targetMuscles,
        secondaryMuscles = secondaryMuscles,
        equipments = equipments,
        instructions = instructions,
        remoteImagesUrls = remoteImages
    )
}

fun ExerciseDto.toDomainModel(): Exercise {
    val baseUrl = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

    val generatedId = "ex_${name.trim().lowercase().replace(" ", "_").replace("-", "_")}"
    val mappedMuscle = when(primaryMuscles.firstOrNull()?.lowercase()) {
        "chest" -> "Pecho"
        "middle back", "lats", "lower back" -> "Espalda"
        "shoulders" -> "Hombros"
        "quadriceps", "hamstrings", "calves", "glutes", "upper legs", "lower legs" -> "Pierna"
        "biceps" -> "Bíceps"
        "triceps" -> "Tríceps"
        else -> "General"
    }
    return Exercise(
        id = generatedId,
        name = name,
        muscleGroup = mappedMuscle,
        mechanic = if (mechanic?.lowercase() == "compound") "Compuesto" else "Aislamiento",
        targetMuscles = primaryMuscles,
        secondaryMuscles = secondaryMuscles,
        equipments = if (equipment != null) listOf(equipment) else emptyList(),
        instructions = instructions,
        remoteImages = images.map { "$baseUrl$it" }
    )
}