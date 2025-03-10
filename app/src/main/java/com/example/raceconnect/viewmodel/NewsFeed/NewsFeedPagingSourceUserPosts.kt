package com.example.raceconnect.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.raceconnect.model.NewsFeedDataClassItem
import java.text.SimpleDateFormat
import java.util.*

class NewsFeedPagingSourceUserPosts(
    private val apiService: ApiService,
    private val userId: Int
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
            val response = apiService.getPostsByUserId(userId = userId, limit = limit, offset = offset)

            if (response.isSuccessful) {
                val posts = response.body()?.toMutableList() ?: mutableListOf()

                // Sort by created_at in descending order (newest first)
                posts.sortByDescending { post ->
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(post.created_at) ?: Date(0)
                }

                LoadResult.Page(
                    data = posts,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (posts.size < limit) null else page + 1  // Stops pagination when there are no more posts
                )
            } else {
                LoadResult.Error(Exception("Failed to load posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
