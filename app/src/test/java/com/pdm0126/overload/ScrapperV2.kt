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

// DTOs Serializados
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
    val mainMuscleGroup: String,
    val mechanic: String,
    val targetMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val equipments: List<String>,
    val instructions: List<String>,
    val remoteImagesUrls: List<String>
)

data class ExerciseMapping(
    val englishName: String,
    val mainMuscleGroup: String
)

const val REMOTE_EXERCISES_URI = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json"
const val GOOGLE_TRANSLATE_URI = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q="

class Scrapper {
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
                println("Iniciando Mapeo y Traducción...")

                val finalExercisesList = mutableListOf<FinalExerciseEntity>()
                val missingExercises = mutableListOf<String>()

                for (mapping in mappings) {
                    val apiMatch = apiExercises.find { it.name.trim().equals(mapping.englishName.trim(), ignoreCase = true) }

                    if (apiMatch != null) {
                        println("Procesando: ${apiMatch.name}...")

                        // Traducir el nombre directamente de la API y el resto de textos
                        val nameTranslated = translateToSpanish(client, apiMatch.name)
                        val targetMusclesTranslated = apiMatch.primaryMuscles.map { translateToSpanish(client, it) }
                        val secondaryMusclesTranslated = apiMatch.secondaryMuscles.map { translateToSpanish(client, it) }
                        val equipmentsTranslated = if (apiMatch.equipment != null) listOf(translateToSpanish(client, apiMatch.equipment)) else emptyList()
                        val instructionsTranslated = apiMatch.instructions.map { translateToSpanish(client, it) }

                        // Mapear mecánica
                        val mechanicTranslated = when (apiMatch.mechanic?.lowercase()) {
                            "compound" -> "Compuesto"
                            "isolation" -> "Aislamiento"
                            else -> "N/A"
                        }

                        // Construir URLs de imágenes
                        val baseUrl = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"
                        val fullRemoteImages = apiMatch.images.map { relativePath ->
                            "$baseUrl$relativePath"
                        }

                        finalExercisesList.add(
                            FinalExerciseEntity(
                                exerciseId = "ex_${mapping.englishName.trim().lowercase().replace(" ", "_").replace("-", "_")}",
                                name = nameTranslated,
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
                        missingExercises.add(mapping.englishName)
                    }
                }

                // Guardar en assets
                val outputJson = jsonParser.encodeToString(finalExercisesList)
                val assetsDir = File("src/main/assets")
                if (!assetsDir.exists()) assetsDir.mkdirs()
                val outputFile = File(assetsDir, "basic_exercises.json")
                outputFile.writeText(outputJson)

                println("Proceso Ktor finalizado")
                println("Archivo maestro creado en: ${outputFile.absolutePath}")
                println("Total emparejado: ${finalExercisesList.size} / ${mappings.size}")

                if (missingExercises.isNotEmpty()) {
                    println("⚠️ Ejercicios no encontrados (ignorado por decisión de negocio):")
                    missingExercises.forEach { println(" - $it") }
                }

            } else {
                println("Error en la respuesta HTTP: ${response.status}")
            }
        } catch (e: Exception) {
            println("Excepción durante el proceso: ${e.message}")
        } finally {
            client.close()
        }
    }

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
            ExerciseMapping("Barbell Bench Press - Medium Grip", "Pecho"),
            ExerciseMapping("Barbell Incline Bench Press - Medium Grip", "Pecho"),
            ExerciseMapping("dumbbell bench press", "Pecho"),
            ExerciseMapping("incline dumbbell press", "Pecho"),
            ExerciseMapping("decline dumbbell bench press", "Pecho"),
            ExerciseMapping("machine bench press", "Pecho"),
            ExerciseMapping("Butterfly", "Pecho"),
            ExerciseMapping("Dumbbell Flyes", "Pecho"),
            ExerciseMapping("Dips - Chest Version", "Pecho"),

            ExerciseMapping("Pullups", "Espalda"),
            ExerciseMapping("Wide-Grip Lat Pulldown", "Espalda"),
            ExerciseMapping("Leverage High Row", "Espalda"),
            ExerciseMapping("Bent-Arm Barbell Pullover", "Espalda"),
            ExerciseMapping("Rope Straight-Arm Pulldown", "Espalda"),
            ExerciseMapping("Bent Over Barbell Row", "Espalda"),
            ExerciseMapping("Seated Cable Rows", "Espalda"),
            ExerciseMapping("Leverage Iso Row", "Espalda"),
            ExerciseMapping("Lying T-Bar Row", "Espalda"),

            ExerciseMapping("Seated Barbell Military Press", "Hombros"),
            ExerciseMapping("Dumbbell Shoulder Press", "Hombros"),
            ExerciseMapping("Machine Shoulder (Military) Press", "Hombros"),
            ExerciseMapping("Side Lateral Raise", "Hombros"),
            ExerciseMapping("Front Dumbbell Raise", "Hombros"),
            ExerciseMapping("Cable Rear Delt Fly", "Hombros"),

            ExerciseMapping("Barbell Full Squat", "Pierna"),
            ExerciseMapping("Split Squat with Dumbbells", "Pierna"),
            ExerciseMapping("Leg Press", "Pierna"),
            ExerciseMapping("Barbell Deadlift", "Pierna"),
            ExerciseMapping("Romanian Deadlift", "Pierna"),
            ExerciseMapping("Barbell Hip Thrust", "Pierna"),
            ExerciseMapping("Leg Extensions", "Pierna"),
            ExerciseMapping("Lying Leg Curls", "Pierna"),
            ExerciseMapping("Thigh Adductor", "Pierna"),
            ExerciseMapping("Thigh Abductor", "Pierna"),
            ExerciseMapping("Standing Calf Raises", "Pierna"),

            ExerciseMapping("Dumbbell Alternate Bicep Curl", "Bíceps"),
            ExerciseMapping("Barbell Curl", "Bíceps"),
            ExerciseMapping("Spider Curl", "Bíceps"),
            ExerciseMapping("Hammer Curls", "Bíceps"),
            ExerciseMapping("Incline Inner Biceps Curl", "Bíceps"),
            ExerciseMapping("Preacher Curl", "Bíceps"),

            ExerciseMapping("Lying Triceps Press", "Tríceps"),
            ExerciseMapping("Triceps Pushdown", "Tríceps"),
            ExerciseMapping("Triceps Overhead Extension with Rope", "Tríceps"),
            ExerciseMapping("Machine Triceps Extension", "Tríceps"),
            ExerciseMapping("Dumbbell Tricep Extension -Pronated Grip", "Tríceps")
        )
    }
}

