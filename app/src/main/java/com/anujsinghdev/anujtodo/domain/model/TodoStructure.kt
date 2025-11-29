package com.anujsinghdev.anujtodo.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "todo_folders")
data class TodoFolder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "todo_lists",
    foreignKeys = [
        ForeignKey(
            entity = TodoFolder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TodoList(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val folderId: Long? = null // Null if it's a root list (not in a folder)
)