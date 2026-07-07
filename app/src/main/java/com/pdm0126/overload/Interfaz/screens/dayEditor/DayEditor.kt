package com.pdm0126.overload.Interfaz.screens.dayEditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.Interfaz.components.ConfirmDialog
import com.pdm0126.overload.Interfaz.components.OverloadInputDialog
import com.pdm0126.overload.Interfaz.components.TopBar
import com.pdm0126.overload.domain.model.RoutineSlot

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)
private val DividerGray = Color(0xFF2A2A2A)
private val ErrorRed = Color(0xFFE53935)

private const val MAX_SLOTS_PER_DAY = 12

@Composable
fun DayEditor(
    dayId: Long,
    onBack: () -> Unit = {},
    onNavigateToLibrarySelection: () -> Unit = {},
    onNavigateToExerciseDetail: (String) -> Unit = {},
    onDayDeleted: () -> Unit = {}
) {
    val viewModel: DayEditorViewModel = viewModel(
        factory = DayEditorViewModel.provideFactory(dayId),
        key = dayId.toString()
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var slotToRemove by remember { mutableStateOf<RoutineSlot?>(null) }
    val day = uiState.day

    LaunchedEffect(uiState.isLoading, day) {
        if (!uiState.isLoading && day == null) {
            onBack()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = day?.focus ?: "Editar día",
                showBackButton = true,
                onBackClick = onBack,
                trailingIcon = Icons.Default.DeleteOutline,
                onTrailingClick = { showDeleteDialog = true },
                trailingContentDescription = "Eliminar día"
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            }

            day == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No se encontró el día.", color = TextGray, fontSize = 14.sp)
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = day.focus,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Renombrar día",
                                tint = GoldAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (day.slots.isEmpty()) {
                        Text(
                            text = "Sin ejercicios en este día",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    } else {
                        day.slots.forEach { slot ->
                            SlotEditorRow(
                                slot = slot,
                                onExerciseClick = { onNavigateToExerciseDetail(slot.exercise.id) },
                                onTargetSetsChange = { newValue ->
                                    viewModel.updateTargetSets(
                                        slot.slotId,
                                        newValue
                                    )
                                },
                                onTargetRepsChange = { newValue ->
                                    viewModel.updateTargetReps(
                                        slot.slotId,
                                        newValue
                                    )
                                },
                                onRemove = { slotToRemove = slot }
                            )
                            HorizontalDivider(color = DividerGray, thickness = 1.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (day.slots.size < MAX_SLOTS_PER_DAY) {
                        TextButton(onClick = onNavigateToLibrarySelection) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Agregar ejercicio",
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Alcanzaste el máximo de $MAX_SLOTS_PER_DAY ejercicios por día.",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        OverloadInputDialog(
            title = "Renombrar día",
            initialValue = day?.focus ?: "",
            label = "Enfoque del día",
            onConfirm = { newName ->
                viewModel.updateDayName(newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Eliminar día",
            text = "Se eliminará este día y todos sus ejercicios. Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            dismissText = "Cancelar",
            isDestructive = true,
            icon = Icons.Default.DeleteOutline,
            onConfirm = {
                viewModel.deleteDay()
                showDeleteDialog = false
                onDayDeleted()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    slotToRemove?.let { slot ->
        ConfirmDialog(
            title = "Quitar ejercicio",
            text = "¿Seguro que quieres quitar \"${slot.exercise.name}\" de este día?",
            confirmText = "Quitar",
            dismissText = "Cancelar",
            isDestructive = true,
            icon = Icons.Default.Close,
            onConfirm = {
                viewModel.removeSlot(slot.slotId)
                slotToRemove = null
            },
            onDismiss = { slotToRemove = null }
        )
    }
}

@Composable
private fun SlotEditorRow(
    slot: RoutineSlot,
    onExerciseClick: () -> Unit,
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
                modifier = Modifier
                    .weight(1f)
                    .clickable { onExerciseClick() }
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