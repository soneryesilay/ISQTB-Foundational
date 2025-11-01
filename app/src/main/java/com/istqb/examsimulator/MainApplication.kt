package com.istqb.examsimulator

import android.app.Application
import com.istqb.examsimulator.data.local.database.AppDatabase
import com.istqb.examsimulator.data.repository.QuestionRepository
import com.istqb.examsimulator.domain.usecase.LoadInitialQuestionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database and load initial questions
        applicationScope.launch {
            val database = AppDatabase.getDatabase(this@MainApplication)
            val questionRepository = QuestionRepository(
                database.questionDao(),
                database.questionSetDao()
            )
            val loadInitialQuestionsUseCase = LoadInitialQuestionsUseCase(
                questionRepository,
                this@MainApplication
            )
            
            // Load initial question sets from assets
            loadInitialQuestionsUseCase.execute()
        }
    }
}

