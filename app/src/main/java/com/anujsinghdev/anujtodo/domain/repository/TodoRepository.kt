package com.anujsinghdev.anujtodo.domain.repository

import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.model.TodoFolder
import com.anujsinghdev.anujtodo.domain.model.TodoList
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getAllTodos(): Flow<List<TodoItem>>
    suspend fun getTodoById(id: Int): TodoItem?
    suspend fun insertTodo(todo: TodoItem)
    suspend fun updateTodo(todo: TodoItem)
    suspend fun deleteTodo(todo: TodoItem)

    // New methods
    fun getAllFolders(): Flow<List<TodoFolder>>
    suspend fun insertFolder(folder: TodoFolder)
    fun getAllLists(): Flow<List<TodoList>>
    suspend fun insertList(list: TodoList)
}