package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val saved: Boolean = false,
    val attachment: Map<String, String>? = null
) {
    companion object {

        fun empty(): Post {
            return Post(
                id = 0,
                content = "",
                author = "",
                authorAvatar = "",
                likedByMe = false,
                likes = 0,
                published = "",
                saved = false,
                attachment = null
            )
        }
    }
}

fun Post?.isNullOrEmpty(): Boolean {
    return this == null || this == Post.empty()
}

