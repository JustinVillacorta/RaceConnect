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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class NewsFeedPagingSourceAllPosts(
    private val apiService: ApiService,
    private val userId: Int,
    private val categories: List<String>,
    private val context: Context
) : PagingSource<Int, NewsFeedDataClassItem>() {

    override fun getRefreshKey(state: PagingState<Int, NewsFeedDataClassItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
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
            Log.d("PagingSourceAllPosts", "Fetched ${posts.size} posts from getPostsByCategoryAndPrivacy: ${posts.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}" }}")
            val allItems = mutableListOf<NewsFeedDataClassItem>()
            val processedIds = mutableSetOf<Int>()

            posts.forEach { post ->
                val updatedPost = post.copy(
                    isRepost = post.isRepost ?: false
                )
                if (!processedIds.contains(updatedPost.id)) {
                    allItems.add(updatedPost)
                    processedIds.add(updatedPost.id)
                    Log.d("PagingSourceAllPosts", "Added post ID: ${updatedPost.id}, IsRepost: ${updatedPost.isRepost}")
                } else {
                    Log.d("PagingSourceAllPosts", "Skipped duplicate post ID: ${updatedPost.id} from getPostsByCategoryAndPrivacy")
                }
            }

            // Fetch reposts in parallel
            coroutineScope {
                val repostJobs = posts.filter { it.isRepost != true }.map { post ->
                    async {
                        try {
                            val repostsResponse: Response<List<Repost>> = apiService.getRepostsByPostId(
                                postId = post.id,
                                limit = limit,
                                offset = offset
                            )
                            if (repostsResponse.isSuccessful) {
                                repostsResponse.body()?.map { repost ->
                                    NewsFeedDataClassItem(
                                        id = repost.id,
                                        user_id = repost.userId,
                                        content = repost.quote ?: "",
                                        created_at = repost.createdAt,
                                        isRepost = true,
                                        original_post_id = repost.postId,
                                        like_count = 0,
                                        comment_count = 0,
                                        repost_count = 0,
                                        category = post.category,
                                        privacy = post.privacy,
                                        type = post.type,
                                        postType = post.postType,
                                        title = post.title,
                                        username = null
                                    )
                                } ?: emptyList()
                            } else {
                                Log.e("PagingSourceAllPosts", "Failed to fetch reposts for post ${post.id}: ${repostsResponse.code()} - ${repostsResponse.message()}")
                                emptyList()
                            }
                        } catch (e: Exception) {
                            Log.e("PagingSourceAllPosts", "Error fetching reposts for post ${post.id}: ${e.message}", e)
                            emptyList()
                        }
                    }
                }

                val repostResults = repostJobs.awaitAll()
                repostResults.flatten().forEach { repost ->
                    if (!processedIds.contains(repost.id)) {
                        allItems.add(repost)
                        processedIds.add(repost.id)
                        Log.d("PagingSourceAllPosts", "Added repost ID: ${repost.id}, IsRepost: ${repost.isRepost}")
                    } else {
                        Log.d("PagingSourceAllPosts", "Skipped duplicate repost ID: ${repost.id}")
                    }
                }
            }

            Log.d("PagingSourceAllPosts", "All items before sorting (${allItems.size} total): ${allItems.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}" }}")
            allItems.sortByDescending { item ->
                try {
                    val parsedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.created_at)
                    parsedDate?.time ?: 0L
                } catch (e: Exception) {
                    Log.e("PagingSourceAllPosts", "Error parsing date ${item.created_at} for post ${item.id}: ${e.message}")
                    0L
                }
            }
            Log.d("PagingSourceAllPosts", "All items after sorting (${allItems.size} total): ${allItems.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}" }}")

            val endTime = System.currentTimeMillis()
            Log.d("PagingSourceAllPosts", "Load completed in ${endTime - startTime}ms")

            LoadResult.Page(
                data = allItems,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (posts.size < limit) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("PagingSourceAllPosts", "Exception during load: ${e.message}", e)
            LoadResult.Error(e)
        }
    }
}