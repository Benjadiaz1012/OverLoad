package com.pdm0126.overload

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pdm0126.overload.ui.screens.InitTestScreen
import com.pdm0126.overload.ui.screens.RoutineConfigTestScreen
import com.pdm0126.overload.ui.screens.SearchTestScreen
import com.pdm0126.overload.ui.theme.OverLoadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OverLoadTheme {
                // Probamos la búsqueda remota de ejercicios y guardamos en local
                //SearchTestScreen()
                // Probamos la carga de ejercicios desde local, los guardados usando el search también deben aparecer
                //InitTestScreen()
                // Buscar ID's de ejercicios en basic_exercises.json, ej. ex_barbell_bench_press___medium_grip para añadirlos
                RoutineConfigTestScreen()
            }
        }
    }
}