package com.example.raceconnect.network




import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.itemPostRequest
import com.example.raceconnect.model.itemPostResponse
import com.example.raceconnect.model.users


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse


    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): users


    @GET("posts")
    suspend fun getAllPosts(): List<NewsFeedDataClassItem>

    @POST("posts")
    suspend fun createPost(@Body post: NewsFeedDataClassItem): Response<NewsFeedDataClassItem>


    @GET("marketplace-items")
    suspend fun getAllMarketplaceItems(): List<MarketplaceDataClassItem>

    @POST("marketplace-items")
    suspend fun createMarketplaceItem(@Body item: MarketplaceDataClassItem): Response<itemPostResponse>

}




