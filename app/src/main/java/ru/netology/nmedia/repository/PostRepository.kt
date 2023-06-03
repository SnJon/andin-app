package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val dbPostsLiveData: LiveData<List<Post>>

    suspend fun loadPostsFromServer()
    suspend fun getPosts(): List<Post>
    suspend fun likeById(id: Long)
    suspend fun unLikeById(id: Long)
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)

}
