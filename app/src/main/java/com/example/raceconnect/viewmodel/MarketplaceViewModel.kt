import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.network.RetrofitInstance

class MarketplaceViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<MarketplaceDataClassItem>>(emptyList())
    val items: StateFlow<List<MarketplaceDataClassItem>> = _items

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        fetchMarketplaceItems()
    }

    private fun fetchMarketplaceItems() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val response = RetrofitInstance.api.getAllMarketplaceItems()
                _items.value = response
            } catch (e: HttpException) {
                println("Error fetching marketplace items: ${e.message}")
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
        imageUrl: String,
        sellerId: Int
    ) {
        viewModelScope.launch {
            try {
                val newItem = MarketplaceDataClassItem(
                    id = 0, // Let the backend assign the ID
                    seller_id = sellerId,
                    title = title,
                    description = description,
                    price = price,
                    category = category,
                    image_url = imageUrl,
                    favorite_count = 0,
                    status = "available",
                    created_at = "",
                    updated_at = ""
                )

                val response = RetrofitInstance.api.createMarketplaceItem(newItem)

                if (response.isSuccessful && response.body() != null) {
                    fetchMarketplaceItems() // Refresh items after successful addition
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
