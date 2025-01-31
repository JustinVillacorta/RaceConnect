import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.network.RetrofitInstance


class MarketplaceViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<MarketplaceDataClassItem>>(emptyList())
    val items: StateFlow<List<MarketplaceDataClassItem>> = _items

    init {
        fetchMarketplaceItems()
    }

    fun fetchMarketplaceItems() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getAllMarketplaceItems()
                _items.value = response
            } catch (e: HttpException) {
                println("Error fetching marketplace items: ${e.message}")
            }
        }
    }

    fun addMarketplaceItem(item: MarketplaceDataClassItem) {
        viewModelScope.launch {
            try {
                Log.d("Marketplace", "Sending request to add item: $item")

                val response = RetrofitInstance.api.createMarketplaceItem(item)

                if (response.isSuccessful && response.body() != null) {
                    val newItem = response.body()!!
                    Log.d("Marketplace", "Item added successfully: ${newItem.message}")

                    // Fetch updated marketplace items from the backend
                    fetchMarketplaceItems()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("Marketplace", "Failed to add item: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("Marketplace", "Error posting marketplace item", e)
            }
        }
    }
}