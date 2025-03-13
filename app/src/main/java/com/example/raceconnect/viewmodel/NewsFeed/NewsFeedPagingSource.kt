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
        private const val MAX_REPOST_FETCHES = 5 // Limit total repost requests per page
        @Volatile private var fetchedRepostPostIds = ConcurrentHashMap<Int, Boolean>()
        @Volatile private var fetchedOriginalPostIds = ConcurrentHashMap<Int, Boolean>()
        @Volatile private var processedRepostIds = ConcurrentHashMap<Int, Boolean>()

        fun clearCaches() {
            fetchedRepostPostIds.clear()
            fetchedOriginalPostIds.clear()
            processedRepostIds.clear()
            Log.d("PagingSourceAllPosts", "All caches cleared")
        }
    }

    private var isInitialLoad = true

    override fun getRefreshKey(state: PagingState<Int, NewsFeedDataClassItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            if (anchorPage?.data.isNullOrEmpty()) {
                0
            } else {
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        } ?: 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsFeedDataClassItem> {
        return try {
            val startTime = System.currentTimeMillis()
            val page = params.key ?: 0
            val limit = params.loadSize
            val offset = page * limit
            Log.d("PagingSourceAllPosts", "Loading page $page, limit $limit, offset $offset for userId $userId with categories $categories, IsInitialLoad: $isInitialLoad")

            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.e("PagingSourceAllPosts", "No network available")
                return LoadResult.Error(Exception("No network available"))
            }

            // Fetch posts
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
            Log.d("PagingSourceAllPosts", "Fetched ${posts.size} posts from getPostsByCategoryAndPrivacy: ${posts.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}, Category=${it.category}" }}")
            val allItems = mutableListOf<NewsFeedDataClassItem>()
            val processedIds = mutableSetOf<Int>()

            // Add posts and track original post IDs
            posts.forEach { post ->
                val updatedPost = post.copy(isRepost = post.isRepost ?: false)
                if (!processedIds.contains(updatedPost.id)) {
                    allItems.add(updatedPost)
                    processedIds.add(updatedPost.id)
                    if (updatedPost.isRepost != true) {
                        fetchedOriginalPostIds[updatedPost.id] = true // Track original posts
                    }
                    Log.d("PagingSourceAllPosts", "Added post ID: ${updatedPost.id}, IsRepost: ${updatedPost.isRepost}, CreatedAt: ${updatedPost.created_at}, Category: ${updatedPost.category}")
                } else {
                    Log.d("PagingSourceAllPosts", "Skipped duplicate post ID: ${updatedPost.id} from getPostsByCategoryAndPrivacy")
                }
            }

            // Fetch reposts in parallel with deduplication and limit, only for posts in saved categories
            coroutineScope {
                // Unique post IDs to fetch reposts for, filtered by category
                val uniquePostIds = posts.filter { post ->
                    // Safely check if the post is not a repost and its category matches
                    (post.isRepost == false) && post.category?.let { category -> categories.contains(category) } == true
                }.map { it.id }.distinct().take(MAX_REPOST_FETCHES)

                Log.d("PagingSourceAllPosts", "Fetching reposts for post IDs: $uniquePostIds (filtered by categories $categories)")

                val repostJobs = uniquePostIds.map { postId ->
                    async {
                        if (page == 0 || !fetchedRepostPostIds.containsKey(postId)) {
                            try {
                                val repostsResponse: Response<List<Repost>> = apiService.getRepostsByPostId(
                                    postId = postId,
                                    limit = limit,
                                    offset = offset
                                )
                                if (repostsResponse.isSuccessful) {
                                    fetchedRepostPostIds[postId] = true // Mark as fetched
                                    repostsResponse.body()?.filter { repost ->
                                        // Ensure required fields are not null
                                        repost.id != null && repost.userId != null && repost.createdAt != null && repost.postId != null
                                    }?.map { repost ->
                                        Log.d("PagingSourceAllPosts", "Mapping repost: ID=${repost.id}, UserId=${repost.userId}, CreatedAt=${repost.createdAt}, PostId=${repost.postId}")
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
                                            category = posts.find { it.id == postId }?.category ?: "Formula 1",
                                            privacy = posts.find { it.id == postId }?.privacy ?: "Public",
                                            type = posts.find { it.id == postId }?.type ?: "text",
                                            postType = posts.find { it.id == postId }?.postType ?: "normal",
                                            title = posts.find { it.id == postId }?.title ?: "Repost",
                                            username = null
                                        )
                                    } ?: emptyList()
                                } else {
                                    Log.e("PagingSourceAllPosts", "Failed to fetch reposts for post $postId: ${repostsResponse.code()} - ${repostsResponse.message()}")
                                    emptyList()
                                }
                            } catch (e: Exception) {
                                Log.e("PagingSourceAllPosts", "Error fetching reposts for post $postId: ${e.message}", e)
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    }
                }

                val repostResults = repostJobs.awaitAll().flatten()
                val missingOriginalIds = mutableSetOf<Int>()
                val tempReposts = mutableListOf<NewsFeedDataClassItem>()

                // First pass: Collect reposts and identify missing originals
                repostResults.forEach { repost ->
                    if (!processedIds.contains(repost.id) && !processedRepostIds.containsKey(repost.id)) {
                        val originalPostId = repost.original_post_id
                        val originalPost = allItems.find { it.id == originalPostId }
                        // Only add repost if the original post's category matches the saved categories
                        if (originalPost != null && originalPost.category?.let { categories.contains(it) } == true) {
                            allItems.add(repost)
                            processedIds.add(repost.id)
                            processedRepostIds[repost.id] = true
                            Log.d("PagingSourceAllPosts", "Added repost ID: ${repost.id}, IsRepost: ${repost.isRepost}, OriginalPostId: ${repost.original_post_id}, CreatedAt: ${repost.created_at}, Original Category: ${originalPost.category}")
                        } else if (originalPostId != null) {
                            if (originalPost != null) {
                                Log.d("PagingSourceAllPosts", "Skipped repost ID: ${repost.id} - Original post category ${originalPost.category} not in saved categories $categories")
                            } else {
                                tempReposts.add(repost)
                                missingOriginalIds.add(originalPostId)
                                Log.d("PagingSourceAllPosts", "Deferred repost ID: ${repost.id} - Original post ${repost.original_post_id} not fetched yet")
                            }
                        }
                    } else {
                        Log.d("PagingSourceAllPosts", "Skipped duplicate or processed repost ID: ${repost.id}")
                    }
                }

                // Fetch missing original posts using getPostById
                if (missingOriginalIds.isNotEmpty()) {
                    Log.d("PagingSourceAllPosts", "Attempting to fetch missing original posts: $missingOriginalIds")
                    val fetchJobs = missingOriginalIds.map { originalPostId ->
                        async {
                            try {
                                val postResponse: Response<NewsFeedDataClassItem> = apiService.getPostById(originalPostId)
                                if (postResponse.isSuccessful) {
                                    postResponse.body()?.let { post ->
                                        if (!processedIds.contains(post.id) && (post.isRepost?.not() ?: true)) {
                                            Log.d("PagingSourceAllPosts", "Fetched missing original post ID: ${post.id}, CreatedAt: ${post.created_at}, Category: ${post.category}")
                                            post
                                        } else {
                                            null
                                        }
                                    }
                                } else {
                                    Log.e("PagingSourceAllPosts", "Failed to fetch post ID $originalPostId: ${postResponse.code()} - ${postResponse.message()}")
                                    null
                                }
                            } catch (e: Exception) {
                                Log.e("PagingSourceAllPosts", "Error fetching post ID $originalPostId: ${e.message}", e)
                                null
                            }
                        }
                    }

                    // Add fetched original posts to allItems
                    fetchJobs.awaitAll().filterNotNull().forEach { fetchedPost ->
                        // Only add fetched original posts if they match the saved categories
                        if (fetchedPost.category?.let { categories.contains(it) } == true) {
                            allItems.add(fetchedPost)
                            processedIds.add(fetchedPost.id)
                            fetchedOriginalPostIds[fetchedPost.id] = true
                            Log.d("PagingSourceAllPosts", "Added fetched original post ID: ${fetchedPost.id}, Category: ${fetchedPost.category}")
                        } else {
                            Log.d("PagingSourceAllPosts", "Skipped fetched original post ID: ${fetchedPost.id} - Category ${fetchedPost.category} not in saved categories $categories")
                        }
                    }

                    // Second pass: Add reposts whose originals are now fetched
                    tempReposts.forEach { deferredRepost ->
                        val originalPostId = deferredRepost.original_post_id
                        val originalPost = allItems.find { it.id == originalPostId }
                        if (originalPostId != null && originalPost != null && originalPost.category?.let { categories.contains(it) } == true) {
                            if (!processedIds.contains(deferredRepost.id) && !processedRepostIds.containsKey(deferredRepost.id)) {
                                allItems.add(deferredRepost)
                                processedIds.add(deferredRepost.id)
                                processedRepostIds[deferredRepost.id] = true
                                Log.d("PagingSourceAllPosts", "Added deferred repost ID: ${deferredRepost.id}, OriginalPostId: ${deferredRepost.original_post_id}, CreatedAt: ${deferredRepost.created_at}, Original Category: ${originalPost.category}")
                            }
                        } else {
                            Log.d("PagingSourceAllPosts", "Skipped repost ID: ${deferredRepost.id} - Original post ${deferredRepost.original_post_id} not fetched or category mismatch")
                        }
                    }
                }
            }

            // Reset isInitialLoad after the first load
            if (isInitialLoad && page == 0) {
                isInitialLoad = false
            }

            Log.d("PagingSourceAllPosts", "All items before sorting (${allItems.size} total): ${allItems.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}, Category=${it.category}" }}")
            // Sort on background thread (ensures chronological order by item's own created_at)
            withContext(Dispatchers.Default) {
                allItems.sortByDescending { item ->
                    try {
                        if (item.created_at.isNullOrEmpty()) {
                            Log.w("PagingSourceAllPosts", "Empty or null created_at for post/repost ID: ${item.id}, using 0L")
                            0L
                        } else {
                            val parsedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.created_at)
                            parsedDate?.time ?: run {
                                Log.w("PagingSourceAllPosts", "Failed to parse created_at ${item.created_at} for post/repost ID: ${item.id}, using 0L")
                                0L
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PagingSourceAllPosts", "Error parsing date ${item.created_at} for post/repost ID: ${item.id}: ${e.message}")
                        0L
                    }
                }
            }
            Log.d("PagingSourceAllPosts", "All items after sorting (${allItems.size} total): ${allItems.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}, Category=${it.category}" }}")

            val endTime = System.currentTimeMillis()
            Log.d("PagingSourceAllPosts", "Load completed in ${endTime - startTime}ms")

            LoadResult.Page(
                data = allItems,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (posts.size < limit) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("PagingSourceAllPosts", "Exception during load: ${e.message}", e)
            return LoadResult.Error(e)
        }
    }
}