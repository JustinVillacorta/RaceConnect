package com.example.raceconnect.Controller

fun validateLogin(email: String, password: String): Boolean {
    return email.isNotEmpty() && password.isNotEmpty()
}

fun validateSignup(email: String, password: String): Boolean {
    return email.isNotEmpty() && password.length >= 6
}