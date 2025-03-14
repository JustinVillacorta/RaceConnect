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
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MarketplaceViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    // State for marketplace items
    private val _marketplaceItems = MutableStateFlow<List<MarketplaceDataClassItem>>(emptyList())
    val marketplaceItems: StateFlow<List<MarketplaceDataClassItem>> = _marketplaceItems.asStateFlow()

    // State for user-specific items (e.g., liked items)
    private val _userItems = MutableStateFlow<List<MarketplaceDataClassItem>>(emptyList())
    val userItems: StateFlow<List<MarketplaceDataClassItem>> = _userItems.asStateFlow()

    // State for error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // State for current user ID
    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    // State for like status (whether the current user has liked an item)
    private val _isLiked = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val isLiked: StateFlow<Map<Int, Boolean>> = _isLiked.asStateFlow()

    // State for refreshing status
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // State for marketplace item images (map of item ID to list of image URLs)
    private val _marketplaceImages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val marketplaceImages: StateFlow<Map<Int, List<String>>> = _marketplaceImages.asStateFlow()

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
                    fetchUserMarketplaceItems() // Fetch liked items initially
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

    fun refreshMarketplaceItems() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                fetchMarketplaceItems()
                fetchUserMarketplaceItems() // Refresh liked items
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun fetchMarketplaceItems() {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value
                val response = RetrofitInstance.api.getAllMarketplaceItems(
                    limit = 10,
                    offset = 0,
                    excludeSellerId = userId
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        Log.e("MarketplaceViewModel", "Response body is null")
                        _marketplaceItems.value = emptyList()
                    } else {
                        _marketplaceItems.value = body
                    }
                    _marketplaceItems.value.forEach { item ->
                        fetchLikeStatus(item.id)
                        getMarketplaceItemImages(item.id)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch marketplace items: $errorBody")
                    _errorMessage.value = "Failed to fetch marketplace items: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching marketplace items", e)
                _errorMessage.value = "Error fetching marketplace items: ${e.message}"
            }
        }
    }

    fun fetchUserMarketplaceItems() {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                val response = RetrofitInstance.api.getUserLikedItems(userId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        Log.e("MarketplaceViewModel", "Response body is null for user $userId")
                        _userItems.value = emptyList()
                    } else {
                        val items = body.data as? List<MarketplaceDataClassItem> ?: emptyList()
                        _userItems.value = items
                        Log.d("MarketplaceViewModel", "Fetched ${items.size} liked items for user $userId")
                        items.forEach { item ->
                            fetchLikeStatus(item.id)
                            getMarketplaceItemImages(item.id)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch user liked items: $errorBody")
                    _errorMessage.value = "Failed to fetch user liked items: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching user liked items", e)
                _errorMessage.value = "Error fetching user liked items: ${e.message}"
            }
        }
    }

    fun fetchUserListedItems() {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                val response = RetrofitInstance.api.getMarketplaceItemsByUserId(userId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        Log.e("MarketplaceViewModel", "Response body is null for user $userId")
                        _userItems.value = emptyList()
                    } else {
                        _userItems.value = body
                        Log.d("MarketplaceViewModel", "Fetched ${_userItems.value.size} listed items for user $userId")
                        _userItems.value.forEach { item ->
                            fetchLikeStatus(item.id)
                            getMarketplaceItemImages(item.id)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch user listed items: $errorBody")
                    _errorMessage.value = "Failed to fetch user listed items: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching user listed items", e)
                _errorMessage.value = "Error fetching user listed items: ${e.message}"
            }
        }
    }

    fun fetchLikeStatus(itemId: Int) {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                val response = RetrofitInstance.api.getMarketplaceItemLikes(itemId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        Log.e("MarketplaceViewModel", "Response body is null for item $itemId")
                        _errorMessage.value = "Failed to fetch like status for item $itemId: Response body is null"
                        _isLiked.value = _isLiked.value.toMutableMap().apply {
                            this[itemId] = false
                        }
                        return@launch
                    }
                    val likes = body.data as? List<MarketplaceItemLike> ?: emptyList()
                    val isLikedByUser = likes.any { it.userId == userId }
                    _isLiked.value = _isLiked.value.toMutableMap().apply {
                        this[itemId] = isLikedByUser
                    }
                    Log.d("MarketplaceViewModel", "Fetched like status for item $itemId: liked=$isLikedByUser")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch like status for item $itemId: $errorBody")
                    _errorMessage.value = "Failed to fetch like status for item $itemId: $errorBody"
                    _isLiked.value = _isLiked.value.toMutableMap().apply {
                        this[itemId] = false
                    }
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching like status for item $itemId", e)
                _errorMessage.value = "Error fetching like status for item $itemId: ${e.message}"
                _isLiked.value = _isLiked.value.toMutableMap().apply {
                    this[itemId] = false
                }
            }
        }
    }

    fun getMarketplaceItemImages(itemId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMarketplaceItemImages(itemId)
                Log.d("MarketplaceViewModel", "Raw response: ${response.body()}")
                if (response.isSuccessful) {
                    val images = response.body()?.map { it.image_url } ?: emptyList()
                    _marketplaceImages.value = _marketplaceImages.value.toMutableMap().apply {
                        this[itemId] = images
                    }
                    Log.d("MarketplaceViewModel", "Fetched images for item $itemId: $images")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch images for item $itemId: $errorBody")
                    _errorMessage.value = "Failed to fetch images for item $itemId: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching images for item $itemId", e)
                _errorMessage.value = "Error fetching images for item $itemId: ${e.message}"
            }
        }
    }

    fun fetchItemById(itemId: Int): MarketplaceDataClassItem? {
        return runBlocking {
            try {
                val response = RetrofitInstance.api.getItemById(itemId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    Log.e("MarketplaceViewModel", "Failed to fetch item $itemId: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching item $itemId", e)
                null
            }
        }
    }

    fun toggleLike(itemId: Int) {
        viewModelScope.launch {
            val userId = _currentUserId.value ?: run {
                Log.e("MarketplaceViewModel", "No user logged in, cannot toggle like")
                _errorMessage.value = "Cannot toggle like: No user logged in"
                return@launch
            }
            val item = _marketplaceItems.value.find { it.id == itemId } ?: _userItems.value.find { it.id == itemId }
            if (item == null) {
                Log.e("MarketplaceViewModel", "Item with ID $itemId not found")
                _errorMessage.value = "Cannot toggle like: Item not found"
                return@launch
            }
            val ownerId = item.seller_id ?: run {
                Log.e("MarketplaceViewModel", "Seller ID not found for item $itemId")
                _errorMessage.value = "Cannot toggle like: Seller ID not found"
                return@launch
            }
            try {
                val params = mapOf(
                    "user_id" to userId,
                    "marketplace_item_id" to itemId,
                    "owner_id" to ownerId
                )
                val response = RetrofitInstance.api.toggleLike(params)
                if (response.isSuccessful) {
                    val result = response.body()
                    val isLiked = result?.get("liked") as? Boolean ?: false
                    _isLiked.value = _isLiked.value.toMutableMap().apply {
                        this[itemId] = isLiked
                    }
                    Log.d("MarketplaceViewModel", if (isLiked) "Liked item $itemId" else "Unliked item $itemId")
                    fetchUserMarketplaceItems() // Refresh liked items after toggling
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to toggle like for item $itemId: $errorBody")
                    _errorMessage.value = "Failed to toggle like for item $itemId: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error toggling like for item $itemId", e)
                _errorMessage.value = "Error toggling like for item $itemId: ${e.message}"
            }
        }
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
                val sellerIdPart = sellerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val pricePart = price.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
                val statusPart = "Active".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = RetrofitInstance.api.MarketplacePostImage(
                    seller_id = sellerIdPart,
                    description = descriptionPart,
                    title = titlePart,
                    category = categoryPart,
                    price = pricePart,
                    status = statusPart,
                    images = null
                )

                if (response.isSuccessful) {
                    Log.d("MarketplaceViewModel", "Item added successfully: ${response.body()}")
                    fetchMarketplaceItems()
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
                    fetchMarketplaceItems()
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

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Log.e("MarketplaceViewModel", "Error converting URI to file", e)
            null
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}