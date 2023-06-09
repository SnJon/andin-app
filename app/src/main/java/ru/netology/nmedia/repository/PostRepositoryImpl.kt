package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

class PostRepositoryImpl(
    private val dao: PostDao,
    private val apiService: PostApiService = PostApi.service
) : PostRepository {

    override val dbPostsLiveData = dao.getAll()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)


    override suspend fun getPosts(): List<Post> {
        return dao.getPosts().map {
            it.toDto()
        }
    }

    override fun getNewerCount(): Flow<Int> {
        return flow {
            while (true) {
                delay(10.seconds)
                loadPostsFromServer(true)
                val newerCount = dao.getHiddenCount()
                emit(newerCount)
            }
        }
            .catch { e -> throw AppError.from(e) }
            .flowOn(Dispatchers.Default)
    }


    override suspend fun loadPostsFromServer(isSilent: Boolean) {
        try {
            val lastPostId = dao.getLastPostId() ?: 0
            val response = if (lastPostId <= 0) {
                apiService.getAll()
            } else {
                apiService.getNewer(lastPostId)
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val updatedPosts = body.map { post ->
                post.copy(
                    saved = true,
                    hidden = isSilent
                )
            }

            dao.insert(updatedPosts.toEntity())

        } catch (e: IOException) {
            throw NetworkError

        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun likeById(id: Long) {
        try {
            val response = PostApi.service.likeById(id)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body.copy(saved = true)))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun unLikeById(id: Long) {
        try {
            val response = apiService.unLikeById(id)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body.copy(saved = true)))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        val response: Response<Post>
        try {
            response = if (post.saved.not()) {
                dao.insert(PostEntity.fromDto(post))
                apiService.save(post.copy(id = 0))
            } else {
                apiService.save(post)
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val updatePost = body.copy(saved = true)

            dao.removeById(post.id)
            dao.insert(PostEntity.fromDto(updatePost))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeById(id)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            dao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun showHiddenPosts() {
        dao.updateHiddenPosts()
    }
}
