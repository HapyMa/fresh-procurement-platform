package com.fresh.procurement.core.network

import com.fresh.procurement.core.security.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证拦截器
 * 自动为请求添加 Authorization Token
 * 支持跳过特定请求的认证
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        // 不需要认证的 API 路径列表
        private val PUBLIC_PATHS = listOf(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // 检查是否是公开 API
        if (isPublicPath(path)) {
            return chain.proceed(request)
        }

        // 获取 Token 并添加到请求头
        val token = runBlocking {
            tokenManager.getAccessToken()
        }

        val newRequest = if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .method(request.method, request.body)
                .build()
        } else {
            request.newBuilder()
                .header("Accept", "application/json")
                .method(request.method, request.body)
                .build()
        }

        return chain.proceed(newRequest)
    }

    /**
     * 检查路径是否是公开 API
     */
    private fun isPublicPath(path: String): Boolean {
        return PUBLIC_PATHS.any { path.contains(it, ignoreCase = true) }
    }
}

/**
 * Token 刷新拦截器
 * 处理 401 响应并尝试刷新 Token
 */
@Singleton
class TokenRefreshInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        private const val MAX_RETRY_COUNT = 1
    }

    @Volatile
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        // 如果响应是 401 且不是刷新 Token 的请求
        if (response.code == 401 && !request.url.encodedPath.contains("/api/auth/refresh")) {
            response.close()

            // 尝试刷新 Token
            synchronized(this) {
                if (!isRefreshing) {
                    isRefreshing = true
                    val newToken = refreshToken()
                    isRefreshing = false

                    if (newToken != null) {
                        // 使用新 Token 重试请求
                        val newRequest = request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                        return chain.proceed(newRequest)
                    }
                }
            }
        }

        return response
    }

    /**
     * 刷新 Token
     * @return 新的访问令牌，如果刷新失败返回 null
     */
    private fun refreshToken(): String? {
        return runBlocking {
            try {
                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken == null) {
                    tokenManager.clearAll()
                    return@runBlocking null
                }

                // TODO: 调用刷新 Token 的 API
                // 这里需要注入 ApiService 来调用刷新接口
                // 暂时返回 null，实际实现时需要完成刷新逻辑

                null
            } catch (e: Exception) {
                tokenManager.clearAll()
                null
            }
        }
    }
}

/**
 * 公共请求头拦截器
 * 为所有请求添加通用请求头
 */
@Singleton
class CommonHeadersInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequest = request.newBuilder().apply {
            header("Accept", "application/json")
            header("Content-Type", "application/json")
            header("X-Client-Platform", "Android")
            header("X-Client-Version", getAppVersion())
        }.build()

        return chain.proceed(newRequest)
    }

    private fun getAppVersion(): String {
        // TODO: 从 BuildConfig 获取应用版本
        return "1.0.0"
    }
}
