package com.example.raceconnect.network




import com.example.raceconnect.model.ImageUploadResponse
import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.LogoutRequest
import com.example.raceconnect.model.LogoutResponse
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.SignupRequest
import com.example.raceconnect.model.SignupResponse
import com.example.raceconnect.model.itemPostRequest
import com.example.raceconnect.model.itemPostResponse
import com.example.raceconnect.model.users
import okhttp3.MultipartBody
import okhttp3.RequestBody


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse


    @POST("logout")
    suspend fun logout(@Header("Authorization") authToken: String): Response<LogoutResponse>



    @POST("users")
    suspend fun signup(@Body request: SignupRequest): SignupResponse


    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): users





    @GET("posts")
    suspend fun getAllPosts(): List<NewsFeedDataClassItem>

    @POST("posts")
    suspend fun createPost(@Body post: NewsFeedDataClassItem): Response<NewsFeedDataClassItem>

    @Multipart
    @POST("posts") // ✅ Ensure this matches the backend route
    suspend fun uploadPostImage(
        @Part image: MultipartBody.Part, // ✅ Only upload the image first
        @Part("user_id") userId: RequestBody,
    @Part("content") content: RequestBody
    ): Response<ImageUploadResponse>


    @PUT("posts/{postId}") // ✅ Ensure this matches your backend route
    suspend fun updatePost(
        @Path("postId") postId: Int, // ✅ Send Post ID in URL
        @Body updateData: Map<String, String> // ✅ Send data as JSON
    ): Response<Unit> // ✅ Expect empty success response



    @GET("marketplace-items")
    suspend fun getAllMarketplaceItems(): List<MarketplaceDataClassItem>

    @POST("marketplace-items")
    suspend fun createMarketplaceItem(@Body item: MarketplaceDataClassItem): Response<itemPostResponse>

}




