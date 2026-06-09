package com.pdm0126.overload.data.mapper

import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.domain.model.Exercise

fun ExerciseEntity.toDomainModel() : Exercise {
    return Exercise(
        id = exerciseId,
        name = name,
        imagePath = localImagePath,
        muscleGroup = mainMuscleGroup,
        mechanic = mechanic,
        targetMuscles = targetMuscles,
        secondaryMuscles = secondaryMuscles,
        equipments = equipments,
        instructions = instructions,
        remoteImages = remoteImagesUrls
    )
}

// Para ejercicios externos
fun Exercise.toEntity() : ExerciseEntity {
    return ExerciseEntity(
        exerciseId = id,
        name = name,
        localImagePath = imagePath,
        mainMuscleGroup = muscleGroup,
        mechanic = mechanic,
        targetMuscles = targetMuscles,
        secondaryMuscles = secondaryMuscles,
        equipments = equipments,
        instructions = instructions,
        remoteImagesUrls = remoteImages
    )
}