package com.example.raceconnect.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.raceconnect.model.NewsFeedDataClassItem
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class NewsFeedPagingSourceAllPosts(
    private val apiService: ApiService,
    private val userId: Int // Required parameter for user-specific posts
) : PagingSource<Int, NewsFeedDataClassItem>() {

    override fun getRefreshKey(state: PagingState<Int, NewsFeedDataClassItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsFeedDataClassItem> {
        return try {
            val page = params.key ?: 0
            val limit = params.loadSize
            val offset = page * limit
            Log.d("PagingSourceAllPosts", "Loading page $page, limit $limit, offset $offset for userId $userId")

            // Fetch original posts for the specified user
            val postsResponse = apiService.getPostsByUserId(userId = userId, limit = limit, offset = offset)
            if (!postsResponse.isSuccessful) {
                Log.e("PagingSourceAllPosts", "Failed to fetch posts: ${postsResponse.message()}")
                return LoadResult.Error(Exception("Failed to load posts: ${postsResponse.message()}"))
            }

            val posts = postsResponse.body()?.toMutableList() ?: mutableListOf()
            Log.d("PagingSourceAllPosts", "Fetched ${posts.size} posts from getPostsByUserId: ${posts.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}" }}")
            val allItems = mutableListOf<NewsFeedDataClassItem>()
            val processedIds = mutableSetOf<Int>() // To prevent duplicates

            // Add user's posts
            posts.forEach { post ->
                val updatedPost = post.copy(
                    isRepost = post.isRepost ?: false // Default to false if null
                )
                if (!processedIds.contains(updatedPost.id)) {
                    allItems.add(updatedPost)
                    processedIds.add(updatedPost.id)
                    Log.d("PagingSourceAllPosts", "Added post ID: ${updatedPost.id}, IsRepost: ${updatedPost.isRepost}")
                } else {
                    Log.d("PagingSourceAllPosts", "Skipped duplicate post ID: ${updatedPost.id} from getPostsByUserId")
                }
            }

            // Fetch reposts for each post
            posts.filter { it.isRepost != true }.forEach { post ->
                val repostsResponse = apiService.getRepostsByPostId( limit = limit, offset = offset)
                if (repostsResponse.isSuccessful) {
                    val reposts = repostsResponse.body()?.map { repost ->
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
                    Log.d("PagingSourceAllPosts", "Fetched ${reposts.size} reposts for post ${post.id}: ${reposts.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}" }}")
                    reposts.forEach { repost ->
                        if (!processedIds.contains(repost.id)) {
                            allItems.add(repost)
                            processedIds.add(repost.id)
                            Log.d("PagingSourceAllPosts", "Added repost ID: ${repost.id}, IsRepost: ${repost.isRepost}")
                        } else {
                            Log.d("PagingSourceAllPosts", "Skipped duplicate repost ID: ${repost.id}")
                        }
                    }
                } else {
                    Log.e("PagingSourceAllPosts", "Failed to fetch reposts for post ${post.id}: ${repostsResponse.code()} - ${repostsResponse.message()}")
                }
            }

            Log.d("PagingSourceAllPosts", "All items before sorting (${allItems.size} total): ${allItems.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}" }}")
            // Sort by created_at descending
            allItems.sortByDescending { item ->
                val parsedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.created_at)
                parsedDate?.time ?: 0L
            }
            Log.d("PagingSourceAllPosts", "All items after sorting (${allItems.size} total): ${allItems.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}" }}")

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