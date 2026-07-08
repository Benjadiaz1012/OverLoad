package com.pdm0126.overload.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines_table")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val routineId: Long = 0,
    val name: String,
    val blueprintType: String,
    val isActive: Boolean = false
)