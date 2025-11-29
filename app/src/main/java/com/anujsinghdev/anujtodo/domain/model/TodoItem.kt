package com.anujsinghdev.anujtodo.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val isFlagged: Boolean = false,
    val priority: Int = 0,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),

    // --- NEW FIELDS ---
    val listId: Long? = null, // Links this task to a specific list
    val repeatMode: RepeatMode = RepeatMode.NONE
)

enum class RepeatMode {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}