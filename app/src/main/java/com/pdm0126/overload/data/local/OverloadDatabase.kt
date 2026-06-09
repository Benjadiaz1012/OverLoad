package com.pdm0126.overload.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pdm0126.overload.data.local.converter.Converters
import com.pdm0126.overload.data.local.dao.ExerciseDao
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Database(
    entities = [ExerciseEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OverloadDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    companion object {
        @Volatile
        private var INSTANCE: OverloadDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): OverloadDatabase {
            // Si la instancia ya existe, la retorna, sino la construye de forma segura
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OverloadDatabase::class.java,
                    "overload_database"
                )
                    // Aquí enganchamos el evento que se dispara la primera vez que se crea la DB
                    .addCallback(OverloadDatabaseCallback(context, scope))
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

    // El callback que hace el trabajo de leer el JSON
    private class OverloadDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.exerciseDao(), context)
                }
            }
        }

        suspend fun populateDatabase(exerciseDao: ExerciseDao, context: Context) {
            try {
                // Abrir y leer el archivo desde la carpeta assets
                val inputStream = context.assets.open("ejercicios_maestros.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }

                // Usar Kotlinx Serialization para convertir el texto a lista de Entidades
                val jsonParser = Json { ignoreUnknownKeys = true }
                val exercises = jsonParser.decodeFromString<List<ExerciseEntity>>(jsonString)

                // Insertar en la tabla SQLite
                exerciseDao.insertAll(exercises)
                Log.d("OverloadDB", "Éxito: Base de datos inicializada con ${exercises.size} ejercicios.")
            } catch (e: Exception) {
                Log.e("OverloadDB", "Error fatal poblando la base de datos: ${e.localizedMessage}")
            }
        }
    }
}

