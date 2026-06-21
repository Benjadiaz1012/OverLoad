package com.pdm0126.overload

object TechnicalDictionary {

    private data class MuscleInfo(
        val specificMuscle: String,
        val generalGroup: String
    )

    val mainMuscleGroupsList = listOf(
        "Pecho", "Hombros", "Espalda", "Tríceps", "Bíceps",
        "Pierna", "Gemelos", "Antebrazos", "Abdominales"
    )

    private val muscleMap = mapOf(
        "chest" to MuscleInfo("Pecho", "Pecho"),
        "shoulders" to MuscleInfo("Hombros", "Hombros"),
        "triceps" to MuscleInfo("Tríceps", "Tríceps"),
        "biceps" to MuscleInfo("Bíceps", "Bíceps"),
        "lats" to MuscleInfo("Dorsales", "Espalda"),
        "middle back" to MuscleInfo("Espalda media", "Espalda"),
        "lower back" to MuscleInfo("Espalda baja", "Espalda"),
        "quadriceps" to MuscleInfo("Cuadríceps", "Pierna"),
        "hamstrings" to MuscleInfo("Isquiotibiales", "Pierna"),
        "glutes" to MuscleInfo("Glúteos", "Pierna"),
        "calves" to MuscleInfo("Pantorrillas", "Gemelos"),
        "forearms" to MuscleInfo("Antebrazos", "Antebrazos"),
        "traps" to MuscleInfo("Trapecios", "Espalda"),
        "abdominals" to MuscleInfo("Abdominales", "Abdominales"),
        "abs" to MuscleInfo("Abdominales", "Abdominales"),
        "adductors" to MuscleInfo("Aductores", "Pierna"),
        "abductors" to MuscleInfo("Abductores", "Pierna"),
        "neck" to MuscleInfo("Cuello", "Espalda")
    )

    private val equipmentMap = mapOf(
        "barbell" to "Barra",
        "dumbbell" to "Mancuernas",
        "body only" to "Peso corporal",
        "machine" to "Máquina",
        "cable" to "Polea",
        "medicine ball" to "Pelota medicinal",
        "bands" to "Bandas de resistencia",
        "e-z curl bar" to "Barra EZ",
        "kettlebells" to "Pesas rusas",
        "exercise ball" to "Pelota de ejercicio",
        "foam roll" to "Rollo de foam",
        "other" to "Otro"
    )

    fun getGeneralGroup(apiMuscle: String?): String {
        val key = apiMuscle?.lowercase() ?: ""
        return muscleMap[key]?.generalGroup ?: "Otros"
    }

    fun getSpecificMuscle(apiMuscle: String): String {
        val key = apiMuscle.lowercase()
        return muscleMap[key]?.specificMuscle ?: apiMuscle
    }

    fun getEquipment(apiEquipment: String?): String {
        val key = apiEquipment?.lowercase() ?: ""
        return equipmentMap[key] ?: apiEquipment ?: "Ninguno"
    }

    fun getMechanic(apiMechanic: String?): String {
        return when (apiMechanic?.lowercase()) {
            "compound" -> "Compuesto"
            "isolation" -> "Aislamiento"
            else -> "N/A"
        }
    }
}