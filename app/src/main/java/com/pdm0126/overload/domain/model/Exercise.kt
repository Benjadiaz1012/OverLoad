package com.pdm0126.overload.domain.model

data class Exercise(
    val id : String,
    val name : String,
    val muscleGroup : String,
    val mechanic : String,
    val targetMuscles : List<String>,
    val secondaryMuscles : List<String>,
    val equipments : List<String>,
    val instructions : List<String>,
    val remoteImages : List<String>
)
