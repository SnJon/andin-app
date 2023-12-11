package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.model.AuthModelState
import ru.netology.nmedia.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthModelState>()
    val autState: LiveData<AuthModelState>
        get() = _authState

    val data: LiveData<Token?> = appAuth.data.asLiveData()

    val authenticated: Boolean
        get() = appAuth.data.value != null


    fun authorization(credentials: Pair<String, String>) {
        viewModelScope.launch {
            _authState.value = AuthModelState(loading = true)
            try {
                val authState = repository.login(credentials)
                authState.token.let { appAuth.setAuth(authState.id, it) }
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
