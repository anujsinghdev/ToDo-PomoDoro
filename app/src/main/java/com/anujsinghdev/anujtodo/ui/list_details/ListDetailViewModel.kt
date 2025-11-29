package com.anujsinghdev.anujtodo.ui.list_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.domain.model.RepeatMode
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

// Special ID for the "Completed" Smart List
const val SMART_LIST_COMPLETED_ID = -2L

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    fun getTasksForList(listId: Long): Flow<List<TodoItem>> {
        return repository.getAllTodos().map { allTodos ->
            if (listId == SMART_LIST_COMPLETED_ID) {
                // If this is the "Completed" screen, show ALL completed tasks
                allTodos.filter { it.isCompleted }
                    .sortedByDescending { it.id }
            } else {
                // Otherwise, show tasks for the specific list (Active & Completed)
                allTodos.filter { it.listId == listId }
                    .sortedWith(compareBy<TodoItem> { it.isCompleted }.thenByDescending { it.id })
            }
        }
    }

    fun addTask(title: String, dueDate: Long?, repeatMode: RepeatMode, listId: Long) {
        viewModelScope.launch {
            // If adding from "Completed" screen, strictly speaking we should probably ask which list,
            // but for now we default to no specific folder (root) or just ignore listId if it's -2.
            val targetListId = if (listId == SMART_LIST_COMPLETED_ID) null else listId

            val todo = TodoItem(
                title = title,
                dueDate = dueDate,
                repeatMode = repeatMode,
                listId = targetListId
            )
            repository.insertTodo(todo)
        }
    }

    fun toggleTask(todo: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun updateTask(todo: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(todo)
        }
    }
}