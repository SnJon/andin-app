package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

sealed interface FeedModelState {

    data class Content(
        val posts: List<Post> = emptyList(),
        val error: FeedErrorEvent? = null
    ) : FeedModelState {

        fun isEmpty() = posts.isEmpty()
    }

    object Loading : FeedModelState
}

data class FeedErrorEvent(
    val itemIndex: Int? = null
)
