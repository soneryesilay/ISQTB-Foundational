package com.istqb.examsimulator.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamSetupScreen(
    viewModel: ExamSetupViewModel,
    isPracticeMode: Boolean,
    onNavigateToExam: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val availableSets = viewModel.availableSets.collectAsState().value
    val selectedSets = viewModel.selectedSets.collectAsState().value
    val totalQuestions = viewModel.totalQuestions.collectAsState().value
    val durationMinutes = viewModel.durationMinutes.collectAsState().value
    val passPercent = viewModel.passPercent.collectAsState().value
    val shuffleQuestions = viewModel.shuffleQuestions.collectAsState().value
    val shuffleOptions = viewModel.shuffleOptions.collectAsState().value
    val seed = viewModel.seed.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val error = viewModel.error.collectAsState().value

    val scope = rememberCoroutineScope()
    
    // Setup mode on first load
    LaunchedEffect(isPracticeMode) {
        viewModel.setupForMode(isPracticeMode)
    }
    
    var totalQuestionsText by remember { mutableStateOf(TextFieldValue(totalQuestions.toString())) }
    var durationText by remember { mutableStateOf(TextFieldValue(durationMinutes.toString())) }
    var passPercentText by remember { mutableStateOf(TextFieldValue(passPercent.toString())) }
    var seedText by remember { mutableStateOf(TextFieldValue(seed?.toString() ?: "")) }

    LaunchedEffect(totalQuestions) {
        if (totalQuestionsText.text != totalQuestions.toString()) {
            totalQuestionsText = TextFieldValue(totalQuestions.toString())
        }
    }

    LaunchedEffect(durationMinutes) {
        if (durationText.text != durationMinutes.toString()) {
            durationText = TextFieldValue(durationMinutes.toString())
        }
    }

    LaunchedEffect(passPercent) {
        if (passPercentText.text != passPercent.toString()) {
            passPercentText = TextFieldValue(passPercent.toString())
        }
    }

    LaunchedEffect(seed) {
        if (seedText.text != (seed?.toString() ?: "")) {
            seedText = TextFieldValue(seed?.toString() ?: "")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isPracticeMode) "Deneme Sınavı" else "Sınava Başla") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
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
            // Question Sets Selection
            item {
                Text(
                    text = "Soru Setleri",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                availableSets.forEach { set ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedSets.contains(set.sourceFileName),
                            onCheckedChange = { viewModel.toggleSetSelection(set.sourceFileName) }
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(set.title)
                            Text(
                                text = "${set.questionCount} soru",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Settings - Only for Practice Mode
            if (isPracticeMode) {
                // Total Questions
                item {
                    OutlinedTextField(
                        value = totalQuestionsText,
                        onValueChange = {
                            totalQuestionsText = it
                            it.text.toIntOrNull()?.let { num -> viewModel.setTotalQuestions(num) }
                        },
                        label = { Text("Soru Sayısı") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Duration
                item {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = {
                            durationText = it
                            it.text.toIntOrNull()?.let { num -> viewModel.setDurationMinutes(num) }
                        },
                        label = { Text("Süre (Dakika)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Pass Percent
                item {
                    OutlinedTextField(
                        value = passPercentText,
                        onValueChange = {
                            passPercentText = it
                            it.text.toIntOrNull()?.let { num -> viewModel.setPassPercent(num) }
                        },
                        label = { Text("Baraj (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Shuffle Options - Only for Practice Mode
            if (isPracticeMode) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Soruları Karıştır")
                        Switch(
                            checked = shuffleQuestions,
                            onCheckedChange = { viewModel.setShuffleQuestions(it) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Şıkları Karıştır")
                        Switch(
                            checked = shuffleOptions,
                            onCheckedChange = { viewModel.setShuffleOptions(it) }
                        )
                    }
                }
            }

            // Error Message
            error?.let {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Start Button
            item {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.startExam(isPracticeMode)?.let { (attemptId, config) ->
                                com.istqb.examsimulator.util.ExamConfigStore.saveConfig(attemptId, config)
                                onNavigateToExam(attemptId)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && selectedSets.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Başla")
                }
            }
        }
    }
}

