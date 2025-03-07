package com.example.raceconnect.viewmodel

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
                val token = userPreferences.token.first() ?: ""
                val response = apiService.getCommentsByPostId("Bearer $token", postId)
                if (response.isSuccessful) {
                    response.body()?.let { comments.addAll(it) } ?: run {
                        errorMessage.value = "No comments available for this post."
                    }
                } else {
                    errorMessage.value = "Failed to load comments: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addComment(comment: PostComment) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val token = userPreferences.token.first() ?: ""
                val response = apiService.addComment("Bearer $token", comment)
                if (response.isSuccessful) {
                    fetchComments(comment.postId) // Refresh comments after adding
                    errorMessage.value = null
                } else {
                    errorMessage.value = "Failed to add comment: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updateComment(commentId: Int, newText: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val token = userPreferences.token.first() ?: ""
                val response = apiService.updateComment("Bearer $token", commentId, mapOf("comment" to newText))
                if (response.isSuccessful) {
                    val comment = comments.find { it.id == commentId }
                    comment?.let { fetchComments(it.postId) }
                } else {
                    errorMessage.value = "Failed to update comment: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val token = userPreferences.token.first() ?: ""
                val response = apiService.deleteComment("Bearer $token", commentId)
                if (response.isSuccessful) {
                    val comment = comments.find { it.id == commentId }
                    comment?.let { fetchComments(it.postId) }
                } else {
                    errorMessage.value = "Failed to delete comment: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}