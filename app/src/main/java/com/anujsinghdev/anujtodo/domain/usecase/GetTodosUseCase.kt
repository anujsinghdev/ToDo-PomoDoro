package com.anujsinghdev.anujtodo.domain.usecase

import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTodosUseCase(
    private val repository: TodoRepository
) {
    operator fun invoke(): Flow<List<TodoItem>> {
        // Here we can apply default sorting logic if needed before sending to UI
        return repository.getAllTodos().map { todos ->
            todos.sortedBy { it.isCompleted } // Example: Move completed items to bottom
        }
    }
}