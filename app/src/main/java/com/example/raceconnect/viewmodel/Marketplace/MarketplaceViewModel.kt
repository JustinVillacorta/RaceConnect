package com.example.raceconnect.viewmodel.Marketplace

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
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

    private val api = RetrofitInstance.api

    init {
        fetchCurrentUser()
        fetchMarketplaceItems()
        fetchUserMarketplaceItems()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            _currentUserId.value = userPreferences.user.first()?.id
            Log.d("MarketplaceViewModel", "Current user ID set to: ${_currentUserId.value}")
        }
    }

    private fun fetchMarketplaceItems() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                Log.d("MarketplaceViewModel", "Fetching marketplace items...")
                val userId = _currentUserId.value
                Log.d("MarketplaceViewModel", "Excluding seller ID: $userId")
                val response = api.getAllMarketplaceItems(limit = 10, offset = 0, excludeSellerId = userId)
                if (response.isSuccessful) {
                    val fetchedItems = response.body() ?: emptyList()
                    _items.value = fetchedItems
                    Log.d("MarketplaceViewModel", "Fetched ${fetchedItems.size} marketplace items: $fetchedItems")
                    fetchedItems.forEach { item ->
                        getMarketplaceItemImages(item.id)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch marketplace items. Code: ${response.code()}, Error: $errorBody")
                    _items.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching marketplace items", e)
                _items.value = emptyList()
            } finally {
                _isRefreshing.value = false
                Log.d("MarketplaceViewModel", "Finished fetching marketplace items")
            }
        }
    }

    fun fetchUserMarketplaceItems() {
        viewModelScope.launch {
            val userId = _currentUserId.value ?: run {
                Log.w("MarketplaceViewModel", "No user ID available, skipping fetchUserMarketplaceItems")
                return@launch
            }
            try {
                Log.d("MarketplaceViewModel", "Fetching user's marketplace items for user ID: $userId...")
                val response = api.getMarketplaceItemsByUserId(userId, limit = 10, offset = 0)
                if (response.isSuccessful) {
                    val fetchedItems = response.body() ?: emptyList()
                    _userItems.value = fetchedItems
                    Log.d("MarketplaceViewModel", "Fetched ${fetchedItems.size} user items: $fetchedItems")
                    fetchedItems.forEach { item ->
                        getMarketplaceItemImages(item.id)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch user's marketplace items. Code: ${response.code()}, Error: $errorBody")
                    _userItems.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching user's marketplace items", e)
                _userItems.value = emptyList()
            }
        }
    }

    fun fetchItemById(itemId: Int): MarketplaceDataClassItem? {
        var item: MarketplaceDataClassItem? = null
        viewModelScope.launch {
            try {
                Log.d("MarketplaceViewModel", "Fetching item by ID: $itemId")
                val response = api.getItemById(itemId)
                if (response.isSuccessful) {
                    item = response.body()
                    item?.let { fetchedItem ->
                        Log.d("MarketplaceViewModel", "Item $itemId fetched: $fetchedItem")
                        if (fetchedItem.seller_id == _currentUserId.value) {
                            _userItems.value = _userItems.value + fetchedItem
                        } else {
                            _items.value = _items.value + fetchedItem
                        }
                        getMarketplaceItemImages(fetchedItem.id)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to fetch item $itemId. Code: ${response.code()}, Error: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching item $itemId", e)
            }
        }
        return item
    }

    fun refreshMarketplaceItems() {
        fetchMarketplaceItems()
        fetchUserMarketplaceItems()
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
                    status = "Available",
                    report = "None",
                    reported_at = null,
                    previous_status = null,
                    listing_status = "Available",
                    created_at = "",
                    updated_at = ""
                )
                val response = api.createMarketplaceItem(newItem)
                if (response.isSuccessful && response.body() != null) {
                    fetchUserMarketplaceItems()
                    Log.d("MarketplaceViewModel", "Item added successfully: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to add item. Code: ${response.code()}, Error: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error adding marketplace item", e)
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
                return@launch
            }
            try {
                Log.d("MarketplaceViewModel", "Adding marketplace item with images...")
                val sellerIdPart = sellerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val pricePart = price.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
                val statusPart = "Available".toRequestBody("text/plain".toMediaTypeOrNull())

                val imageParts: List<MultipartBody.Part> = imageUris?.mapNotNull { uri ->
                    val file = getFileFromUri(context, uri)
                    file?.let {
                        val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image[]", it.name, requestFile)
                    }
                } ?: emptyList()

                Log.d("MarketplaceViewModel", "Prepared ${imageParts.size} image parts for upload")
                val response = api.MarketplacePostImage(
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
                    fetchUserMarketplaceItems()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to add item with images. Code: ${response.code()}, Error: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error adding marketplace item with images", e)
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

    private val _marketplaceImages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val marketplaceImages: StateFlow<Map<Int, List<String>>> = _marketplaceImages.asStateFlow()

    fun getMarketplaceItemImages(itemId: Int) {
        viewModelScope.launch {
            try {
                Log.d("MarketplaceViewModel", "Fetching images for marketplace item ID: $itemId")
                val response = api.GetMarketplaceImage(itemId)
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
                    Log.e("MarketplaceViewModel", "Failed to fetch images for item $itemId. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _marketplaceImages.value = _marketplaceImages.value.toMutableMap().apply {
                    this[itemId] = emptyList()
                }
                Log.e("MarketplaceViewModel", "Error fetching images for item $itemId", e)
            }
        }
    }
}