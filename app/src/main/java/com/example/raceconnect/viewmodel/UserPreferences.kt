package com.example.raceconnect.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.raceconnect.model.users
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val USER_ID = intPreferencesKey("user_id")
        private val USERNAME = stringPreferencesKey("username")
        private val EMAIL = stringPreferencesKey("email")
        private val TOKEN = stringPreferencesKey("token")
        private val BIRTHDATE = stringPreferencesKey("birthdate")
        private val NUMBER = stringPreferencesKey("number")
        private val ADDRESS = stringPreferencesKey("address")
        private val AGE = intPreferencesKey("age")
        private val PROFILE_PICTURE = stringPreferencesKey("profile_picture")
        private val BIO = stringPreferencesKey("bio")
        // New keys using stringSetPreferencesKey
        private val FAVORITE_CATEGORIES = stringSetPreferencesKey("favorite_categories_v2")
        private val FAVORITE_MARKETPLACE_ITEMS = stringSetPreferencesKey("favorite_marketplace_items_v2")
        private val FRIENDS_LIST = stringSetPreferencesKey("friends_list_v2")
        // Old keys using stringPreferencesKey (for migration)
        private val OLD_FAVORITE_CATEGORIES = stringPreferencesKey("favorite_categories")
        private val OLD_FAVORITE_MARKETPLACE_ITEMS = stringPreferencesKey("favorite_marketplace_items")
        private val OLD_FRIENDS_LIST = stringPreferencesKey("friends_list")
        private val FRIEND_PRIVACY = stringPreferencesKey("friend_privacy")
        private val LAST_ONLINE = stringPreferencesKey("last_online")
        private val STATUS = stringPreferencesKey("status")
        private val REPORT = stringPreferencesKey("report")
        private val SUSPENSION_END_DATE = stringPreferencesKey("suspension_end_date")
        private val CREATED_AT = stringPreferencesKey("created_at")
        private val UPDATED_AT = stringPreferencesKey("updated_at")
    }

    // Perform migration if old data exists
    suspend fun migrateOldDataIfNeeded() {
        context.dataStore.edit { preferences ->
            // Migrate favorite categories
            val oldFavoriteCategories = preferences[OLD_FAVORITE_CATEGORIES]
            if (oldFavoriteCategories != null) {
                val categoriesSet = oldFavoriteCategories.split(",").filter { it.isNotEmpty() }.toSet()
                preferences[FAVORITE_CATEGORIES] = categoriesSet
                preferences.remove(OLD_FAVORITE_CATEGORIES) // Remove old key
                Log.d("UserPreferences", "Migrated favorite_categories to favorite_categories_v2: $categoriesSet")
            }

            // Migrate favorite marketplace items
            val oldFavoriteMarketplaceItems = preferences[OLD_FAVORITE_MARKETPLACE_ITEMS]
            if (oldFavoriteMarketplaceItems != null) {
                val itemsSet = oldFavoriteMarketplaceItems.split(",").filter { it.isNotEmpty() }.toSet()
                preferences[FAVORITE_MARKETPLACE_ITEMS] = itemsSet
                preferences.remove(OLD_FAVORITE_MARKETPLACE_ITEMS) // Remove old key
                Log.d("UserPreferences", "Migrated favorite_marketplace_items to favorite_marketplace_items_v2: $itemsSet")
            }

            // Migrate friends list
            val oldFriendsList = preferences[OLD_FRIENDS_LIST]
            if (oldFriendsList != null) {
                val friendsSet = oldFriendsList.split(",").mapNotNull { it.toIntOrNull()?.toString() }.toSet()
                preferences[FRIENDS_LIST] = friendsSet
                preferences.remove(OLD_FRIENDS_LIST) // Remove old key
                Log.d("UserPreferences", "Migrated friends_list to friends_list_v2: $friendsSet")
            }
        }
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
        favoriteCategories: Set<String>? = null,
        favoriteMarketplaceItems: Set<String>? = null,
        friendsList: Set<Int>? = null,
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
            profilePicture?.let {
                preferences[PROFILE_PICTURE] = it
                Log.d("UserPreferences", "Saving profile picture: $it")
            }
            bio?.let { preferences[BIO] = it }
            favoriteCategories?.let {
                preferences[FAVORITE_CATEGORIES] = it
                Log.d("UserPreferences", "Saving favorite categories: $it")
            }
            favoriteMarketplaceItems?.let {
                preferences[FAVORITE_MARKETPLACE_ITEMS] = it
                Log.d("UserPreferences", "Saving favorite marketplace items: $it")
            }
            friendsList?.let {
                preferences[FRIENDS_LIST] = it.map { it.toString() }.toSet()
                Log.d("UserPreferences", "Saving friends list: $it")
            }
            friendPrivacy?.let { preferences[FRIEND_PRIVACY] = it }
            lastOnline?.let { preferences[LAST_ONLINE] = it }
            status?.let { preferences[STATUS] = it }
            report?.let { preferences[REPORT] = it }
            suspensionEndDate?.let { preferences[SUSPENSION_END_DATE] = it }
            createdAt?.let { preferences[CREATED_AT] = it }
            updatedAt?.let { preferences[UPDATED_AT] = it }
            Log.d("UserPreferences", "User data saved: userId=$userId, username=$username, email=$email")
        }
    }

    val user: Flow<users?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID] ?: return@map null
        val username = preferences[USERNAME] ?: return@map null
        val email = preferences[EMAIL] ?: return@map null
        val profilePicture = preferences[PROFILE_PICTURE]

        Log.d("UserPreferences", "Retrieved user: id=$id, username=$username, email=$email, profilePicture=$profilePicture")

        users(
            id = id,
            username = username,
            email = email,
            birthdate = preferences[BIRTHDATE],
            number = preferences[NUMBER],
            address = preferences[ADDRESS],
            age = preferences[AGE],
            profilePicture = profilePicture,
            bio = preferences[BIO],
            favoriteCategories = preferences[FAVORITE_CATEGORIES]?.toList() ?: emptyList(),
            favoriteMarketplaceItems = preferences[FAVORITE_MARKETPLACE_ITEMS]?.toList() ?: emptyList(),
            friendsList = preferences[FRIENDS_LIST]?.mapNotNull { it.toIntOrNull() } ?: emptyList(),
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

    val selectedCategories: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITE_CATEGORIES]?.toList() ?: emptyList()
    }

    suspend fun saveSelectedCategories(categories: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[FAVORITE_CATEGORIES] = categories.toSet()
            Log.d("UserPreferences", "Saved selected categories: $categories")
        }
    }

    suspend fun clearSelectedCategories() {
        context.dataStore.edit { preferences ->
            preferences.remove(FAVORITE_CATEGORIES)
            Log.d("UserPreferences", "Cleared selected categories")
        }
    }

    suspend fun getUserId(): Int? {
        return user.first()?.id
    }

    suspend fun getToken(): String? {
        return token.first()
    }

    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            // Clear all preferences first
            preferences.clear()
            // Force clear critical user identifiers
            preferences.remove(USER_ID)
            preferences.remove(TOKEN)
            // Clear all other user data
            preferences.remove(USERNAME)
            preferences.remove(EMAIL)
            preferences.remove(BIRTHDATE)
            preferences.remove(NUMBER)
            preferences.remove(ADDRESS)
            preferences.remove(AGE)
            preferences.remove(PROFILE_PICTURE)
            preferences.remove(BIO)
            preferences.remove(FAVORITE_CATEGORIES)
            preferences.remove(FAVORITE_MARKETPLACE_ITEMS)
            preferences.remove(FRIENDS_LIST)
            preferences.remove(FRIEND_PRIVACY)
            preferences.remove(LAST_ONLINE)
            preferences.remove(STATUS)
            preferences.remove(REPORT)
            preferences.remove(SUSPENSION_END_DATE)
            preferences.remove(CREATED_AT)
            preferences.remove(UPDATED_AT)
            Log.d("UserPreferences", "User data cleared from preferences")
        }
        // The user Flow will automatically emit null since USER_ID is cleared
        Log.d("UserPreferences", "User state cleared")
    }
}