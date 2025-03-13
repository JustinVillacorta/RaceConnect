package com.example.raceconnect.viewmodel.NewsFeed.ListedItems

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListedItemsViewModel(
    private val userPreferences: UserPreferences,
    private val context: Context
) : ViewModel() {

    private val _listedItems = MutableStateFlow<List<MarketplaceDataClassItem>>(emptyList())
    val listedItems: StateFlow<List<MarketplaceDataClassItem>> = _listedItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadListedItems()
    }

    fun loadListedItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val userId = userPreferences.getUserId() ?: throw Exception("User not logged in")
                val response = RetrofitInstance.api.getMarketplaceItemsByUserId(userId)

                if (response.isSuccessful) {
                    _listedItems.value = response.body() ?: emptyList()
                    Log.d("ListedItemsViewModel", "Loaded ${listedItems.value.size} items for user $userId")
                } else {
                    _errorMessage.value = "Failed to load items: ${response.message()}"
                    Log.e("ListedItemsViewModel", "API error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading items: ${e.message}"
                Log.e("ListedItemsViewModel", "Exception: ${e.stackTraceToString()}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}