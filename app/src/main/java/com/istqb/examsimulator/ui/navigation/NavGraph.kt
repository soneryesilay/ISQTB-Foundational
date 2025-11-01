package com.istqb.examsimulator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.istqb.examsimulator.ui.dashboard.DashboardScreen
import com.istqb.examsimulator.ui.dashboard.DashboardViewModel
import com.istqb.examsimulator.ui.exam.ExamScreen
import com.istqb.examsimulator.ui.exam.ExamViewModel
import com.istqb.examsimulator.ui.questionsets.QuestionSetListScreen
import com.istqb.examsimulator.ui.questionsets.QuestionSetViewModel
import com.istqb.examsimulator.ui.result.ResultScreen
import com.istqb.examsimulator.ui.result.ResultViewModel
import com.istqb.examsimulator.ui.review.ReviewScreen
import com.istqb.examsimulator.ui.review.ReviewViewModel
import com.istqb.examsimulator.ui.setup.ExamSetupScreen
import com.istqb.examsimulator.ui.setup.ExamSetupViewModel

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object ExamSetup : Screen("exam_setup")
    object PracticeSetup : Screen("practice_setup")
    object Exam : Screen("exam") {
        const val ARG_ATTEMPT_ID = "attemptId"
        val routeWithArgs = "$route/{$ARG_ATTEMPT_ID}"
    }
    object Result : Screen("result") {
        const val ARG_ATTEMPT_ID = "attemptId"
        val routeWithArgs = "$route/{$ARG_ATTEMPT_ID}"
    }
    object Review : Screen("review") {
        const val ARG_ATTEMPT_ID = "attemptId"
        val routeWithArgs = "$route/{$ARG_ATTEMPT_ID}"
    }
    object QuestionSets : Screen("question_sets")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    examSetupViewModel: ExamSetupViewModel,
    examViewModel: ExamViewModel,
    resultViewModel: ResultViewModel,
    reviewViewModel: ReviewViewModel,
    questionSetViewModel: QuestionSetViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onQuickExamClick = {
                    navController.navigate(Screen.ExamSetup.route)
                },
                onPracticeModeClick = {
                    navController.navigate(Screen.PracticeSetup.route)
                },
                onQuestionSetsClick = {
                    navController.navigate(Screen.QuestionSets.route)
                }
            )
        }

        composable(Screen.ExamSetup.route) {
            ExamSetupScreen(
                viewModel = examSetupViewModel,
                isPracticeMode = false,
                onNavigateToExam = { attemptId: String ->
                    navController.navigate("${Screen.Exam.route}/$attemptId") {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PracticeSetup.route) {
            ExamSetupScreen(
                viewModel = examSetupViewModel,
                isPracticeMode = true,
                onNavigateToExam = { attemptId: String ->
                    navController.navigate("${Screen.Exam.route}/$attemptId") {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Exam.routeWithArgs) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getString(Screen.Exam.ARG_ATTEMPT_ID) ?: return@composable
            ExamScreen(
                viewModel = examViewModel,
                attemptId = attemptId,
                onExamFinished = { finishedAttemptId ->
                    navController.navigate("${Screen.Result.route}/$finishedAttemptId") {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Result.routeWithArgs) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getString(Screen.Result.ARG_ATTEMPT_ID) ?: return@composable
            ResultScreen(
                viewModel = resultViewModel,
                attemptId = attemptId,
                onNavigateToReview = { reviewAttemptId ->
                    navController.navigate("${Screen.Review.route}/$reviewAttemptId")
                },
                onNavigateBack = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Review.routeWithArgs) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getString(Screen.Review.ARG_ATTEMPT_ID) ?: return@composable
            ReviewScreen(
                viewModel = reviewViewModel,
                attemptId = attemptId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.QuestionSets.route) {
            QuestionSetListScreen(
                viewModel = questionSetViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

