package com.anujsinghdev.anujtodo.ui.todo_list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.data.local.UserPreferencesRepository
import com.anujsinghdev.anujtodo.domain.model.TodoFolder
import com.anujsinghdev.anujtodo.domain.model.TodoList
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import com.anujsinghdev.anujtodo.domain.usecase.TodoUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// Simple models for UI representation
data class UiFolder(
    val id: Long = 0,
    val name: String,
    val isExpanded: Boolean = false,
    val lists: List<UiTaskList> = emptyList()
)

data class UiTaskList(
    val id: Long = 0,
    val name: String,
    val count: Int = 0
)

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val todoUseCases: TodoUseCases,
    private val todoRepository: TodoRepository, // Added Repository
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = mutableStateOf(TodoListState())
    val state: State<TodoListState> = _state

    var userName = mutableStateOf("User")
    var userEmail = mutableStateOf("email@example.com")

    // UI State
    private val _folders = mutableStateListOf<UiFolder>()
    val folders: List<UiFolder> get() = _folders

    private val _rootLists = mutableStateListOf<UiTaskList>()
    val rootLists: List<UiTaskList> get() = _rootLists

    init {
        fetchUserProfile()
        observeFoldersAndLists()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            userName.value = userPrefs.userName.first() ?: "Anuj Singh"
            userEmail.value = userPrefs.userEmail.first() ?: "anuj@gmail.com"
        }
    }

    private fun observeFoldersAndLists() {
        viewModelScope.launch {
            // Combine Folders and Lists flows from DB
            combine(
                todoRepository.getAllFolders(),
                todoRepository.getAllLists()
            ) { dbFolders, dbLists ->

                // 1. Process Folders (and find their lists)
                val mappedFolders = dbFolders.map { folder ->
                    // Preserve expanded state if we already have it in memory
                    val isExpanded = _folders.find { it.id == folder.id }?.isExpanded ?: false

                    UiFolder(
                        id = folder.id,
                        name = folder.name,
                        isExpanded = isExpanded,
                        lists = dbLists.filter { it.folderId == folder.id }
                            .map { list -> UiTaskList(id = list.id, name = list.name) }
                    )
                }

                // 2. Process Root Lists (those without a folder)
                val mappedRootLists = dbLists.filter { it.folderId == null }
                    .map { list -> UiTaskList(id = list.id, name = list.name) }

                Pair(mappedFolders, mappedRootLists)

            }.collect { (newFolders, newRootLists) ->
                _folders.clear()
                _folders.addAll(newFolders)

                _rootLists.clear()
                _rootLists.addAll(newRootLists)
            }
        }
    }

    // --- Actions ---

    fun createFolder(name: String) {
        viewModelScope.launch {
            todoRepository.insertFolder(TodoFolder(name = name))
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            todoRepository.insertList(TodoList(name = name, folderId = null))
        }
    }

    fun addListToFolder(folderId: Long, listName: String) {
        viewModelScope.launch {
            todoRepository.insertList(TodoList(name = listName, folderId = folderId))
            // Expand the folder in UI so user sees the new list immediately
            val index = _folders.indexOfFirst { it.id == folderId }
            if (index != -1) {
                val folder = _folders[index]
                _folders[index] = folder.copy(isExpanded = true)
            }
        }
    }

    fun toggleFolderExpanded(folderId: Long) {
        val index = _folders.indexOfFirst { it.id == folderId }
        if (index != -1) {
            val folder = _folders[index]
            _folders[index] = folder.copy(isExpanded = !folder.isExpanded)
        }
    }
}