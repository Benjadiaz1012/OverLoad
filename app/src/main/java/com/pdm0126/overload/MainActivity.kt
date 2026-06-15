package com.pdm0126.overload

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pdm0126.overload.ui.screens.InitTestScreen
import com.pdm0126.overload.ui.screens.RoutineConfigTestScreen
import com.pdm0126.overload.ui.screens.SearchTestScreen
import com.pdm0126.overload.ui.screens.WorkoutTestScreen
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
                //RoutineConfigTestScreen()
                // Validación del Módulo 3: sesión de entrenamiento, registro de series y referencia histórica
                //WorkoutTestScreen()
            }
        }
    }
}

/* Para testear:

1. [MainActivity] → RoutineConfigTestScreen
   └── Crear microciclo PPL
   └── Añadir días (ej. "Push")
   └── Añadir slots con ID's reales (ej. ex_barbell_bench_press___medium_grip)

2. [MainActivity] -> WorkoutTestScreen  <- ya está activa
   └── Ver Dashboard con referencia histórica vacía
   └── "Iniciar Sesión"
   └── Registrar series con peso/reps y Slider RIR
   └── Verificar que rirFactor correcto aparece en pantalla (×1.0, ×0.8, ×0.4, ×0.5)
   └── "Finalizar" -> vuelve al Dashboard
   └── Iniciar segunda sesión → ver referencia histórica poblada


 */