package com.pdm0126.overload.ui.screens.routines

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.domain.model.Blueprint
import com.pdm0126.overload.domain.model.BlueprintCatalog
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.ui.components.OverloadScaffold


@Composable
fun RoutinesScreen(
    viewModel: RoutineViewModel = viewModel(factory = RoutineViewModel.Factory),
    onNavigateToCreateRoutine: () -> Unit,
    onNavigateToRoutineEditor: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RoutinesListContent(
        state = state,
        onStartCreating = onNavigateToCreateRoutine,
        onNavigateToRoutineEditor = onNavigateToRoutineEditor
    )
}

@Composable
fun RoutinesListContent(
    state: RoutinesUiState,
    onStartCreating: () -> Unit,
    onNavigateToRoutineEditor: (Long) -> Unit
) {
    OverloadScaffold(
        title = "Mis Rutinas",
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartCreating,
                icon = { Icon(Icons.Default.Add, contentDescription = "Nueva Rutina") },
                text = { Text("Crear") },
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
                        text = "No tienes rutinas.\nCrea tu primer microciclo",
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
                        SavedMicrocycleCard(
                            microcycle = microcycle,
                            onNavigateToRoutineEditor = onNavigateToRoutineEditor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedMicrocycleCard(
    microcycle: RoutineMicrocycle,
    onNavigateToRoutineEditor: (Long) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = microcycle.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sistema Base: ${microcycle.blueprintType}\nLongitud: ${microcycle.days.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (microcycle.isActive) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Activa",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Único botón de acción que redirige al editor
                Button(
                    onClick = { onNavigateToRoutineEditor(microcycle.microcycleId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Editar",
                        fontWeight = FontWeight.Bold
                    )
                }

                microcycle.days.forEachIndexed { index, day ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(true)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = day.focus,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${day.slots.size} ejercicios",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlueprintSelectionScreen(
    viewModel: RoutineViewModel = viewModel(factory = RoutineViewModel.Factory),
    onBackClick: () -> Unit,
    onRoutineCreated: () -> Unit
) {
    var blueprintToConfirm by rememberSaveable { mutableStateOf<Blueprint?>(null) }

    OverloadScaffold(
        title = "Elige un Sistema",
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
                    text = "Selecciona una plantilla base. Podrás modificar los días y el nombre de tu rutina más adelante.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(BlueprintCatalog.systems) { blueprint ->
                Card(
                    onClick = { blueprintToConfirm = blueprint },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Seleccionar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text(blueprint.level.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.SignalCellularAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text(blueprint.goal.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.TrackChanges,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = blueprint.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Longitud",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${blueprint.minMicrocycleDays}-${blueprint.maxMicrocycleDays} días",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Frecuencia",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (blueprint.id == "blank") "Personalizada" else ("x${blueprint.maxFrequencyPerMuscle}/semana"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (blueprintToConfirm != null) {
        AlertDialog(
            onDismissRequest = { blueprintToConfirm = null },
            title = { Text("Crear Nueva Rutina") },
            text = { Text("¿Deseas crear un nuevo microciclo basado en el sistema ${blueprintToConfirm!!.name}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.createMicrocycleFromBlueprint(blueprintToConfirm!!)
                    blueprintToConfirm = null
                    onRoutineCreated()
                }) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { blueprintToConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

