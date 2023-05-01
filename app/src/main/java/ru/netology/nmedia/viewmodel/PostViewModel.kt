package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAll(object : PostRepository.PostsCallback<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                _data.postValue(FeedModel(posts = data, empty = data.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }

        })
    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.PostsCallback<Unit> {
                override fun onSuccess(data: Unit) {
                    _postCreated.postValue(data)
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun onLikeClicked(post: Post) {
        val userActionPost = post.copy(likedByMe = !post.likedByMe)

        if (userActionPost.likedByMe) {
            repository.likeById(userActionPost.id, object : PostRepository.PostsCallback<Post> {
                override fun onSuccess(data: Post) {
                    updatePost(data)
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        } else {
            repository.unLikeById(userActionPost.id, object : PostRepository.PostsCallback<Post> {
                override fun onSuccess(data: Post) {
                    updatePost(data)
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        }
    }


    private fun updatePost(post: Post) {
        val feedModel = _data.value
        if (feedModel?.isContentShowed() == true) {
            val postIndex = feedModel.posts.toMutableList().indexOfFirst { oldPost ->
                oldPost.id == post.id
            }
            if (postIndex != -1) {
                val updatedPosts = feedModel.posts.toMutableList()
                updatedPosts[postIndex] = post
                _data.postValue(feedModel.copy(posts = updatedPosts))
            }
        }
    }

    fun removeById(id: Long) {
        // Оптимистичная модель
        val old = _data.value?.posts.orEmpty()

        repository.removeById(id, object : PostRepository.PostsCallback<Unit> {
            override fun onSuccess(data: Unit) {
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty().filter { it.id != id })
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        })
    }
}
