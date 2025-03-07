package com.example.raceconnect.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.raceconnect.model.users
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val USER_ID = intPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val TOKEN = stringPreferencesKey("token")
        val BIRTHDATE = stringPreferencesKey("birthdate")
        val NUMBER = stringPreferencesKey("number")
        val ADDRESS = stringPreferencesKey("address")
        val AGE = intPreferencesKey("age")
        val PROFILE_PICTURE = stringPreferencesKey("profile_picture")
        val BIO = stringPreferencesKey("bio")
        val FAVORITE_CATEGORIES = stringPreferencesKey("favorite_categories")
        val FAVORITE_MARKETPLACE_ITEMS = stringPreferencesKey("favorite_marketplace_items")
        val FRIENDS_LIST = stringPreferencesKey("friends_list")
        val FRIEND_PRIVACY = stringPreferencesKey("friend_privacy")
        val LAST_ONLINE = stringPreferencesKey("last_online")
        val STATUS = stringPreferencesKey("status")
        val REPORT = stringPreferencesKey("report")
        val SUSPENSION_END_DATE = stringPreferencesKey("suspension_end_date")
        val CREATED_AT = stringPreferencesKey("created_at")
        val UPDATED_AT = stringPreferencesKey("updated_at")
    }

    suspend fun saveUser(
        userId: Int,
        username: String,
        email: String,
        token: String,
        birthdate: String? = null,
        number: String? = null,
        address: String? = null,
        age: Int? = null,
        profilePicture: String? = null,
        bio: String? = null,
        favoriteCategories: String? = null,
        favoriteMarketplaceItems: String? = null,
        friendsList: String? = null,
        friendPrivacy: String? = null,
        lastOnline: String? = null,
        status: String? = null,
        report: String? = null,
        suspensionEndDate: String? = null,
        createdAt: String? = null,
        updatedAt: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USERNAME] = username
            preferences[EMAIL] = email
            preferences[TOKEN] = token
            birthdate?.let { preferences[BIRTHDATE] = it }
            number?.let { preferences[NUMBER] = it }
            address?.let { preferences[ADDRESS] = it }
            age?.let { preferences[AGE] = it }
            profilePicture?.let { preferences[PROFILE_PICTURE] = it }
            bio?.let { preferences[BIO] = it }
            favoriteCategories?.let { preferences[FAVORITE_CATEGORIES] = it }
            favoriteMarketplaceItems?.let { preferences[FAVORITE_MARKETPLACE_ITEMS] = it }
            friendsList?.let { preferences[FRIENDS_LIST] = it }
            friendPrivacy?.let { preferences[FRIEND_PRIVACY] = it }
            lastOnline?.let { preferences[LAST_ONLINE] = it }
            status?.let { preferences[STATUS] = it }
            report?.let { preferences[REPORT] = it }
            suspensionEndDate?.let { preferences[SUSPENSION_END_DATE] = it }
            createdAt?.let { preferences[CREATED_AT] = it }
            updatedAt?.let { preferences[UPDATED_AT] = it }
        }
    }

    val user: Flow<users?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID] ?: return@map null
        val name = preferences[USERNAME] ?: return@map null
        val email = preferences[EMAIL] ?: return@map null
        users(
            id = id,
            username = name,
            email = email,
            birthdate = preferences[BIRTHDATE],
            number = preferences[NUMBER],
            address = preferences[ADDRESS],
            age = preferences[AGE],
            profilePicture = preferences[PROFILE_PICTURE],
            bio = preferences[BIO],
            favoriteCategories = preferences[FAVORITE_CATEGORIES]?.let { listOf(it) } ?: emptyList(),
            favoriteMarketplaceItems = preferences[FAVORITE_MARKETPLACE_ITEMS]?.let { listOf(it) } ?: emptyList(),
            friendsList = preferences[FRIENDS_LIST]?.let { listOf(it.toInt()) } ?: emptyList(),
            friendPrivacy = preferences[FRIEND_PRIVACY],
            lastOnline = preferences[LAST_ONLINE],
            status = preferences[STATUS],
            report = preferences[REPORT],
            suspensionEndDate = preferences[SUSPENSION_END_DATE],
            createdAt = preferences[CREATED_AT],
            updatedAt = preferences[UPDATED_AT]
        )
    }

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN]
    }

    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getToken(): String? {
        return token.first()
    }
}