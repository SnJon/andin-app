package ru.netology.nmedia.model

sealed class ErrorModel(
    val postIndex: Int? = null
) {
    class LikeUnexpected(
        index: Int? = null,
        val onError: Boolean = false,
        val onFailure: Boolean = false
    ) :
        ErrorModel(index)

    class Unexpected(
        val isNavigate: Boolean = false,
        val onError: Boolean = false,
        val onFailure: Boolean = false,
    ) : ErrorModel()
}