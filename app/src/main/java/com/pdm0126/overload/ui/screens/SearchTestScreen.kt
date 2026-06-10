package com.pdm0126.overload.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.ui.viewmodels.test.SearchTestViewModel
import com.pdm0126.overload.ui.viewmodels.test.SearchTestUiState

@Composable
fun SearchTestScreen(
    viewModel: SearchTestViewModel = viewModel(factory = SearchTestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // BARRA DE BÚSQUEDA
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                label = { Text("Buscar en ExerciseDB (Inglés)") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.search(searchQuery) }) {
                Text("Buscar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RESULTADOS
        when (val state = uiState) {
            is SearchTestUiState.Idle -> {
                Text("Escribe el nombre de un ejercicio para buscarlo en la API.")
            }
            is SearchTestUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is SearchTestUiState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is SearchTestUiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.exercises) { exercise ->
                        ExerciseResultCard(
                            exercise = exercise,
                            onSaveClick = {
                                viewModel.saveExerciseToLocal(exercise) {
                                    Toast.makeText(context, "¡Guardado offline!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseResultCard(exercise: Exercise, onSaveClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, fontWeight = FontWeight.Bold)
                Text(text = "Músculo: ${exercise.muscleGroup}", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onSaveClick) {
                Text("Guardar")
            }
        }
    }
}

