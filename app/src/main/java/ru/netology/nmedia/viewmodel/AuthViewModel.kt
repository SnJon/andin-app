package ru.netology.nmedia.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.model.AuthModelState
import ru.netology.nmedia.repository.AuthRepository
import ru.netology.nmedia.repository.AuthRepositoryImpl

class AuthViewModel : ViewModel() {
    private val repository: AuthRepository =
        AuthRepositoryImpl()

    private val _authState = MutableLiveData<AuthModelState>()
    val autState: LiveData<AuthModelState>
        get() = _authState

    val data: LiveData<Token?> = AppAuth.getInstance().data.asLiveData()

    val authenticated: Boolean
        get() = AppAuth.getInstance().data.value != null


    fun authorization(credentials: Pair<String, String>) {
        viewModelScope.launch {
            _authState.value = AuthModelState(loading = true)
            try {
                val authState = repository.login(credentials)
                // проверить на кол-во получаемых токенов
                Log.e("asd", "$authState")
                authState.token.let { AppAuth.getInstance().setAuth(authState.id, it) }
                _authState.value = AuthModelState(success = true)
            } catch (e: Exception) {
                _authState.value = AuthModelState(error = true)
            }
        }
    }

    fun restoreState() {
        _authState.value = AuthModelState(loading = false, error = false, success = false)
    }
}
