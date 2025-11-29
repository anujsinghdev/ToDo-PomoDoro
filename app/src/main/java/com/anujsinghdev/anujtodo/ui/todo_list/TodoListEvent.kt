package com.anujsinghdev.anujtodo.ui.todo_list

import com.anujsinghdev.anujtodo.domain.model.TodoItem

sealed class TodoListEvent {
    data class DeleteTodo(val todo: TodoItem): TodoListEvent()
    data class OnToggleTodo(val todo: TodoItem): TodoListEvent()
    object UndoDelete: TodoListEvent()
}