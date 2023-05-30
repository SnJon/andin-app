package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.isNullOrEmpty
import ru.netology.nmedia.model.FeedErrorEvent
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent


class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

    val dbPostLiveData = repository.dbPostsLiveData

    private val _feedState = MutableLiveData<FeedModelState>(FeedModelState.Loading)
    val feedState: LiveData<FeedModelState> = _feedState

    private val _feedErrorEvent = SingleLiveEvent<FeedErrorEvent>()
    val feedErrorEvent: LiveData<FeedErrorEvent>
        get() = _feedErrorEvent

    private val _navigateToFeedCommand = SingleLiveEvent<Unit>()

    val navigateToFeedCommand: LiveData<Unit>
        get() = _navigateToFeedCommand

    private val newPostState = MutableLiveData(Post.empty())

    init {
        loadPosts()
    }


    fun loadPosts() {
        viewModelScope.launch {
            try {
                repository.loadPostsFromServer()
            } catch (e: Exception) {
                _feedErrorEvent.value = FeedErrorEvent()
            }
        }
    }

    fun updateContentState(posts: List<Post>) {
        _feedState.value = FeedModelState.Content(posts)
    }

    fun save() {
        val editedPost = newPostState.value
        newPostState.value = Post.empty()

        if (editedPost.isNullOrEmpty()) return

        viewModelScope.launch {
            try {
                if (editedPost!!.saved) {
                    repository.save(editedPost)
                } else {
                    val lastPostId = dbPostLiveData.value?.first()?.id
                    if (lastPostId != null) {
                        repository.save(editedPost.copy(id = (lastPostId + 1000)))
                    }
                }
                _navigateToFeedCommand.value = Unit
            } catch (e: Exception) {
                _navigateToFeedCommand.value = Unit
                _feedErrorEvent.value = FeedErrorEvent()
            }
        }
    }

    fun saveExist(post: Post) {
        viewModelScope.launch {
            try {
                repository.save(post)
            } catch (e: Exception) {
                _feedErrorEvent.value = FeedErrorEvent()
            }
        }
    }

    fun edit(post: Post) {
        newPostState.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (newPostState.value?.content == text) {
            return
        }
        newPostState.value = newPostState.value?.copy(content = text)
    }

    fun onLikeClicked(post: Post) {
        val isLike: Boolean = post.likedByMe.not()
        val index = getPostIndexOrNull(post)

        if (isLike) {
            viewModelScope.launch {
                try {
                    repository.likeById(post.id)
                } catch (e: Exception) {
                    _feedErrorEvent.value = FeedErrorEvent(index)
                }
            }
        } else {
            viewModelScope.launch {
                try {
                    repository.unLikeById(post.id)
                } catch (e: Exception) {
                    _feedErrorEvent.value = FeedErrorEvent(index)
                }
            }
        }
    }


    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _feedErrorEvent.value = FeedErrorEvent()
            }
        }
    }

    private fun getPostIndexOrNull(post: Post): Int? {
        return dbPostLiveData.value.orEmpty().indexOfFirst { it.id == post.id }.takeIf { it >= 0 }
    }
}
