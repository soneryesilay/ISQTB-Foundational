package com.istqb.examsimulator.ui.questiondetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.istqb.examsimulator.data.model.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailScreen(
    viewModel: QuestionDetailViewModel,
    questionSetId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(questionSetId) {
        viewModel.loadQuestions(questionSetId)
    }

    val questions = viewModel.questions.collectAsState().value
    val selectedQuestion = viewModel.selectedQuestion.collectAsState().value
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (questions.isEmpty()) {
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
                title = { Text("Soru Detayları") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (selectedQuestion != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil")
                        }
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
            itemsIndexed(questions) { index, question ->
                QuestionDetailCard(
                    questionNumber = index + 1,
                    question = question,
                    isSelected = selectedQuestion?.id == question.id,
                    onClick = { viewModel.selectQuestion(question) }
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && selectedQuestion != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Soruyu Sil") },
            text = { Text("Bu soruyu silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteQuestion(selectedQuestion.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Edit dialog
    if (showEditDialog && selectedQuestion != null) {
        EditQuestionDialog(
            question = selectedQuestion,
            onDismiss = { showEditDialog = false },
            onSave = { updatedQuestion ->
                viewModel.updateQuestion(updatedQuestion)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailCard(
    questionNumber: Int,
    question: Question,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Question Number and Text
            Text(
                text = "Soru $questionNumber",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyMedium
            )

            // Image if exists
            if (!question.image.isNullOrBlank()) {
                AsyncImage(
                    model = question.image.replace("asset://", "file:///android_asset/"),
                    contentDescription = "Soru görseli",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                )
            }

            // Options
            Text(
                text = "Şıklar:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            question.options.entries.forEachIndexed { index, (key, optionText) ->
                val isCorrect = question.answer.contains(key)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val optionLabel = key
                    
                    Text(
                        text = "$optionLabel) ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCorrect) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCorrect) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    if (isCorrect) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Doğru cevap",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var questionText by remember { mutableStateOf(question.text) }
    var optionA by remember { mutableStateOf(question.options["a"] ?: "") }
    var optionB by remember { mutableStateOf(question.options["b"] ?: "") }
    var optionC by remember { mutableStateOf(question.options["c"] ?: "") }
    var optionD by remember { mutableStateOf(question.options["d"] ?: "") }
    var correctAnswer by remember { mutableStateOf(question.answer.firstOrNull() ?: "a") }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Soruyu Düzenle") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Soru Metni") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Text(
                    text = "Şıklar:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = optionA,
                    onValueChange = { optionA = it },
                    label = { Text("A Şıkkı") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = optionB,
                    onValueChange = { optionB = it },
                    label = { Text("B Şıkkı") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = optionC,
                    onValueChange = { optionC = it },
                    label = { Text("C Şıkkı") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = optionD,
                    onValueChange = { optionD = it },
                    label = { Text("D Şıkkı") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Doğru Cevap:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("a", "b", "c", "d").forEach { option ->
                        FilterChip(
                            selected = correctAnswer == option,
                            onClick = { correctAnswer = option },
                            label = { Text(option.uppercase()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedQuestion = question.copy(
                        text = questionText,
                        options = mapOf(
                            "a" to optionA,
                            "b" to optionB,
                            "c" to optionC,
                            "d" to optionD
                        ),
                        answer = listOf(correctAnswer)
                    )
                    onSave(updatedQuestion)
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

