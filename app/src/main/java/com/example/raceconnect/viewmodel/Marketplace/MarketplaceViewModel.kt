package com.example.raceconnect.viewmodel.Marketplace

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.model.MarketplaceItemLike
import com.example.raceconnect.model.ReportRequest
import com.example.raceconnect.model.SendMessageRequest
import com.example.raceconnect.model.SendMessageResponse
import com.example.raceconnect.model.UpdateMarketplaceItemRequest
import com.example.raceconnect.network.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

    // State for user-specific items (e.g., listed or liked items)
    private val _userItems = MutableStateFlow<List<MarketplaceDataClassItem>>(emptyList())
    val userItems: StateFlow<List<MarketplaceDataClassItem>> = _userItems.asStateFlow()

    // State for error messages
    internal val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // State for current user ID
    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    // State for like status
    private val _isLiked = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val isLiked: StateFlow<Map<Int, Boolean>> = _isLiked.asStateFlow()

    // State for refreshing status
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // State for marketplace item images
    private val _marketplaceImages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val marketplaceImages: StateFlow<Map<Int, List<String>>> = _marketplaceImages.asStateFlow()

    // State for message sending status
    private val _messageSentStatus = MutableStateFlow<String?>(null)
    val messageSentStatus: StateFlow<String?> = _messageSentStatus.asStateFlow()

    private val _lastConversationId = MutableStateFlow<Int?>(null)
    val lastConversationId: StateFlow<Int?> = _lastConversationId.asStateFlow()

    private val _conversationExists = MutableStateFlow<Pair<Boolean, Int?>?>(null)
    val conversationExists: StateFlow<Pair<Boolean, Int?>?> = _conversationExists.asStateFlow()

    private val _updateStatus = MutableStateFlow<Boolean?>(null)
    val updateStatus: StateFlow<Boolean?> = _updateStatus.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.user.collect { user ->
                val newUserId = user?.id
                Log.d("MarketplaceViewModel", "User changed: $newUserId")
                if (newUserId != _currentUserId.value) {
                    _currentUserId.value = newUserId
                    if (newUserId != null) {
                        Log.d("MarketplaceViewModel", "Initiating fetches for user ID: $newUserId")
                        fetchMarketplaceItems()
                        fetchUserMarketplaceItems()
                    } else {
                        Log.d("MarketplaceViewModel", "User logged out, clearing data")
                        clear()
                    }
                }
            }
        }

        viewModelScope.launch {
            userPreferences.user.collect { user ->
                val favorites = user?.favoriteMarketplaceItems?.toSet() ?: emptySet()
                val updatedLikes = _isLiked.value.toMutableMap()
                _marketplaceItems.value.forEach { item ->
                    updatedLikes[item.id] = favorites.contains(item.id.toString())
                }
                _userItems.value.forEach { item ->
                    updatedLikes[item.id] = favorites.contains(item.id.toString())
                }
                _isLiked.value = updatedLikes
            }
        }
    }

    fun clear() {
        _marketplaceItems.value = emptyList()
        _userItems.value = emptyList()
        _isLiked.value = emptyMap()
        _marketplaceImages.value = emptyMap()
        _errorMessage.value = null
        _isRefreshing.value = false
        _messageSentStatus.value = null
        Log.d("MarketplaceViewModel", "Cleared all state")
    }

    fun refreshMarketplaceItems() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                fetchMarketplaceItems()
                fetchUserMarketplaceItems()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun fetchMarketplaceItems() {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
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
                val response = RetrofitInstance.api.getLikedItemsByUserIds(userId.toString())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        Log.e("MarketplaceViewModel", "Response body is null for user $userId")
                        _userItems.value = emptyList()
                    } else {
                        val data = body["data"] as? Map<String, List<Map<String, Any>>> ?: emptyMap()
                        val userItemsList = data[userId.toString()] ?: emptyList()
                        val items = userItemsList.mapNotNull { itemMap ->
                            try {
                                MarketplaceDataClassItem(
                                    id = (itemMap["id"] as? Number)?.toInt() ?: return@mapNotNull null,
                                    seller_id = (itemMap["seller_id"] as? Number)?.toInt() ?: 0,
                                    title = itemMap["title"] as? String ?: "",
                                    description = itemMap["description"] as? String ?: "",
                                    category = itemMap["category"] as? String ?: "",
                                    price = itemMap["price"] as? String ?: "0.0",
                                    listing_status = itemMap["listing_status"] as? String ?: "Available",
                                    status = itemMap["status"] as? String ?: "Active",
                                    image_url = itemMap["image_url"] as? String,
                                    favorite_count = (itemMap["favorite_count"] as? Number)?.toInt() ?: 0,
                                    archived_at = itemMap["archived_at"] as? String,
                                    report = itemMap["report"] as? String ?: "none",
                                    reported_at = itemMap["reported_at"] as? String,
                                    created_at = itemMap["created_at"] as? String ?: "",
                                    updated_at = itemMap["updated_at"] as? String ?: "",
                                    previous_status = itemMap["previous_status"] as? String
                                )
                            } catch (e: Exception) {
                                Log.e("MarketplaceViewModel", "Error mapping item: $itemMap", e)
                                null
                            }
                        }
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
                        // Filter out "Archived" items client-side, though backend should already handle this
                        val filteredItems = body.filter { item ->
                            val status = item.status?.lowercase()
                            status == "active" || status == "hidden"
                        }
                        _userItems.value = filteredItems
                        Log.d("MarketplaceViewModel", "Fetched ${filteredItems.size} listed items for user $userId (Active/Hidden only)")
                        filteredItems.forEach { item ->
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
            val ownerId = item.seller_id
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
                    val currentFavorites = userPreferences.user.first()?.favoriteMarketplaceItems?.toSet() ?: emptySet()
                    if (isLiked) {
                        userPreferences.saveUser(
                            userId = userId,
                            username = userPreferences.user.first()?.username ?: "",
                            email = userPreferences.user.first()?.email ?: "",
                            token = userPreferences.getToken() ?: "",
                            favoriteMarketplaceItems = currentFavorites + itemId.toString()
                        )
                    } else {
                        userPreferences.saveUser(
                            userId = userId,
                            username = userPreferences.user.first()?.username ?: "",
                            email = userPreferences.user.first()?.email ?: "",
                            token = userPreferences.getToken() ?: "",
                            favoriteMarketplaceItems = currentFavorites - itemId.toString()
                        )
                    }
                    Log.d("MarketplaceViewModel", if (isLiked) "Liked item $itemId" else "Unliked item $itemId")
                    fetchUserMarketplaceItems()
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
                val listingStatusPart = "Available".toRequestBody("text/plain".toMediaTypeOrNull())

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
                    title = titlePart,
                    description = descriptionPart,
                    price = pricePart,
                    category = categoryPart,
                    status = statusPart,
                    listing_status = listingStatusPart,
                    images = if (imageParts.isNotEmpty()) imageParts else null
                )

                if (response.isSuccessful) {
                    Log.d("MarketplaceViewModel", "Item added successfully with images: ${response.body()}")
                    fetchMarketplaceItems()
                    fetchUserListedItems()
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

    fun updateItem(
        itemId: Int,
        updatedItem: MarketplaceDataClassItem,
        newImageUris: List<Uri>? = null,
        context: Context,
        imagesToDelete: List<Int> = emptyList()
    ) {
        viewModelScope.launch {
            val userId = _currentUserId.value ?: run {
                _errorMessage.value = "Cannot update item: No user logged in"
                return@launch
            }

            try {
                Log.d("MarketplaceViewModel", "Updating item with ID: $itemId")
                val titlePart = updatedItem.title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = updatedItem.description.toRequestBody("text/plain".toMediaTypeOrNull())
                val pricePart = updatedItem.price.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = updatedItem.category.toRequestBody("text/plain".toMediaTypeOrNull())
                val listingStatusPart = updatedItem.listing_status.toRequestBody("text/plain".toMediaTypeOrNull())
                val statusPart = updatedItem.status.toRequestBody("text/plain".toMediaTypeOrNull())
                val deleteImageIdsPart = if (imagesToDelete.isNotEmpty()) {
                    imagesToDelete.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull())
                } else null
                val imageParts: List<MultipartBody.Part> = newImageUris?.mapNotNull { uri ->
                    val file = getFileFromUri(context, uri)
                    file?.let {
                        val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image[]", it.name, requestFile)
                    }
                } ?: emptyList()

                val response = RetrofitInstance.api.updateMarketplaceItemWithImages(
                    id = itemId,
                    title = titlePart,
                    description = descriptionPart,
                    price = pricePart,
                    category = categoryPart,
                    listingStatus = listingStatusPart,
                    status = statusPart,
                    deleteImageIds = deleteImageIdsPart,
                    images = if (imageParts.isNotEmpty()) imageParts else null
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.message == "Item updated successfully") {
                        Log.d("MarketplaceViewModel", "Item updated successfully: ${responseBody.item_id}")
                        _userItems.value = _userItems.value.map {
                            if (it.id == itemId) updatedItem else it
                        }
                        _marketplaceImages.value = _marketplaceImages.value.toMutableMap().apply {
                            this[itemId] = responseBody.image_urls.map { "$it?ts=${System.currentTimeMillis()}" }
                        }
                        _updateStatus.value = true // Signal success
                    } else {
                        _errorMessage.value = "Failed to update item $itemId: ${responseBody?.message}"
                        _updateStatus.value = false
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _errorMessage.value = "Failed to update item $itemId: $errorBody"
                    _updateStatus.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating item $itemId: ${e.message}"
                _updateStatus.value = false
            }
        }
    }

    fun deleteItem(itemId: Int, context: Context) {
        viewModelScope.launch {
            val userId = _currentUserId.value ?: run {
                Log.e("MarketplaceViewModel", "No user logged in, cannot delete item")
                _errorMessage.value = "Cannot delete item: No user logged in"
                return@launch
            }

            try {
                Log.d("MarketplaceViewModel", "Deleting item with ID: $itemId")
                val deleteResponse = RetrofitInstance.api.deleteMarketplaceItem(itemId)
                if (deleteResponse.isSuccessful) {
                    Log.d("MarketplaceViewModel", "Item deleted successfully: ${deleteResponse.body()}")
                    val updatedItems = _userItems.value.filter { it.id != itemId }
                    _userItems.value = updatedItems
                    fetchUserListedItems()
                } else {
                    val errorBody = deleteResponse.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to delete item $itemId: $errorBody")
                    _errorMessage.value = "Failed to delete item $itemId: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error deleting item $itemId", e)
                _errorMessage.value = "Error deleting item $itemId: ${e.message}"
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

    fun checkConversationExists(buyerId: Int, sellerId: Int, productId: Int, onResult: (Boolean, Int?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.checkConversationExists(buyerId, sellerId, productId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val exists = body["exists"] as? Boolean ?: false
                        val conversationId = if (exists) (body["conversation_id"] as? Number)?.toInt() else null
                        _conversationExists.value = Pair(exists, conversationId)
                        onResult(exists, conversationId)
                        Log.d("MarketplaceViewModel", "Conversation check result: exists=$exists, id=$conversationId")
                    } else {
                        _conversationExists.value = Pair(false, null)
                        onResult(false, null)
                        Log.w("MarketplaceViewModel", "Response body is null")
                    }
                } else {
                    Log.e("MarketplaceViewModel", "Failed to check conversation: ${response.errorBody()?.string()}")
                    _conversationExists.value = Pair(false, null)
                    onResult(false, null)
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error checking conversation: ${e.message}")
                _conversationExists.value = Pair(false, null)
                onResult(false, null)
            }
        }
    }

    fun sendMessage(
        buyerId: Int,
        sellerId: Int,
        productId: Int,
        message: String,
        messageType: String = "text",
        mediaUrl: String? = null
    ) {
        viewModelScope.launch {
            val senderId = _currentUserId.value ?: return@launch
            try {
                val request = SendMessageRequest(
                    buyer_id = buyerId,
                    seller_id = sellerId,
                    product_id = productId,
                    sender_id = senderId,
                    message = message,
                    message_type = messageType,
                    media_url = mediaUrl
                )
                val response = RetrofitInstance.api.createMessage(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _messageSentStatus.value = "Message sent successfully"
                    _lastConversationId.value = response.body()?.conversation_id
                } else {
                    _errorMessage.value = "Failed to send message: ${response.body()?.error ?: "Unknown error"}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error sending message: ${e.message}"
            }
        }
    }

    fun clearMessageSentStatus() {
        _messageSentStatus.value = null
    }

    fun resetUpdateStatus() {
        _updateStatus.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
        _messageSentStatus.value = null
    }

    fun reportMarketplaceItem(
        marketplaceItemId: Int,
        reason: String,
        otherText: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d("ReportMarketplaceItem", "Attempting to report marketplace item $marketplaceItemId with reason: $reason")

                val userId = _currentUserId.value ?: run {
                    Log.w("ReportMarketplaceItem", "User not logged in, aborting report")
                    onFailure("User not logged in")
                    return@launch
                }

                val finalReason = if (reason == "Others" && otherText != null) otherText else reason
                Log.i("ReportMarketplaceItem", "Final reason determined: $finalReason")

                val reportRequest = ReportRequest(
                    post_id = null,
                    marketplace_item_id = marketplaceItemId,
                    reporter_id = userId,
                    reason = finalReason
                )
                Log.d("ReportMarketplaceItem", "Report request created: $reportRequest")

                val response = RetrofitInstance.api.createReport(reportRequest)
                Log.i("ReportMarketplaceItem", "API response received with code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.i("ReportMarketplaceItem", "Marketplace item reported successfully")
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ReportMarketplaceItem", "Failed to report marketplace item. Error: $errorBody")
                    onFailure("Failed to report marketplace item: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("ReportMarketplaceItem", "Exception occurred while reporting marketplace item", e)
                onFailure("Error reporting marketplace item: ${e.message}")
            }
        }
    }

}