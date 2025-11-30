package com.anujsinghdev.anujtodo.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.domain.model.TodoList
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    val archivedLists: StateFlow<List<TodoList>> = repository.getArchivedLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unarchiveList(list: TodoList) {
        viewModelScope.launch {
            repository.updateList(list.copy(isArchived = false))
        }
    }

    fun deleteListForever(list: TodoList) {
        viewModelScope.launch {
            repository.deleteListById(list.id)
        }
    }
}