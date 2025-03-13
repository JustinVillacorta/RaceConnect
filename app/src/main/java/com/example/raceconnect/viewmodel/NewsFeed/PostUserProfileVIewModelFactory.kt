    package com.example.raceconnect.viewmodel.ProfileDetails

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import com.example.raceconnect.datastore.UserPreferences

    class PostUserProfileViewModelFactory(
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostUserProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PostUserProfileViewModel(userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }