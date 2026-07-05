package com.pdm0126.overload.screens.training


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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)
private val DividerGray = Color(0xFF2E2E2E)


data class ExerciseLastResult(
    val weight: String,
    val reps: String
)

data class ExerciseItem(
    val id: String,
    val name: String,
    val muscleImageRes: Int? = null,
    val lastResult: ExerciseLastResult
)

data class WorkoutSummary(
    val date: String,
    val durationMin: String,
    val volumeKg: String
)

private val fakeExercises = listOf(
    ExerciseItem(
        id = "press_banca",
        name = "Press de Banca Plano",
        lastResult = ExerciseLastResult("80 kg", "8 reps")
    ),
    ExerciseItem(
        id = "press_militar",
        name = "Press Militar con Mancuernas",
        lastResult = ExerciseLastResult("22.5 kg", "10 reps")
    ),
    ExerciseItem(
        id = "fondos_paralelas",
        name = "Fondos en Paralelas",
        lastResult = ExerciseLastResult("Peso corporal", "12 reps")
    ),
)

private val fakeSummary = WorkoutSummary(
    date = "12 de mayo, 2024 - 10:45 AM",
    durationMin = "68 min",
    volumeKg = "8,450 kg"
)

@Composable
fun Training(
    dayTitle: String = "Lunes - Push",
    exerciseCount: Int = fakeExercises.size,
    summary: WorkoutSummary = fakeSummary,
    exercises: List<ExerciseItem> = fakeExercises,
    onMenuClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onDaySelectorClick: () -> Unit = {},
    onExerciseClick: (ExerciseItem) -> Unit = {},
    onStartSession: () -> Unit = {},
    onNext: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(onMenuClick = onMenuClick, onCalendarClick = onCalendarClick)
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 55.dp)
            ) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Iniciar Sesión de entrenamiento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DaySelectorSection(
                    dayTitle = dayTitle,
                    exerciseCount = exerciseCount,
                    onClick = onDaySelectorClick
                )
            }

            item {
                LastSessionSummaryCard(summary = summary)
            }

            items(exercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
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
            .padding(horizontal = 16.dp, vertical = 20.dp),
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
private fun LastSessionSummaryCard(summary: WorkoutSummary) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lo último realizado",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = summary.date,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            StatColumn(label = "Duración", value = summary.durationMin)

            Spacer(modifier = Modifier.width(16.dp))

            StatColumn(label = "Volumen", value = summary.volumeKg)
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, color = TextGray, fontSize = 12.sp)
        Text(text = value, color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Accessibility,
                        contentDescription = "Mapa muscular",
                        tint = GoldAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = exercise.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Lo último realizado",
                color = TextGray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ExerciseStat(
                    icon = Icons.Default.ShoppingBag,
                    value = exercise.lastResult.weight,
                    label = "Peso",
                    modifier = Modifier.weight(1f)
                )
                ExerciseStat(
                    icon = Icons.Default.Repeat,
                    value = exercise.lastResult.reps,
                    label = "Repeticiones",
                    modifier = Modifier.weight(1f)
                )
            }
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
