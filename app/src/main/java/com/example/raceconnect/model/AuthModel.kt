package com.example.raceconnect.model

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

// Data class representing a User object
data class users(
    val id: Int,
    val username: String,
    val email: String,
    val birthdate: String?,
    val number: String?,
    val address: String?,
    val age: Int?,
    @SerializedName("profile_picture") val profilePicture: String?,
    val bio: String?,
    @SerializedName("favorite_categories")
    @JsonAdapter(FavoriteCategoriesAdapter::class)
    val favoriteCategories: List<String>?,
    @SerializedName("favorite_marketplace_items")
    @JsonAdapter(FavoriteMarketplaceItemsAdapter::class)
    val favoriteMarketplaceItems: List<String>?,
    @JsonAdapter(FriendsListDeserializer::class)
    @SerializedName("friends_list") val friendsList: List<Int>?,
    @SerializedName("friend_privacy") val friendPrivacy: String?,
    @SerializedName("last_online") val lastOnline: String?,
    val status: String?,
    val report: String?,
    @SerializedName("suspension_end_date") val suspensionEndDate: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)



class FavoriteCategoriesAdapter : JsonDeserializer<List<String>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<String> {
        return if (json?.isJsonArray == true) {
            // If it's an array, deserialize it as a list
            context?.deserialize<List<String>>(json, typeOfT) ?: emptyList()
        } else if (json?.isJsonPrimitive == true && json.asJsonPrimitive.isString) {
            // If it's a string, wrap it into a single-item list
            listOf(json.asString)
        } else {
            emptyList() // Default to empty list if neither
        }
    }
}

class FavoriteMarketplaceItemsAdapter : JsonDeserializer<List<String>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<String> {
        return if (json?.isJsonArray == true) {
            context?.deserialize<List<String>>(json, typeOfT) ?: emptyList()
        } else if (json?.isJsonPrimitive == true && json.asJsonPrimitive.isString) {
            listOf(json.asString)
        } else {
            emptyList()
        }
    }
}

class FriendsListDeserializer : JsonDeserializer<List<Int>?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): List<Int>? {
        return when {
            json == null || json.isJsonNull -> null  // If null, return null
            json.isJsonArray -> json.asJsonArray.mapNotNull { it.asIntOrNull() } // If array, parse it
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> emptyList() // If string, return empty list
            else -> null
        }
    }

    private fun JsonElement.asIntOrNull(): Int? {
        return try {
            this.asInt
        } catch (e: Exception) {
            null
        }
    }
}

// Rest of the data classes remain unchanged
data class UpdateUserRequest(
    val username: String,
    val birthdate: String?,
    val number: String?,
    val address: String?,
    val bio: String?
)

data class UploadProfilePictureResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("image_url") val imageUrl: String?
)

data class ProfileImage(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("image_url") val imageUrl: String
)

data class UserSimpleResponse(
    val success: Boolean,
    val message: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: users?,
    val token: String?,
    val status: String?,
    val suspension_end_date: String?
)

data class SignupRequest(
    val username: String,
    val email: String,
    val password: String
)

data class SignupResponse(
    val token: String?,
    val user: users?,
    val message: String?
)

data class LogoutRequest(val token: String)
data class LogoutResponse(val message: String)

data class ForgotPasswordRequest(val email: String)
data class ForgotPasswordResponse(val message: String)

data class VerifyOtpRequest(val email: String, val otp: String)
data class VerifyOtpResponse(val message: String, val verified: Boolean)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String
)

data class ResetPasswordResponse(val message: String)

data class ApiResponse(
    val message: String
)