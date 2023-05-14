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
    published = "",
    attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val onFailureLiveData = MutableLiveData<CRUD>()
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
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                _data.postValue(FeedModel(posts = data, empty = data.isEmpty()))
            }

            override fun onError(e: Exception) {
                if (e.cause == null) {
                    _data.postValue(FeedModel(error = true))
                } else {
                    onFailureLiveData.value = CRUD.ANY
                }
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(data: Post) {
                    _postCreated.value = Unit
                }

                override fun onError(e: Exception) {
                    if (e.cause == null) {
                        _data.postValue(FeedModel(error = true))
                        onFailureLiveData.value = CRUD.SAVE_ERROR
                    } else {
                        onFailureLiveData.value = CRUD.SAVE_FAILURE
                    }
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
            repository.likeById(userActionPost.id, object : PostRepository.Callback<Post> {
                override fun onSuccess(data: Post) {
                    updatePost(data)
                }

                override fun onError(e: Exception) {
                    if (e.cause == null) {
                        _data.postValue(FeedModel(error = true))
                    } else {
                        onFailureLiveData.value = CRUD.ANY
                    }
                }
            })
        } else {
            repository.unLikeById(userActionPost.id, object : PostRepository.Callback<Post> {
                override fun onSuccess(data: Post) {
                    updatePost(data)
                }

                override fun onError(e: Exception) {
                    if (e.cause == null) {
                        _data.postValue(FeedModel(error = true))
                    } else {
                        onFailureLiveData.value = CRUD.ANY
                    }
                }
            })
        }
    }

    private fun getPostIndexOrNull(post: Post): Int? {
        return _data.value?.posts.orEmpty().indexOf(post).takeIf { it >= 0 }
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

        repository.removeById(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(data: Unit) {
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty().filter { it.id != id })
                )
            }

            override fun onError(e: Exception) {
                if (e.cause == null) {
                    _data.postValue(_data.value?.copy(posts = old))
                    _data.postValue(FeedModel(error = true))
                } else {
                    onFailureLiveData.value = CRUD.ANY
                }
            }
        })
    }

    enum class CRUD {
        ANY,
        SAVE_ERROR,
        SAVE_FAILURE,
        TRANSIT,
        EMPTY
    }
}
