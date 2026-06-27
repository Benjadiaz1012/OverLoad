package com.pdm0126.overload.ui.screens.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill // <-- 2. IMPORT DE LA CLASE FILL
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.pdm0126.overload.ui.components.OverloadScaffold

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val distributionModelProducer = remember { CartesianChartModelProducer() }

    val labelListKey = remember { ExtraStore.Key<List<String>>() }

    val labelFormatter = remember(uiState.muscleDistribution) {
        CartesianValueFormatter { context, x, _ ->
            val labels = context.model.extraStore.getOrNull(labelListKey)
            labels?.getOrNull(x.toInt())?.replaceFirstChar { it.uppercase() } ?: ""
        }
    }

    LaunchedEffect(uiState.muscleDistribution) {
        if (uiState.muscleDistribution.isNotEmpty()) {
            val volumes = uiState.muscleDistribution.map { it.totalEffectiveVolume }
            val muscleNames = uiState.muscleDistribution.map { it.muscleGroup }

            distributionModelProducer.runTransaction {
                columnModel { series(volumes) }
                extras { it[labelListKey] = muscleNames }
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
                                    text = "Aún no hay datos suficientes",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 32.dp)
                                )
                            } else {
                                CartesianChartHost(
                                    chart = rememberCartesianChart(
                                        rememberColumnCartesianLayer(
                                            columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                                rememberLineComponent(
                                                    fill = Fill(MaterialTheme.colorScheme.primary),
                                                    thickness = 16.dp,
                                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                                )
                                            )
                                        ),
                                        startAxis = VerticalAxis.rememberStart(),
                                        bottomAxis = HorizontalAxis.rememberBottom(
                                            valueFormatter = labelFormatter
                                        )
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