package com.pdm0126.overload.Interfaz.components

import com.pdm0126.overload.domain.model.Exercise


object ExerciseNavCache {
    private val cache = mutableMapOf<String, Exercise>()

    fun put(exercise: Exercise) {
        cache[exercise.id] = exercise
    }

    fun get(exerciseId: String): Exercise? = cache[exerciseId]
}