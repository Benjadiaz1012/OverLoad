package com.pdm0126.overload.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.ui.viewmodels.test.RoutineConfigUiState
import com.pdm0126.overload.ui.viewmodels.test.RoutineConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineConfigTestScreen(
    viewModel: RoutineConfigViewModel = viewModel(factory = RoutineConfigViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Test: Lienzo de Rutina") }) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            when (val state = uiState) {
                is RoutineConfigUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RoutineConfigUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is RoutineConfigUiState.Empty -> {
                    // ESTADO 1: No hay microciclo, mostramos el botón creador
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("El lienzo está en blanco.", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.startNewMicrocycle("Fase de Volumen", "PPL") }) {
                            Text("Crear Microciclo (PPL)")
                        }
                    }
                }
                is RoutineConfigUiState.Active -> {
                    // ESTADO 2: Microciclo activo, dibujamos el árbol de datos
                    ActiveRoutineCanvas(viewModel = viewModel, microcycle = state.microcycle)
                }
            }
        }
    }
}

@Composable
fun ActiveRoutineCanvas(viewModel: RoutineConfigViewModel, microcycle: RoutineMicrocycle) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera del Microciclo
        Text(text = microcycle.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Esquema sugerido: ${microcycle.blueprintType}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para añadir un día (Respeta el límite de 6)
        Button(
            onClick = { viewModel.addDay(microcycle, "Día ${microcycle.days.size + 1}") },
            enabled = microcycle.days.size < 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (microcycle.days.size < 6) "Añadir Día al Microciclo" else "Límite de 6 días alcanzado")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Días y sus Slots
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(microcycle.days) { day ->
                DayCard(viewModel = viewModel, day = day)
            }
        }
    }
}

@Composable
fun DayCard(viewModel: RoutineConfigViewModel, day: RoutineDay) {
    // Variable temporal para probar la inserción con un ID real del ejercicio guardado
    var testExerciseId by remember { mutableStateOf("ex_star_jump") }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Orden: ${day.order} - Etiqueta: ${day.focus}", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            // Simulación del buscador de la librería
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = testExerciseId,
                    onValueChange = { testExerciseId = it },
                    label = { Text("ID del Ejercicio guardado") },
                    modifier = Modifier.weight(1f).height(60.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.addExerciseToDay(day, testExerciseId, targetSets = 4) },
                    enabled = day.slots.size < 12 // Respeta el límite de 12 ejercicios
                ) {
                    Text("Añadir Slot")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dibujamos los Slots (Ejercicios asignados)
            if (day.slots.isEmpty()) {
                Text("Día vacío. Añade un ejercicio.", style = MaterialTheme.typography.bodySmall)
            } else {
                day.slots.forEach { slot ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${slot.order}. ${slot.exercise.name}", fontWeight = FontWeight.SemiBold)
                            Text(text = "${slot.targetSets} Series | Músculo: ${slot.exercise.muscleGroup}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.removeExercise(slot.slotId) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Slot", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

