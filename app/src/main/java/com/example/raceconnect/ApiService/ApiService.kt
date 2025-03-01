package com.example.raceconnect.network




import com.example.raceconnect.model.ApiResponse
import com.example.raceconnect.model.ForgotPasswordRequest
import com.example.raceconnect.model.ForgotPasswordResponse

import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.LogoutRequest
import com.example.raceconnect.model.LogoutResponse
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.PostLike
import com.example.raceconnect.model.PostResponse
import com.example.raceconnect.model.ResetPasswordRequest
import com.example.raceconnect.model.ResetPasswordResponse
import com.example.raceconnect.model.SignupRequest
import com.example.raceconnect.model.SignupResponse
import com.example.raceconnect.model.VerifyOtpRequest
import com.example.raceconnect.model.VerifyOtpResponse
import com.example.raceconnect.model.itemPostRequest
import com.example.raceconnect.model.itemPostResponse
import com.example.raceconnect.model.users
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse


    @POST("logout")
    suspend fun logout(@Header("Authorization") authToken: String): Response<LogoutResponse>



    @POST("users")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>



    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): users

    @POST("forgot-password")
    suspend fun requestOtp(
        @Body request: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse>


    @POST("verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>


    @PUT("reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ResetPasswordResponse>



    @GET("posts")
    suspend fun getAllPosts(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): List<NewsFeedDataClassItem>

    @Multipart
    @POST("posts")
    suspend fun createPostWithImage(
        @Part("user_id") userId: RequestBody,
        @Part("content") content: RequestBody,
        @Part("title") title: RequestBody,
        @Part("category") category: RequestBody,
        @Part("privacy") privacy: RequestBody,
        @Part("type") type: RequestBody,
        @Part("post_type") postType: RequestBody,
        @Part image: MultipartBody.Part? // Nullable for text-only posts
    ): Response<PostResponse>



    @GET("posts/{id}/images")
    suspend fun GetPostImg(@Path("id") id: Int): Response<List<PostResponse>> // ✅ Expect a list


    @POST("post-likes")
    suspend fun likePost(@Body requestBody: Map<String, Int>): Response<ResponseBody>

    @DELETE("post-likes/{id}")
    suspend fun unlikePost(@Path("id") postLikeId: Int): Response<ResponseBody>

    @GET("post-likes")
    suspend fun getPostLikes(@Query("post_id") postId: Int): Response<List<PostLike>>


    @GET("marketplace-items")
    suspend fun getAllMarketplaceItems(): List<MarketplaceDataClassItem>

    @POST("marketplace-items")
    suspend fun createMarketplaceItem(@Body item: MarketplaceDataClassItem): Response<itemPostResponse>

}




