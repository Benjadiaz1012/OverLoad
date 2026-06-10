package com.pdm0126.overload.data.remote

import android.util.Log
import com.pdm0126.overload.data.remote.dto.ExerciseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ExerciseApiClient {

    private val baseUrl = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json"

    val client = HttpClient(OkHttp) {
        // Parseo automático de JSON
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        // Plugin de logging
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorClient", message)
                }
            }
            level = LogLevel.ALL
        }

        // Configuración aplicada a todas las peticiones
        defaultRequest {
            url(baseUrl)
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.ContentType, "application/json")
        }
    }
    private var cachedExercises: List<ExerciseDto>? = null
    // Función para obtener todos los ejercicios
    suspend fun fetchRemoteExercises() : List<ExerciseDto>  {
        if (cachedExercises != null) {
            return cachedExercises!!
        }
        return try {
            val response: List<ExerciseDto> = client.get("").body()
            cachedExercises = response
            response
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}