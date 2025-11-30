package com.anujsinghdev.anujtodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.model.TodoFolder
import com.anujsinghdev.anujtodo.domain.model.TodoList

// 1. Create a converter for the Enum
class Converters {
    @androidx.room.TypeConverter
    fun fromRepeatMode(value: com.anujsinghdev.anujtodo.domain.model.RepeatMode) = value.name
    @androidx.room.TypeConverter
    fun toRepeatMode(value: String) = com.anujsinghdev.anujtodo.domain.model.RepeatMode.valueOf(value)
}

// 2. Add Converters and bump version to 3

@Database(entities = [TodoItem::class, TodoFolder::class, TodoList::class], version = 5, exportSchema = false) // <--- Version 5
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}