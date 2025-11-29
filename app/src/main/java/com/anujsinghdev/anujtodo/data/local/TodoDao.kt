package com.anujsinghdev.anujtodo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.model.TodoFolder
import com.anujsinghdev.anujtodo.domain.model.TodoList
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    // --- Todos ---
    @Query("SELECT * FROM todo_items ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun getAllTodos(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE id = :id")
    suspend fun getTodoById(id: Int): TodoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoItem)

    @Update
    suspend fun updateTodo(todo: TodoItem)

    @Delete
    suspend fun deleteTodo(todo: TodoItem)

    // --- Folders ---
    @Query("SELECT * FROM todo_folders")
    fun getAllFolders(): Flow<List<TodoFolder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: TodoFolder)

    // --- Lists ---
    @Query("SELECT * FROM todo_lists")
    fun getAllLists(): Flow<List<TodoList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TodoList)
}