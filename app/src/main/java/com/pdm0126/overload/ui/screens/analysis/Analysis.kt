package com.pdm0126.overload.ui.screens.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.pdm0126.overload.ui.components.OverloadScaffold

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val distributionModelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(uiState.muscleDistribution) {
        if (uiState.muscleDistribution.isNotEmpty()) {
            val volumes = uiState.muscleDistribution.map { it.totalEffectiveVolume }
            distributionModelProducer.runTransaction {
                columnModel { series(volumes) }
            }
        }
    }

    OverloadScaffold(
        title = "Análisis",
        showBackButton = false
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                            Text(
                                text = "Volumen Efectivo Total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Distribución del esfuerzo por grupo muscular",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            if (uiState.muscleDistribution.isEmpty()) {
                                Text(
                                    text = "Aún no hay datos suficientes. Completa algunas sesiones de entrenamiento para generar estadísticas.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 32.dp)
                                )
                            } else {
                                CartesianChartHost(
                                    chart = rememberCartesianChart(
                                        rememberColumnCartesianLayer(),
                                        startAxis = VerticalAxis.rememberStart(),
                                        bottomAxis = HorizontalAxis.rememberBottom()
                                    ),
                                    modelProducer = distributionModelProducer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

                }
            }
        }
    }
}
