package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val saved: Boolean = false,
    val hidden: Boolean = false,
    val attachment: Attachment? = null
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

data class Attachment(
    val url: String,
    val type: AttachmentType
)

fun Post?.isNullOrEmpty(): Boolean {
    return this == null || this == Post.empty()
}

