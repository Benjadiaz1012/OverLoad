package com.pdm0126.overload.Interfaz.screens.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.overload.domain.model.Exercise


private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)
private val DividerGray = Color(0xFF2E2E2E)


@Composable
fun Training(
    viewModel: TrainingViewModel = viewModel(factory = TrainingViewModel.Factory),
    onMenuClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onDaySelectorClick: () -> Unit = {},
    onExerciseClick: (ExerciseDisplayItem) -> Unit = {},
    onSessionStarted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isSessionActiveForThisDay = uiState.activeSessionDayId != null &&
            uiState.activeSessionDayId == uiState.day?.dayId

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(onMenuClick = onMenuClick, onCalendarClick = onCalendarClick)
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
            ) {
                Button(
                    onClick = { viewModel.startWorkout(onSessionStarted) },
                    enabled = uiState.day != null && uiState.exercises.isNotEmpty() && !isSessionActiveForThisDay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSessionActiveForThisDay) "Sesión en curso" else "Iniciar Sesión de entrenamiento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
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
                    CircularProgressIndicator(color = GoldAccent)
                }
            }

            uiState.day == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes una rutina activa. Crea una desde el tab \"Rutinas\".",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DaySelectorSection(
                            dayTitle = uiState.day?.focus.orEmpty(),
                            exerciseCount = uiState.exercises.size,
                            onClick = onDaySelectorClick
                        )
                    }

                    items(uiState.exercises, key = { it.slot.slotId }) { item ->
                        ExerciseCard(
                            item = item,
                            onClick = { onExerciseClick(item) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onMenuClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
        }

        Text(
            text = "Entrenamiento",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        IconButton(onClick = onCalendarClick) {
            Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario", tint = Color.White)
        }
    }
}

@Composable
private fun DaySelectorSection(
    dayTitle: String,
    exerciseCount: Int,
    onClick: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Text(
                text = dayTitle,
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Cambiar día",
                tint = GoldAccent
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$exerciseCount ejercicios",
            color = TextGray,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun ExerciseCard(
    item: ExerciseDisplayItem,
    onClick: () -> Unit
) {
    val exercise = item.slot.exercise
    val lastSet = item.lastSets.lastOrNull()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                ExerciseThumbnail(exercise = exercise)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = exercise.muscleGroup,
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            if (lastSet != null) {
                Text(text = "Lo último realizado", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ExerciseStat(
                        icon = Icons.Default.FitnessCenter,
                        value = "${lastSet.weightKg} kg",
                        label = "Peso",
                        modifier = Modifier.weight(1f)
                    )
                    ExerciseStat(
                        icon = Icons.Default.Repeat,
                        value = "${lastSet.reps} reps",
                        label = "Repeticiones",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(text = "Objetivo (sin registros previos)", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ExerciseStat(
                        icon = Icons.Default.Repeat,
                        value = "${item.slot.targetSets} series",
                        label = "Series objetivo",
                        modifier = Modifier.weight(1f)
                    )
                    ExerciseStat(
                        icon = Icons.Default.FitnessCenter,
                        value = item.slot.targetReps?.let { "$it reps" } ?: "Al fallo",
                        label = "Repeticiones objetivo",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseThumbnail(exercise: Exercise) {
    val imageUrl = exercise.remoteImages.firstOrNull()

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A2A)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Accessibility,
                contentDescription = exercise.name,
                tint = GoldAccent,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun ExerciseStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = value, color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = TextGray, fontSize = 12.sp)
    }
}