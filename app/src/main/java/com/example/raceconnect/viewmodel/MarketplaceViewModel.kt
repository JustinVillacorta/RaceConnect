package com.example.raceconnect.viewmodel

import androidx.lifecycle.ViewModel
import com.example.raceconnect.model.MarkeplaceItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MarketplaceViewModel : ViewModel() {

    // State flow for managing marketplace items
    private val _items = MutableStateFlow<List<MarkeplaceItem>>(listOf(
        MarkeplaceItem(
            id = 1,
            title = "McLaren 2024 Team Polo",
            price = "₱3,705",
            description = "The Men's Official McLaren F1 Team Polo Shirt features striking McLaren-colored tape along the shoulders.",
            images = listOf("https://example.com/image1.jpg") // Replace with a valid placeholder URL or file path
        )
    ))

    // Public state flow for observing items
    val items: StateFlow<List<MarkeplaceItem>> = _items

    // Mutable state flow for managing selected images in a new listing
    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    val selectedImages: StateFlow<List<String>> = _selectedImages

    // Add a new item to the marketplace
    fun addItem(item: MarkeplaceItem) {
        _items.value = _items.value + item
    }

    // Add a selected image to the state
    fun addImage(imagePath: String) {
        _selectedImages.value = _selectedImages.value + imagePath
    }

    // Remove a specific image from the selected images
    fun removeImage(imagePath: String) {
        _selectedImages.value = _selectedImages.value - imagePath
    }

    // Clear all selected images (e.g., after publishing or canceling a listing)
    fun clearSelectedImages() {
        _selectedImages.value = emptyList()
    }
}
