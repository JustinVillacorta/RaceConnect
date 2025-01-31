package com.example.raceconnect.network




import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.model.NewsFeedDataClass
import com.example.raceconnect.model.NewsFeedDataClassItem


import com.example.raceconnect.model.users
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("users?action=login")
    suspend fun login(@Body request: LoginRequest): LoginResponse


    @GET("posts")
    suspend fun getAllPosts(): List<NewsFeedDataClassItem>

    @POST("posts/id")
    suspend fun createPost(@Body post: NewsFeedDataClassItem): Response<NewsFeedDataClassItem>


    @GET("marketplace-items")
    suspend fun getAllMarketplaceItems(): List<MarketplaceDataClassItem>

    @POST("marketplace-items")
    suspend fun createMarketplaceItem(@Body item: MarketplaceDataClassItem): Response<MarketplaceDataClassItem>

}




