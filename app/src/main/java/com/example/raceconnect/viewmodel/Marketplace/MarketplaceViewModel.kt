package com.example.raceconnect.viewmodel.Marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.asStateFlow

class MarketplaceViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _items = MutableStateFlow<List<MarketplaceDataClassItem>>(emptyList())
    val items: StateFlow<List<MarketplaceDataClassItem>> = _items.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    init {
        fetchCurrentUser()
        fetchMarketplaceItems()
    }

    // Fetch user data from DataStore
    private fun fetchCurrentUser() {
        viewModelScope.launch {
            _currentUserId.value = userPreferences.user.first()?.id
        }
    }

    private fun fetchMarketplaceItems() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val response = RetrofitInstance.api.getAllMarketplaceItems()

                println("API Response: $response") // Debugging

                _items.value = response
            } catch (e: Exception) {
                println("Error fetching marketplace items: ${e.localizedMessage}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }


    fun refreshMarketplaceItems() {
        fetchMarketplaceItems()
    }

    fun addMarketplaceItem(
        title: String,
        price: String,
        description: String,
        category: String,
        imageUrl: String = ""
    ) {
        viewModelScope.launch {
            val sellerId = _currentUserId.value
            if (sellerId == null) {
                println("Error: No user logged in, cannot add marketplace item.")
                return@launch
            }

            try {
                val newItem = MarketplaceDataClassItem(
                    id = 0, // Let backend assign ID
                    seller_id = sellerId,
                    title = title,
                    description = description,
                    price = price,
                    category = category,
                    image_url = imageUrl,
                    favorite_count = 0,
                    status = "Available",
                    report = "None",
                    reported_at = null,
                    previous_status = null,
                    listing_status = "Available",
                    created_at = "", // Backend will set this
                    updated_at = "" // Backend will set this
                )

                val response = RetrofitInstance.api.createMarketplaceItem(newItem)

                if (response.isSuccessful && response.body() != null) {
                    fetchMarketplaceItems() // Refresh after successful addition
                    println("Item added successfully: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    println("Failed to add item: $errorBody")
                }
            } catch (e: Exception) {
                println("Error adding marketplace item: ${e.message}")
            }
        }
    }
}