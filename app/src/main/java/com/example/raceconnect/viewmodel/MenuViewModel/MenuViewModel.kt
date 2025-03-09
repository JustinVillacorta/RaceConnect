package com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.users
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MenuViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _profileData = MutableStateFlow<users?>(null)
    val profileData: StateFlow<users?> = _profileData

    init {
        viewModelScope.launch {
            userPreferences.user.collect { newUser ->
                if (newUser != null) {
                    _profileData.value = newUser // Initialize with saved data
                } else {
                    clearData()
                }
            }
        }
    }

    fun clearData() {
        _profileData.value = null
    }
}