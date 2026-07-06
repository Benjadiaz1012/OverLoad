package com.pdm0126.overload.Interfaz.screens.routineEditor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.overload.Interfaz.components.ConfirmDialog
import com.pdm0126.overload.Interfaz.components.TopBar
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.model.RoutineSlot

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val FieldDark = Color(0xFF222222)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)
private val DividerGray = Color(0xFF2A2A2A)
private val ErrorRed = Color(0xFFE53935)

private val previewExercise = Exercise(
    id = "press_banca",
    name = "Press de Banca Plano",
    muscleGroup = "Pecho",
    mechanic = "Compuesto",
    targetMuscles = listOf("Pectoral"),
    secondaryMuscles = listOf("Tríceps"),
    equipment = "Barra",
    instructions = emptyList(),
    remoteImages = emptyList()
)

private val previewMicrocycle = RoutineMicrocycle(
    microcycleId = 1L,
    name = "Nuevo: Push / Pull / Legs",
    blueprintType = "PPL",
    isActive = true,
    days = listOf(
        RoutineDay(
            dayId = 1L,
            order = 0,
            focus = "Push",
            slots = listOf(
                RoutineSlot(
                    slotId = 1L,
                    order = 0,
                    targetSets = 4,
                    targetReps = 8,
                    exercise = previewExercise
                )
            )
        ),
        RoutineDay(dayId = 2L, order = 1, focus = "Pull", slots = emptyList())
    )
)

@Composable
fun RoutineEditor(
    microcycle: RoutineMicrocycle? = previewMicrocycle,
    isLoading: Boolean = false,
    onBack: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onSetActive: () -> Unit = {},
    onDeleteMicrocycle: () -> Unit = {},
    onAddDay: () -> Unit = {},
    onDayFocusChange: (Long, String) -> Unit = { _, _ -> },
    onDeleteDay: (Long) -> Unit = {},
    onAddExerciseToDay: (Long) -> Unit = {},
    onSlotTargetSetsChange: (Long, Int) -> Unit = { _, _ -> },
    onSlotTargetRepsChange: (Long, Int?) -> Unit = { _, _ -> },
    onRemoveSlot: (Long) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = microcycle?.name ?: "Editar rutina",
                showBackButton = true,
                onBackClick = onBack,
                trailingContent = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (microcycle != null && !microcycle.isActive) {
                                DropdownMenuItem(
                                    text = { Text("Marcar como activa") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = GoldAccent
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onSetActive()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Eliminar rutina") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = ErrorRed
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            }

            microcycle == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No se encontró la rutina.", color = TextGray, fontSize = 14.sp)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        MicrocycleNameField(
                            name = microcycle.name,
                            isActive = microcycle.isActive,
                            onNameChange = onNameChange
                        )
                    }

                    items(microcycle.days, key = { it.dayId }) { day ->
                        DayEditorCard(
                            day = day,
                            onFocusChange = { newFocus -> onDayFocusChange(day.dayId, newFocus) },
                            onDeleteDay = { onDeleteDay(day.dayId) },
                            onAddExercise = { onAddExerciseToDay(day.dayId) },
                            onSlotTargetSetsChange = onSlotTargetSetsChange,
                            onSlotTargetRepsChange = onSlotTargetRepsChange,
                            onRemoveSlot = onRemoveSlot
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = onAddDay,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Agregar día",
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Eliminar rutina",
            text = "Se eliminará esta rutina y todos sus días. Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            dismissText = "Cancelar",
            isDestructive = true,
            icon = Icons.Default.DeleteOutline,
            onConfirm = {
                onDeleteMicrocycle()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun MicrocycleNameField(
    name: String,
    isActive: Boolean,
    onNameChange: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rutina activa",
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = "Nombre de la rutina", color = TextGray, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = TextGray,
                cursorColor = GoldAccent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DayEditorCard(
    day: RoutineDay,
    onFocusChange: (String) -> Unit,
    onDeleteDay: () -> Unit,
    onAddExercise: () -> Unit,
    onSlotTargetSetsChange: (Long, Int) -> Unit,
    onSlotTargetRepsChange: (Long, Int?) -> Unit,
    onRemoveSlot: (Long) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = day.focus,
                    onValueChange = onFocusChange,
                    singleLine = true,
                    label = { Text("Enfoque del día", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = TextGray,
                        cursorColor = GoldAccent
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteDay) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar día",
                        tint = ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (day.slots.isEmpty()) {
                Text(text = "Sin ejercicios en este día", color = TextGray, fontSize = 13.sp)
            } else {
                day.slots.forEach { slot ->
                    SlotEditorRow(
                        slot = slot,
                        onTargetSetsChange = { newValue ->
                            onSlotTargetSetsChange(
                                slot.slotId,
                                newValue
                            )
                        },
                        onTargetRepsChange = { newValue ->
                            onSlotTargetRepsChange(
                                slot.slotId,
                                newValue
                            )
                        },
                        onRemove = { onRemoveSlot(slot.slotId) }
                    )
                    HorizontalDivider(color = DividerGray, thickness = 1.dp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onAddExercise) {
                Icon(Icons.Default.Add, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Agregar ejercicio",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SlotEditorRow(
    slot: RoutineSlot,
    onTargetSetsChange: (Int) -> Unit,
    onTargetRepsChange: (Int?) -> Unit,
    onRemove: () -> Unit
) {
    var setsInput by remember(slot.slotId) { mutableStateOf(slot.targetSets.toString()) }
    var repsInput by remember(slot.slotId) { mutableStateOf(slot.targetReps?.toString() ?: "") }

    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = slot.exercise.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitar ejercicio",
                    tint = ErrorRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = setsInput,
                onValueChange = { newValue ->
                    if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                        setsInput = newValue
                        newValue.toIntOrNull()?.let { onTargetSetsChange(it) }
                    }
                },
                label = { Text("Series", color = TextGray, fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = TextGray,
                    cursorColor = GoldAccent
                ),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = repsInput,
                onValueChange = { newValue ->
                    if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                        repsInput = newValue
                        onTargetRepsChange(newValue.toIntOrNull())
                    }
                },
                label = { Text("Reps (opcional)", color = TextGray, fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = TextGray,
                    cursorColor = GoldAccent
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
} 