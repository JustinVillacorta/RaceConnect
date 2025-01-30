package com.example.raceconnect.network




import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse

import com.example.raceconnect.model.users
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("users?action=login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

}
