package com.anujsinghdev.anujtodo.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_NAME = stringPreferencesKey("user_name")
    private val KEY_EMAIL = stringPreferencesKey("user_email") // Added
    private val KEY_PASSWORD = stringPreferencesKey("user_password")

    // Updated to save email
    suspend fun saveUser(name: String, email: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NAME] = name
            preferences[KEY_EMAIL] = email
            preferences[KEY_PASSWORD] = password
        }
    }

    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] } // Added
    val userPassword: Flow<String?> = context.dataStore.data.map { it[KEY_PASSWORD] }
}