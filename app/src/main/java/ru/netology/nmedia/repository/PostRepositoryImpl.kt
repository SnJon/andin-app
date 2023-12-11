package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApiService
) : PostRepository {

    override val dbPostsLiveData = dao.getAll()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

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
        withContext(Dispatchers.IO) {
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
    }


    override suspend fun likeById(id: Long) {
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.likeById(id)

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
    }

    override suspend fun unLikeById(id: Long) {
        withContext(Dispatchers.IO) {
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
    }

    override suspend fun save(post: Post) {
        val response: Response<Post>
        withContext(Dispatchers.IO) {
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
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
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

    override suspend fun removeById(id: Long) {
        withContext(Dispatchers.IO) {
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
    }

    override suspend fun showHiddenPosts() {
        dao.updateHiddenPosts()
    }
}
