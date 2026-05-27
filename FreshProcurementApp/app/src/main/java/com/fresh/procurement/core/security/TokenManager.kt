package com.fresh.procurement.core.security

/**
 * Token 管理器接口
 * 定义 Token 的存储、获取和清除操作
 */
interface TokenManager {
    /**
     * 保存访问令牌
     * @param token JWT 访问令牌
     */
    suspend fun saveAccessToken(token: String)

    /**
     * 保存刷新令牌
     * @param token JWT 刷新令牌
     */
    suspend fun saveRefreshToken(token: String)

    /**
     * 保存用户角色
     * @param role 用户角色（BUYER, SUPPLIER, ADMIN）
     */
    suspend fun saveUserRole(role: String)

    /**
     * 保存用户 ID
     * @param userId 用户 ID
     */
    suspend fun saveUserId(userId: Long)

    /**
     * 获取访问令牌
     * @return 访问令牌，如果不存在返回 null
     */
    suspend fun getAccessToken(): String?

    /**
     * 同步获取访问令牌（非 suspend 版本）
     * 用于 OkHttp Interceptor 等无法使用协程的场景
     * @return 访问令牌，如果不存在返回 null
     */
    fun getAccessTokenSync(): String?

    /**
     * 同步获取刷新令牌（非 suspend 版本）
     * 用于 OkHttp Interceptor 等无法使用协程的场景
     * @return 刷新令牌，如果不存在返回 null
     */
    fun getRefreshTokenSync(): String?

    /**
     * 获取刷新令牌
     * @return 刷新令牌，如果不存在返回 null
     */
    suspend fun getRefreshToken(): String?

    /**
     * 获取用户角色
     * @return 用户角色，如果不存在返回 null
     */
    suspend fun getUserRole(): String?

    /**
     * 获取用户 ID
     * @return 用户 ID，如果不存在返回 null
     */
    suspend fun getUserId(): Long?

    /**
     * 清除所有存储的认证信息
     */
    suspend fun clearAll()

    /**
     * 检查用户是否已登录
     * @return 如果存在访问令牌返回 true
     */
    suspend fun isLoggedIn(): Boolean
}
