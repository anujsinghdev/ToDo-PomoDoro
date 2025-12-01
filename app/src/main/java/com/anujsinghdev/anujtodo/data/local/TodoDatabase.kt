package com.anujsinghdev.anujtodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.model.TodoFolder
import com.anujsinghdev.anujtodo.domain.model.TodoList
import com.anujsinghdev.anujtodo.domain.model.FocusSession

// 1. Create a converter for the Enum
class Converters {
    @androidx.room.TypeConverter
    fun fromRepeatMode(value: com.anujsinghdev.anujtodo.domain.model.RepeatMode) = value.name
    @androidx.room.TypeConverter
    fun toRepeatMode(value: String) = com.anujsinghdev.anujtodo.domain.model.RepeatMode.valueOf(value)

    @androidx.room.TypeConverter
    fun fromSessionStatus(value: com.anujsinghdev.anujtodo.domain.model.SessionStatus) = value.name
    @androidx.room.TypeConverter
    fun toSessionStatus(value: String) = com.anujsinghdev.anujtodo.domain.model.SessionStatus.valueOf(value)
}

// 2. Add FocusSession to entities and ensure version is 7
@Database(
    entities = [
        TodoItem::class,
        TodoFolder::class,
        TodoList::class,
        FocusSession::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        // --- MIGRATION LOGIC ---
        // This tells Room how to move from Version 6 to Version 7
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // We added 'completedAt' (Long?) to 'todo_items'.
                // In SQLite, Long is stored as INTEGER.
                db.execSQL("ALTER TABLE todo_items ADD COLUMN completedAt INTEGER DEFAULT NULL")
            }
        }
    }
}