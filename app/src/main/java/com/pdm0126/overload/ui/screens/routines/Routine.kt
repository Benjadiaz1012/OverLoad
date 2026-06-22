package com.pdm0126.overload.ui.screens.routines

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.domain.model.Blueprint
import com.pdm0126.overload.domain.model.BlueprintCatalog
import com.pdm0126.overload.ui.components.OverloadScaffold

// Enumerador interno para gestionar la animación del Wizard
private enum class WizardStep { LIST, BLUEPRINTS, DRAFT }

@Composable
fun RoutinesScreen(
    viewModel: RoutineViewModel = viewModel(factory = RoutineViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val currentStep = when {
        !state.isCreating -> WizardStep.LIST
        state.selectedBlueprint == null -> WizardStep.BLUEPRINTS
        else -> WizardStep.DRAFT
    }

    // Transición animada entre los pasos del Wizard
    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "wizard_transition"
    ) { step ->
        when (step) {
            WizardStep.LIST -> {
                RoutinesListContent(
                    state = state,
                    onStartCreating = viewModel::startCreating
                )
            }
            WizardStep.BLUEPRINTS -> {
                BlueprintSelectionContent(
                    onBackClick = viewModel::cancelCreating,
                    onBlueprintSelected = viewModel::selectBlueprint
                )
            }
            WizardStep.DRAFT -> {
                MicrocycleDraftContent(
                    state = state,
                    onBackClick = { viewModel.selectBlueprint(state.selectedBlueprint!!) }, // Truco: Al re-seleccionar, se resetea al paso anterior (o puedes crear un clearSelectedBlueprint)
                    onBackToBlueprints = { viewModel.startCreating() },
                    onNameChange = viewModel::updateDraftName,
                    onAddDay = { viewModel.addDraftDay() },
                    onRemoveDay = viewModel::removeDraftDay,
                    onDayFocusChange = viewModel::updateDraftDayFocus,
                    onSave = viewModel::saveDraftToDatabase
                )
            }
        }
    }
}

// VISTA 1: LISTA DE RUTINAS CONSOLIDADAS
@Composable
fun RoutinesListContent(
    state: RoutinesUiState,
    onStartCreating: () -> Unit
) {
    OverloadScaffold(
        title = "Mis Rutinas",
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartCreating,
                icon = { Icon(Icons.Default.Add, contentDescription = "Nueva Rutina") },
                text = { Text("Crear Microciclo") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.savedMicrocycles.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tienes rutinas activas.\n¡Crea tu primer microciclo!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.savedMicrocycles, key = { it.microcycleId }) { microcycle ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = microcycle.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (microcycle.isActive) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Activa",
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sistema Base: ${microcycle.blueprintType}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "${microcycle.days.size} días de entrenamiento",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// VISTA 2: SELECCIÓN DE BLUEPRINT (SISTEMA BASE)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlueprintSelectionContent(
    onBackClick: () -> Unit,
    onBlueprintSelected: (Blueprint) -> Unit
) {
    OverloadScaffold(
        title = "Paso 1: Elige un Sistema",
        showBackButton = true,
        onBackClick = onBackClick
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            item {
                Text(
                    text = "Selecciona una plantilla base. No te preocupes, podrás modificar los días y nombres en el siguiente paso.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(BlueprintCatalog.systems) { blueprint ->
                Card(
                    onClick = { onBlueprintSelected(blueprint) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Cabecera: Nombre y Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = blueprint.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Seleccionar", tint = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Badges de Nivel y Objetivo
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text(blueprint.level.label) },
                                leadingIcon = { Icon(Icons.Default.SignalCellularAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text(blueprint.goal.label) },
                                leadingIcon = { Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = blueprint.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Frecuencia y Días
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Longitud", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = blueprint.formattedMicrocycle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Frecuencia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "x${blueprint.maxFrequencyPerMuscle}/semana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Tags
                        if (blueprint.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                blueprint.tags.forEach { tag ->
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// VISTA 3: EL LIENZO DEL MICROCICLO (BORRADOR A GUARDAR)
@Composable
fun MicrocycleDraftContent(
    state: RoutinesUiState,
    onBackClick: () -> Unit,
    onBackToBlueprints: () -> Unit,
    onNameChange: (String) -> Unit,
    onAddDay: () -> Unit,
    onRemoveDay: (String) -> Unit,
    onDayFocusChange: (String, String) -> Unit,
    onSave: () -> Unit
) {
    val canSave = state.draftName.isNotBlank() && state.draftDays.isNotEmpty()

    OverloadScaffold(
        title = "Paso 2: Estructura",
        showBackButton = true,
        onBackClick = onBackToBlueprints,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSave,
                icon = { Icon(Icons.Default.Save, contentDescription = "Guardar") },
                text = { Text("Crear Rutina") },
                containerColor = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (canSave) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 16.dp),
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre de la Rutina
            item {
                OutlinedTextField(
                    value = state.draftName,
                    onValueChange = onNameChange,
                    label = { Text("Nombre del Microciclo") },
                    placeholder = { Text("Ej: Volumen Invierno 2026") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sistema base: ${state.selectedBlueprint?.name}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Lista de Días (Tarjetas Editables)
            itemsIndexed(state.draftDays, key = { _, day -> day.tempId }) { index, day ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Indicador de Día
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Input del Enfoque
                        OutlinedTextField(
                            value = day.focus,
                            onValueChange = { onDayFocusChange(day.tempId, it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )

                        // Botón Borrar
                        IconButton(onClick = { onRemoveDay(day.tempId) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Borrar Día", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Botón Agregar Día
            item {
                val canAddMore = state.draftDays.size < 9 // Límite funcional
                OutlinedButton(
                    onClick = onAddDay,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    enabled = canAddMore,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (canAddMore) "Añadir un Día extra" else "Límite de 9 días alcanzado")
                }
            }
        }
    }
}

