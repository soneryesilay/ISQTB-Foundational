package com.istqb.examsimulator.ui.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.istqb.examsimulator.data.model.Question
import com.istqb.examsimulator.ui.exam.ExamViewModel
import com.istqb.examsimulator.ui.components.QuestionWithTables
import com.istqb.examsimulator.ui.components.FullScreenImageDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    viewModel: ExamViewModel,
    attemptId: String,
    onExamFinished: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val examState = viewModel.examState.collectAsState().value
    var showFinishDialog by remember { mutableStateOf(false) }
    var showGridNavigator by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(attemptId) {
        viewModel.loadExamData(attemptId)
    }

    LaunchedEffect(examState.isFinished) {
        if (examState.isFinished) {
            onExamFinished(examState.attemptId)
        }
    }

    if (examState.questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentQuestion = examState.questions[examState.currentQuestionIndex]
    val currentAnswer = examState.answers[currentQuestion.id]
    val isFlagged = examState.flaggedQuestions.contains(currentQuestion.id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sınav", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${examState.currentQuestionIndex + 1} / ${examState.questions.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showFinishDialog = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (!examState.config?.unlimitedTime!!) {
                        Text(
                            text = viewModel.getFormattedTimeRemaining(),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (examState.timeRemainingSeconds < 300) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.toggleFlag() }) {
                        Icon(
                            imageVector = if (isFlagged) Icons.Default.Flag else Icons.Default.Flag,
                            contentDescription = "İşaretle",
                            tint = if (isFlagged) Color(0xFFFF6F00) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = { showGridNavigator = true }) {
                        Icon(Icons.Default.List, contentDescription = "Sorular")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { viewModel.previousQuestion() },
                        enabled = examState.currentQuestionIndex > 0
                    ) {
                        Text("Önceki")
                    }
                    Button(
                        onClick = {
                            if (examState.currentQuestionIndex < examState.questions.size - 1) {
                                viewModel.nextQuestion()
                            } else {
                                showFinishDialog = true
                            }
                        }
                    ) {
                        Text(
                            if (examState.currentQuestionIndex < examState.questions.size - 1) "Sonraki" else "Bitir"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Question Text
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    QuestionWithTables(text = currentQuestion.text)
                    
                    // Display image if exists (clickable for full screen)
                    if (!currentQuestion.image.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    fullScreenImageUrl = currentQuestion.image.replace("asset://", "file:///android_asset/")
                                    showFullScreenImage = true
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = currentQuestion.image.replace("asset://", "file:///android_asset/"),
                                    contentDescription = "Soru görseli - Tıklayın",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 200.dp, max = 400.dp),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                                Text(
                                    text = "🔍 Büyütmek için tıklayın",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Options
            val isMultiple = currentQuestion.type == "multiple"
            currentQuestion.options.toList().forEach { (key, text) ->
                val isSelected = currentAnswer?.selectedOptions?.contains(key) == true

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectAnswer(key, !isSelected)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isMultiple) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.selectAnswer(key, it) }
                            )
                        } else {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectAnswer(key, true) }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$key) $text",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (showFinishDialog) {
            FinishExamDialog(
                totalQuestions = examState.questions.size,
                answeredCount = examState.answers.values.count { it.selectedOptions.isNotEmpty() },
                onConfirm = {
                    showFinishDialog = false
                    viewModel.finishExam()
                },
                onDismiss = { showFinishDialog = false }
            )
        }

        if (showGridNavigator) {
            GridNavigatorDialog(
                questions = examState.questions,
                currentIndex = examState.currentQuestionIndex,
                answers = examState.answers,
                flaggedQuestions = examState.flaggedQuestions,
                onQuestionClick = { index ->
                    viewModel.navigateToQuestion(index)
                    showGridNavigator = false
                },
                onDismiss = { showGridNavigator = false }
            )
        }

        if (showFullScreenImage && fullScreenImageUrl != null) {
            FullScreenImageDialog(
                imageUrl = fullScreenImageUrl!!,
                onDismiss = { 
                    showFullScreenImage = false
                    fullScreenImageUrl = null
                }
            )
        }
    }
}

@Composable
fun FinishExamDialog(
    totalQuestions: Int,
    answeredCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sınavı Bitirmek İstiyor musunuz?") },
        text = {
            val blankCount = totalQuestions - answeredCount
            if (blankCount > 0) {
                Text("$blankCount soruyu boş bırakmak üzeresiniz. Sınavı bitirmek istiyor musunuz?")
            } else {
                Text("Sınavı bitirmek istiyor musunuz?")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Onayla")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun GridNavigatorDialog(
    questions: List<Question>,
    currentIndex: Int,
    answers: Map<Int, com.istqb.examsimulator.data.model.Answer>,
    flaggedQuestions: Set<Int>,
    onQuestionClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Sorular",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(questions) { index, question ->
                        val isAnswered = answers[question.id]?.selectedOptions?.isNotEmpty() == true
                        val isFlagged = flaggedQuestions.contains(question.id)
                        val isCurrent = index == currentIndex

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(
                                    color = when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isFlagged -> Color(0xFFFF6F00)
                                        isAnswered -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable { onQuestionClick(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = if (isCurrent || isFlagged) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

