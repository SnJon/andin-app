package ru.netology.nmedia.repository

import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: PostApiService
) : AuthRepository {
    override suspend fun login(credentials: Pair<String, String>): Token {
        try {
            val response = apiService.updateUser(credentials.first, credentials.second)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}
