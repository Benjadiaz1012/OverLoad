package com.pdm0126.overload.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.domain.model.RoutineSlot
import com.pdm0126.overload.domain.model.WorkoutSet
import com.pdm0126.overload.ui.components.OverloadScaffold

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // SOLUCIÓN AL CRASHEO: Tomamos una "fotografía" inmutable del estado.
    // Esto garantiza que si la base de datos se actualiza ultrarrápido al "Finalizar",
    // estas variables locales no se volverán nulas a la mitad del renderizado de la UI.
    val isLoading = uiState.isLoading
    val activeMicrocycle = uiState.activeMicrocycle
    val activeSession = uiState.activeSession
    val activeDay = uiState.activeDay
    val sessionSets = uiState.sessionSets

    var showEndWorkoutDialog by rememberSaveable { mutableStateOf(false) }

    OverloadScaffold(
        title = if (activeSession != null && activeDay != null) "Entrenando: ${activeDay.focus}" else "Entrenamiento",
        showBackButton = false,
        actions = {
            if (activeSession != null) {
                IconButton(onClick = { showEndWorkoutDialog = true }) {
                    Icon(Icons.Default.StopCircle, contentDescription = "Finalizar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // --- ESTADO 3: ENTRENAMIENTO EN PROGRESO (LA BESTIA) ---
                // Al validar que no sean nulos, el compilador hace un 'Smart Cast' seguro
                activeSession != null && activeDay != null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(activeDay.slots, key = { _, slot -> slot.slotId }) { index, slot ->
                            // Filtramos solo las series que le pertenecen a este ejercicio
                            val slotSets = sessionSets.filter { it.slotId == slot.slotId }

                            ActiveSlotCard(
                                index = index + 1,
                                slot = slot,
                                loggedSets = slotSets,
                                onLogSet = viewModel::logSet,
                                onDeleteSet = viewModel::deleteSet
                            )
                        }

                        item {
                            Button(
                                onClick = { showEndWorkoutDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Finalizar Entrenamiento", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- ESTADO 1: NO HAY RUTINA ---
                activeMicrocycle == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tienes ninguna rutina seleccionada.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ve a la pestaña 'Mis Rutinas', crea un microciclo y presiona el botón 'Activar' para comenzar a entrenar.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // --- ESTADO 2: LOBBY ---
                else -> {
                    // Safe call natural porque cayó en el else (significa que activeMicrocycle no es nulo)
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                                Text("Plan actual", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(activeMicrocycle.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("¿Qué toca entrenar hoy?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        itemsIndexed(activeMicrocycle.days, key = { _, day -> day.dayId }) { index, day ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.startWorkout(day.dayId) }
                                        .padding(vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(28.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        Text(day.focus, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${day.slots.size} ejercicios programados", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        // --- DIÁLOGO FINALIZAR ---
        if (showEndWorkoutDialog) {
            AlertDialog(
                onDismissRequest = { showEndWorkoutDialog = false },
                title = { Text("Finalizar Sesión") },
                text = { Text("¿Estás seguro de que deseas dar por terminado este entrenamiento? Los datos registrados se guardarán en tu historial.") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.endWorkout()
                        showEndWorkoutDialog = false
                    }) { Text("Finalizar") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndWorkoutDialog = false }) { Text("Continuar Entrenando") }
                }
            )
        }
    }
}

// --- COMPONENTE: TARJETA DE EJERCICIO EN VIVO ---
@Composable
fun ActiveSlotCard(
    index: Int,
    slot: RoutineSlot,
    loggedSets: List<WorkoutSet>,
    onLogSet: (Long, String, Int, Float, Int, Int?, Boolean) -> Unit,
    onDeleteSet: (Long) -> Unit
) {
    var weightInput by rememberSaveable { mutableStateOf("") }
    var repsInput by rememberSaveable { mutableStateOf("") }

    var isRirEnabled by rememberSaveable { mutableStateOf(true) }
    var rirInput by rememberSaveable { mutableIntStateOf(2) }
    var showRirMenu by remember { mutableStateOf(false) }

    val nextSetNumber = (loggedSets.maxOfOrNull { it.setNumber } ?: 0) + 1
    val isCompleted = loggedSets.size >= slot.targetSets

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // CABECERA
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = "$index.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = slot.exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Objetivo: ${slot.targetSets} Series",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // SERIES YA REGISTRADAS
            if (loggedSets.isNotEmpty()) {
                loggedSets.forEach { set ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${set.setNumber}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(24.dp)
                        )
                        Text("${set.weightKg} kg", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        Text("${set.reps} reps", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)

                        Text(
                            text = if (set.isRirEnabled) "RIR ${set.rir}" else "-",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { onDeleteSet(set.setId) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Borrar Serie", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // LÓGICA CONDICIONAL DE BLOQUEO
            if (isCompleted) {
                // Banner de éxito
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¡Ejercicio Completado!", color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                }
            } else {
                // INPUT PARA LA SIGUIENTE SERIE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showRirMenu = true },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(if (isRirEnabled) "RIR $rirInput" else "Fijo", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        }
                        DropdownMenu(expanded = showRirMenu, onDismissRequest = { showRirMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Fijo (Sin RIR)") },
                                onClick = { isRirEnabled = false; showRirMenu = false }
                            )
                            (0..5).forEach { rirVal ->
                                DropdownMenuItem(
                                    text = { Text("RIR $rirVal") },
                                    onClick = { isRirEnabled = true; rirInput = rirVal; showRirMenu = false }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            val w = weightInput.toFloatOrNull()
                            val r = repsInput.toIntOrNull()
                            if (w != null && r != null) {
                                val finalRir = if (isRirEnabled) rirInput else null
                                onLogSet(slot.slotId, slot.exercise.id, nextSetNumber, w, r, finalRir, isRirEnabled)
                                repsInput = ""
                            }
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}