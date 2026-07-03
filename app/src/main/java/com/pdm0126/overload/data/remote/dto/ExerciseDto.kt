package com.pdm0126.overload.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseDto(
    val name: String,
    val primaryMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val equipment: String? = null,
    val mechanic: String? = null,
    val instructions: List<String> = emptyList(),
    val images: List<String> = emptyList()
)