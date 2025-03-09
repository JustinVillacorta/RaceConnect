package com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.UpdateUserRequest
import com.example.raceconnect.model.UploadProfilePictureResponse
import com.example.raceconnect.model.users
import com.example.raceconnect.network.ApiService
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProfileDetailsViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _profileData = MutableStateFlow<users?>(null)
    val profileData: StateFlow<users?> = _profileData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    internal val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode

    private val apiService: ApiService = RetrofitInstance.api

    init {
        viewModelScope.launch {
            userPreferences.user.collect { newUser ->
                Log.d("ProfileDetailsViewModel", "User data collected: $newUser, Profile Picture: ${newUser?.profilePicture}")
                _profileData.value = newUser
            }
        }
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userPreferences.getUserId() ?: return@launch
                Log.d("ProfileDetailsViewModel", "Loading profile data for userId: $userId")
                val response = apiService.getUser(userId)
                if (response.isSuccessful) {
                    val user = response.body()
                    Log.d("ProfileDetailsViewModel", "Profile data loaded: $user, Profile Picture: ${user?.profilePicture}")
                    _profileData.value = user
                    user?.let { syncWithUserPreferences(it) } // Sync with UserPreferences
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to load profile: ${response.message()}"
                    Log.e("ProfileDetailsViewModel", "Failed to load profile: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load profile data: ${e.message}"
                Log.e("ProfileDetailsViewModel", "Load error", e)
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
        bio: String,
        userPreferences: UserPreferences
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = _profileData.value?.id ?: return@launch
                val token = userPreferences.getToken() ?: ""

                // Prepare update request
                val updateRequest = UpdateUserRequest(
                    username = username,
                    birthdate = birthDate,
                    number = contactNumber,
                    address = address,
                    bio = bio
                )

                // Update user data on the server
                val response = apiService.updateUser(userId, updateRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Sync with local data and UserPreferences
                    val updatedUser = _profileData.value?.copy(
                        username = username,
                        birthdate = birthDate,
                        number = contactNumber,
                        address = address,
                        bio = bio
                    )
                    updatedUser?.let {
                        _profileData.value = it
                        syncWithUserPreferences(it) // Update UserPreferences
                        Log.d("ProfileDetailsViewModel", "Profile updated: $updatedUser, Profile Picture: ${it.profilePicture}")
                    }
                    _isEditMode.value = false
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = response.body()?.message ?: "Failed to save user data: ${response.message()}"
                    Log.e("ProfileDetailsViewModel", "Failed to save user data: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save changes: ${e.message}"
                Log.e("ProfileDetailsViewModel", "Save error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadProfileImage(file: File) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = _profileData.value?.id?.toString() ?: return@launch
                val token = userPreferences.getToken() ?: ""

                // Convert file to MultipartBody.Part
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val userIdPart = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                // Upload image to the server
                val response = apiService.uploadProfilePicture(userIdPart, imagePart)
                if (response.isSuccessful && response.body()?.success == true) {
                    val newProfilePictureUrl = response.body()?.imageUrl ?: ""
                    val currentUser = _profileData.value?.copy(profilePicture = newProfilePictureUrl) ?: return@launch
                    _profileData.value = currentUser
                    syncWithUserPreferences(currentUser) // Update UserPreferences with new profile picture
                    Log.d("ProfileDetailsViewModel", "Profile picture uploaded: $newProfilePictureUrl")
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = response.body()?.message ?: "Failed to upload image: ${response.message()}"
                    Log.e("ProfileDetailsViewModel", "Failed to upload image: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to upload image: ${e.message}"
                Log.e("ProfileDetailsViewModel", "Upload error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    /**
     * Syncs the provided user data with UserPreferences
     */
    private suspend fun syncWithUserPreferences(user: users) {
        userPreferences.saveUser(
            userId = user.id,
            username = user.username,
            email = user.email,
            token = userPreferences.getToken() ?: "",
            birthdate = user.birthdate,
            number = user.number,
            address = user.address,
            bio = user.bio,
            profilePicture = user.profilePicture
        )
        Log.d("ProfileDetailsViewModel", "User preferences synced: $user")
    }
}