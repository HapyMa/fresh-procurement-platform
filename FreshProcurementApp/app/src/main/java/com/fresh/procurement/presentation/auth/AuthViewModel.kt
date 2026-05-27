package com.fresh.procurement.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.procurement.common.components.UiState
import com.fresh.procurement.domain.error.toAppError
import com.fresh.procurement.domain.model.LoginResult
import com.fresh.procurement.domain.model.User
import com.fresh.procurement.domain.model.UserType
import com.fresh.procurement.domain.usecase.auth.LoginUseCase
import com.fresh.procurement.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<LoginResult>>(UiState.Success(LoginResult(User(0, "", "", null, UserType.BUYER, 0, null, null), "", "")))
    val loginState: StateFlow<UiState<LoginResult>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Success(User(0, "", "", null, UserType.BUYER, 0, null, null)))
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            loginUseCase(phone, password)
                .onSuccess { result -> _loginState.value = UiState.Success(result) }
                .onFailure { error -> _loginState.value = UiState.Error(error.toAppError()) }
        }
    }

    fun register(phone: String, password: String, nickname: String, userType: Int) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            registerUseCase(phone, password, nickname, UserType.fromValue(userType))
                .onSuccess { user -> _registerState.value = UiState.Success(user) }
                .onFailure { error -> _registerState.value = UiState.Error(error.toAppError()) }
        }
    }

    fun clearLoginState() {
        _loginState.value = UiState.Success(LoginResult(User(0, "", "", null, UserType.BUYER, 0, null, null), "", ""))
    }

    fun clearRegisterState() {
        _registerState.value = UiState.Success(User(0, "", "", null, UserType.BUYER, 0, null, null))
    }
}
