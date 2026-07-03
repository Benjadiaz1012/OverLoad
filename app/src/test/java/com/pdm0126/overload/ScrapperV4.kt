package com.pdm0126.overload

import com.pdm0126.overload.domain.TechnicalDictionary
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

@Serializable
data class ApiExerciseDto(
    val id: String,
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
    val equipment: String,
    val instructions: List<String>,
    val remoteImagesUrls: List<String>
)

const val REMOTE_EXERCISES_URI = "https://overload-api.vercel.app/api/exercises"

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

        val starterPackIds = getStarterPackIds()
        println("Conectando a la API de Overload en Vercel...")

        try {
            val response: HttpResponse = client.get(REMOTE_EXERCISES_URI)

            if (response.status == HttpStatusCode.OK) {
                val rawJson = response.bodyAsText()
                val apiExercises: List<ApiExerciseDto> = jsonParser.decodeFromString(rawJson)

                println("Ejercicios totales en la API: ${apiExercises.size}")

                val finalExercisesList = mutableListOf<FinalExerciseEntity>()

                for (targetId in starterPackIds) {

                    val apiMatch = apiExercises.find { it.id.equals(targetId, ignoreCase = true) }

                    if (apiMatch != null) {
                        println("Procesando: ${apiMatch.name} (ID: ${apiMatch.id})")

                        val mainMuscleGroup = TechnicalDictionary.getGeneralGroup(apiMatch.primaryMuscles.firstOrNull())
                        val targetMuscles = apiMatch.primaryMuscles.map { TechnicalDictionary.getSpecificMuscle(it) }
                        val secondaryMuscles = apiMatch.secondaryMuscles.map { TechnicalDictionary.getSpecificMuscle(it) }
                        val equipment = if (apiMatch.equipment != null) TechnicalDictionary.getEquipment(apiMatch.equipment) else "N/A"
                        val mechanic = TechnicalDictionary.getMechanic(apiMatch.mechanic)

                        val remoteImages = apiMatch.images

                        finalExercisesList.add(
                            FinalExerciseEntity(
                                exerciseId = apiMatch.id,
                                name = apiMatch.name,
                                mainMuscleGroup = mainMuscleGroup,
                                mechanic = mechanic,
                                targetMuscles = targetMuscles,
                                secondaryMuscles = secondaryMuscles,
                                equipment = equipment,
                                instructions = apiMatch.instructions,
                                remoteImagesUrls = remoteImages
                            )
                        )
                    } else {
                        println("ADVERTENCIA: No se encontró coincidencia en la API para el ID -> $targetId")
                    }
                }

                val outputJson = jsonParser.encodeToString(finalExercisesList)
                val assetsDir = File("src/main/assets")
                if (!assetsDir.exists()) assetsDir.mkdirs()
                val outputFile = File(assetsDir, "basic_exercises.json")
                outputFile.writeText(outputJson)

                println("Proceso finalizado con éxito.")
                println("Ejercicios guardados: ${finalExercisesList.size}")
                println("Archivo creado en: ${outputFile.absolutePath}")
            }
        } catch (e: Exception) {
            println("Excepcion: ${e.message}")
        } finally {
            client.close()
        }
    }


    private fun getStarterPackIds(): List<String> {
        return listOf(
            "Barbell_Bench_Press_-_Medium_Grip",
            "Barbell_Incline_Bench_Press_-_Medium_Grip",
            "Dumbbell_Bench_Press",
            "Incline_Dumbbell_Press",
            "Decline_Dumbbell_Bench_Press",
            "Machine_Bench_Press",
            "Butterfly",
            "Dumbbell_Flyes",
            "Dips_-_Chest_Version",

            "Pullups",
            "Wide-Grip_Lat_Pulldown",
            "Leverage_High_Row",
            "Bent-Arm_Barbell_Pullover",
            "Rope_Straight-Arm_Pulldown",
            "Bent_Over_Barbell_Row",
            "Seated_Cable_Rows",
            "Leverage_Iso_Row",
            "Lying_T-Bar_Row",

            "Seated_Barbell_Military_Press",
            "Dumbbell_Shoulder_Press",
            "Machine_Shoulder_Military_Press",
            "Side_Lateral_Raise",
            "Front_Dumbbell_Raise",
            "Cable_Rear_Delt_Fly",

            "Barbell_Full_Squat",
            "Split_Squat_with_Dumbbells",
            "Leg_Press",
            "Barbell_Deadlift",
            "Romanian_Deadlift",
            "Barbell_Hip_Thrust",
            "Leg_Extensions",
            "Lying_Leg_Curls",
            "Thigh_Adductor",
            "Thigh_Abductor",
            "Standing_Calf_Raises",

            "Dumbbell_Alternate_Bicep_Curl",
            "Barbell_Curl",
            "Spider_Curl",
            "Hammer_Curls",
            "Incline_Inner_Biceps_Curl",
            "Preacher_Curl",

            "Lying_Triceps_Press",
            "Triceps_Pushdown",
            "Triceps_Overhead_Extension_with_Rope",
            "Machine_Triceps_Extension",
            "Dumbbell_Tricep_Extension_-Pronated_Grip"
        )
    }
}