package com.anujsinghdev.anujtodo.domain.usecase

import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository

class AddTodoUseCase(
    private val repository: TodoRepository
) {
    @Throws(Exception::class)
    suspend operator fun invoke(todo: TodoItem) {
        if (todo.title.isBlank()) {
            throw Exception("The title of the todo cannot be empty.")
        }
        repository.insertTodo(todo)
    }
}