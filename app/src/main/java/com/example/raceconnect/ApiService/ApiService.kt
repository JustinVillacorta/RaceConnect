package com.example.raceconnect.network


import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("users?action=login")
    suspend fun login(@Body credentials: LoginRequest): LoginResponse
}
