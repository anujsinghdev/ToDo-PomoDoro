package com.anujsinghdev.anujtodo.ui.todo_list

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.data.local.UserPreferencesRepository
import com.anujsinghdev.anujtodo.domain.model.TodoFolder
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.model.TodoList
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import com.anujsinghdev.anujtodo.domain.usecase.TodoUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Simple models for UI representation
// Updated to include sortOrder
data class UiFolder(
    val id: Long = 0,
    val name: String,
    val isExpanded: Boolean = false,
    val lists: List<UiTaskList> = emptyList(),
    val sortOrder: Int = 0 // <--- Added for sorting
)

// Updated to include sortOrder
data class UiTaskList(
    val id: Long = 0,
    val name: String,
    val count: Int = 0,
    val sortOrder: Int = 0 // <--- Added for sorting
)

// Data class to hold search results
data class TodoListSearchResults(
    val lists: List<TodoList> = emptyList(),
    val tasks: List<TodoItem> = emptyList()
)

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val todoUseCases: TodoUseCases,
    private val todoRepository: TodoRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    // --- User Info ---
    var userName = mutableStateOf("User")
    var userEmail = mutableStateOf("email@example.com")

    // --- Sort State (New) ---
    var isSortMode = mutableStateOf(false)

    // --- Search State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    // --- Combined Search Results Flow ---
    val searchResults: Flow<TodoListSearchResults> = combine(
        _searchQuery,
        todoRepository.getAllLists(),
        todoRepository.getAllTodos()
    ) { query, lists, tasks ->
        if (query.isBlank()) {
            TodoListSearchResults()
        } else {
            val filteredLists = lists.filter { it.name.contains(query, ignoreCase = true) }
            val filteredTasks = tasks.filter { it.title.contains(query, ignoreCase = true) }
            TodoListSearchResults(filteredLists, filteredTasks)
        }
    }

    // --- Folders & Lists State ---
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
            combine(
                todoRepository.getAllFolders(),
                todoRepository.getAllLists()
            ) { dbFolders, dbLists ->
                val mappedFolders = dbFolders.map { folder ->
                    val isExpanded = _folders.find { it.id == folder.id }?.isExpanded ?: false

                    // Map sortOrder and sort nested lists
                    UiFolder(
                        id = folder.id,
                        name = folder.name,
                        isExpanded = isExpanded,
                        sortOrder = folder.sortOrder,
                        lists = dbLists.filter { it.folderId == folder.id }
                            .map { list -> UiTaskList(id = list.id, name = list.name, sortOrder = list.sortOrder) }
                            .sortedBy { it.sortOrder } // Ensure nested lists are sorted
                    )
                }

                // Map sortOrder for root lists
                val mappedRootLists = dbLists.filter { it.folderId == null }
                    .map { list -> UiTaskList(id = list.id, name = list.name, sortOrder = list.sortOrder) }

                Pair(mappedFolders, mappedRootLists)
            }.collect { (newFolders, newRootLists) ->
                _folders.clear()
                _folders.addAll(newFolders)
                _rootLists.clear()
                _rootLists.addAll(newRootLists)
            }
        }
    }

    // --- Search Actions ---
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = "" // Clear query when closing search
        }
    }

    // --- Folder/List Actions ---

    fun createFolder(name: String) {
        viewModelScope.launch {
            todoRepository.insertFolder(TodoFolder(name = name))
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            // Create a list with NO folder (root level)
            todoRepository.insertList(TodoList(name = name, folderId = null))
        }
    }

    fun addListToFolder(folderId: Long, listName: String) {
        viewModelScope.launch {
            // Create a list linked to the specific folder ID
            todoRepository.insertList(TodoList(name = listName, folderId = folderId))

            // Auto-expand the folder so the user sees the new list immediately
            toggleFolderExpanded(folderId, forceExpand = true)
        }
    }

    fun toggleFolderExpanded(folderId: Long, forceExpand: Boolean = false) {
        val index = _folders.indexOfFirst { it.id == folderId }
        if (index != -1) {
            val folder = _folders[index]
            val newExpandedState = if (forceExpand) true else !folder.isExpanded
            _folders[index] = folder.copy(isExpanded = newExpandedState)
        }
    }

    // --- Sorting Actions (New) ---

    fun moveFolder(fromIndex: Int, toIndex: Int) {
        if (toIndex < 0 || toIndex >= _folders.size) return

        // Swap in UI immediately for smoothness
        val item = _folders.removeAt(fromIndex)
        _folders.add(toIndex, item)

        // Update DB with new indices
        viewModelScope.launch {
            _folders.forEachIndexed { index, folder ->
                // Ensure TodoFolder entity has sortOrder field from Step 1
                todoRepository.updateFolder(TodoFolder(id = folder.id, name = folder.name, sortOrder = index))
            }
        }
    }

    fun moveRootList(fromIndex: Int, toIndex: Int) {
        if (toIndex < 0 || toIndex >= _rootLists.size) return

        val item = _rootLists.removeAt(fromIndex)
        _rootLists.add(toIndex, item)

        viewModelScope.launch {
            _rootLists.forEachIndexed { index, list ->
                // Ensure TodoList entity has sortOrder field from Step 1
                todoRepository.updateList(TodoList(id = list.id, name = list.name, folderId = null, sortOrder = index))
            }
        }
    }
}