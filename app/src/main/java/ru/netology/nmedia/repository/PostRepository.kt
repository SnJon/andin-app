package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val dbPostsLiveData: Flow<List<Post>>

    suspend fun loadPostsFromServer(isSilent: Boolean)
    suspend fun likeById(id: Long)
    suspend fun unLikeById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun removeById(id: Long)
    suspend fun showHiddenPosts()
    fun getNewerCount(): Flow<Int>
}
