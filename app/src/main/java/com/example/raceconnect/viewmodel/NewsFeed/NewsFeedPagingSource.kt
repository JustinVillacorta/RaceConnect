package com.example.raceconnect.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.raceconnect.model.NewsFeedDataClassItem
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class NewsFeedPagingSourceAllPosts(
    private val apiService: ApiService
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
            Log.d("PagingSourceAllPosts", "Loading page $page, limit $limit, offset $offset")

            // Fetch original posts only
            val postsResponse = apiService.getAllPosts(limit = limit, offset = offset)
            if (!postsResponse.isSuccessful) {
                Log.e("PagingSourceAllPosts", "Failed to fetch posts: ${postsResponse.message()}")
                return LoadResult.Error(Exception("Failed to load posts: ${postsResponse.message()}"))
            }

            val posts = postsResponse.body()?.toMutableList() ?: mutableListOf()
            Log.d("PagingSourceAllPosts", "Fetched ${posts.size} posts from getAllPosts: ${posts.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}" }}")
            val allItems = mutableListOf<NewsFeedDataClassItem>()

            // Add original posts, ensuring isRepost defaults to false
            posts.forEach { post ->
                val updatedPost = post.copy(
                    isRepost = post.isRepost ?: false, // Default to false if null
                    original_post_id = post.original_post_id
                )
                if (updatedPost.isRepost != true) { // Only add non-reposts
                    allItems.add(updatedPost)
                    Log.d("PagingSourceAllPosts", "Added original post ID: ${updatedPost.id}, IsRepost: ${updatedPost.isRepost}")
                } else {
                    Log.d("PagingSourceAllPosts", "Skipped post ID: ${updatedPost.id} as it’s marked as a repost from getAllPosts")
                }
            }

            // Fetch reposts for each original post
            posts.filter { it.isRepost != true }.forEach { post ->
                val repostsResponse = apiService.getRepostsByPostId()
                if (repostsResponse.isSuccessful) {
                    val reposts = repostsResponse.body()?.map { repost ->
                        NewsFeedDataClassItem(
                            id = repost.id,
                            user_id = repost.userId,
                            content = repost.quote ?: "",
                            created_at = repost.createdAt,
                            isRepost = true, // Explicitly true for reposts
                            original_post_id = repost.postId,
                            like_count = 0,
                            comment_count = 0,
                            repost_count = 0,
                            category = post.category,
                            privacy = post.privacy,
                            type = post.type,
                            postType = post.postType,
                            title = post.title,
                            username = null // Add if available from API
                        )
                    } ?: emptyList()
                    Log.d("PagingSourceAllPosts", "Fetched ${reposts.size} reposts for post ${post.id}: ${reposts.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}" }}")
                    allItems.addAll(reposts)
                } else {
                    Log.e("PagingSourceAllPosts", "Failed to fetch reposts for post ${post.id}: ${repostsResponse.message()}")
                }
            }

            Log.d("PagingSourceAllPosts", "All items before sorting (${allItems.size} total): ${allItems.map { "ID=${it.id}, IsRepost=${it.isRepost}, CreatedAt=${it.created_at}" }}")
            // Sort by created_at descending (newest first)
            allItems.sortByDescending { item ->
                val parsedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.created_at)
                parsedDate?.time ?: 0L
            }
            Log.d("PagingSourceAllPosts", "All items after sorting (${allItems.size} total): ${allItems.map { "ID=${it.id}, IsRepost=${it.isRepost}, CreatedAt=${it.created_at}" }}")

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