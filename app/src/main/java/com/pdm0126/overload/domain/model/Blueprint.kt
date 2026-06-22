package com.pdm0126.overload.domain.model

enum class ExperienceLevel(val label: String) {
    BEGINNER("Principiante"),
    INTERMEDIATE("Intermedio"),
    ADVANCED("Avanzado"),
    ANY("Cualquiera")
}

enum class TrainingGoal(val label: String) {
    HYPERTROPHY("Hipertrofia"),
    STRENGTH("Fuerza"),
    CONDITIONING("Acondicionamiento"),
    CUSTOM("Personalizado")
}

data class Blueprint(
    val id: String,
    val name: String,
    val description: String,
    val defaultDays: List<String>,

    // Cambiamos Strings por valores numéricos que la lógica de la app pueda entender
    val minMicrocycleDays: Int, // Mínimo recomendado para un microciclo
    val maxMicrocycleDays: Int, // Máximo recomendado para un microciclo
    val maxFrequencyPerMuscle: Int, // Máxima frecuencia considerando el máximo recomendado de días por microciclo

    // Uso de Enums
    val level: ExperienceLevel,
    val goal: TrainingGoal,

    // NUEVO: Arrays para guiar al usuario visualmente en la UI
    val tags: List<String> = emptyList(),
    val pros: List<String> = emptyList(),
    val considerations: List<String> = emptyList()
) {
    // Helper para la UI: devuelve un texto formateado si lo necesitas pintar directo
    val formattedMicrocycle: String
        get() = if (minMicrocycleDays == maxMicrocycleDays) "$minMicrocycleDays días"
        else "De $minMicrocycleDays a $maxMicrocycleDays días"
}

object BlueprintCatalog {

    val systems = listOf(
        Blueprint(
            id = "ppl",
            name = "Push / Pull / Legs",
            description = "Divide el cuerpo por patrones de movimiento: empujes (pecho, hombro, tríceps), tracciones (espalda, bíceps) y piernas. Es el estándar de oro actual para estética y fuerza.",
            defaultDays = listOf("Push", "Pull", "Legs"),
            minMicrocycleDays = 3,
            maxMicrocycleDays = 6,
            maxFrequencyPerMuscle = 2,
            level = ExperienceLevel.INTERMEDIATE,
            goal = TrainingGoal.HYPERTROPHY,
            tags = listOf("Equilibrado", "Flexible", "Popular"),
            pros = listOf(
                "Excelente separación de grupos musculares",
                "Evita la fatiga residual de un día para otro",
                "Fácil de adaptar a 3, 4, 5 o 6 días por semana"
            ),
            considerations = listOf(
                "Hacerlo 6 días (PPL-PPL-Descanso) requiere muy buena nutrición y descanso",
                "El día de pierna puede volverse muy exhaustivo"
            )
        ),

        Blueprint(
            id = "arnold",
            name = "Arnold Split",
            description = "La división legendaria de la era dorada. Agrupa músculos antagonistas (pecho y espalda juntos) para un bombeo masivo, dejando los brazos y hombros para su propio día.",
            defaultDays = listOf("Pecho & Espalda", "Hombros & Brazos", "Piernas"),
            minMicrocycleDays = 6,
            maxMicrocycleDays = 6,
            maxFrequencyPerMuscle = 2,
            level = ExperienceLevel.ADVANCED,
            goal = TrainingGoal.HYPERTROPHY,
            tags = listOf("Alto Volumen", "Bombeo Extremo", "Clásico"),
            pros = listOf(
                "Prioriza fuertemente el desarrollo de los brazos y hombros",
                "Trabajar músculos antagonistas permite superseries muy efectivas"
            ),
            considerations = listOf(
                "Altamente demandante; requiere dedicar 6 días a la semana",
                "El día de Pecho/Espalda es brutal y requiere excelente capacidad cardiovascular"
            )
        ),

        Blueprint(
            id = "upper_lower",
            name = "Torso / Pierna",
            description = "Alterna el cuerpo en dos mitades. Es matemáticamente una de las rutinas más eficientes para el atleta natural que busca ganar fuerza e hipertrofia.",
            defaultDays = listOf("Torso Fuerza", "Pierna Fuerza", "Torso Hipertrofia", "Pierna Hipertrofia"),
            minMicrocycleDays = 4,
            maxMicrocycleDays = 4,
            maxFrequencyPerMuscle = 2,
            level = ExperienceLevel.INTERMEDIATE,
            goal = TrainingGoal.STRENGTH,
            tags = listOf("Eficiente", "Fuerza y Tamaño", "Atlético"),
            pros = listOf(
                "Frecuencia 2 perfecta para maximizar la síntesis proteica",
                "Permite programar días pesados (fuerza) y días ligeros (hipertrofia)",
                "Deja 3 días libres a la semana para recuperar o hacer cardio"
            ),
            considerations = listOf(
                "Los entrenamientos de Torso pueden ser largos por la cantidad de músculos involucrados",
                "Menos enfoque directo en brazos y gemelos si no se añade trabajo extra"
            )
        ),

        Blueprint(
            id = "full_body",
            name = "Full Body",
            description = "Entrena el cuerpo completo en cada sesión utilizando principalmente ejercicios multiarticulares (sentadillas, dominadas, press).",
            defaultDays = listOf("Full Body A", "Full Body B", "Full Body C"),
            minMicrocycleDays = 2,
            maxMicrocycleDays = 3,
            maxFrequencyPerMuscle = 3,
            level = ExperienceLevel.BEGINNER,
            goal = TrainingGoal.CONDITIONING,
            tags = listOf("Básicos", "Principiantes", "Ahorro de Tiempo"),
            pros = listOf(
                "Ideal para dominar la técnica de los ejercicios fundamentales",
                "Altísimo gasto calórico por sesión",
                "Si faltas al gimnasio un día, no arruinas la programación semanal"
            ),
            considerations = listOf(
                "Poco espacio para ejercicios de aislamiento (bíceps, tríceps, etc.)",
                "Puede causar mucha fatiga sistémica si se entrena muy pesado"
            )
        ),

        Blueprint(
            id = "heavy_duty",
            name = "Heavy Duty (HIT)",
            description = "Sistema de alta intensidad llevado a la fama por Mike Mentzer. Volumen mínimo y descansos prolongados, llevando una única serie de trabajo al fallo absoluto y más allá.",
            defaultDays = listOf("Pecho & Espalda", "Piernas & Abs", "Hombros & Brazos"),
            minMicrocycleDays = 3, // Se suele entrenar 1 día sí, 1 o 2 no.
            maxMicrocycleDays = 4,
            maxFrequencyPerMuscle = 1,
            level = ExperienceLevel.ADVANCED,
            goal = TrainingGoal.HYPERTROPHY,
            tags = listOf("Intensidad Extrema", "Bajo Volumen", "Al Fallo"),
            pros = listOf(
                "Ideal para quienes se estancan por sobreentrenamiento",
                "Las sesiones son extremadamente cortas (30-40 minutos max)",
                "Desarrolla una fuerza mental y tolerancia al esfuerzo excepcionales"
            ),
            considerations = listOf(
                "Exige un calentamiento perfecto para evitar lesiones",
                "Requiere saber fallar de verdad (fallo concéntrico, isométrico y excéntrico)",
                "Mentalmente agotador; requiere total concentración en cada repetición"
            )
        ),

        Blueprint(
            id = "blank",
            name = "Lienzo en Blanco",
            description = "Construye tu propio microciclo desde cero, definiendo tus propios días y agrupaciones.",
            defaultDays = emptyList(),
            minMicrocycleDays = 1,
            maxMicrocycleDays = 9,
            maxFrequencyPerMuscle = 0,
            level = ExperienceLevel.ANY,
            goal = TrainingGoal.CUSTOM,
            tags = listOf("Personalizado", "Libre"),
            pros = emptyList(),
            considerations = emptyList()
        )
    )
}

