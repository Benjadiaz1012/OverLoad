package com.pdm0126.overload.Interfaz.screens.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.Interfaz.components.TopBar
import com.pdm0126.overload.domain.TechnicalDictionary
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.model.MuscleDistribution
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val FieldDark = Color(0xFF222222)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)

private val barPalette = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
    Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1)
)

@Composable
fun Analysis(
    viewModel: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory),
    onNavigateToLibrary: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = { TopBar(title = "Análisis") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PillTabRow(
                tabs = listOf("Distribución", "Evolución"),
                selectedIndex = uiState.selectedTabIndex,
                onSelect = { viewModel.onTabSelected(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (uiState.selectedTabIndex == 0) {
                        DistributionSection(muscleDistribution = uiState.muscleDistribution)
                    } else {
                        EvolutionSection(
                            uiState = uiState,
                            onModeChanged = viewModel::onEvolutionModeChanged,
                            onNavigateToLibrary = onNavigateToLibrary,
                            onClearExercise = { viewModel.selectExercise(null) },
                            onMuscleGroupSelected = { viewModel.selectMuscleGroup(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PillTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) GoldAccent else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.Black else TextGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DistributionSection(muscleDistribution: List<MuscleDistribution>) {
    val distributionByGroup = muscleDistribution.associateBy { it.muscleGroup.lowercase() }
    val fullDistribution = TechnicalDictionary.mainMuscleGroupsList.map { group ->
        distributionByGroup[group.lowercase()] ?: MuscleDistribution(group, 0f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Volumen Efectivo por Grupo Muscular",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (muscleDistribution.isEmpty()) {
                EmptyHint(text = "Aún no tienes entrenamientos registrados.")
            } else {
                BarChart(
                    data = fullDistribution,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }
        }
    }
}

@Composable
private fun BarChart(
    data: List<MuscleDistribution>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.maxOfOrNull { it.totalEffectiveVolume } ?: 1f).coerceAtLeast(1f)

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Text(
                    text = "%,.0f kg".format(item.totalEffectiveVolume),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                val barHeightFraction = item.totalEffectiveVolume / maxValue

                Box(
                    modifier = Modifier
                        .fillMaxHeight(barHeightFraction.coerceIn(0.05f, 1f))
                        .width(28.dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(barPalette[index % barPalette.size])
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.muscleGroup.replaceFirstChar { it.uppercase() },
                    color = TextGray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EvolutionSection(
    uiState: AnalysisUiState,
    onModeChanged: (EvolutionMode) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onClearExercise: () -> Unit,
    onMuscleGroupSelected: (String?) -> Unit
) {
    Column {
        EvolutionModeSelector(
            selected = uiState.evolutionMode,
            onSelect = onModeChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState.evolutionMode) {
            EvolutionMode.EXERCISE -> {
                val selectedExercise =
                    uiState.availableExercises.find { it.id == uiState.selectedExerciseId }
                ExerciseSelectorCard(
                    selectedExercise = selectedExercise,
                    onNavigateToLibrary = onNavigateToLibrary,
                    onClear = onClearExercise
                )
            }

            EvolutionMode.MUSCLE_GROUP -> {
                MuscleGroupSelector(
                    options = uiState.availableMuscleGroups,
                    selected = uiState.selectedMuscleGroup,
                    onSelect = onMuscleGroupSelected
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sobrecarga Progresiva",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.evolutionProgression.isEmpty()) {
                    EmptyHint(
                        text = if (uiState.evolutionMode == EvolutionMode.EXERCISE) {
                            "Selecciona un ejercicio para ver su progreso."
                        } else {
                            "Selecciona un grupo muscular para ver su progreso."
                        }
                    )
                } else {
                    LineChart(
                        points = uiState.evolutionProgression,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EvolutionModeSelector(
    selected: EvolutionMode,
    onSelect: (EvolutionMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ModeChip(
            label = "Por ejercicio",
            isSelected = selected == EvolutionMode.EXERCISE,
            onClick = { onSelect(EvolutionMode.EXERCISE) },
            modifier = Modifier.weight(1f)
        )
        ModeChip(
            label = "Por músculo",
            isSelected = selected == EvolutionMode.MUSCLE_GROUP,
            onClick = { onSelect(EvolutionMode.MUSCLE_GROUP) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) GoldAccent else CardDark)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextGray,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ExerciseSelectorCard(
    selectedExercise: Exercise?,
    onNavigateToLibrary: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        onClick = onNavigateToLibrary,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = selectedExercise?.name ?: "Buscar ejercicio en la biblioteca",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (selectedExercise != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitar selección",
                    tint = TextGray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onClear() }
                )
            }
        }
    }
}

@Composable
private fun MuscleGroupSelector(
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    if (options.isEmpty()) {
        EmptyHint(text = "Aún no hay grupos musculares con datos registrados.")
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScrollChips(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { muscle ->
            val isSelected = muscle == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) GoldAccent else FieldDark)
                    .clickable { onSelect(if (isSelected) null else muscle) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = muscle.replaceFirstChar { it.uppercase() },
                    color = if (isSelected) Color.Black else TextGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun Modifier.horizontalScrollChips(): Modifier = this.then(
    Modifier.horizontalScroll(rememberScrollState())
)

@Composable
private fun LineChart(
    points: List<ProgressionPoint>,
    modifier: Modifier = Modifier
) {
    val maxValue = (points.maxOfOrNull { it.volume } ?: 1f).coerceAtLeast(1f)
    val minValue = (points.minOfOrNull { it.volume } ?: 0f)
    val range = (maxValue - minValue).coerceAtLeast(1f)

    val sdf = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (points.size < 2) return@Canvas

            val stepX = size.width / (points.size - 1)
            val path = Path()

            points.forEachIndexed { index, point ->
                val x = index * stepX
                val normalized = (point.volume - minValue) / range
                val y = size.height - (normalized * size.height)

                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = GoldAccent,
                style = Stroke(width = 4f, pathEffect = PathEffect.cornerPathEffect(8f))
            )

            points.forEachIndexed { index, point ->
                val x = index * stepX
                val normalized = (point.volume - minValue) / range
                val y = size.height - (normalized * size.height)
                drawCircle(color = GoldAccent, radius = 6f, center = Offset(x, y))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val firstDate = points.firstOrNull()?.timestamp?.let { sdf.format(Date(it)) } ?: ""
            val lastDate = points.lastOrNull()?.timestamp?.let { sdf.format(Date(it)) } ?: ""
            Text(text = firstDate, color = TextGray, fontSize = 11.sp)
            Text(text = lastDate, color = TextGray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TextGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
