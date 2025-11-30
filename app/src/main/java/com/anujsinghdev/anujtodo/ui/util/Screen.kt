package com.anujsinghdev.anujtodo.ui.util

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login_screen")
    object TodoListScreen : Screen("todo_list_screen")
    object ArchiveScreen : Screen("archive_screen")
    object MyDayScreen : Screen("my_day_screen")
    object PomodoroScreen : Screen("pomodoro_screen") // <--- New Route


    // New route with arguments
    object ListDetailScreen : Screen("list_detail_screen/{listId}/{listName}") {
        fun createRoute(listId: Long, listName: String): String {
            return "list_detail_screen/$listId/$listName"
        }
    }

}