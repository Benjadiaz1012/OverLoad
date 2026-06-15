package com.pdm0126.overload.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets_table",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE // Si borras la sesión, sí se borran sus series
        ),
        ForeignKey(
            entity = SlotEntity::class,
            parentColumns = ["slotId"],
            childColumns = ["slotId"],
            onDelete = ForeignKey.SET_NULL // Si borran el slot, la serie sobrevive
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE // Protege la integridad del ejercicio
        )
    ],
    indices = [Index("sessionId"), Index("slotId"), Index("exerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val setId: Long = 0,
    val sessionId: Long,        // Sesión a la que pertenece esta serie
    val slotId: Long?,          // Slot de rutina que se está ejecutando
    val exerciseId: String,       // Ejercicio único al que pertenece esta serie
    val setNumber: Int,         // Número ordinal de la serie dentro del slot (1, 2, 3…)
    val weightKg: Float,        // Peso utilizado en kilogramos
    val reps: Int,              // Repeticiones completadas
    val rir: Int? = null,       // Repeticiones en reserva (0–5); null si RIR está desactivado
    val isRirEnabled: Boolean,  // true = el usuario midió el RIR; false = reps fijas
    val rirFactor: Float        // Factor calculado y persistido para uso directo en el Módulo 4
)
