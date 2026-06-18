package com.pdm0126.overload.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.ui.components.OverloadScaffold

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    OverloadScaffold(title = "Librería de Ejercicios", showBackButton = false) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Barra de Búsqueda
            OutlinedTextField(
                value = state.searchState.query,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar en internet...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged(""); focusManager.clearFocus() }) {
                            Icon(Icons.Default.Cancel, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.searchRemoteExercises(); focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Chips de Filtro
            if (state.searchState.remoteResults.isEmpty() && state.searchState.query.isBlank()) {
                val muscleGroups = listOf("pecho", "espalda", "hombros", "bíceps", "tríceps", "pierna")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(muscleGroups) { muscle ->
                        FilterChip(
                            selected = state.selectedMuscle == muscle,
                            onClick = { viewModel.onMuscleFilterSelected(muscle) },
                            label = { Text(muscle.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Contenido (Lista o Estados)
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.searchState.isSearching -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    state.searchState.errorMessage != null -> {
                        Text(
                            text = state.searchState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        val isShowingRemote = state.searchState.remoteResults.isNotEmpty()
                        val listToShow = if (isShowingRemote) state.searchState.remoteResults else state.localExercises

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isShowingRemote) {
                                item {
                                    Text("Resultados", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                            } else if (listToShow.isEmpty()) {
                                item {
                                    Text("No tienes ejercicios guardados en este grupo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            items(listToShow, key = { it.id }) { exercise ->
                                ExerciseCard(
                                    exercise = exercise,
                                    isRemote = isShowingRemote,
                                    onSaveClick = { viewModel.saveExerciseToLocal(exercise) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isRemote: Boolean,
    onSaveClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = exercise.remoteImages.firstOrNull(),
                contentDescription = exercise.name,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${exercise.muscleGroup.replaceFirstChar { it.uppercase() }} • ${exercise.mechanic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isRemote) {
                IconButton(onClick = onSaveClick) {
                    Icon(
                        Icons.Default.SaveAlt,
                        contentDescription = "Guardar Ejercicio",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Icon(
                    Icons.Filled.DownloadForOffline,
                    contentDescription = "Guardado local",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}