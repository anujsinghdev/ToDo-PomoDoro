package com.anujsinghdev.anujtodo.ui.todo_list

import com.anujsinghdev.anujtodo.domain.model.TodoItem

data class TodoListState(
    val todoList: List<TodoItem> = emptyList(),
    val recentlyDeletedTodo: TodoItem? = null
)