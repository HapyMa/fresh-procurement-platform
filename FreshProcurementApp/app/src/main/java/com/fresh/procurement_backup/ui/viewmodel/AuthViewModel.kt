package com.fresh.procurement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.procurement.data.model.LoginResponse
import com.fresh.procurement.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState<LoginResponse>>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState<LoginResponse>> = _loginState

    private val _registerState = MutableStateFlow<AuthUiState<LoginResponse>>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState<LoginResponse>> = _registerState

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            authRepository.login(phone, password)
                .onSuccess { response ->
                    _loginState.value = AuthUiState.Success(response)
                }
                .onFailure { error ->
                    _loginState.value = AuthUiState.Error(error.message ?: "登录失败")
                }
        }
    }

    fun register(phone: String, password: String, userType: Int, nickname: String?) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            authRepository.register(phone, password, userType, nickname)
                .onSuccess { response ->
                    _registerState.value = AuthUiState.Success(response)
                }
                .onFailure { error ->
                    _registerState.value = AuthUiState.Error(error.message ?: "注册失败")
                }
        }
    }

    fun logout() {
        authRepository.logout()
        _loginState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState<out T> {
    data object Idle : AuthUiState<Nothing>()
    data object Loading : AuthUiState<Nothing>()
    data class Success<T>(val data: T) : AuthUiState<T>()
    data class Error(val message: String) : AuthUiState<Nothing>()
}
