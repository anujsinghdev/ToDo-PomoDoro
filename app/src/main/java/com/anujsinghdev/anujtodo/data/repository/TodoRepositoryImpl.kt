package com.anujsinghdev.anujtodo.data.repository

import com.anujsinghdev.anujtodo.data.local.TodoDao
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.model.TodoFolder
import com.anujsinghdev.anujtodo.domain.model.TodoList
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class TodoRepositoryImpl(
    private val dao: TodoDao
) : TodoRepository {

    override fun getAllTodos(): Flow<List<TodoItem>> = dao.getAllTodos()

    override suspend fun getTodoById(id: Int): TodoItem? = dao.getTodoById(id)

    override suspend fun insertTodo(todo: TodoItem) = dao.insertTodo(todo)

    override suspend fun updateTodo(todo: TodoItem) = dao.updateTodo(todo)

    override suspend fun deleteTodo(todo: TodoItem) = dao.deleteTodo(todo)

    // New Implementations
    override fun getAllFolders(): Flow<List<TodoFolder>> = dao.getAllFolders()

    override suspend fun insertFolder(folder: TodoFolder) = dao.insertFolder(folder)

    override fun getAllLists(): Flow<List<TodoList>> = dao.getAllLists()

    override suspend fun insertList(list: TodoList) = dao.insertList(list)
}