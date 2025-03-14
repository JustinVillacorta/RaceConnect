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
        val refreshKey = state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            if (anchorPage?.data.isNullOrEmpty()) 0 else anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        } ?: 0
        Log.d("PagingSourceAllPosts", "Refresh key calculated: $refreshKey")
        return refreshKey
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsFeedDataClassItem> {
        return try {
            val startTime = System.currentTimeMillis()
            val page = params.key ?: 0
            val limit = params.loadSize
            val offset = page * limit
            Log.d("PagingSourceAllPosts", "Loading page $page, limit $limit, offset $offset for userId $userId with categories $categories")

            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.e("PagingSourceAllPosts", "No network available")
                return LoadResult.Error(Exception("No network available"))
            }

            // Fetch original posts for the current page
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
            Log.d("PagingSourceAllPosts", "Fetched ${posts.size} posts: ${posts.map { "ID=${it.id}, CreatedAt=${it.created_at}" }}")
            val allItems = mutableListOf<NewsFeedDataClassItem>()
            val processedIds = mutableSetOf<Int>() // Page-specific deduplication for original posts
            val processedRepostIds = mutableSetOf<Int>() // Separate deduplication for reposts
            val originalPostIds = mutableSetOf<Int>()

            // Add original posts to the current page
            posts.forEach { post ->
                val updatedPost = post.copy(isRepost = post.isRepost ?: false)
                if (!processedIds.contains(updatedPost.id) && !processedIdsGlobal.containsKey(updatedPost.id)) {
                    allItems.add(updatedPost)
                    processedIds.add(updatedPost.id)
                    processedIdsGlobal[updatedPost.id] = true
                    originalPostIds.add(updatedPost.id)
                    Log.d("PagingSourceAllPosts", "Added original post ID: ${updatedPost.id}, CreatedAt: ${updatedPost.created_at}")
                } else {
                    Log.d("PagingSourceAllPosts", "Skipped original post ID: ${updatedPost.id} due to deduplication")
                }
            }

            // Fetch reposts only for the original posts in this page
            val repostResults: List<NewsFeedDataClassItem> = coroutineScope {
                val postIdsToFetch = originalPostIds.toList()
                Log.d("PagingSourceAllPosts", "Fetching reposts for ${postIdsToFetch.size} post IDs: $postIdsToFetch")

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
                                            && repost.postId == postId // Ensure reposts match the requested postId
                                } ?: emptyList()
                                Log.d("PagingSourceAllPosts", "Fetched ${reposts.size} reposts for postId $postId: ${reposts.map { "RepostID=${it.id}, OriginalPostId=${it.postId}, CreatedAt=${it.createdAt}" }}")

                                reposts.map { repost ->
                                    val originalPost = posts.find { it.id == repost.postId }
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
                                        username = null
                                    )
                                }
                            } else {
                                Log.e("PagingSourceAllPosts", "Failed to fetch reposts for post $postId: ${repostsResponse.code()} - ${repostsResponse.message()}")
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

            // Add reposts to the current page with separate deduplication
            repostResults.forEach { repost ->
                if (!processedRepostIds.contains(repost.id)) { // Check only repost-specific deduplication
                    allItems.add(repost)
                    processedRepostIds.add(repost.id)
                    Log.d("PagingSourceAllPosts", "Added repost ID: ${repost.id}, OriginalPostId: ${repost.original_post_id}, CreatedAt: ${repost.created_at}")
                } else {
                    Log.d("PagingSourceAllPosts", "Skipped repost ID: ${repost.id} due to repost-specific deduplication")
                }
            }

            // Sort items by created_at in descending order
            withContext(Dispatchers.Default) {
                allItems.sortByDescending { item ->
                    try {
                        if (item.created_at.isNullOrEmpty()) 0L
                        else SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.created_at)?.time ?: 0L
                    } catch (e: Exception) {
                        Log.e("PagingSourceAllPosts", "Error parsing date ${item.created_at} for ID: ${item.id}: ${e.message}")
                        0L
                    }
                }
            }

            val endTime = System.currentTimeMillis()
            Log.d("PagingSourceAllPosts", "Load completed in ${endTime - startTime}ms with ${allItems.size} items: ${allItems.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}" }}")

            LoadResult.Page(
                data = allItems,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (posts.isEmpty() && repostResults.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("PagingSourceAllPosts", "Exception during load: ${e.message}", e)
            LoadResult.Error(e)
        }
    }
}