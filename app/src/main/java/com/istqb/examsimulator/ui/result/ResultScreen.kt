package com.istqb.examsimulator.ui.result

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    attemptId: String,
    onNavigateToReview: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onRetryExam: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(attemptId) {
        viewModel.loadAttempt(attemptId)
    }

    val attempt = viewModel.attempt.collectAsState().value

    if (attempt == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sınav Sonucu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (attempt.passed) {
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    } else {
                        Color(0xFFF44336).copy(alpha = 0.2f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (attempt.passed) "GEÇTİ" else "KALDI",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (attempt.passed) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("%.1f%%", attempt.successPercent),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem("Toplam", attempt.totalQuestions.toString(), Modifier.weight(1f))
                StatItem("Doğru", attempt.correct.toString(), Modifier.weight(1f))
                StatItem("Yanlış", attempt.wrong.toString(), Modifier.weight(1f))
                StatItem("Boş", attempt.blank.toString(), Modifier.weight(1f))
            }

            // Time Info
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kullanılan Süre")
                    Text(
                        text = "${attempt.timeUsedMinutes} / ${attempt.durationMinutes} dakika",
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Actions
            Button(
                onClick = { onNavigateToReview(attemptId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Detaylı İnceleme")
            }

            if (!attempt.passed) {
                Button(
                    onClick = onRetryExam,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tekrar Dene")
                }
            }

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ana Sayfaya Dön")
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

