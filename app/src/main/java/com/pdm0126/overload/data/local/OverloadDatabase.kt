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
import com.pdm0126.overload.data.local.dao.RoutineDao
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.data.local.entity.DayEntity
import com.pdm0126.overload.data.local.entity.MicrocycleEntity
import com.pdm0126.overload.data.local.entity.SlotEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Database(
    entities = [
        ExerciseEntity::class,
        MicrocycleEntity::class,
        DayEntity::class,
        SlotEntity::class
               ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OverloadDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile
        private var INSTANCE: OverloadDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): OverloadDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OverloadDatabase::class.java,
                    "overload_database"
                )
                    .addCallback(OverloadDatabaseCallback(context, scope) { INSTANCE!! })
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

    private class OverloadDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope,
        private val provider: () -> OverloadDatabase
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                populateDatabase(provider().exerciseDao(), context)
            }
        }
        suspend fun populateDatabase(exerciseDao: ExerciseDao, context: Context) {
            try {
                val inputStream = context.assets.open("basic_exercises.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }

                val jsonParser = Json { ignoreUnknownKeys = true }
                val exercises = jsonParser.decodeFromString<List<ExerciseEntity>>(jsonString)

                exerciseDao.insertAll(exercises)
                Log.d("OverloadDB", "Éxito: Base de datos inicializada con ${exercises.size} ejercicios.")
            } catch (e: Exception) {
                Log.e("OverloadDB", "Error fatal poblando la base de datos: ${e.localizedMessage}")
            }
        }
    }
}