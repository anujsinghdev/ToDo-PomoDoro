package com.anujsinghdev.anujtodo.domain.usecase

import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository

class DeleteTodoUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todo: TodoItem) {
        repository.deleteTodo(todo)
    }
}