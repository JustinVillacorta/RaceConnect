package com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
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
                // Load from UserPreferences or API
                // For simplicity, assuming UserPreferences has a method to get selected categories
                val preferences = userPreferences.selectedCategories.first() // Assuming this exists
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
            savePreferences() // Save to UserPreferences or API
        }
    }

    fun savePreferences() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Save to UserPreferences or API
                userPreferences.saveSelectedCategories(_selectedBrands.value) // Assuming this method exists
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