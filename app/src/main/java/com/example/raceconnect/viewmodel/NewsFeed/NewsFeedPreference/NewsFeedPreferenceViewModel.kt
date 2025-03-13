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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val preferences = userPreferences.selectedCategories.first()
                _selectedBrands.value = preferences
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load preferences: ${e.message}"
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
            // Optionally save immediately, or wait for explicit save
            // savePreferences()
        }
    }

    fun savePreferences() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = userPreferences.getUserId() ?: throw IllegalStateException("User ID not found")
                val brands = _selectedBrands.value

                userPreferences.saveSelectedCategories(brands) // This might be blocking
                val request = UpdateUserFavoriteCategoriesRequest(favoriteCategories = brands)
                val response = apiService.updateUserCategories(userId, request) // Network call
                if (response.isSuccessful) {
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to save preferences to server: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save preferences: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearPreferences() {
        viewModelScope.launch {
            _selectedBrands.value = emptyList()
            savePreferences()
        }
    }
}