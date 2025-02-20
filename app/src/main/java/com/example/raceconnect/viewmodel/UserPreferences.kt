package com.example.raceconnect.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.raceconnect.model.users
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension property for DataStore
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val USER_ID = intPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val TOKEN = stringPreferencesKey("token")
    }

    // Save user data
    suspend fun saveUser(userId: Int, username: String, email: String, token: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USERNAME] = username
            preferences[EMAIL] = email
            preferences[TOKEN] = token
        }
    }

    // Retrieve user data
    val user: Flow<users?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID] ?: return@map null
        val name = preferences[USERNAME] ?: return@map null
        val email = preferences[EMAIL] ?: return@map null
        users(id, name, email)
    }

    // Retrieve token
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN]
    }

    // Clear user data (Logout)
    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getToken(): String? {
        return token.first()
    }

}



