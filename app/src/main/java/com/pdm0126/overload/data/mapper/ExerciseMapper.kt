package com.pdm0126.overload.data.mapper

import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.data.remote.dto.ExerciseDto
import com.pdm0126.overload.domain.TechnicalDictionary
import com.pdm0126.overload.domain.model.Exercise

fun ExerciseEntity.toDomainModel() : Exercise {
    return Exercise(
        id = exerciseId,
        name = name,
        muscleGroup = mainMuscleGroup,
        mechanic = mechanic,
        targetMuscles = targetMuscles,
        secondaryMuscles = secondaryMuscles,
        equipment = equipment,
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
        equipment = equipment,
        instructions = instructions,
        remoteImagesUrls = remoteImages
    )
}

fun ExerciseDto.toDomainModel(): Exercise {
    val baseUrl = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"
    val generatedId = "ex_${name.trim().lowercase().replace(" ", "_").replace("-", "_")}"

    return Exercise(
        id = generatedId,
        name = name,
        muscleGroup = TechnicalDictionary.getGeneralGroup(primaryMuscles.first()),
        mechanic = TechnicalDictionary.getMechanic(mechanic),
        targetMuscles = primaryMuscles.map { muscle -> TechnicalDictionary.getSpecificMuscle(muscle) },
        secondaryMuscles = secondaryMuscles.map {muscle -> TechnicalDictionary.getSpecificMuscle(muscle) },
        equipment = TechnicalDictionary.getEquipment(equipment),
        instructions = instructions,
        remoteImages = images.map { "$baseUrl$it" }
    )
}