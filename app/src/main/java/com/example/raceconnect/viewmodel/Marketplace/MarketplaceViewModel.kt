package com.example.raceconnect.viewmodel.Marketplace

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.model.MarketplaceItemLike
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MarketplaceViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _items = MutableStateFlow<List<MarketplaceDataClassItem>>(emptyList())
    val items: StateFlow<List<MarketplaceDataClassItem>> = _items.asStateFlow()

    private val _userItems = MutableStateFlow<List<MarketplaceDataClassItem>>(emptyList())
    val userItems: StateFlow<List<MarketplaceDataClassItem>> = _userItems.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    private val _isLiked = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val isLiked: StateFlow<Map<Int, Boolean>> = _isLiked.asStateFlow()

    private val _likeCount = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val likeCount: StateFlow<Map<Int, Int>> = _likeCount.asStateFlow()

    private val _marketplaceImages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val marketplaceImages: StateFlow<Map<Int, List<String>>> = _marketplaceImages.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            try {
                Log.d("MarketplaceViewModel", "Fetching current user from UserPreferences")
                val user = userPreferences.user.first()
                _currentUserId.value = user?.id
                Log.d("MarketplaceViewModel", "Current user ID set to: ${_currentUserId.value}")
                if (_currentUserId.value != null) {
                    Log.d("MarketplaceViewModel", "Initiating fetches for user ID: ${_currentUserId.value}")
                    fetchMarketplaceItems()
                    fetchUserMarketplaceItems()
                } else {
                    Log.e("MarketplaceViewModel", "No user logged in")
                    _errorMessage.value = "No user logged in"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching current user", e)
                _errorMessage.value = "Failed to fetch user: ${e.message}"
            }
        }
    }

    private fun fetchMarketplaceItems() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                Log.d("MarketplaceViewModel", "Fetching marketplace items...")
                val userId = _currentUserId.value
                if (userId == null) {
                    Log.e("MarketplaceViewModel", "Cannot fetch marketplace items: userId is null")
                    _errorMessage.value = "Cannot fetch marketplace items: No user logged in"
                    return@launch
                }
                val response = RetrofitInstance.api.getAllMarketplaceItems(
                    excludeSellerId = userId,
                    limit = 10,
                    offset = 0
                )
                Log.d("MarketplaceViewModel", "Marketplace items response: ${response.code()} - ${response.body()}")
                if (response.isSuccessful) {
                    val fetchedItems = response.body() ?: emptyList()
                    _items.value = fetchedItems
                    Log.d("MarketplaceViewModel", "Fetched ${fetchedItems.size} marketplace items")
                    fetchedItems.forEach { item ->
                        getMarketplaceItemImages(item.id)
                        fetchLikeStatus(item.id)
                    }
                } else {
                    _items.value = emptyList()
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch marketplace items: $errorBody")
                    _errorMessage.value = "Failed to fetch marketplace items: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching marketplace items", e)
                _items.value = emptyList()
                _errorMessage.value = "Error fetching marketplace items: ${e.message}"
            } finally {
                _isRefreshing.value = false
                Log.d("MarketplaceViewModel", "Finished fetching marketplace items")
            }
        }
    }

    private fun fetchUserMarketplaceItems() {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value
                if (userId == null) {
                    Log.e("MarketplaceViewModel", "Cannot fetch user items: userId is null")
                    _errorMessage.value = "Cannot fetch user items: No user logged in"
                    return@launch
                }
                Log.d("MarketplaceViewModel", "Fetching user marketplace items for user ID: $userId")
                val response = RetrofitInstance.api.getMarketplaceItemsByUserId(
                    userId = userId,
                    limit = 10,
                    offset = 0
                )
                Log.d("MarketplaceViewModel", "User marketplace items response: ${response.code()} - ${response.body()}")
                if (response.isSuccessful) {
                    val fetchedItems = response.body() ?: emptyList()
                    _userItems.value = fetchedItems
                    Log.d("MarketplaceViewModel", "Fetched ${fetchedItems.size} user items for user $userId")
                    fetchedItems.forEach { item ->
                        getMarketplaceItemImages(item.id)
                        fetchLikeStatus(item.id)
                    }
                } else {
                    _userItems.value = emptyList()
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch user marketplace items: $errorBody")
                    _errorMessage.value = "Failed to fetch user items: $errorBody (Code: ${response.code()})"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching user marketplace items", e)
                _userItems.value = emptyList()
                _errorMessage.value = "Error fetching user items: ${e.message}"
            }
        }
    }

    fun refreshMarketplaceItems() {
        Log.d("MarketplaceViewModel", "Refreshing marketplace items")
        _errorMessage.value = null // Clear previous errors on refresh
        fetchMarketplaceItems()
        fetchUserMarketplaceItems()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
        Log.d("MarketplaceViewModel", "Cleared error message")
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
                Log.e("MarketplaceViewModel", "No user logged in, cannot add marketplace item")
                _errorMessage.value = "Cannot add item: No user logged in"
                return@launch
            }

            try {
                Log.d("MarketplaceViewModel", "Adding marketplace item without images...")
                val newItem = MarketplaceDataClassItem(
                    id = 0,
                    seller_id = sellerId,
                    title = title,
                    description = description,
                    price = price,
                    category = category,
                    image_url = imageUrl,
                    favorite_count = 0,
                    status = "Active",
                    report = "none",
                    reported_at = null,
                    previous_status = null,
                    listing_status = "Available",
                    created_at = "",
                    updated_at = "",
                    archived_at = null
                )

                val response = RetrofitInstance.api.createMarketplaceItem(newItem)

                if (response.isSuccessful) {
                    Log.d("MarketplaceViewModel", "Item added successfully: ${response.body()}")
                    refreshMarketplaceItems()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to add item: $errorBody")
                    _errorMessage.value = "Failed to add item: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error adding marketplace item", e)
                _errorMessage.value = "Error adding item: ${e.message}"
            }
        }
    }

    fun addMarketplaceItemWithImages(
        context: Context,
        title: String,
        price: String,
        description: String,
        category: String,
        imageUris: List<Uri>? = null
    ) {
        viewModelScope.launch {
            val sellerId = _currentUserId.value
            if (sellerId == null) {
                Log.e("MarketplaceViewModel", "No user logged in, cannot add marketplace item with images")
                _errorMessage.value = "Cannot add item with images: No user logged in"
                return@launch
            }
            try {
                Log.d("MarketplaceViewModel", "Adding marketplace item with images...")
                val sellerIdPart = sellerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val pricePart = price.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
                val statusPart = "Active".toRequestBody("text/plain".toMediaTypeOrNull())

                val imageParts: List<MultipartBody.Part> = imageUris?.mapNotNull { uri ->
                    val file = getFileFromUri(context, uri)
                    file?.let {
                        val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image[]", it.name, requestFile)
                    }
                } ?: emptyList()

                Log.d("MarketplaceViewModel", "Prepared ${imageParts.size} image parts for upload")

                val response = RetrofitInstance.api.MarketplacePostImage(
                    seller_id = sellerIdPart,
                    description = descriptionPart,
                    title = titlePart,
                    category = categoryPart,
                    price = pricePart,
                    status = statusPart,
                    images = if (imageParts.isNotEmpty()) imageParts else null
                )

                if (response.isSuccessful) {
                    Log.d("MarketplaceViewModel", "Item added successfully with images: ${response.body()}")
                    refreshMarketplaceItems()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to add item with images: $errorBody")
                    _errorMessage.value = "Failed to add item with images: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error adding marketplace item with images", e)
                _errorMessage.value = "Error adding item with images: ${e.message}"
            }
        }
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            Log.d("MarketplaceViewModel", "Getting file from URI: $uri")
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            Log.d("MarketplaceViewModel", "File created: ${tempFile.absolutePath}")
            tempFile
        } catch (e: Exception) {
            Log.e("MarketplaceViewModel", "Failed to get file from URI", e)
            null
        }
    }

    fun getMarketplaceItemImages(itemId: Int) {
        viewModelScope.launch {
            try {
                Log.d("MarketplaceViewModel", "Fetching images for marketplace item ID: $itemId")
                val response = RetrofitInstance.api.GetMarketplaceImage(itemId)
                if (response.isSuccessful) {
                    val imageResponses = response.body() ?: emptyList()
                    val imageUrls = imageResponses.map { it.image_url }
                    _marketplaceImages.value = _marketplaceImages.value.toMutableMap().apply {
                        this[itemId] = imageUrls
                    }
                    Log.d("MarketplaceViewModel", "Fetched images for item $itemId: $imageUrls")
                } else {
                    _marketplaceImages.value = _marketplaceImages.value.toMutableMap().apply {
                        this[itemId] = emptyList()
                    }
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch images for item $itemId: $errorBody")
                    _errorMessage.value = "Failed to fetch images for item $itemId: $errorBody"
                }
            } catch (e: Exception) {
                _marketplaceImages.value = _marketplaceImages.value.toMutableMap().apply {
                    this[itemId] = emptyList()
                }
                Log.e("MarketplaceViewModel", "Error fetching images for item $itemId", e)
                _errorMessage.value = "Error fetching images for item $itemId: ${e.message}"
            }
        }
    }

    suspend fun toggleLike(itemId: Int) {
        val userId = _currentUserId.value ?: return
        val currentLiked = _isLiked.value[itemId] ?: false
        if (currentLiked) {
            unlikeItem(itemId, userId)
        } else {
            likeItem(itemId, userId)
        }
    }

    private suspend fun likeItem(itemId: Int, userId: Int) {
        try {
            val params = mapOf("item_id" to itemId, "user_id" to userId)
            val response = RetrofitInstance.api.likeMarketplaceItem(params)
            if (response.isSuccessful) {
                _isLiked.value = _isLiked.value.toMutableMap().apply {
                    this[itemId] = true
                }
                updateLikeCount(itemId)
                Log.d("MarketplaceViewModel", "Liked item $itemId")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("MarketplaceViewModel", "Failed to like item $itemId: $errorBody")
                _errorMessage.value = "Failed to like item $itemId: $errorBody"
            }
        } catch (e: Exception) {
            Log.e("MarketplaceViewModel", "Error liking item $itemId", e)
            _errorMessage.value = "Error liking item $itemId: ${e.message}"
        }
    }

    private suspend fun unlikeItem(itemId: Int, userId: Int) {
        try {
            val params = mapOf("item_id" to itemId, "user_id" to userId)
            val response = RetrofitInstance.api.unlikeMarketplaceItem(params)
            if (response.isSuccessful) {
                _isLiked.value = _isLiked.value.toMutableMap().apply {
                    this[itemId] = false
                }
                updateLikeCount(itemId)
                Log.d("MarketplaceViewModel", "Unliked item $itemId")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("MarketplaceViewModel", "Failed to unlike item $itemId: $errorBody")
                _errorMessage.value = "Failed to unlike item $itemId: $errorBody"
            }
        } catch (e: Exception) {
            Log.e("MarketplaceViewModel", "Error unliking item $itemId", e)
            _errorMessage.value = "Error unliking item $itemId: ${e.message}"
        }
    }

    private suspend fun updateLikeCount(itemId: Int) {
        try {
            val response = RetrofitInstance.api.getMarketplaceItemLikes(itemId)
            if (response.isSuccessful) {
                val likes = response.body() ?: emptyList()
                _likeCount.value = _likeCount.value.toMutableMap().apply {
                    this[itemId] = likes.size
                }
                Log.d("MarketplaceViewModel", "Updated like count for item $itemId: ${likes.size}")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("MarketplaceViewModel", "Failed to update like count for item $itemId: $errorBody")
                _errorMessage.value = "Failed to update like count for item $itemId: $errorBody"
            }
        } catch (e: Exception) {
            Log.e("MarketplaceViewModel", "Error updating like count for item $itemId", e)
            _errorMessage.value = "Error updating like count for item $itemId: ${e.message}"
        }
    }

    fun fetchLikeStatus(itemId: Int) {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                val response = RetrofitInstance.api.getMarketplaceItemLikes(itemId)
                if (response.isSuccessful) {
                    val likes = response.body() ?: emptyList()
                    val isLikedByUser = likes.any { it.userId == userId }
                    _isLiked.value = _isLiked.value.toMutableMap().apply {
                        this[itemId] = isLikedByUser
                    }
                    _likeCount.value = _likeCount.value.toMutableMap().apply {
                        this[itemId] = likes.size
                    }
                    Log.d("MarketplaceViewModel", "Fetched like status for item $itemId: liked=$isLikedByUser, count=${likes.size}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch like status for item $itemId: $errorBody")
                    _errorMessage.value = "Failed to fetch like status for item $itemId: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching like status for item $itemId", e)
                _errorMessage.value = "Error fetching like status for item $itemId: ${e.message}"
            }
        }
    }

    suspend fun fetchItemById(itemId: Int): MarketplaceDataClassItem? {
        return try {
            Log.d("MarketplaceViewModel", "Fetching item by ID: $itemId")
            val response = RetrofitInstance.api.getItemById(itemId)
            if (response.isSuccessful) {
                val item = response.body()
                if (item != null) {
                    getMarketplaceItemImages(itemId)
                    fetchLikeStatus(itemId)
                    Log.d("MarketplaceViewModel", "Fetched item $itemId: $item")
                    item
                } else {
                    Log.e("MarketplaceViewModel", "Item $itemId not found")
                    _errorMessage.value = "Item $itemId not found"
                    null
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("MarketplaceViewModel", "Failed to fetch item $itemId: $errorBody")
                _errorMessage.value = "Failed to fetch item $itemId: $errorBody"
                null
            }
        } catch (e: Exception) {
            Log.e("MarketplaceViewModel", "Error fetching item $itemId", e)
            _errorMessage.value = "Error fetching item $itemId: ${e.message}"
            null
        }
    }
}