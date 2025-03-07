package com.example.raceconnect.viewmodel.MenuViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.UpdateUserRequest
import com.example.raceconnect.model.users
import com.example.raceconnect.model.SimpleResponse
import com.example.raceconnect.model.UploadProfilePictureResponse
import com.example.raceconnect.model.UserSimpleResponse
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import retrofit2.Response

class MenuViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _profileData = MutableStateFlow<users?>(null)
    val profileData: StateFlow<users?> = _profileData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isEditMode = MutableStateFlow(false) // New state for edit mode
    val isEditMode: StateFlow<Boolean> = _isEditMode

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userPreferences.user.first()?.id ?: return@launch
                val response: Response<users> = RetrofitInstance.api.getUser(userId)
                if (response.isSuccessful) {
                    _profileData.value = response.body()
                    _isEditMode.value = false // Reset to view mode after loading
                } else {
                    _errorMessage.value = "Failed to load profile: ${response.errorBody()?.string() ?: response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveProfileChanges(
        username: String,
        birthDate: String,
        contactNumber: String,
        address: String,
        bio: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userPreferences.user.first()?.id ?: return@launch
                val request = UpdateUserRequest(username, birthDate, contactNumber, address, bio)
                val response: Response<UserSimpleResponse> = RetrofitInstance.api.updateUser(userId, request)
                if (response.isSuccessful) {
                    loadProfileData() // Refresh profile data and switch to view mode
                } else {
                    _errorMessage.value = "Failed to update profile: ${response.errorBody()?.string() ?: response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadProfileImage(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userPreferences.user.first()?.id ?: return@launch
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                val userIdBody = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())

                val response: Response<UploadProfilePictureResponse> = RetrofitInstance.api.uploadProfilePicture(userIdBody, imagePart)
                if (response.isSuccessful) {
                    loadProfileData() // Refresh profile with new image URL
                } else {
                    _errorMessage.value = "Failed to upload image: ${response.errorBody()?.string() ?: response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error uploading image: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value // Toggle between edit and view mode
    }

    fun clearError() {
        _errorMessage.value = null
    }
}