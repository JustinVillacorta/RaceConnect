package com.example.raceconnect.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.model.PostComment
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.ReplyRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CommentViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val apiService = RetrofitInstance.api
    val comments = mutableStateListOf<PostComment>()
    var errorMessage = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)
    val isLoadingRepliesMap = mutableStateMapOf<Int, Boolean>()
    private val replyPages = mutableMapOf<Int, Int>() // parentCommentId -> current page
    val canLoadMoreReplies = mutableMapOf<Int, Boolean>() // parentCommentId -> can load more
    private val replyPageSize = 5

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

    // Like a comment
    fun likeComment(commentId: Int) {
        viewModelScope.launch {
            try {
                val token = userPreferences.token.first() ?: ""
                val userId = userPreferences.user.first()?.id ?: 0
                if (userId == 0) {
                    errorMessage.value = "User not logged in"
                    return@launch
                }
                val likeData = mapOf("user_id" to userId, "comment_id" to commentId)
                val response = apiService.addCommentLike("Bearer $token", likeData)
                if (response.isSuccessful) {
                    val comment = comments.find { it.id == commentId }
                    comment?.let {
                        val updated = it.copy(likes = it.likes + 1, isLiked = true)
                        comments[comments.indexOf(it)] = updated
                    }
                } else {
                    errorMessage.value = "Failed to like comment: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    // Unlike a comment
    fun unlikeComment(commentId: Int) {
        viewModelScope.launch {
            try {
                val token = userPreferences.token.first() ?: ""
                val userId = userPreferences.user.first()?.id ?: 0
                if (userId == 0) {
                    errorMessage.value = "User not logged in"
                    return@launch
                }
                val unlikeData = mapOf("user_id" to userId, "comment_id" to commentId)
                val response = apiService.removeCommentLike("Bearer $token", unlikeData)
                if (response.isSuccessful) {
                    val comment = comments.find { it.id == commentId }
                    comment?.let {
                        val updated = it.copy(likes = it.likes - 1, isLiked = false)
                        comments[comments.indexOf(it)] = updated
                    }
                } else {
                    errorMessage.value = "Failed to unlike comment: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    // Fetch replies for a comment
    fun fetchReplies(parentCommentId: Int, loadMore: Boolean = false) {
        if (isLoadingRepliesMap[parentCommentId] == true) {
            Log.d("CommentViewModel", "Already loading replies for comment $parentCommentId, skipping")
            return
        }
        if (isLoading.value || !(canLoadMoreReplies[parentCommentId] ?: true)) {
            Log.d("CommentViewModel", "Skipping fetchReplies: isLoading=${isLoading.value}, loadMore=$loadMore, currentPage=${replyPages[parentCommentId] ?: 1}, canLoadMore=${canLoadMoreReplies[parentCommentId] ?: true}")
            return
        }

        viewModelScope.launch {
            isLoadingRepliesMap[parentCommentId] = true
            try {
                val token = userPreferences.token.first() ?: run {
                    Log.w("CommentViewModel", "No auth token available for fetching replies for comment ID: $parentCommentId")
                    errorMessage.value = "Authentication token is missing"
                    isLoadingRepliesMap[parentCommentId] = false
                    return@launch
                }
                val currentPage = if (loadMore) (replyPages[parentCommentId] ?: 1) else 1
                Log.d("CommentViewModel", "Fetching replies for comment $parentCommentId, page $currentPage")
                val response = apiService.getRepliesByCommentId("Bearer $token", parentCommentId, currentPage, replyPageSize)
                Log.d("CommentViewModel", "API response: isSuccessful=${response.isSuccessful}, code=${response.code()}, body=${response.body()}")
                if (response.isSuccessful) {
                    val newReplies = response.body() ?: emptyList()
                    Log.d("CommentViewModel", "Fetched ${newReplies.size} replies for comment $parentCommentId: $newReplies")
                    val comment = comments.find { it.id == parentCommentId }
                    if (comment != null) {
                        val currentReplies = if (loadMore) comment.replies ?: emptyList() else emptyList()
                        val updatedReplies = currentReplies + newReplies
                        Log.d("CommentViewModel", "Updating comment $parentCommentId with ${updatedReplies.size} replies")
                        val updated = comment.copy(replies = updatedReplies)
                        val index = comments.indexOf(comment)
                        if (index != -1) {
                            comments[index] = updated
                            replyPages[parentCommentId] = currentPage + 1
                            canLoadMoreReplies[parentCommentId] = newReplies.size == replyPageSize
                            Log.d("CommentViewModel", "Updated comments list: ${comments[index].replies}")
                        } else {
                            Log.w("CommentViewModel", "Comment $parentCommentId not found in comments list")
                        }
                    } else {
                        Log.w("CommentViewModel", "Comment with ID $parentCommentId not found")
                    }
                } else {
                    errorMessage.value = "Failed to fetch replies: ${response.message()}, code: ${response.code()}"
                    Log.w("CommentViewModel", "Failed to fetch replies for comment ID: $parentCommentId, HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Error fetching replies: ${e.message}"
                Log.e("CommentViewModel", "Exception while fetching replies for comment ID: $parentCommentId", e)
            } finally {
                isLoadingRepliesMap[parentCommentId] = false
                isLoading.value = false
                Log.d("CommentViewModel", "Finished fetching replies for comment $parentCommentId")
            }
        }
    }

    fun addReply(parentCommentId: Int, replyText: String) {
        viewModelScope.launch {
            // Log the start of the method with input parameters
            Log.d("CommentViewModel", "addReply called with parentCommentId: $parentCommentId, replyText: $replyText")

            try {
                val token = userPreferences.token.first() ?: ""
                Log.d("CommentViewModel", "Retrieved token: $token")

                val userId = userPreferences.user.first()?.id ?: 0
                Log.d("CommentViewModel", "Retrieved userId: $userId")

                if (userId == 0) {
                    Log.w("CommentViewModel", "User not logged in, aborting reply addition")
                    errorMessage.value = "User not logged in"
                    return@launch
                }

                val replyData = ReplyRequest(userId, parentCommentId, replyText)
                Log.d("CommentViewModel", "Sending reply request: $replyData")

                val response = apiService.addReply("Bearer $token", replyData)
                if (response.isSuccessful) {
                    Log.i("CommentViewModel", "Reply added successfully: ${response.body()}")
                    fetchReplies(parentCommentId)
                } else {
                    Log.e("CommentViewModel", "Failed to add reply: ${response.message()}, code: ${response.code()}")
                    errorMessage.value = "Failed to add reply: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("CommentViewModel", "Exception while adding reply: ${e.message}", e)
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }
}