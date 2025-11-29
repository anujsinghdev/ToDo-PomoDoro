package com.anujsinghdev.anujtodo.di

import android.app.Application
import androidx.room.Room
import com.anujsinghdev.anujtodo.data.local.TodoDatabase
import com.anujsinghdev.anujtodo.data.repository.TodoRepositoryImpl
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import com.anujsinghdev.anujtodo.domain.usecase.AddTodoUseCase
import com.anujsinghdev.anujtodo.domain.usecase.DeleteTodoUseCase
import com.anujsinghdev.anujtodo.domain.usecase.GetTodosUseCase
import com.anujsinghdev.anujtodo.domain.usecase.TodoUseCases
import com.anujsinghdev.anujtodo.domain.usecase.ToggleTodoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 1. PROVIDE DATABASE
    @Provides
    @Singleton
    fun provideTodoDatabase(app: Application): TodoDatabase {
        return Room.databaseBuilder(
            app,
            TodoDatabase::class.java,
            "anuj_todo_db"
        )
            .fallbackToDestructiveMigration() // <--- ADD THIS LINE
            .build()
    }

    // ... keep the rest of your code the same ...
    @Provides
    @Singleton
    fun provideTodoRepository(db: TodoDatabase): TodoRepository {
        return TodoRepositoryImpl(db.todoDao())
    }

    @Provides
    @Singleton
    fun provideTodoUseCases(repository: TodoRepository): TodoUseCases {
        return TodoUseCases(
            getTodos = GetTodosUseCase(repository),
            deleteTodo = DeleteTodoUseCase(repository),
            addTodo = AddTodoUseCase(repository),
            toggleTodo = ToggleTodoUseCase(repository)
        )
    }
}