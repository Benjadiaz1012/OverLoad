package com.pdm0126.overload.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "exercises_table")
data class ExerciseEntity(
    @PrimaryKey
    val exerciseId: String,
    val name: String,
    val localImagePath: String,
    val mainMuscleGroup: String,
    val mechanic: String,
    val targetMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val equipments: List<String>,
    val instructions: List<String>,
    val remoteImagesUrls: List<String>
)

