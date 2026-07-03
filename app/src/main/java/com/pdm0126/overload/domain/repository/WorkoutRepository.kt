package com.pdm0126.overload.domain.repository

import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    // Flujo reactivo de la sesión activa (sin endTimestamp). null si no hay sesión en curso.
    fun getActiveSession(): Flow<WorkoutSession?>

    // Flujo reactivo de todas las series de una sesión (para el estado en vivo de la UI)
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>>

    // Flujo reactivo de las series de un slot específico dentro de la sesión activa
    fun getSetsBySlotAndSession(slotId: Long, sessionId: Long): Flow<List<WorkoutSet>>

    // Recupera la referencia histórica de las series del slot en la sesión anterior del mismo día
    suspend fun getLastSetsForExercise(exerciseId: String): List<WorkoutSet>

    // Crea e inicia una nueva sesión para el día indicado. Retorna el sessionId generado.
    suspend fun startSession(dayId: Long): Long

    // Marca la sesión como completada guardando el endTimestamp actual
    suspend fun endSession(sessionId: Long)

    // Registra una serie individual. Aplica la lógica de rirFactor internamente.
    suspend fun logSet(
        sessionId: Long,
        slotId: Long?,
        exerciseId: String,
        setNumber: Int,
        weightKg: Float,
        reps: Int,
        rir: Int?,
        isRirEnabled: Boolean
    ): Long

    // Elimina una serie (corrección de errores del usuario durante la sesión)
    suspend fun deleteSet(setId: Long)
}
