package com.anujsinghdev.anujtodo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anujsinghdev.anujtodo.domain.model.FocusSession
import com.anujsinghdev.anujtodo.domain.model.TodoFolder
import com.anujsinghdev.anujtodo.domain.model.TodoItem
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
    @Query("SELECT * FROM todo_folders ORDER BY sortOrder ASC")
    fun getAllFolders(): Flow<List<TodoFolder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: TodoFolder)

    @Update
    suspend fun updateFolder(folder: TodoFolder)

    // --- Lists ---

    // 1. Update List Order
    @Update
    suspend fun updateList(list: TodoList)

    // 2. Get specific List
    @Query("SELECT * FROM todo_lists WHERE id = :id")
    fun getListById(id: Long): Flow<TodoList>

    // 3. Create List
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TodoList): Long

    // 4. Rename List
    @Query("UPDATE todo_lists SET name = :name WHERE id = :id")
    suspend fun updateListName(id: Long, name: String)

    // 5. Delete List
    @Query("DELETE FROM todo_lists WHERE id = :id")
    suspend fun deleteListById(id: Long)

    // 6. Get All Active Lists (Filtered by isArchived = 0)
    @Query("SELECT * FROM todo_lists WHERE isArchived = 0 ORDER BY sortOrder ASC")
    fun getAllLists(): Flow<List<TodoList>>

    // 7. Get Archived Lists
    @Query("SELECT * FROM todo_lists WHERE isArchived = 1 ORDER BY id DESC")
    fun getArchivedLists(): Flow<List<TodoList>>

    // --- Stats / Focus History ---
    @Insert
    suspend fun insertFocusSession(session: FocusSession)

    // Get sessions for a specific time range (e.g., this week)
    @Query("SELECT * FROM focus_sessions WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getFocusSessions(start: Long, end: Long): Flow<List<FocusSession>>

    // Get total minutes focused ever (For Gamification Level)
    @Query("SELECT SUM(durationMinutes) FROM focus_sessions")
    fun getTotalFocusMinutes(): Flow<Int?>

    // Get count of completed tasks for stats
    @Query("SELECT COUNT(*) FROM todo_items WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>

    // --- For Import/Restore (Clear Data) ---
    @Query("DELETE FROM todo_items")
    suspend fun deleteAllTodos()

    @Query("DELETE FROM todo_folders")
    suspend fun deleteAllFolders()

    @Query("DELETE FROM todo_lists")
    suspend fun deleteAllLists()

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAllFocusSessions()
}