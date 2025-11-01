package com.istqb.examsimulator.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.istqb.examsimulator.data.model.ExamAttempt
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onQuickExamClick: () -> Unit,
    onPracticeModeClick: () -> Unit,
    onQuestionSetsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentAttempts = viewModel.recentAttempts.value
    val averageSuccess = viewModel.averageSuccess.value
    val totalAttempts = viewModel.totalAttempts.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ana Sayfa") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statistics Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Toplam Deneme",
                        value = totalAttempts.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Ortalama Başarı",
                        value = if (averageSuccess != null) {
                            String.format("%.1f%%", averageSuccess)
                        } else {
                            "-"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Mini Chart
            item {
                if (recentAttempts.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Son 5 Deneme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            MiniChart(recentAttempts)
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onQuickExamClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hızlı Sınav")
                    }
                    Button(
                        onClick = onPracticeModeClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pratik Mod")
                    }
                    OutlinedButton(
                        onClick = onQuestionSetsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Soru Setleri")
                    }
                }
            }

            // Recent Attempts List
            if (recentAttempts.isNotEmpty()) {
                item {
                    Text(
                        text = "Son Denemeler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(recentAttempts) { attempt ->
                    AttemptListItem(attempt)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MiniChart(attempts: List<ExamAttempt>) {
    if (attempts.isEmpty()) return

    val maxValue = 100.0
    val chartHeight = 100.dp
    val barWidth = 40.dp
    val spacing = 8.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        attempts.forEachIndexed { index, attempt ->
            val heightRatio = (attempt.successPercent / maxValue).coerceIn(0.0, 1.0).toFloat()
            val barHeight = chartHeight * heightRatio

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = spacing / 2)
            ) {
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(chartHeight)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            color = if (attempt.passed) Color(0xFF4CAF50) else Color(0xFFF44336),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                0f,
                                size.height - barHeight.toPx()
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                size.width,
                                barHeight.toPx()
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun AttemptListItem(attempt: ExamAttempt) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(attempt.createdAt)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%.1f%%", attempt.successPercent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = if (attempt.passed) Color(0xFF4CAF50) else Color(0xFFF44336),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (attempt.passed) "GEÇTİ" else "KALDI",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

