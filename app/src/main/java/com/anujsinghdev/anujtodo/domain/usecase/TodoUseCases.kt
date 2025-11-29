package com.anujsinghdev.anujtodo.domain.usecase

data class TodoUseCases(
    val getTodos: GetTodosUseCase,
    val deleteTodo: DeleteTodoUseCase,
    val addTodo: AddTodoUseCase,
    val toggleTodo: ToggleTodoUseCase
)