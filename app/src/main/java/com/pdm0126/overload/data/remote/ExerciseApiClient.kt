package com.pdm0126.overload.data.remote

import com.pdm0126.overload.data.remote.dto.ExerciseDto
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ExerciseApiClient {

    private val baseUrl = "https://overload-api.vercel.app/api/exercises"

    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            },
                contentType = ContentType.Any
            )
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorClient", message)
                }
            }
            level = LogLevel.ALL
        }

        defaultRequest {
            url(baseUrl)
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.ContentType, "application/json")
        }
    }
    private var cachedExercises: List<ExerciseDto>? = null
    suspend fun fetchRemoteExercises() : List<ExerciseDto>  {
        if (cachedExercises != null) {
            return cachedExercises!!
        }

        val response: List<ExerciseDto> = client.get("").body()

        cachedExercises = response
        return response
    }
}