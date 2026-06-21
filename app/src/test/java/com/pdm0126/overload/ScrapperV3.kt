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
    fun generateExerciseAssets() = runBlocking {
        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(jsonParser)
            }
        }

        val mappings = getExerciseMappings()
        println("Conectando al repositorio...")

        try {
            val response: HttpResponse = client.get(REMOTE_EXERCISES_URI)

            if (response.status == HttpStatusCode.OK) {
                val rawJson = response.bodyAsText()
                val apiExercises: List<ApiExerciseDto> = jsonParser.decodeFromString(rawJson)

                println("Ejercicios totales: ${apiExercises.size}")

                val finalExercisesList = mutableListOf<FinalExerciseEntity>()

                for (mapping in mappings) {
                    val apiMatch = apiExercises.find { it.name.trim().equals(mapping.englishName.trim(), ignoreCase = true) }

                    if (apiMatch != null) {
                        println("Procesando: ${apiMatch.name}")

                        val nameTranslated = translateToSpanish(client, apiMatch.name)
                        val instructionsTranslated = apiMatch.instructions.map { translateToSpanish(client, it) }

                        val mainMuscleGroup = TechnicalDictionary.getGeneralGroup(apiMatch.primaryMuscles.firstOrNull())
                        val targetMuscles = apiMatch.primaryMuscles.map { TechnicalDictionary.getSpecificMuscle(it) }
                        val secondaryMuscles = apiMatch.secondaryMuscles.map { TechnicalDictionary.getSpecificMuscle(it) }
                        val equipments = if (apiMatch.equipment != null) listOf(TechnicalDictionary.getEquipment(apiMatch.equipment)) else emptyList()
                        val mechanic = TechnicalDictionary.getMechanic(apiMatch.mechanic)

                        val baseUrl = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"
                        val remoteImages = apiMatch.images.map { "$baseUrl$it" }

                        finalExercisesList.add(
                            FinalExerciseEntity(
                                exerciseId = "ex_${mapping.englishName.trim().lowercase().replace(" ", "_").replace("-", "_")}",
                                name = nameTranslated,
                                mainMuscleGroup = mainMuscleGroup,
                                mechanic = mechanic,
                                targetMuscles = targetMuscles,
                                secondaryMuscles = secondaryMuscles,
                                equipments = equipments,
                                instructions = instructionsTranslated,
                                remoteImagesUrls = remoteImages
                            )
                        )
                    }
                }

                val outputJson = jsonParser.encodeToString(finalExercisesList)
                val assetsDir = File("src/main/assets")
                if (!assetsDir.exists()) assetsDir.mkdirs()
                val outputFile = File(assetsDir, "basic_exercises.json")
                outputFile.writeText(outputJson)

                println("Proceso finalizado")
                println("Archivo creado en: ${outputFile.absolutePath}")
            }
        } catch (e: Exception) {
            println("Excepcion: ${e.message}")
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
            ExerciseMapping("Barbell Bench Press - Medium Grip"),
            ExerciseMapping("Barbell Incline Bench Press - Medium Grip"),
            ExerciseMapping("dumbbell bench press"),
            ExerciseMapping("incline dumbbell press"),
            ExerciseMapping("decline dumbbell bench press"),
            ExerciseMapping("machine bench press"),
            ExerciseMapping("Butterfly"),
            ExerciseMapping("Dumbbell Flyes"),
            ExerciseMapping("Dips - Chest Version"),

            ExerciseMapping("Pullups"),
            ExerciseMapping("Wide-Grip Lat Pulldown"),
            ExerciseMapping("Leverage High Row"),
            ExerciseMapping("Bent-Arm Barbell Pullover"),
            ExerciseMapping("Rope Straight-Arm Pulldown"),
            ExerciseMapping("Bent Over Barbell Row"),
            ExerciseMapping("Seated Cable Rows"),
            ExerciseMapping("Leverage Iso Row"),
            ExerciseMapping("Lying T-Bar Row"),

            ExerciseMapping("Seated Barbell Military Press"),
            ExerciseMapping("Dumbbell Shoulder Press"),
            ExerciseMapping("Machine Shoulder (Military) Press"),
            ExerciseMapping("Side Lateral Raise"),
            ExerciseMapping("Front Dumbbell Raise"),
            ExerciseMapping("Cable Rear Delt Fly"),

            ExerciseMapping("Barbell Full Squat"),
            ExerciseMapping("Split Squat with Dumbbells"),
            ExerciseMapping("Leg Press"),
            ExerciseMapping("Barbell Deadlift"),
            ExerciseMapping("Romanian Deadlift"),
            ExerciseMapping("Barbell Hip Thrust"),
            ExerciseMapping("Leg Extensions"),
            ExerciseMapping("Lying Leg Curls"),
            ExerciseMapping("Thigh Adductor"),
            ExerciseMapping("Thigh Abductor"),
            ExerciseMapping("Standing Calf Raises"),

            ExerciseMapping("Dumbbell Alternate Bicep Curl"),
            ExerciseMapping("Barbell Curl"),
            ExerciseMapping("Spider Curl"),
            ExerciseMapping("Hammer Curls"),
            ExerciseMapping("Incline Inner Biceps Curl"),
            ExerciseMapping("Preacher Curl"),

            ExerciseMapping("Lying Triceps Press"),
            ExerciseMapping("Triceps Pushdown"),
            ExerciseMapping("Triceps Overhead Extension with Rope"),
            ExerciseMapping("Machine Triceps Extension"),
            ExerciseMapping("Dumbbell Tricep Extension -Pronated Grip")
        )
    }
}
