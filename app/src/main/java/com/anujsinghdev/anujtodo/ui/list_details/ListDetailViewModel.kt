package com.anujsinghdev.anujtodo.ui.list_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: TodoRepository
) : ViewModel() {

    private val _currentSortOption = MutableStateFlow(SortOption.CREATION_DATE)
    val currentSortOption = _currentSortOption.asStateFlow()

    // --- FIX: Observe List Name from DB ---
    // This ensures when you rename, the top bar updates immediately.
    fun getListNameFlow(listId: Long, defaultName: String): Flow<String> {
        return if (listId == SMART_LIST_COMPLETED_ID) {
            flowOf(defaultName)
        } else {
            repository.getListById(listId)
                .map { it.name }
                .catch { emit(defaultName) } // Fallback if deleted/error
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
        return combine(repository.getAllTodos(), _currentSortOption) { allTodos, sortOption ->
            val filtered = if (listId == SMART_LIST_COMPLETED_ID) {
                allTodos.filter { it.isCompleted }
            } else {
                allTodos.filter { it.listId == listId }
            }

            when (sortOption) {
                SortOption.IMPORTANCE -> filtered.sortedWith(compareByDescending<TodoItem> { it.isFlagged }.thenByDescending { it.createdAt })
                SortOption.DUE_DATE -> filtered.sortedWith(compareBy<TodoItem> { it.dueDate ?: Long.MAX_VALUE }.thenBy { it.createdAt })
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
            // 1. Create New List and get its ID
            val newListName = "$originalName (Copy)"
            val newListId = repository.insertList(TodoList(name = newListName))

            // 2. Copy tasks to the new list
            val originalTasks = repository.getAllTodos().first().filter { it.listId == listId }
            originalTasks.forEach { task ->
                repository.insertTodo(
                    task.copy(
                        id = 0, // Reset ID for new entry
                        listId = newListId,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteList(listId: Long, onDeletionFinished: () -> Unit) {
        viewModelScope.launch {
            // 1. Delete tasks inside
            val tasks = repository.getAllTodos().first().filter { it.listId == listId }
            tasks.forEach { repository.deleteTodo(it) }

            // 2. Delete the list itself
            repository.deleteListById(listId)

            // 3. Navigate back
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
    fun updateSortOption(option: SortOption) { _currentSortOption.value = option }

    fun archiveList(listId: Long) {
        viewModelScope.launch {
            // Fetch the current list first to keep other properties intact
            val currentList = repository.getListById(listId).first()
            repository.updateList(currentList.copy(isArchived = true))
        }
    }
}