package com.pdm0126.overload

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import java.io.File
import java.net.URLEncoder

// DTOs (Data Transfer Objects) Serializados
@Serializable
data class ApiExerciseDto(
    val name: String,
    val primaryMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val equipment: String? = null,
    val mechanic: String? = null,
    val instructions: List<String> = emptyList(),
    val images: List<String> = emptyList()
)

@Serializable
data class FinalExerciseEntity(
    val exerciseId: String,
    val name: String,
    val localImagePath: String,
    val mainMuscleGroup: String,
    val mechanic: String,
    val targetMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val equipments: List<String>,
    val instructions: List<String>,
    val remoteImagesUrls: List<String>
)

data class ExerciseMapping(
    val spanishName: String,
    val englishName: String,
    val localImagePath: String,
    val mainMuscleGroup: String
)
val REMOTE_EXERCISES_URI = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json"
val GOOGLE_TRANSLATE_URI = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q="


// Script de scrapper (Ktor + corrutinas)
class Scrapper {
    // Instancia de JSON
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    @Test
    fun ejecutarScrapperAsincrono() = runBlocking {
        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(jsonParser)
            }
        }

        val mappings = getExerciseMappings()
        println("Conectando al repositorio con Ktor...")
        try {
            val response: HttpResponse = client.get(REMOTE_EXERCISES_URI)

            if (response.status == HttpStatusCode.OK) {
                val rawJson = response.bodyAsText()
                val apiExercises: List<ApiExerciseDto> = jsonParser.decodeFromString(rawJson)

                println("Base de datos obtenida. Total ejercicios: ${apiExercises.size}")
                println("Iniciando Mapeo, Clasificación y Traducción...")

                val finalExercisesList = mutableListOf<FinalExerciseEntity>()
                val missingExercises = mutableListOf<String>()

                for (mapping in mappings) {
                    val apiMatch = apiExercises.find { it.name.trim().equals(mapping.englishName.trim(), ignoreCase = true) }

                    if (apiMatch != null) {
                        println("Procesando: ${mapping.spanishName}...")

                        // Traducir textos
                        val targetMusclesTranslated = apiMatch.primaryMuscles.map { translateToSpanish(client, it) }
                        val secondaryMusclesTranslated = apiMatch.secondaryMuscles.map { translateToSpanish(client, it) }
                        val equipmentsTranslated = if (apiMatch.equipment != null) listOf(translateToSpanish(client, apiMatch.equipment)) else emptyList()
                        val instructionsTranslated = apiMatch.instructions.map { translateToSpanish(client, it) }

                        // Mapear mecánica (aislamiento o Compuesto)
                        val mechanicTranslated = when (apiMatch.mechanic?.lowercase()) {
                            "compound" -> "Compuesto"
                            "isolation" -> "Aislamiento"
                            else -> "N/A"
                        }

                        // Construir URL completas de las imágenes
                        val baseUrl = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"
                        val fullRemoteImages = apiMatch.images.map { relativePath ->
                            "$baseUrl$relativePath"
                        }

                        finalExercisesList.add(
                            FinalExerciseEntity(
                                exerciseId = "ex_${mapping.englishName.trim().lowercase().replace(" ", "_").replace("-", "_")}",
                                name = mapping.spanishName,
                                localImagePath = mapping.localImagePath,
                                mainMuscleGroup = mapping.mainMuscleGroup,
                                mechanic = mechanicTranslated,
                                targetMuscles = targetMusclesTranslated,
                                secondaryMuscles = secondaryMusclesTranslated,
                                equipments = equipmentsTranslated,
                                instructions = instructionsTranslated,
                                remoteImagesUrls = fullRemoteImages
                            )
                        )
                    } else {
                        missingExercises.add("${mapping.spanishName} (${mapping.englishName})")
                    }
                }

                // Guardar en assets
                val outputJson = jsonParser.encodeToString(finalExercisesList)
                val assetsDir = File("src/main/assets")
                if (!assetsDir.exists()) assetsDir.mkdirs()
                val outputFile = File(assetsDir, "ejercicios_maestros.json")
                outputFile.writeText(outputJson)

                println("Proceso Ktor finalizado")
                println("Archivo maestro creado en: ${outputFile.absolutePath}")
                println("Total emparejado: ${finalExercisesList.size} / ${mappings.size}")

                if (missingExercises.isNotEmpty()) {
                    println("Ejercicios no encontrados:")
                    missingExercises.forEach { println(" - $it") }
                }

            } else {
                println("Error en la respuesta HTTP: ${response.status}")
            }
        } catch (e: Exception) {
            println("Excepción durante el proceso: ${e.message}")
        } finally {
            client.close() // Cerrar el cliente
        }
    }

    // Funciones Auxiliares
    private suspend fun translateToSpanish(client: HttpClient, text: String): String {
        if (text.isBlank()) return ""
        return try {
            val encodedText = withContext(Dispatchers.IO) {
                URLEncoder.encode(text, "UTF-8")
            }

            val response: HttpResponse = client.get(GOOGLE_TRANSLATE_URI + encodedText) {
                header("User-Agent", "Mozilla/5.0")
            }

            if (response.status == HttpStatusCode.OK) {
                val rawJson = response.bodyAsText()
                // Parseo nativo con kotlinx.serialization
                val rootArray = Json.parseToJsonElement(rawJson).jsonArray
                val linesArray = rootArray[0].jsonArray

                val translatedText = java.lang.StringBuilder()
                for (lineElement in linesArray) {
                    translatedText.append(lineElement.jsonArray[0].jsonPrimitive.content)
                }
                translatedText.toString().trim()
            } else {
                text
            }
        } catch (e: Exception) {
            text
        }
    }

    private fun getExerciseMappings(): List<ExerciseMapping> {
        return listOf(
            ExerciseMapping("Press de banca plano con barra", "Barbell Bench Press - Medium Grip", "chest_barbell_bench_press.webp", "Pecho"),
            ExerciseMapping("Press de banca inclinado con barra", "Barbell Incline Bench Press - Medium Grip", "chest_barbell_incline_press.webp", "Pecho"),
            ExerciseMapping("Press con mancuernas en banco plano", "dumbbell bench press", "chest_dumbbell_bench_press.webp", "Pecho"),
            ExerciseMapping("Press con mancuernas en banco inclinado", "incline dumbbell press", "chest_dumbbell_incline_press.webp", "Pecho"), // Corrección de nombre
            ExerciseMapping("Press con mancuernas en banco declinado", "decline dumbbell bench press", "chest_dumbbell_decline_press.webp", "Pecho"),
            ExerciseMapping("Press plano en maquina", "machine bench press", "chest_machine_flat_press.webp", "Pecho"),
            ExerciseMapping("Aperturas en maquina", "Butterfly", "butterfly_machine.webp", "Pecho"),
            ExerciseMapping("Aperturas con mancuernas (Flyes)", "Dumbbell Flyes", "chest_dumbbell_fly.webp", "Pecho"),
            ExerciseMapping("Fondos en paralelas (Enfoque pectoral)", "Dips - Chest Version", "chest_dips.webp", "Pecho"),

            ExerciseMapping("Dominadas (Pull-ups)", "Pullups", "back_pullups.webp", "Espalda"),
            ExerciseMapping("Jalón al pecho con agarre ancho", "Wide-Grip Lat Pulldown", "back_lat_pulldown.webp", "Espalda"),
            ExerciseMapping("Remo vertical en máquina", "Leverage High Row", "back_machine_vertical_row.webp", "Espalda"),
            ExerciseMapping("Pullover con barra", "Bent-Arm Barbell Pullover", "back_barbell_pullover.webp", "Espalda"),
            ExerciseMapping("Pullover polea alta", "Rope Straight-Arm Pulldown", "back_rope_pulldown.webp", "Espalda"),
            ExerciseMapping("Remo inclinado con barra", "Bent Over Barbell Row", "back_barbell_row.webp", "Espalda"),
            ExerciseMapping("Remo en polea baja con agarre", "Seated Cable Rows", "back_cable_seated_row.webp", "Espalda"),
            ExerciseMapping("Remo en horizontal en maquina", "Leverage Iso Row", "back_machine_horizontal_row.webp", "Espalda"),
            ExerciseMapping("Remo en barra T", "Lying T-Bar Row", "back_t_bar_row.webp", "Espalda"),

            ExerciseMapping("Press militar con barra", "Seated Barbell Military Press", "shoulder_barbell_military_press.webp", "Hombros"),
            ExerciseMapping("Press de hombros con mancuernas", "Dumbbell Shoulder Press", "shoulder_dumbbell_press.webp", "Hombros"),
            ExerciseMapping("Press de hombros en máquina", "Machine Shoulder (Military) Press", "shoulder_machine_press.webp", "Hombros"),
            ExerciseMapping("Elevaciones laterales con mancuernas", "Side Lateral Raise", "shoulder_dumbbell_lateral_raise.webp", "Hombros"),
            ExerciseMapping("Elevaciones frontales con mancuernas", "Front Dumbbell Raise", "shoulder_dumbbell_front_raise.webp", "Hombros"),
            ExerciseMapping("Face Pull en polea alta", "Cable Rear Delt Fly", "shoulder_cable_face_pull.webp", "Hombros"),

            ExerciseMapping("Sentadilla con barra trasera (Back Squat)", "Barbell Full Squat", "legs_barbell_squat.webp", "Pierna"),
            ExerciseMapping("Sentadilla búlgara con mancuernas", "Split Squat with Dumbbells", "legs_bulgarian_split_squat.webp", "Pierna"),
            ExerciseMapping("Prensa de piernas inclinada (45°)", "Leg Press", "legs_leg_press_45.webp", "Pierna"),
            ExerciseMapping("Peso muerto convencional con barra", "Barbell Deadlift", "legs_barbell_deadlift.webp", "Pierna"),
            ExerciseMapping("Peso muerto rumano", "Romanian Deadlift", "legs_barbell_rdl.webp", "Pierna"),
            ExerciseMapping("Hip Thrust (Empuje de cadera) con barra", "Barbell Hip Thrust", "legs_barbell_hip_thrust.webp", "Pierna"),
            ExerciseMapping("Extensiones de cuádriceps en máquina", "Leg Extensions", "legs_machine_extensions.webp", "Pierna"),
            ExerciseMapping("Curl femoral en máquina", "Lying Leg Curls", "legs_machine_leg_curl.webp", "Pierna"),
            ExerciseMapping("Aductor interno en maquina", "Thigh Adductor", "legs_machine_adductor.webp", "Pierna"),
            ExerciseMapping("Aductor externo en maquina", "Thigh Abductor", "legs_machine_abductor.webp", "Pierna"),
            ExerciseMapping("Elevación de talones en máquina (De pie)", "Standing Calf Raises", "legs_machine_calf_raise.webp", "Pierna"),

            ExerciseMapping("Curl de bíceps con mancuernas (Alterno)", "Dumbbell Alternate Bicep Curl", "biceps_dumbbell_alternate_curl.webp", "Bíceps"),
            ExerciseMapping("Curl de bíceps con barra", "Barbell Curl", "biceps_barbell_curl.webp", "Bíceps"), // Corrección de barra baja
            ExerciseMapping("Spider Curl con barra Z (Curl araña)", "Spider Curl", "biceps_ez_bar_spider_curl.webp", "Bíceps"),
            ExerciseMapping("Curl martillo (Hammer Curl)", "Hammer Curls", "biceps_dumbbell_hammer_curl.webp", "Bíceps"),
            ExerciseMapping("Curl de bíceps en banco inclinado", "Incline Inner Biceps Curl", "biceps_dumbbell_incline_curl.webp", "Bíceps"),
            ExerciseMapping("Curl de biceps en banco Scott", "Preacher Curl", "biceps_preacher_curl.webp", "Bíceps"),

            ExerciseMapping("Press francés con barra", "Lying Triceps Press", "triceps_barbell_skullcrusher.webp", "Tríceps"),
            ExerciseMapping("Extensión de tríceps en polea alta", "Triceps Pushdown", "triceps_cable_pushdown.webp", "Tríceps"),
            ExerciseMapping("Extensión de tríceps tras nuca en polea", "Triceps Overhead Extension with Rope", "triceps_cable_overhead_ext.webp", "Tríceps"),
            ExerciseMapping("Extensión de tríceps en máquina", "Machine Triceps Extension", "triceps_machine_extension.webp", "Tríceps"),
            ExerciseMapping("Press francés con mancuernas", "Dumbbell Tricep Extension -Pronated Grip", "triceps_dumbbell_skullcrusher.webp", "Tríceps") // Corrección del grip
        )
    }
}

