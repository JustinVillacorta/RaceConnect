package com.example.raceconnect.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.Repost
import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Response
import com.example.raceconnect.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class NewsFeedPagingSourceAllPosts(
    private val apiService: ApiService,
    private val userId: Int,
    private val categories: List<String>,
    private val context: Context
) : PagingSource<Int, NewsFeedDataClassItem>() {

    companion object {
        @Volatile private var processedIdsGlobal = ConcurrentHashMap<Int, Boolean>()

        fun clearCaches() {
            processedIdsGlobal.clear()
            Log.d("PagingSourceAllPosts", "Global cache cleared")
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NewsFeedDataClassItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            if (anchorPage?.data.isNullOrEmpty()) 0 else anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        } ?: 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsFeedDataClassItem> {
        return try {
            val page = params.key ?: 0
            val limit = params.loadSize
            val offset = page * limit
            Log.d("PagingSourceAllPosts", "Loading page $page, limit $limit, offset $offset for userId $userId with categories $categories")

            val postsResponse: Response<List<NewsFeedDataClassItem>> = apiService.getPostsByCategoryAndPrivacy(
                userId = userId,
                categories = categories.joinToString(","),
                limit = limit,
                offset = offset
            )
            if (!postsResponse.isSuccessful) {
                Log.e("PagingSourceAllPosts", "Failed to fetch posts: ${postsResponse.code()} - ${postsResponse.message()}")
                return LoadResult.Error(Exception("Failed to load posts: ${postsResponse.message()}"))
            }

            val posts = postsResponse.body() ?: emptyList()
            Log.d("PagingSourceAllPosts", "Raw posts from API (count: ${posts.size}): ${posts.map { "ID=${it.id}, Status=${it.status}, Report=${it.report}" }}")

            val allItems = mutableListOf<NewsFeedDataClassItem>()
            val processedIds = mutableSetOf<Int>()
            val originalPostIds = mutableSetOf<Int>()

            posts.forEach { post ->
                val statusLower = post.status?.lowercase()
                Log.d("PagingSourceAllPosts", "Processing post ID: ${post.id}, Status: ${post.status}, Report: ${post.report}")
                if (statusLower != "archived") { // Exclude 'Archived' posts
                    val updatedPost = post.copy(isRepost = post.isRepost ?: false)
                    if (!processedIds.contains(updatedPost.id) && !processedIdsGlobal.containsKey(updatedPost.id)) {
                        allItems.add(updatedPost)
                        processedIds.add(updatedPost.id)
                        processedIdsGlobal[updatedPost.id] = true
                        originalPostIds.add(updatedPost.id)
                        Log.d("PagingSourceAllPosts", "Added post ID: ${updatedPost.id}, Status: ${updatedPost.status}, Report: ${updatedPost.report}")
                        if (statusLower == "hidden") {
                            Log.d("PagingSourceAllPosts", "Explicitly included hidden post ID: ${updatedPost.id}")
                        }
                    } else {
                        Log.d("PagingSourceAllPosts", "Skipped post ID: ${updatedPost.id} due to deduplication")
                    }
                } else {
                    Log.d("PagingSourceAllPosts", "Excluded archived post ID: ${post.id}")
                }
            }

            // Reposts (same logic)
            val repostResults: List<NewsFeedDataClassItem> = coroutineScope {
                val postIdsToFetch = originalPostIds.toList()
                val repostJobs = postIdsToFetch.map { postId ->
                    async {
                        try {
                            val repostsResponse: Response<List<Repost>> = apiService.getRepostsByPostId(
                                postId = postId,
                                limit = limit,
                                offset = 0
                            )
                            if (repostsResponse.isSuccessful) {
                                val reposts = repostsResponse.body()?.filter { repost ->
                                    repost.id != null && repost.userId != null && repost.createdAt != null && repost.postId != null
                                            && repost.postId == postId
                                } ?: emptyList()
                                reposts.map { repost ->
                                    val originalPost = posts.find { it.id == repost.postId }
                                    val userResponse = apiService.getUser(repost.userId!!)
                                    val username = if (userResponse.isSuccessful) userResponse.body()?.username else null
                                    val profilePicture = if (userResponse.isSuccessful) userResponse.body()?.profilePicture else null
                                    NewsFeedDataClassItem(
                                        id = repost.id!!,
                                        user_id = repost.userId!!,
                                        content = repost.quote ?: "",
                                        created_at = repost.createdAt!!,
                                        isRepost = true,
                                        original_post_id = repost.postId!!,
                                        like_count = 0,
                                        comment_count = 0,
                                        repost_count = 0,
                                        category = originalPost?.category ?: "Formula 1",
                                        privacy = originalPost?.privacy ?: "Public",
                                        type = originalPost?.type ?: "text",
                                        postType = originalPost?.postType ?: "normal",
                                        title = originalPost?.title ?: "Repost",
                                        profile_picture = profilePicture,
                                        username = username,
                                        report = originalPost?.report,
                                        status = originalPost?.status
                                    )
                                }.filter { it.status?.lowercase() != "archived" }
                            } else {
                                emptyList()
                            }
                        } catch (e: Exception) {
                            Log.e("PagingSourceAllPosts", "Error fetching reposts for post $postId: ${e.message}", e)
                            emptyList()
                        }
                    }
                }
                repostJobs.awaitAll().flatten()
            }

            repostResults.forEach { repost ->
                if (!processedIds.contains(repost.id)) {
                    allItems.add(repost)
                    processedIds.add(repost.id)
                    Log.d("PagingSourceAllPosts", "Added repost ID: ${repost.id}, Status: ${repost.status}")
                }
            }

            withContext(Dispatchers.Default) {
                allItems.sortByDescending { item ->
                    try {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.created_at ?: "")?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
            }

            LoadResult.Page(
                data = allItems,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (allItems.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("PagingSourceAllPosts", "Exception during load: ${e.message}", e)
            LoadResult.Error(e)
        }
    }
}