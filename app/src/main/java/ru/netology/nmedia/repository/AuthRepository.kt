package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Token

interface AuthRepository {
    suspend fun login(credentials: Pair<String, String>): Token
}
