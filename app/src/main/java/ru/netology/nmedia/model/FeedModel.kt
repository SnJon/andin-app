package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

sealed interface FeedModelState {

    data class Content(
        val posts: List<Post> = emptyList(),
        val newerCount: Int = 0,
        val isScrollToTopNeeded: Boolean = false
    ) : FeedModelState {

        fun isEmpty() = posts.isEmpty()
    }

    object Loading : FeedModelState
}

data class FeedErrorEvent(
    val itemIndex: Int? = null
)

fun FeedModelState.getContentOrNull(): FeedModelState.Content? {
    return this as? FeedModelState.Content
}
