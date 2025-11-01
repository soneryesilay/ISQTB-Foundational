package com.istqb.examsimulator.ui.questionsets

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionSetListScreen(
    viewModel: QuestionSetViewModel,
    onNavigateBack: () -> Unit,
    onQuestionSetClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val questionSets = viewModel.questionSets.collectAsState().value
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    val fileName = uri.lastPathSegment ?: "imported_${System.currentTimeMillis()}.json"
                    viewModel.importQuestionSet(jsonString, fileName)
                }
            } catch (e: Exception) {
                viewModel.showError("İçe aktarma hatası: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soru Setleri") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                            }
                            filePickerLauncher.launch(intent)
                        }
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Dosyadan İçe Aktar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                    }
                    filePickerLauncher.launch(intent)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dosyadan İçe Aktar")
            }
        }
    ) { paddingValues ->
        if (questionSets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Henüz soru seti yok")
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(questionSets) { set ->
                    QuestionSetCard(
                        set = set,
                        onClick = { onQuestionSetClick(set.id) }
                    )
                }
            }
        }

        // Show error snackbar if needed
        val errorMessage = viewModel.error.collectAsState().value
        errorMessage?.let { message ->
            LaunchedEffect(message) {
                // Could show Snackbar here - for now just clear after delay
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionSetCard(
    set: com.istqb.examsimulator.data.model.QuestionSet,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = set.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Versiyon: ${set.version}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${set.questionCount} soru",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = dateFormat.format(Date(set.importTimestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

