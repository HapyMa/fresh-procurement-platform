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
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证仓库实现类
 * 实现 Domain 层的 AuthRepository 接口
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : BaseRepository(), AuthRepository {

    // 当前用户状态流
    private val _currentUserFlow = MutableStateFlow<User?>(null)

    /**
     * 用户登录
     * @param phone 手机号
     * @param password 密码
     * @return Result<LoginResult> 登录结果
     */
    override suspend fun login(phone: String, password: String): Result<LoginResult> {
        return safeApiCall {
            apiService.login(
                LoginRequestDto(phone = phone, password = password)
            )
        }.map { loginResponseDto ->
            // 保存 Token
            tokenManager.saveAccessToken(loginResponseDto.token)
            tokenManager.saveUserId(loginResponseDto.userId)
            tokenManager.saveUserRole(
                UserType.fromValue(loginResponseDto.userType).name
            )

            // 获取用户信息并转换为 Domain Model
            val user = fetchUserProfile().getOrNull()
                ?: throw IllegalStateException("无法获取用户信息")

            // 创建登录结果
            val loginResult = LoginResult(
                user = user,
                token = loginResponseDto.token,
                expireAt = loginResponseDto.expireAt
            )

            // 更新当前用户状态
            _currentUserFlow.value = user

            loginResult
        }
    }

    /**
     * 用户注册
     * @param phone 手机号
     * @param password 密码
     * @param nickname 昵称
     * @param userType 用户类型
     * @return Result<User> 注册成功的用户信息
     */
    override suspend fun register(
        phone: String,
        password: String,
        nickname: String,
        userType: Int
    ): Result<User> {
        return safeApiCall {
            apiService.register(
                RegisterRequestDto(
                    phone = phone,
                    password = password,
                    userType = userType,
                    nickname = nickname
                )
            )
        }.map { loginResponseDto ->
            // 保存 Token
            tokenManager.saveAccessToken(loginResponseDto.token)
            tokenManager.saveUserId(loginResponseDto.userId)
            tokenManager.saveUserRole(
                UserType.fromValue(loginResponseDto.userType).name
            )

            // 获取用户信息
            val user = fetchUserProfile().getOrNull()
                ?: throw IllegalStateException("无法获取用户信息")

            // 更新当前用户状态
            _currentUserFlow.value = user

            user
        }
    }

    /**
     * 用户登出
     * 清除所有存储的认证信息
     * @return Result<Unit>
     */
    override suspend fun logout(): Result<Unit> {
        return try {
            // 清除 Token 和用户信息
            tokenManager.clearAll()
            _currentUserFlow.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * 观察当前用户状态
     * @return Flow<User?> 用户状态流
     */
    override fun observeCurrentUser(): Flow<User?> {
        return _currentUserFlow.asStateFlow()
    }

    /**
     * 获取当前登录用户
     * @return User? 当前用户，未登录返回 null
     */
    override suspend fun getCurrentUser(): User? {
        // 如果内存中有用户，直接返回
        _currentUserFlow.value?.let { return it }

        // 检查是否已登录
        if (!tokenManager.isLoggedIn()) {
            return null
        }

        // 从网络获取用户信息
        return fetchUserProfile().fold(
            onSuccess = { user ->
                _currentUserFlow.value = user
                user
            },
            onFailure = {
                // 获取失败，清除登录状态
                tokenManager.clearAll()
                null
            }
        )
    }

    /**
     * 从服务器获取用户信息
     * @return Result<User> 用户信息
     */
    private suspend fun fetchUserProfile(): Result<User> {
        return safeApiCall {
            apiService.getUserProfile()
        }.map { userDto ->
            userDto.toDomain()
        }
    }

    /**
     * 检查用户是否已登录
     * @return Boolean
     */
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * 获取当前用户类型
     * @return UserType?
     */
    suspend fun getCurrentUserType(): UserType? {
        val role = tokenManager.getUserRole() ?: return null
        return try {
            UserType.valueOf(role)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * 获取当前用户 ID
     * @return Long?
     */
    suspend fun getCurrentUserId(): Long? {
        return tokenManager.getUserId()
    }
}
