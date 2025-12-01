package com.anujsinghdev.anujtodo.domain.usecase

import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository

class ToggleTodoUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todo: TodoItem) {
        val newCompletedState = !todo.isCompleted
        val completedTime = if (newCompletedState) System.currentTimeMillis() else null

        repository.updateTodo(
            todo.copy(
                isCompleted = newCompletedState,
                completedAt = completedTime
            )
        )
    }
}