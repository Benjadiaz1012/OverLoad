package com.pdm0126.overload.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "slots_table",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["dayId"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE // Si se remueve un ejercicio, se limpia el slot
        )
    ],
    indices = [Index("dayId"), Index("exerciseId")]
)
data class SlotEntity(
    @PrimaryKey(autoGenerate = true)
    val slotId: Long = 0,
    val dayId: Long,
    val exerciseId: String,   // Referencia directa a cualquier ejercicio del catálogo
    val order: Int,           // Orden secuencial dentro del día (1 a 12)
    val targetSets: Int       // Objetivo de series (entre 1 y 6)
)

