package ru.netology.nmedia.repository

import android.util.Log
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException

class AuthRepositoryImpl(
    private val apiService: PostApiService = PostApi.service
) : AuthRepository {
    override suspend fun login(credentials: Pair<String, String>): Token {
        try {
            val response = apiService.updateUser(credentials.first, credentials.second)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            Log.e("asd", "${response.body()}")
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}
