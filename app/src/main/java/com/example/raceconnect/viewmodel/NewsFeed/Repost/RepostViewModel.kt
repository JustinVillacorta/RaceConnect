package com.example.raceconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.model.CreateRepostRequest
import com.example.raceconnect.network.ApiService
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RepostViewModel(private val apiService: ApiService) : ViewModel() {

    fun repostPost(userId: Int, postId: Int, quote: String? = null) {
        viewModelScope.launch {
            try {
                val request = CreateRepostRequest(
                    userId = userId,
                    postId = postId,
                    quote = quote?.takeIf { it.isNotBlank() }
                )
                val response = apiService.createRepost(request)
                if (!response.isSuccessful) {
                    throw HttpException(response)
                }
            } catch (e: IOException) {
                // Handle network errors
                e.printStackTrace()
            } catch (e: HttpException) {
                // Handle HTTP errors
                e.printStackTrace()
            } catch (e: Exception) {
                // Handle other errors
                e.printStackTrace()
            }
        }
    }
}