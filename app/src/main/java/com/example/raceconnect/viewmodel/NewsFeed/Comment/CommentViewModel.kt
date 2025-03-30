package com.example.raceconnect.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.model.PostComment
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CommentViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val apiService = RetrofitInstance.api
    val comments = mutableStateListOf<PostComment>()
    var errorMessage = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    fun fetchComments(postId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            comments.clear() // Clear existing comments before fetching
            try {
                val token = userPreferences.token.first() ?: run {
                    Log.w("CommentViewModel", "No auth token available")
                    errorMessage.value = "Authentication required"
                    isLoading.value = false
                    return@launch
                }
                Log.d("CommentViewModel", "Fetching comments for post ID: $postId")
                val response = apiService.getCommentsByPostId("Bearer $token", postId)
                if (response.isSuccessful) {
                    response.body()?.let { commentList ->
                        Log.d("CommentViewModel", "Raw comments response: $commentList")
                        // Assuming the API returns profile_picture in the response
                        comments.addAll(commentList)
                        if (commentList.isNotEmpty() && commentList[0].profilePicture == null) {
                            // If profile_picture is missing, fetch it separately (optional)
                            val enrichedComments = commentList.map { comment ->
                                val profilePicture = fetchProfilePicture(comment.userId, token)
                                comment.copy(profilePicture = profilePicture)
                            }
                            comments.clear()
                            comments.addAll(enrichedComments)
                        }
                    } ?: run {
                        errorMessage.value = "No comments available for this post."
                        Log.w("CommentViewModel", "Response body is null for post ID: $postId")
                    }
                } else {
                    errorMessage.value = "Failed to load comments: ${response.message()}"
                    Log.e("CommentViewModel", "Failed to fetch comments: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
                Log.e("CommentViewModel", "Exception fetching comments: ${e.message}", e)
            } finally {
                isLoading.value = false
            }
        }
    }

    // Optional: Fetch profile picture if not included in comments response
    private suspend fun fetchProfilePicture(userId: Int, token: String): String? {
        return try {
            val response = apiService.getUser(userId) // Hypothetical API call
            if (response.isSuccessful) {
                Log.d("CommentViewModel", "Fetched profile picture for user ID: $userId - ${response.body()?.profilePicture}")
                response.body()?.profilePicture
            } else {
                Log.w("CommentViewModel", "Failed to fetch profile picture for user ID: $userId, HTTP ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("CommentViewModel", "Error fetching profile picture for user ID: $userId", e)
            null
        }
    }

    fun addComment(comment: PostComment) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val token = userPreferences.token.first() ?: run {
                    errorMessage.value = "Authentication required"
                    return@launch
                }
                Log.d("CommentViewModel", "Adding comment for post ID: ${comment.postId}")
                val response = apiService.addComment("Bearer $token", comment)
                if (response.isSuccessful) {
                    fetchComments(comment.postId) // Refresh comments after adding
                    errorMessage.value = null
                    Log.d("CommentViewModel", "Comment added successfully")
                } else {
                    errorMessage.value = "Failed to add comment: ${response.message()}"
                    Log.e("CommentViewModel", "Failed to add comment: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
                Log.e("CommentViewModel", "Exception adding comment: ${e.message}", e)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updateComment(commentId: Int, newText: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val token = userPreferences.token.first() ?: run {
                    errorMessage.value = "Authentication required"
                    return@launch
                }
                Log.d("CommentViewModel", "Updating comment ID: $commentId")
                val response = apiService.updateComment("Bearer $token", commentId, mapOf("comment" to newText))
                if (response.isSuccessful) {
                    val comment = comments.find { it.id == commentId }
                    comment?.let { fetchComments(it.postId) }
                    Log.d("CommentViewModel", "Comment updated successfully")
                } else {
                    errorMessage.value = "Failed to update comment: ${response.message()}"
                    Log.e("CommentViewModel", "Failed to update comment: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
                Log.e("CommentViewModel", "Exception updating comment: ${e.message}", e)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val token = userPreferences.token.first() ?: run {
                    errorMessage.value = "Authentication required"
                    return@launch
                }
                Log.d("CommentViewModel", "Deleting comment ID: $commentId")
                val response = apiService.deleteComment("Bearer $token", commentId)
                if (response.isSuccessful) {
                    val comment = comments.find { it.id == commentId }
                    comment?.let { fetchComments(it.postId) }
                    Log.d("CommentViewModel", "Comment deleted successfully")
                } else {
                    errorMessage.value = "Failed to delete comment: ${response.message()}"
                    Log.e("CommentViewModel", "Failed to delete comment: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
                Log.e("CommentViewModel", "Exception deleting comment: ${e.message}", e)
            } finally {
                isLoading.value = false
            }
        }
    }
}