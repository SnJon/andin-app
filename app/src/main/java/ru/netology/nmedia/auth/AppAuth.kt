package ru.netology.nmedia.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"

    private val _data = MutableStateFlow<Token?>(null)
    val data = _data.asStateFlow()

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null) {
            _data.value = null
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _data.value = Token(id, token)
        }
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _data.value = Token(id, token)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            apply()
        }
    }

    @Synchronized
    fun removeAuth() {
        _data.value = null
        with(prefs.edit()) {
            clear()
            commit()
        }
    }
}
