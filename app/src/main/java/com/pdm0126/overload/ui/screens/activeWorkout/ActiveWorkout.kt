package com.pdm0126.overload.ui.screens.activeWorkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.overload.ui.components.ConfirmDialog
import com.pdm0126.overload.ui.components.OverloadTopBar
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineSlot
import com.pdm0126.overload.domain.model.WorkoutSet
import kotlinx.coroutines.launch

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val FieldDark = Color(0xFF222222)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)
private val DividerGray = Color(0xFF2A2A2A)
private val ErrorRed = Color(0xFFE53935)

data class LogSetRequest(
    val slotId: Long,
    val exerciseId: String,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val rir: Int?,
    val isRirEnabled: Boolean
)

@Composable
fun ActiveWorkout(
    day: RoutineDay?,
    sessionSets: List<WorkoutSet> = emptyList(),
    lastSets: Map<String, List<WorkoutSet>> = emptyMap(),
    isLoading: Boolean = false,
    onLogSet: (LogSetRequest) -> Unit = {},
    onDeleteSet: (Long) -> Unit = {},
    onEndWorkout: () -> Unit = {},
    onCancelWorkout: () -> Unit = {}
) {
    var showEndWorkoutDialog by remember { mutableStateOf(false) }
    var showCancelWorkoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = day != null) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = "Finaliza o cancela el entrenamiento para salir",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            OverloadTopBar(
                title = if (day != null) "Entrenando: ${day.focus}" else "Cargando sesión...",
                trailingContent = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = GoldAccent
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Finalizar Entrenamiento") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = null,
                                        tint = GoldAccent
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showEndWorkoutDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cancelar Entrenamiento") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = ErrorRed
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showCancelWorkoutDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading || day == null) {
                CircularProgressIndicator(
                    color = GoldAccent,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(day.slots, key = { _, slot -> slot.slotId }) { index, slot ->
                        val slotSets = sessionSets.filter { it.slotId == slot.slotId }
                        val historicalSets = lastSets[slot.exercise.id] ?: emptyList()

                        ActiveSlotItem(
                            index = index + 1,
                            slot = slot,
                            loggedSets = slotSets,
                            lastSets = historicalSets,
                            onLogSet = onLogSet,
                            onDeleteSet = onDeleteSet
                        )
                    }

                    item {
                        Button(
                            onClick = { showEndWorkoutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Finalizar Entrenamiento", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showEndWorkoutDialog) {
        val hasLoggedSets = sessionSets.isNotEmpty()
        ConfirmDialog(
            title = "Finalizar Sesión",
            text = if (hasLoggedSets) {
                "Los datos registrados se guardarán en tu historial"
            } else {
                "No registraste ninguna serie. Esta sesión se descartará y no aparecerá en tu historial."
            },
            confirmText = "Finalizar",
            icon = Icons.Default.DoneAll,
            onConfirm = {
                onEndWorkout()
                showEndWorkoutDialog = false
            },
            onDismiss = { showEndWorkoutDialog = false }
        )
    }

    if (showCancelWorkoutDialog) {
        ConfirmDialog(
            title = "Cancelar Sesión",
            text = "Se perderán todas las series registradas en este momento y nada se guardará en tu historial",
            confirmText = "Cancelar",
            dismissText = "Volver",
            isDestructive = true,
            icon = Icons.Default.DeleteOutline,
            onConfirm = {
                onCancelWorkout()
                showCancelWorkoutDialog = false
            },
            onDismiss = { showCancelWorkoutDialog = false }
        )
    }
}

@Composable
private fun ActiveSlotItem(
    index: Int,
    slot: RoutineSlot,
    loggedSets: List<WorkoutSet>,
    lastSets: List<WorkoutSet>,
    onLogSet: (LogSetRequest) -> Unit,
    onDeleteSet: (Long) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf(slot.targetReps ?: 8) }

    var isRirEnabled by remember { mutableStateOf(true) }
    var rirInput by remember { mutableStateOf(2) }
    var showRirMenu by remember { mutableStateOf(false) }
    var showRepsMenu by remember { mutableStateOf(false) }

    val nextSetNumber = (loggedSets.maxOfOrNull { it.setNumber } ?: 0) + 1
    val isCompleted = loggedSets.size >= slot.targetSets

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$index",
                color = TextGray,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.width(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = slot.exercise.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Objetivo: ${slot.targetSets} series" +
                            if (slot.targetReps != null) " de ${slot.targetReps} reps" else "",
                    color = TextGray,
                    fontSize = 12.sp
                )

                if (lastSets.isNotEmpty() && !isCompleted) {
                    val refSet =
                        lastSets.find { it.setNumber == nextSetNumber } ?: lastSets.lastOrNull()
                    if (refSet != null) {
                        val rirText = if (refSet.isRirEnabled) "RIR ${refSet.rir}" else "Fijo"
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Objetivo a superar: ${refSet.weightKg}kg × ${refSet.reps} reps · $rirText",
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (loggedSets.isNotEmpty()) {
                    loggedSets.forEach { set ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${set.setNumber}",
                                color = TextGray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(24.dp)
                            )
                            Text(
                                text = "${set.weightKg} kg",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${set.reps} reps",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (set.isRirEnabled) "RIR ${set.rir}" else "-",
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteSet(set.setId) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Borrar serie",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isCompleted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ejercicio Completado",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    if (newValue.length <= 5) weightInput = newValue
                                }
                            },
                            label = { Text("Kg", color = TextGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = TextGray,
                                cursorColor = GoldAccent
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showRepsMenu = true },
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "$repsInput Reps",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            DropdownMenu(
                                expanded = showRepsMenu,
                                onDismissRequest = { showRepsMenu = false },
                                modifier = Modifier.heightIn(max = 250.dp)
                            ) {
                                (1..20).forEach { repVal ->
                                    DropdownMenuItem(
                                        text = { Text("$repVal Reps") },
                                        onClick = {
                                            repsInput = repVal
                                            showRepsMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showRirMenu = true },
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isRirEnabled) "RIR $rirInput" else "Fijo",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            DropdownMenu(
                                expanded = showRirMenu,
                                onDismissRequest = { showRirMenu = false },
                                modifier = Modifier.heightIn(max = 250.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Fijo (Sin RIR)") },
                                    onClick = { isRirEnabled = false; showRirMenu = false }
                                )
                                (0..5).forEach { rirVal ->
                                    DropdownMenuItem(
                                        text = { Text("RIR $rirVal") },
                                        onClick = {
                                            isRirEnabled = true
                                            rirInput = rirVal
                                            showRirMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        val weight = weightInput.toFloatOrNull()
                        val isValidWeight = weight != null && weight in 0.0f..999.9f

                        IconButton(
                            onClick = {
                                if (weight != null) {
                                    val finalRir = if (isRirEnabled) rirInput else null
                                    onLogSet(
                                        LogSetRequest(
                                            slotId = slot.slotId,
                                            exerciseId = slot.exercise.id,
                                            setNumber = nextSetNumber,
                                            weightKg = weight,
                                            reps = repsInput,
                                            rir = finalRir,
                                            isRirEnabled = isRirEnabled
                                        )
                                    )
                                    weightInput = ""
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = if (isValidWeight) GoldAccent else FieldDark,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Guardar",
                                tint = if (isValidWeight) Color.Black else TextGray
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = DividerGray)
    }
}