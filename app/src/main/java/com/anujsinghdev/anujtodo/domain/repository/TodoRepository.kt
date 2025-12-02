package com.anujsinghdev.anujtodo.domain.repository

import com.anujsinghdev.anujtodo.domain.model.FocusSession
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

    fun getAllFolders(): Flow<List<TodoFolder>>
    suspend fun insertFolder(folder: TodoFolder)

    fun getAllLists(): Flow<List<TodoList>>
    fun getListById(id: Long): Flow<TodoList> // New
    suspend fun insertList(list: TodoList): Long // Changed to return Long
    suspend fun updateListName(id: Long, name: String) // New
    suspend fun deleteListById(id: Long) // New

    suspend fun updateFolder(folder: TodoFolder) // <--- Add this
    suspend fun updateList(list: TodoList)

    fun getArchivedLists(): Flow<List<TodoList>> // <--- Add this

    suspend fun saveFocusSession(session: FocusSession)
    fun getFocusSessions(start: Long, end: Long): Flow<List<FocusSession>>
    fun getTotalFocusMinutes(): Flow<Int>

    fun getCompletedTaskCount(): Flow<Int> // <--- Add this

    suspend fun clearAllData() // Add this single method to wipe DB
}