package com.fresh.procurement.data.repository

import com.fresh.procurement.core.security.TokenManager
import com.fresh.procurement.data.remote.ApiService
import com.fresh.procurement.data.remote.dto.LoginRequestDto
import com.fresh.procurement.data.remote.dto.RegisterRequestDto
import com.fresh.procurement.data.remote.mapper.AuthMapper.toDomain
import com.fresh.procurement.domain.model.LoginResult
import com.fresh.procurement.domain.model.User
import com.fresh.procurement.domain.model.UserType
import com.fresh.procurement.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : BaseRepository(), AuthRepository {

    private val _currentUserFlow = MutableStateFlow<User?>(null)

    override suspend fun login(phone: String, password: String): Result<LoginResult> {
        return try {
            // 1. 调用登录 API
            val loginResponse = safeApiCall {
                apiService.login(LoginRequestDto(phone = phone, password = password))
            }.getOrThrow()

            // 2. 保存 Token
            tokenManager.saveAccessToken(loginResponse.token)
            tokenManager.saveUserId(loginResponse.userId)
            tokenManager.saveUserRole(UserType.fromValue(loginResponse.userType).name)

            // 3. 获取用户信息
            val user = fetchUserProfile().getOrThrow()

            // 4. 更新当前用户状态
            _currentUserFlow.value = user

            Result.success(LoginResult(user = user, token = loginResponse.token, expireAt = loginResponse.expireAt))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        phone: String,
        password: String,
        nickname: String,
        userType: Int
    ): Result<User> {
        return try {
            // 1. 调用注册 API
            val registerResponse = safeApiCall {
                apiService.register(RegisterRequestDto(phone = phone, password = password, userType = userType, nickname = nickname))
            }.getOrThrow()

            // 2. 保存 Token
            tokenManager.saveAccessToken(registerResponse.token)
            tokenManager.saveUserId(registerResponse.userId)
            tokenManager.saveUserRole(UserType.fromValue(registerResponse.userType).name)

            // 3. 获取用户信息
            val user = fetchUserProfile().getOrThrow()

            // 4. 更新当前用户状态
            _currentUserFlow.value = user

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.clearAll()
            _currentUserFlow.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeCurrentUser(): Flow<User?> {
        return _currentUserFlow.asStateFlow()
    }

    override suspend fun getCurrentUser(): User? {
        _currentUserFlow.value?.let { return it }
        if (!tokenManager.isLoggedIn()) return null
        return fetchUserProfile().fold(
            onSuccess = { user ->
                _currentUserFlow.value = user
                user
            },
            onFailure = {
                tokenManager.clearAll()
                null
            }
        )
    }

    private suspend fun fetchUserProfile(): Result<User> {
        return safeApiCall {
            apiService.getUserProfile()
        }.map { userDto ->
            userDto.toDomain()
        }
    }
}
