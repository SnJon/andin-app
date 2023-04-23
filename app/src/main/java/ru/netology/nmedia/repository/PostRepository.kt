package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(callback: PostsCallback<List<Post>>)
    fun likeById(id: Long, callback: PostsCallback<Post>)
    fun unLikeById(id: Long, callback: PostsCallback<Post>)
    fun save(post: Post, callback: PostsCallback<Unit>)
    fun removeById(id: Long, callback: PostsCallback<Unit>)

    interface PostsCallback<T> {
        fun onSuccess(data: T)
        fun onError(e: Exception)
    }
}
