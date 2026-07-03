package com.pdm0126.overload.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "microcycles_table")
data class MicrocycleEntity(
    @PrimaryKey(autoGenerate = true)
    val microcycleId: Long = 0,
    val name: String,           // ej. "Rutina de Fuerza - 2026"
    val blueprintType: String,  // ej. "PPL", "Arnold Split" (Solo informativo para la UI)
    val isActive: Boolean = false
)