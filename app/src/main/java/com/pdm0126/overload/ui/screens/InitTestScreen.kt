package com.pdm0126.overload.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.ui.viewmodels.test.InitTestUiState
import com.pdm0126.overload.ui.viewmodels.test.InitTestViewModel

@Composable
fun InitTestScreen(
    viewModel: InitTestViewModel = viewModel(factory = InitTestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is InitTestUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is InitTestUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is InitTestUiState.Success -> {
                    ExerciseList(
                        exercises = state.exercises
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseList(exercises: List<Exercise>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(exercises) { exercise ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Músculo principal: ${exercise.muscleGroup}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Mecánica: ${exercise.mechanic}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


