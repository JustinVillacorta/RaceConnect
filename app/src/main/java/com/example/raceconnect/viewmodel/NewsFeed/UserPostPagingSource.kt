package com.example.raceconnect.network

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.raceconnect.model.NewsFeedDataClassItem

class UserPostsPagingSource(
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
            Log.d("UserPostsPagingSource", "Loading page $page, limit $limit, offset $offset for userId $userId")

            val response = apiService.getPostsByUserId(
                userId = userId,
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful) {
                val posts = response.body() ?: emptyList()
                Log.d("UserPostsPagingSource", "Fetched ${posts.size} posts for userId $userId")
                LoadResult.Page(
                    data = posts,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (posts.isEmpty() || posts.size < limit) null else page + 1
                )
            } else {
                Log.e("UserPostsPagingSource", "Failed to fetch posts: ${response.message()}")
                LoadResult.Error(Exception("Failed to load posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserPostsPagingSource", "Error loading posts: ${e.message}")
            LoadResult.Error(e)
        }
    }
}