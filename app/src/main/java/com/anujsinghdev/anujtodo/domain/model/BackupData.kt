package com.anujsinghdev.anujtodo.domain.model

data class BackupData(
    val userName: String?,
    val userEmail: String?,
    val folders: List<TodoFolder>,
    val lists: List<TodoList>,
    val todos: List<TodoItem>,
    val focusSessions: List<FocusSession>
)