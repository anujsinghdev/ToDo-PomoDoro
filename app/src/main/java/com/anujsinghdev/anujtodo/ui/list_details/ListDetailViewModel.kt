package com.anujsinghdev.anujtodo.ui.list_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.data.local.UserPreferencesRepository
import com.anujsinghdev.anujtodo.domain.model.RepeatMode
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.model.TodoList
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val SMART_LIST_COMPLETED_ID = -2L

enum class SortOption {
    IMPORTANCE, DUE_DATE, ALPHABETICAL, CREATION_DATE
}

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val userPrefs: UserPreferencesRepository // 1. Inject Preferences
) : ViewModel() {

    // 2. Replace MutableStateFlow with StateFlow from DataStore
    val currentSortOption = userPrefs.sortOption
        .map { savedOption ->
            try {
                SortOption.valueOf(savedOption)
            } catch (e: Exception) {
                SortOption.CREATION_DATE
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SortOption.CREATION_DATE
        )

    // --- FIX: Observe List Name from DB ---
    fun getListNameFlow(listId: Long, defaultName: String): Flow<String> {
        return if (listId == SMART_LIST_COMPLETED_ID) {
            flowOf(defaultName)
        } else {
            repository.getListById(listId)
                .map { it.name }
                .catch { emit(defaultName) }
        }
    }

    val groupedCompletedTasks = combine(
        repository.getAllTodos(),
        repository.getAllLists()
    ) { todos, lists ->
        val completed = todos.filter { it.isCompleted }
        val listNameMap = lists.associate { it.id to it.name }
        completed.groupBy { task -> task.listId?.let { listNameMap[it] } ?: "Tasks" }.toSortedMap()
    }

    fun getTasksForList(listId: Long): Flow<List<TodoItem>> {
        return combine(repository.getAllTodos(), currentSortOption) { allTodos, sortOption ->
            val filtered = if (listId == SMART_LIST_COMPLETED_ID) {
                allTodos.filter { it.isCompleted }
            } else {
                allTodos.filter { it.listId == listId }
            }

            when (sortOption) {
                SortOption.IMPORTANCE -> filtered.sortedWith(compareByDescending<TodoItem> { it.isFlagged }.thenByDescending { it.createdAt })

                // Sorted by Due Date Ascending (Earliest Date First: 1 Dec, 2 Dec...)
                SortOption.DUE_DATE -> filtered.sortedWith(
                    compareBy<TodoItem> { it.dueDate ?: Long.MAX_VALUE }
                        .thenByDescending { it.createdAt }
                )

                SortOption.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
                SortOption.CREATION_DATE -> filtered.sortedByDescending { it.createdAt }
            }
        }
    }

    // --- ACTIONS ---

    fun renameList(listId: Long, newName: String) {
        viewModelScope.launch {
            repository.updateListName(listId, newName)
        }
    }

    fun duplicateList(listId: Long, originalName: String) {
        viewModelScope.launch {
            val newListName = "$originalName (Copy)"
            val newListId = repository.insertList(TodoList(name = newListName))

            val originalTasks = repository.getAllTodos().first().filter { it.listId == listId }
            originalTasks.forEach { task ->
                repository.insertTodo(
                    task.copy(
                        id = 0,
                        listId = newListId,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteList(listId: Long, onDeletionFinished: () -> Unit) {
        viewModelScope.launch {
            val tasks = repository.getAllTodos().first().filter { it.listId == listId }
            tasks.forEach { repository.deleteTodo(it) }
            repository.deleteListById(listId)
            onDeletionFinished()
        }
    }

    fun addTask(title: String, dueDate: Long?, repeatMode: RepeatMode, listId: Long) {
        viewModelScope.launch {
            val targetId = if (listId == SMART_LIST_COMPLETED_ID) null else listId
            repository.insertTodo(TodoItem(title = title, dueDate = dueDate, repeatMode = repeatMode, listId = targetId))
        }
    }

    fun toggleTask(todo: TodoItem) = viewModelScope.launch { repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted)) }
    fun toggleFlag(todo: TodoItem) = viewModelScope.launch { repository.updateTodo(todo.copy(isFlagged = !todo.isFlagged)) }
    fun updateTask(todo: TodoItem) = viewModelScope.launch { repository.updateTodo(todo) }

    // 3. Update this function to save to DataStore
    fun updateSortOption(option: SortOption) {
        viewModelScope.launch {
            userPrefs.saveSortOption(option.name)
        }
    }

    fun archiveList(listId: Long) {
        viewModelScope.launch {
            val currentList = repository.getListById(listId).first()
            repository.updateList(currentList.copy(isArchived = true))
        }
    }

    fun deleteTask(todo: TodoItem) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }
}