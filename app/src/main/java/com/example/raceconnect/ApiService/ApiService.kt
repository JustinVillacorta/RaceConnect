package com.example.raceconnect.network

import com.example.raceconnect.model.ApiResponse
import com.example.raceconnect.model.CreateNotificationResponse
import com.example.raceconnect.model.CreateRepostRequest
import com.example.raceconnect.model.CreateRepostResponse
import com.example.raceconnect.model.ForgotPasswordRequest
import com.example.raceconnect.model.ForgotPasswordResponse
import com.example.raceconnect.model.Friend
import com.example.raceconnect.model.FriendRequest
import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.LogoutRequest
import com.example.raceconnect.model.LogoutResponse
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.Notification
import com.example.raceconnect.model.NotificationRequest
import com.example.raceconnect.model.PostLike
import com.example.raceconnect.model.PostResponse
import com.example.raceconnect.model.RemoveFriendRequest
import com.example.raceconnect.model.ResetPasswordRequest
import com.example.raceconnect.model.ResetPasswordResponse
import com.example.raceconnect.model.SignupRequest
import com.example.raceconnect.model.SignupResponse
import com.example.raceconnect.model.SimpleResponse
import com.example.raceconnect.model.UpdateFriendStatus
import com.example.raceconnect.model.VerifyOtpRequest
import com.example.raceconnect.model.VerifyOtpResponse
import com.example.raceconnect.model.itemPostRequest
import com.example.raceconnect.model.itemPostResponse
import com.example.raceconnect.model.users
import com.example.raceconnect.model.PostComment
import com.example.raceconnect.model.Repost
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

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
        @Part image: MultipartBody.Part?
    ): Response<PostResponse>

    @GET("posts/{id}/images")
    suspend fun GetPostImg(@Path("id") id: Int): Response<List<PostResponse>>

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

    @GET("friends/list")
    suspend fun getFriendsList(@Query("user_id") userId: String): Response<List<Map<String, Any?>>>

    @GET("friends/pending")
    suspend fun getPendingRequests(
        @Query("action") action: String = "pending",
        @Query("user_id") userId: String
    ): Response<List<Map<String, Any?>>>

    @GET("friends/nonfriends")
    suspend fun getNonFriends(
        @Query("action") string: String = "non_friends",
        @Query("user_id") userId: String
    ): Response<List<Map<String, Any?>>>

    @POST("friends/add")
    suspend fun sendFriendRequest(@Body request: FriendRequest): Response<Unit>

    @PUT("friends/update")
    suspend fun updateFriendStatus(@Body request: UpdateFriendStatus): Response<Unit>

    @DELETE("friends/remove")
    suspend fun removeFriend(
        @Query("user_id") userId: String,
        @Query("friend_id") friendId: String
    ): Response<Unit>

    @GET("notifications")
    suspend fun getAllNotifications(
        @Query("user_id") userId: Int
    ): Response<List<Notification>>

    @GET("notifications/{id}")
    suspend fun getNotificationById(
        @Path("id") id: Int
    ): Response<Notification>

    @POST("notifications")
    suspend fun createNotification(
        @Body notification: NotificationRequest
    ): Response<CreateNotificationResponse>

    @PUT("notifications/{id}")
    suspend fun markAsRead(
        @Path("id") id: Int
    ): Response<SimpleResponse>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") id: Int
    ): Response<SimpleResponse>

    @GET("post-comments")
    suspend fun getCommentsByPostId(
        @Header("Authorization") authToken: String,
        @Query("post_id") postId: Int
    ): Response<List<PostComment>>

    @POST("post-comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Body comment: PostComment
    ): Response<Unit>

    @PUT("post-comments/{id}")
    suspend fun updateComment(
        @Header("Authorization") authToken: String,
        @Path("id") id: Int,
        @Body comment: Map<String, String>
    ): Response<Map<String, String>>

    @DELETE("post-comments/{id}")
    suspend fun deleteComment(
        @Header("Authorization") authToken: String,
        @Path("id") id: Int
    ): Response<Map<String, String>>

    // Add new repost endpoints
    @POST("post-reposts")
    suspend fun createRepost(
        @Body request: CreateRepostRequest
    ): Response<CreateRepostResponse>

    @GET("post-reposts")
    suspend fun getRepostsByPostId(
        @Query("post_id") postId: Int
    ): Response<List<Repost>>

    @DELETE("post-reposts/{id}")
    suspend fun deleteRepost(
        @Path("id") repostId: Int
    ): Response<SimpleResponse>
}