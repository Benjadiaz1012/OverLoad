package com.pdm0126.overload.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.domain.model.RoutineSlot
import com.pdm0126.overload.domain.model.WorkoutSet
import com.pdm0126.overload.ui.viewmodels.test.WorkoutTestUiState
import com.pdm0126.overload.ui.viewmodels.test.WorkoutTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTestScreen(
    viewModel: WorkoutTestViewModel = viewModel(factory = WorkoutTestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Test: Módulo 3 — Entrenamiento") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is WorkoutTestUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is WorkoutTestUiState.NoRoutine -> {
                    Text(
                        text = "No hay microciclo activo.\nCrea uno primero desde la pantalla de Configuración.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is WorkoutTestUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is WorkoutTestUiState.Ready -> {
                    val day = state.microcycle.days.getOrNull(state.currentDayIndex)
                    if (day == null) {
                        Text("El microciclo no tiene días. Añade días y ejercicios primero.",
                            modifier = Modifier.align(Alignment.Center))
                        return@Box
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Cabecera
                        Text(
                            text = "Día ${day.order} — ${day.focus}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.microcycle.name} · ${state.microcycle.blueprintType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Lista de ejercicios (modo lectura con referencia histórica)
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(day.slots) { slot ->
                                ExercisePreviewCard(
                                    slot = slot,
                                    lastSets = state.lastSets[slot.slotId] ?: emptyList()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.startSession(day.dayId) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = day.slots.isNotEmpty()
                        ) {
                            Text("▶ Iniciar Sesión de Entrenamiento")
                        }
                    }
                }

                is WorkoutTestUiState.SessionActive -> {
                    val day = state.microcycle.days.getOrNull(state.currentDayIndex) ?: return@Box

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Cabecera con estado de sesión activa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sesión Activa",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Día ${day.order} — ${day.focus}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.endSession(state.session.sessionId) }
                            ) {
                                Text("Finalizar")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Lista de ejercicios con entrada de datos
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(day.slots) { slot ->
                                val currentSets = state.setsBySlot[slot.slotId] ?: emptyList()
                                val lastSets = state.lastSets[slot.slotId] ?: emptyList()
                                val isComplete = currentSets.size >= slot.targetSets

                                ActiveSlotCard(
                                    slot = slot,
                                    currentSets = currentSets,
                                    lastSets = lastSets,
                                    isComplete = isComplete,
                                    onLogSet = { weight, reps, rir, isRirEnabled ->
                                        viewModel.logSet(
                                            sessionId = state.session.sessionId,
                                            slotId = slot.slotId,
                                            exerciseId = slot.exercise.id,
                                            currentSetCount = currentSets.size,
                                            weightKg = weight,
                                            reps = reps,
                                            rir = rir,
                                            isRirEnabled = isRirEnabled
                                        )
                                    },
                                    onDeleteSet = { setId -> viewModel.deleteSet(setId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Componentes

// Tarjeta de vista previa (modo Dashboard, antes de iniciar sesión)
@Composable
fun ExercisePreviewCard(slot: RoutineSlot, lastSets: List<WorkoutSet>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${slot.order}. ${slot.exercise.name}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${slot.targetSets} series · ${slot.exercise.muscleGroup}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (lastSets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Última sesión:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                lastSets.forEach { set ->
                    val rirText = if (set.isRirEnabled) " · RIR ${set.rir}" else " · Fijo"
                    Text(
                        text = "  Serie ${set.setNumber}: ${set.weightKg}kg × ${set.reps} reps$rirText",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sin historial previo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Tarjeta activa con entrada de datos por serie (modo Sesión)
@Composable
fun ActiveSlotCard(
    slot: RoutineSlot,
    currentSets: List<WorkoutSet>,
    lastSets: List<WorkoutSet>,
    isComplete: Boolean,
    onLogSet: (Float, Int, Int?, Boolean) -> Unit,
    onDeleteSet: (Long) -> Unit
) {
    // Estado de inputs locales de este slot
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var rir by remember { mutableStateOf(2) }
    var isRirEnabled by remember { mutableStateOf(true) }

    val containerColor = when {
        isComplete -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Nombre del ejercicio y progreso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${slot.order}. ${slot.exercise.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${currentSets.size}/${slot.targetSets}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isComplete) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Referencia histórica (la sesión anterior)
            if (lastSets.isNotEmpty()) {
                val ref = lastSets.firstOrNull()
                val rirRef = if (ref?.isRirEnabled == true) " · RIR ${ref.rir}" else " · Fijo"
                Text(
                    text = "Ref: ${ref?.weightKg}kg × ${ref?.reps} reps$rirRef",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Series ya registradas en esta sesión
            if (currentSets.isNotEmpty()) {
                currentSets.forEach { set ->
                    val rirText = if (set.isRirEnabled) "RIR ${set.rir}" else "Fijo"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓ Serie ${set.setNumber}: ${set.weightKg}kg × ${set.reps} reps · $rirText  (×${set.rirFactor})",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDeleteSet(set.setId) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Borrar serie",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Formulario de entrada (solo si el slot aún no está completo)
            if (!isComplete) {
                Text(
                    text = "Serie ${currentSets.size + 1}:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle RIR
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Medir RIR", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isRirEnabled,
                        onCheckedChange = { isRirEnabled = it }
                    )
                }

                // Selector de RIR (0–5) visible solo si el toggle está activo
                if (isRirEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RIR: $rir",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = rir.toFloat(),
                        onValueChange = { rir = it.toInt() },
                        valueRange = 0f..5f,
                        steps = 4, // 0, 1, 2, 3, 4, 5
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val w = weight.toFloatOrNull() ?: return@Button
                        val r = reps.toIntOrNull() ?: return@Button
                        onLogSet(w, r, if (isRirEnabled) rir else null, isRirEnabled)
                        weight = ""
                        reps = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = weight.isNotBlank() && reps.isNotBlank()
                ) {
                    Text("Registrar Serie ${currentSets.size + 1}")
                }
            } else {
                Text(
                    text = "Ejercicio completado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
