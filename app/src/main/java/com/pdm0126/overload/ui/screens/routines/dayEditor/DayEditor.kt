package com.pdm0126.overload.ui.screens.routines.dayEditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.ui.components.OverloadConfirmDialog
import com.pdm0126.overload.ui.components.OverloadInputDialog
import com.pdm0126.overload.ui.components.OverloadTopBar
import com.pdm0126.overload.domain.model.PlannedExercise

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
    var slotToRemove by remember { mutableStateOf<PlannedExercise?>(null) }
    val day = uiState.day

    LaunchedEffect(uiState.isLoading, day) {
        if (!uiState.isLoading && day == null) {
            onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OverloadTopBar(
                title = day?.focus ?: "Editar día",
                showBackButton = true,
                onBackClick = onBack,
                trailingIcon = Icons.Default.DeleteOutline,
                onTrailingClick = { showDeleteDialog = true },
                trailingContentDescription = "Eliminar día"
            )
        },
        bottomBar = {
            if (day != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
                ) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Listo", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    Text(
                        text = "No se encontró el día.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = day.focus,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Renombrar día",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (day.plannedExercises.isEmpty()) {
                        Text(
                            text = "Sin ejercicios en este día",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        day.plannedExercises.forEach { slot ->
                            SlotEditorRow(
                                slot = slot,
                                onExerciseClick = { onNavigateToExerciseDetail(slot.exercise.id) },
                                onTargetSetsChange = { newValue ->
                                    viewModel.updateTargetSets(
                                        slot.plannedExerciseId,
                                        newValue
                                    )
                                },
                                onTargetRepsChange = { newValue ->
                                    viewModel.updateTargetReps(
                                        slot.plannedExerciseId,
                                        newValue
                                    )
                                },
                                onRemove = { slotToRemove = slot }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (day.plannedExercises.size < MAX_SLOTS_PER_DAY) {
                        TextButton(onClick = onNavigateToLibrarySelection) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Agregar ejercicio",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp)
                            )
                        }
                    } else {
                        Text(
                            text = "Alcanzaste el máximo de $MAX_SLOTS_PER_DAY ejercicios por día.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
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
        OverloadConfirmDialog(
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
        OverloadConfirmDialog(
            title = "Quitar ejercicio",
            text = "¿Seguro que quieres quitar \"${slot.exercise.name}\" de este día?",
            confirmText = "Quitar",
            dismissText = "Cancelar",
            isDestructive = true,
            icon = Icons.Default.Close,
            onConfirm = {
                viewModel.removePlannedExercise(slot.plannedExerciseId)
                slotToRemove = null
            },
            onDismiss = { slotToRemove = null }
        )
    }
}

@Composable
private fun SlotEditorRow(
    slot: PlannedExercise,
    onExerciseClick: () -> Unit,
    onTargetSetsChange: (Int) -> Unit,
    onTargetRepsChange: (Int?) -> Unit,
    onRemove: () -> Unit
) {
    var setsInput by remember(slot.plannedExerciseId) { mutableStateOf(slot.targetSets.toString()) }
    var repsInput by remember(slot.plannedExerciseId) { mutableStateOf(slot.targetReps?.toString() ?: "") }

    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = slot.exercise.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onExerciseClick() }
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitar ejercicio",
                    tint = MaterialTheme.colorScheme.error,
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
                label = {
                    Text(
                        "Series",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
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
                label = {
                    Text(
                        "Reps (opcional)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}