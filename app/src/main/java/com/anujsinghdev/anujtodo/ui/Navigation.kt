package com.anujsinghdev.anujtodo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.anujsinghdev.anujtodo.ui.login.LoginScreen
import com.anujsinghdev.anujtodo.ui.list_detail.ListDetailScreen
import com.anujsinghdev.anujtodo.ui.todo_list.TodoListScreen
import com.anujsinghdev.anujtodo.ui.util.Screen
import com.anujsinghdev.anujtodo.ui.my_day.MyDayScreen
import com.anujsinghdev.anujtodo.ui.pomodoro.PomodoroScreen // Import new screen


@Composable
fun Navigation(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(navController = navController)
        }

        composable(route = Screen.TodoListScreen.route) {
            TodoListScreen(navController = navController)
        }

        composable(route = Screen.TodoListScreen.route) {
            TodoListScreen(navController = navController)
        }

        // --- NEW ---
        composable(route = Screen.ArchiveScreen.route) {
            com.anujsinghdev.anujtodo.ui.archive.ArchiveScreen(navController = navController)
        }

        composable(route = Screen.ArchiveScreen.route) {
            com.anujsinghdev.anujtodo.ui.archive.ArchiveScreen(navController = navController)
        }

        // --- NEW ROUTE ---
        composable(route = Screen.MyDayScreen.route) {
            MyDayScreen(navController = navController)
        }

        // New Route for List Details
        composable(
            route = Screen.ListDetailScreen.route,
            arguments = listOf(
                navArgument("listId") { type = NavType.LongType },
                navArgument("listName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
            val listName = backStackEntry.arguments?.getString("listName") ?: "List"

            ListDetailScreen(
                navController = navController,
                listId = listId,
                listName = listName
            )
        }

        // --- NEW POMODORO ROUTE ---
        composable(route = Screen.PomodoroScreen.route) {
            PomodoroScreen(navController = navController)
        }


    }
}