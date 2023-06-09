package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val dbPostsLiveData: Flow<List<Post>>

    suspend fun loadPostsFromServer(isSilent: Boolean)
    suspend fun getPosts(): List<Post>
    suspend fun likeById(id: Long)
    suspend fun unLikeById(id: Long)
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun showHiddenPosts()
    fun getNewerCount(): Flow<Int>
}
