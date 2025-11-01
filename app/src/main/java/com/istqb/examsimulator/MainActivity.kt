package com.istqb.examsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.istqb.examsimulator.data.local.database.AppDatabase
import com.istqb.examsimulator.data.repository.ExamRepository
import com.istqb.examsimulator.data.repository.QuestionRepository
import com.istqb.examsimulator.domain.usecase.LoadInitialQuestionsUseCase
import com.istqb.examsimulator.ui.dashboard.DashboardScreen
import com.istqb.examsimulator.ui.dashboard.DashboardViewModel
import com.istqb.examsimulator.ui.exam.ExamViewModel
import com.istqb.examsimulator.ui.navigation.AppNavHost
import com.istqb.examsimulator.ui.questiondetail.QuestionDetailViewModel
import com.istqb.examsimulator.ui.questionsets.QuestionSetListScreen
import com.istqb.examsimulator.ui.questionsets.QuestionSetViewModel
import com.istqb.examsimulator.ui.result.ResultScreen
import com.istqb.examsimulator.ui.result.ResultViewModel
import com.istqb.examsimulator.ui.review.ReviewScreen
import com.istqb.examsimulator.ui.review.ReviewViewModel
import com.istqb.examsimulator.ui.setup.ExamSetupScreen
import com.istqb.examsimulator.ui.setup.ExamSetupViewModel
import com.istqb.examsimulator.ui.theme.ISTQBExamSimulatorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for modern Android devices (notch/punch-hole support)
        enableEdgeToEdge()

        // Initialize database and repositories
        val database = AppDatabase.getDatabase(applicationContext)
        val questionRepository = QuestionRepository(database.questionDao(), database.questionSetDao())
        val examRepository = ExamRepository(database.examDao())
        val loadInitialQuestionsUseCase = LoadInitialQuestionsUseCase(questionRepository, applicationContext)

        setContent {
            ISTQBExamSimulatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Load initial questions on first launch
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            loadInitialQuestionsUseCase.execute()
                        }
                    }
                    
                    // Initialize ViewModels
                    val dashboardViewModel = remember { DashboardViewModel(examRepository) }
                    val examSetupViewModel = remember { ExamSetupViewModel(questionRepository, androidx.lifecycle.SavedStateHandle()) }
                    val examViewModel = remember { ExamViewModel(questionRepository, examRepository, androidx.lifecycle.SavedStateHandle()) }
                    val resultViewModel = remember { ResultViewModel(examRepository) }
                    val reviewViewModel = remember { ReviewViewModel(examRepository, questionRepository) }
                    val questionSetViewModel = remember { QuestionSetViewModel(questionRepository) }
                    val questionDetailViewModel = remember { QuestionDetailViewModel(questionRepository) }

                    AppNavHost(
                        navController = navController,
                        dashboardViewModel = dashboardViewModel,
                        examSetupViewModel = examSetupViewModel,
                        examViewModel = examViewModel,
                        resultViewModel = resultViewModel,
                        reviewViewModel = reviewViewModel,
                        questionSetViewModel = questionSetViewModel,
                        questionDetailViewModel = questionDetailViewModel
                    )
                }
            }
        }
    }
}

