package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.isNullOrEmpty
import ru.netology.nmedia.model.FeedErrorEvent
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.model.getContentOrNull
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File


class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dbPostLiveData = AppAuth.getInstance()
        .data
        .flatMapLatest { token ->
            repository.dbPostsLiveData
                .map { posts ->
                    posts.map { it.copy(ownedByMe = it.authorId == token?.id) }
                }
        }.asLiveData(Dispatchers.Default)

    // Feed fragment states start //

    private val _feedState = MutableLiveData<FeedModelState>(FeedModelState.Loading)
    val feedState: LiveData<FeedModelState> = _feedState

    private val _feedErrorEvent = SingleLiveEvent<FeedErrorEvent>()
    val feedErrorEvent: LiveData<FeedErrorEvent>
        get() = _feedErrorEvent

    private val _postsLoadedEvent = MutableLiveData<Unit>()

    val postLoadedEvent: LiveData<Unit>
        get() = _postsLoadedEvent

    private var isScrollToTopNeeded = false


    // Feed fragment states end //

    // New Post fragment states start //

    private val noPhoto = PhotoModel()

    private val _navigateToFeedCommand = SingleLiveEvent<Unit>()
    val navigateToFeedCommand: LiveData<Unit>
        get() = _navigateToFeedCommand

    private val newPostState = MutableLiveData(Post.empty())

    val newerCountLiveData: LiveData<Int> = dbPostLiveData.switchMap {
        repository.getNewerCount()
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    // New Post fragment states end //

    init {
        loadPosts()
    }


    fun loadPosts() {
        viewModelScope.launch {
            try {
                repository.loadPostsFromServer(false)
                _postsLoadedEvent.value = Unit
            } catch (e: Exception) {
                _feedErrorEvent.value = FeedErrorEvent()
            }
        }
    }

    fun updateContentState(posts: List<Post>) {
        _feedState.value = FeedModelState.Content(
            posts = posts,
            isScrollToTopNeeded = isScrollToTopNeeded
        )
        isScrollToTopNeeded = false
    }

    fun updateNewerCount(newerCount: Int) {
        val content = _feedState.value?.getContentOrNull()
        viewModelScope.launch {
            _feedState.postValue(
                FeedModelState.Content(
                    posts = content?.posts.orEmpty(),
                    newerCount = newerCount
                )
            )
        }
    }

    fun save() {
        val editedPost = newPostState.value
        newPostState.value = Post.empty()

        if (editedPost.isNullOrEmpty()) return

        val lastPostId = dbPostLiveData.value?.first()?.id
        val newPost = if (editedPost!!.saved) editedPost else {
            lastPostId?.let {
                editedPost.copy(id = (lastPostId + 1000))
            }
        }

        viewModelScope.launch {
            try {
                when (_photo.value) {
                    noPhoto -> {
                        repository.save(newPost!!)
                    }

                    else -> _photo.value?.uri?.let { uri ->
                        repository.saveWithAttachment(newPost!!, MediaUpload(uri.toFile()))
                    }
                }
                _navigateToFeedCommand.value = Unit
            } catch (e: Exception) {
                _navigateToFeedCommand.value = Unit
                _feedErrorEvent.value = FeedErrorEvent()
            }
        }
        _photo.value = noPhoto
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

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
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

    fun showRecentPosts() {
        val newerPostsCount = (_feedState.value as? FeedModelState.Content)?.newerCount ?: 0
        if (newerPostsCount > 0) {
            isScrollToTopNeeded = true
            viewModelScope.launch {
                repository.showHiddenPosts()
            }
        } else {
            loadPosts()
        }
    }

    private fun getPostIndexOrNull(post: Post): Int? {
        return dbPostLiveData.value.orEmpty().indexOfFirst { it.id == post.id }.takeIf { it >= 0 }
    }
}
