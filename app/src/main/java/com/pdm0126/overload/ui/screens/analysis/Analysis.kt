package com.pdm0126.overload.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.pdm0126.overload.R
import com.pdm0126.overload.domain.TechnicalDictionary
import com.pdm0126.overload.ui.components.OverloadScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun rememberToolTipMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter = remember { DefaultCartesianMarker.ValueFormatter.default() }
) = rememberDefaultCartesianMarker(
    label = rememberTextComponent(
        background = rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(1.dp)
        )
    ),
    valueFormatter = valueFormatter
)

@Composable
fun AnalysisScreen(
    onNavigateToLibrary : () -> Unit,
    viewModel: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("Distribución", "Evolución")

    OverloadScaffold(
        title = "Análisis",
        showBackButton = false
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SecondaryTabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.selectedTabIndex == 0) {
                        item {
                            DistributionTab(uiState = uiState)
                        }
                    } else {
                        item {
                            EvolutionTab(
                                uiState = uiState,
                                onNavigateToLibrary = onNavigateToLibrary,
                                onClearSelection = { viewModel.selectExercise(null) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DistributionTab(uiState: AnalysisUiState) {
    val distributionModelProducer = remember { CartesianChartModelProducer() }
    val muscleListKey = remember { ExtraStore.Key<List<String>>() }

    val muscleFormatter = remember(uiState.muscleDistribution) {
        CartesianValueFormatter { context, x, _ ->
            val fullName = context.model.extraStore.getOrNull(muscleListKey)?.getOrNull(x.toInt())?.replaceFirstChar { it.uppercase() }
            TechnicalDictionary.getPaddedMuscleNameForChart(fullName)
        }
    }

    val distributionTooltipFormatter = remember(uiState.muscleDistribution) {
        DefaultCartesianMarker.ValueFormatter { context, targets ->
            val xIndex = targets.first().x.toInt()
            val fullName = context.model.extraStore.getOrNull(muscleListKey)?.getOrNull(xIndex)?.replaceFirstChar { it.uppercase() } ?: ""

            if (uiState.muscleDistribution.isEmpty()) {
                "Sin datos registrados"
            } else {
                val distributionRecord = uiState.muscleDistribution.find {
                    it.muscleGroup.equals(fullName.trim(), ignoreCase = true)
                }
                val volume = distributionRecord?.totalEffectiveVolume ?: 0f
                "$fullName: ${String.format(Locale.US, "%.1f", volume)} Kg"
            }
        }
    }

    LaunchedEffect(uiState.muscleDistribution) {
        val muscleNames = TechnicalDictionary.mainMuscleGroupsList
        val volumes = mutableListOf<Float>()
        val distributionMap = uiState.muscleDistribution.associateBy { it.muscleGroup.lowercase() }

        muscleNames.forEach { muscle ->
            val volume = distributionMap[muscle.lowercase()]?.totalEffectiveVolume ?: 0f
            volumes.add(volume)
        }
        distributionModelProducer.runTransaction {
            columnModel { series(volumes) }
            extras { it[muscleListKey] = muscleNames }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Volumen Efectivo Total",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Distribución ponderada por grupo muscular",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(MaterialTheme.colorScheme.primary),
                            thickness = 58.dp,
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                    ),
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = muscleFormatter,
                    labelRotationDegrees = -45f,
                    label = rememberTextComponent(
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                ),
                marker = rememberToolTipMarker(valueFormatter = distributionTooltipFormatter),
            ),
            modelProducer = distributionModelProducer,
            scrollState = rememberVicoScrollState(),
            zoomState = rememberVicoZoomState(zoomEnabled = true),
            modifier = Modifier
                .width(950.dp)
                .height(350.dp)
        )
    }
}

@Composable
fun EvolutionTab(
    uiState: AnalysisUiState,
    onNavigateToLibrary: () -> Unit,
    onClearSelection: () -> Unit
) {
    val trendModelProducer = remember { CartesianChartModelProducer() }
    val dateListKey = remember { ExtraStore.Key<List<String>>() }

    val dateFormatter = remember(uiState.exerciseProgression) {
        CartesianValueFormatter { context, x, _ ->
            val date = context.model.extraStore.getOrNull(dateListKey)?.getOrNull(x.toInt())
            if (date.isNullOrBlank()) "\u200B" else date
        }
    }

    val trendTooltipFormatter = remember(uiState.exerciseProgression) {
        DefaultCartesianMarker.ValueFormatter { context, targets ->
            val xIndex = targets.first().x.toInt()
            val date = context.model.extraStore.getOrNull(dateListKey)?.getOrNull(xIndex) ?: ""

            if (uiState.exerciseProgression.isEmpty()) {
                "Sin datos registrados"
            } else {
                val volume = uiState.exerciseProgression.getOrNull(xIndex)?.totalVolume ?: 0f
                "$date: ${String.format(Locale.US, "%.1f", volume)} Kg"
            }
        }
    }

    LaunchedEffect(uiState.exerciseProgression, uiState.selectedExerciseId) {
        val volumes: List<Float>
        val dates: List<String>

        if (uiState.exerciseProgression.isEmpty()) {
            volumes = listOf(0f, 0f, 0f, 0f, 0f)
            dates = listOf("\u200B", "\u200B", "\u200B", "\u200B", "\u200B")
        } else {
            volumes = uiState.exerciseProgression.map { it.totalVolume }
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            dates = uiState.exerciseProgression.map { sdf.format(Date(it.timestamp)) }
        }

        trendModelProducer.runTransaction {
            lineModel { series(volumes) }
            extras { it[dateListKey] = dates }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Sobrecarga Progresiva",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        val selectedExercise = uiState.availableExercises.find { it.id == uiState.selectedExerciseId }

        Spacer(modifier = Modifier.height(16.dp))

        ExerciseSelectorCard(
            selectedExercise = selectedExercise?.name,
            onNavigateToLibrary = onNavigateToLibrary,
            onClearSelection = onClearSelection
        )

        Spacer(modifier = Modifier.height(24.dp))

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = dateFormatter),
                marker = rememberToolTipMarker(valueFormatter = trendTooltipFormatter)
            ),
            modelProducer = trendModelProducer,
            scrollState = rememberVicoScrollState(),
            zoomState = rememberVicoZoomState(zoomEnabled = true),
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )
    }
}

@Composable
fun ExerciseSelectorCard(
    selectedExercise: String?,
    onNavigateToLibrary: () -> Unit,
    onClearSelection: () -> Unit
) {
    val isExerciseSelected = selectedExercise != null

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            onClick = onNavigateToLibrary,
            modifier = Modifier.fillMaxWidth(),
            shape = CardDefaults.elevatedShape,
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 12.dp,
                pressedElevation = 4.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isExerciseSelected) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Buscar ejercicio",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.search_insights_24px),
                            contentDescription = "Analizar ejercicio",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = selectedExercise ?: "Buscar ejercicio en la biblioteca",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                if (isExerciseSelected) {
                    IconButton(
                        onClick = onClearSelection,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quitar ejercicio",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (isExerciseSelected) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Toca arriba para cambiar de ejercicio",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}