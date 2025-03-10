package com.example.raceconnect.viewmodel.ProfileDetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.users
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostUserProfileViewModel(private val userPreferences: UserPreferences) : ViewModel() {
    private val _profileData = MutableStateFlow<users?>(null)
    val profileData: StateFlow<users?> = _profileData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val apiService = RetrofitInstance.api

    fun loadProfileData(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getUser(userId) // Changed from getUserProfile to getUser
                if (response.isSuccessful) {
                    _profileData.value = response.body()
                    Log.d("PostUserProfileViewModel", "Profile data loaded successfully for userId: $userId, data: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Failed to load profile: ${response.code()} - ${response.message()}"
                    Log.e("PostUserProfileViewModel", "API error: ${response.code()} - ${response.message()}, body: $errorBody")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Load error: ${e.message}"
                Log.e("PostUserProfileViewModel", "Load error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}