package com.example.raceconnect.ViewModel

import androidx.lifecycle.ViewModel

class AuthenticationViewModel : ViewModel() {

    fun validateLogin(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    fun validateSignup(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.length >= 6
    }
}