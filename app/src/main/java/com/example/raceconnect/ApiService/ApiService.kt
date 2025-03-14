package com.example.raceconnect.network

import com.example.raceconnect.model.*
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
    suspend fun getUser(@Path("id") id: Int): Response<users>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: UpdateUserRequest
    ): Response<UserSimpleResponse>

    @PUT("users/{id}")
    suspend fun updateUserCategories(
        @Path("id") id: Int,
        @Body request: UpdateUserFavoriteCategoriesRequest
    ): Response<UserSimpleResponse>

    @Multipart
    @POST("upload-profile-picture")
    suspend fun uploadProfilePicture(
        @Part("user_id") userId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<UploadProfilePictureResponse>

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
    ): Response<List<NewsFeedDataClassItem>>

    @GET("posts")
    suspend fun getPostsByUserId(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<List<NewsFeedDataClassItem>>

    @GET("posts")
    suspend fun getPostsByCategoryAndPrivacy(
        @Query("user_id") userId: Int,
        @Query("category") categories: String,
        @Query("limit") limit: Int = 100000,
        @Query("offset") offset: Int = 0
    ): Response<List<NewsFeedDataClassItem>>

    @GET("posts/{id}")
    suspend fun getPostById(
        @Path("id") id: Int
    ): Response<NewsFeedDataClassItem>

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

    @DELETE("likes/{id}")
    suspend fun unlikePost(@Path("id") likeId: Int): Response<ResponseBody>

    @GET("post-likes")
    suspend fun getPostLikes(@Query("post_id") postId: Int): Response<List<PostLike>>

    @POST("reports")
    suspend fun createReport(@Body report: ReportRequest): Response<ReportResponse>

    // Marketplace
    @GET("marketplace-items/{id}/images")
    suspend fun getMarketplaceItemImages(@Path("id") itemId: Int): Response<List<MarketplaceImageResponse>>

    @GET("marketplace-items/{id}")
    suspend fun getItemById(@Path("id") itemId: Int): Response<MarketplaceDataClassItem>

    @GET("marketplace-items/user/{userId}")
    suspend fun getMarketplaceItemsByUserId(
        @Path("userId") userId: Int,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<List<MarketplaceDataClassItem>>

    @GET("marketplace-item-likes/liked-posts")
    suspend fun getLikedItemsByUserIds(
        @Query("user_ids") userIds: String // Comma-separated user IDs, e.g., "4"
    ): Response<Map<String, Any>>

    @Multipart
    @POST("marketplace-items")
    suspend fun MarketplacePostImage(
        @Part("seller_id") seller_id: RequestBody,
        @Part("description") description: RequestBody,
        @Part("title") title: RequestBody,
        @Part("category") category: RequestBody,
        @Part("price") price: RequestBody,
        @Part("status") status: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): Response<itemPostResponse> // Updated return type

    @GET("marketplace-items")
    suspend fun getAllMarketplaceItems(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("exclude_seller_id") excludeSellerId: Int? = null
    ): Response<List<MarketplaceDataClassItem>>

    @GET("marketplace-item-likes")
    suspend fun getAllLikes(): Response<MarketplaceItemLikesResponse>

    @GET("marketplace-item-likes/{id}")
    suspend fun getLikesByItemId(@Path("id") itemId: Int): Response<MarketplaceItemLikesResponse>

    @GET("marketplace-items/{id}/likes")
    suspend fun getMarketplaceItemLikes(@Path("id") itemId: Int): Response<MarketplaceItemLikesResponse>

    @GET("marketplace-item-likes/user/{userId}")
    suspend fun getUserLikedItems(@Path("userId") userId: Int): Response<MarketplaceItemLikesResponse>

    @POST("marketplace-item-likes")
    suspend fun toggleLike(@Body params: Map<String, Int>): Response<Map<String, Any>>

    @DELETE("marketplace-item-likes/{id}")
    suspend fun deleteLike(@Path("id") likeId: Int): Response<Map<String, String>>

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

    @POST("post-reposts")
    suspend fun createRepost(
        @Body request: CreateRepostRequest
    ): Response<CreateRepostResponse>

    @GET("post-reposts")
    suspend fun getRepostsByPostId(
        @Query("postId") postId: Int,
        @Query("limit") limit: Int = 100000,
        @Query("offset") offset: Int = 0
    ): Response<List<Repost>>

    @DELETE("post-reposts/{id}")
    suspend fun deleteRepost(
        @Path("id") repostId: Int
    ): Response<SimpleResponse>
}