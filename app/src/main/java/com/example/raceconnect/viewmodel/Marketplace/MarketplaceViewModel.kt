package com.example.raceconnect.viewmodel.Marketplace

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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
            Log.d("MarketplaceViewModel", "Current user id: ${_currentUserId.value}")
        }
    }

    private fun fetchMarketplaceItems() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                Log.d("MarketplaceViewModel", "Fetching marketplace items...")
                val response = RetrofitInstance.api.getAllMarketplaceItems()
                Log.d("MarketplaceViewModel", "Marketplace items fetched: $response")
                _items.value = response

                // Fetch images for each item
                response.forEach { item ->
                    getMarketplaceItemImages(item.id)
                }
            } catch (e: Exception) {
                Log.e("MarketplaceViewModel", "Error fetching marketplace items", e)
                _items.value = emptyList() // Optional: Clear items on error
            } finally {
                _isRefreshing.value = false
                Log.d("MarketplaceViewModel", "Finished fetching marketplace items")
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
                Log.e("MarketplaceViewModel", "No user logged in, cannot add marketplace item")
                return@launch
            }

            try {
                Log.d("MarketplaceViewModel", "Adding marketplace item without images...")
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
                    Log.d("MarketplaceViewModel", "Item added successfully: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to add item: $errorBody")
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
                // Create RequestBody parts for text fields
                val sellerIdPart = sellerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val pricePart = price.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
                val statusPart = "Available".toRequestBody("text/plain".toMediaTypeOrNull())

                // Prepare the image parts with key "image[]"
                val imageParts: List<MultipartBody.Part> = imageUris?.mapNotNull { uri ->
                    val file = getFileFromUri(context, uri)
                    file?.let {
                        val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                        // Use "image[]" to explicitly indicate an array of images
                        MultipartBody.Part.createFormData("image[]", it.name, requestFile)
                    }
                } ?: emptyList()

                Log.d("MarketplaceViewModel", "Prepared ${imageParts.size} image parts for upload")

                // Call the multipart API endpoint (ensure Retrofit interface matches this)
                val response = RetrofitInstance.api.MarketplacePostImage(
                    seller_id = sellerIdPart,
                    description = descriptionPart,
                    title = titlePart,
                    category = categoryPart,
                    price = pricePart,
                    status = statusPart,
                    images = if (imageParts.isNotEmpty()) imageParts else null // Send null if no images
                )

                if (response.isSuccessful) {
                    Log.d("MarketplaceViewModel", "Item added successfully with images: ${response.body()}")
                    fetchMarketplaceItems() // Refresh the list
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MarketplaceViewModel", "Failed to add item with images: $errorBody")
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
    val marketplaceImages: StateFlow<Map<Int, List<String>>> = _marketplaceImages

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
                    Log.e("MarketplaceViewModel", "Failed to fetch images for item $itemId: ${response.errorBody()?.string()}")
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
