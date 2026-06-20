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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.pdm0126.overload.ui.components.BookmarkButton
import com.pdm0126.overload.ui.components.Error
import com.pdm0126.overload.ui.components.OverloadScaffold
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    OverloadScaffold(
        title = "Librería de Ejercicios",
        showBackButton = false,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SecondaryTabRow(
                selectedTabIndex = state.selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = state.selectedTabIndex == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("Guardados") }

                )
                Tab(
                    selected = state.selectedTabIndex == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("Explorar") }
                )
            }

            val isLocal = state.selectedTabIndex == 0
            OutlinedTextField(
                value = state.query,
                onValueChange = {  viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(if (isLocal) "Tu biblioteca" else "Buscar ejercicio") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChanged(""); focusManager.clearFocus() }
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = "Limpiar búsqueda",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (!isLocal) {
                            viewModel.searchRemoteExercises()
                            focusManager.clearFocus()
                        }
                    },
                    onDone = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            val muscleGroups = listOf("pecho", "espalda", "hombros", "bíceps", "tríceps", "pierna")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLocal) {
                    if (state.localExercises.isEmpty()) {
                        Text(
                            text = "No tienes ejercicios guardados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.localExercises, key = { it.id }) { exercise ->
                                ExerciseCard(
                                    exercise = exercise,
                                    isSaved = true,
                                    onSaveClick = {}
                                )
                            }
                        }
                    }
                } else {
                    when {
                        state.remoteState.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        state.remoteState.errorMessage != null -> {
                            Error(
                                onRetryClick = { viewModel.searchRemoteExercises() },
                                error = state.remoteState.errorMessage
                            )
                        }

                        state.remoteState.results.isEmpty() && state.query.isBlank() -> {
                            Text(
                                text = "...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.remoteState.results, key = { it.id }) { exercise ->
                                    val isSaved = state.localExercisesIds.contains(exercise.id)

                                    ExerciseCard(
                                        exercise = exercise,
                                        isSaved = isSaved,
                                        onSaveClick = {
                                            viewModel.saveExerciseToLocal(exercise)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Guardado en tu biblioteca",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
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

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isSaved: Boolean,
    onSaveClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth(),
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
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${exercise.muscleGroup.replaceFirstChar { it.uppercase() }} • ${exercise.mechanic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            BookmarkButton(
                isBookmarked = isSaved,
                onCheckedChange = { onSaveClick() }
            )
        }
    }
}