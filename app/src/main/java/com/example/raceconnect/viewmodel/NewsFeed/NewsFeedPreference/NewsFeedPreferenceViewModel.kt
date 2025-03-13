package com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.UpdateUserFavoriteCategoriesRequest
import com.example.raceconnect.network.ApiService
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.flow.first

class NewsFeedPreferenceViewModel(
    private val userPreferences: UserPreferences,
    private val apiService: ApiService = RetrofitInstance.api
) : ViewModel() {

    private val _selectedBrands = MutableStateFlow<List<String>>(emptyList())
    val selectedBrands: StateFlow<List<String>> = _selectedBrands.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Continuously observe user preferences
        viewModelScope.launch {
            userPreferences.selectedCategories.collect { categories ->
                _selectedBrands.value = categories
                Log.d("NewsFeedPrefVM", "Updated categories from preferences: $categories")
            }
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = userPreferences.getUserId()
                if (userId == null) {
                    _selectedBrands.value = listOf("F1") // Default for new users
                    Log.d("NewsFeedPrefVM", "No user ID found, using default categories")
                    return@launch
                }
                val preferences = userPreferences.selectedCategories.first()
                _selectedBrands.value = preferences
                Log.d("NewsFeedPrefVM", "Loaded categories for user $userId: $preferences")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load preferences: ${e.message}"
                Log.e("NewsFeedPrefVM", "Error loading preferences", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleBrand(brand: String) {
        viewModelScope.launch {
            val currentBrands = _selectedBrands.value.toMutableList()
            if (brand in currentBrands) {
                currentBrands.remove(brand)
            } else {
                currentBrands.add(brand)
            }
            _selectedBrands.value = currentBrands
            Log.d("NewsFeedPrefVM", "Toggled brand $brand, new selection: $currentBrands")
        }
    }

    fun savePreferences() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = userPreferences.getUserId() ?: throw IllegalStateException("User ID not found")
                val brands = _selectedBrands.value

                // Save to local preferences first
                userPreferences.saveSelectedCategories(brands)
                Log.d("NewsFeedPrefVM", "Saved categories locally: $brands")

                // Then update server
                val request = UpdateUserFavoriteCategoriesRequest(favoriteCategories = brands)
                val response = apiService.updateUserCategories(userId, request)
                if (response.isSuccessful) {
                    _errorMessage.value = null
                    Log.d("NewsFeedPrefVM", "Saved categories to server for user $userId")
                } else {
                    _errorMessage.value = "Failed to save preferences to server: ${response.message()}"
                    Log.e("NewsFeedPrefVM", "Server error saving categories: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save preferences: ${e.message}"
                Log.e("NewsFeedPrefVM", "Error saving preferences", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearPreferences() {
        viewModelScope.launch {
            try {
                _selectedBrands.value = listOf("F1") // Reset to default
                userPreferences.clearSelectedCategories()
                savePreferences() // Save the default to both local and server
                Log.d("NewsFeedPrefVM", "Cleared preferences and set to default")
            } catch (e: Exception) {
                Log.e("NewsFeedPrefVM", "Error clearing preferences", e)
            }
        }
    }
}