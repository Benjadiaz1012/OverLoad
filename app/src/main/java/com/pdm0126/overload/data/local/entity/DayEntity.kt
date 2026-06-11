package com.pdm0126.overload.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "days_table",
    foreignKeys = [
        ForeignKey(
            entity = MicrocycleEntity::class,
            parentColumns = ["microcycleId"],
            childColumns = ["microcycleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("microcycleId")]
)
data class DayEntity(
    @PrimaryKey(autoGenerate = true)
    val dayId: Long = 0,
    val microcycleId: Long,
    val order: Int,         // Dato posicional (Día 1, Día 2... Máximo 6)
    val focus: String       // Etiqueta visual elegida por el usuarip: "Push", "Pull", "Legs", "Arm Day", etc.
)

