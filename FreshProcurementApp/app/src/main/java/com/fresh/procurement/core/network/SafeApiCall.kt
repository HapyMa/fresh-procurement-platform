package com.fresh.procurement.core.network

import com.fresh.procurement.data.remote.dto.ApiResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * API 调用结果封装
 */
sealed class ApiResult<out T> {
    /**
     * 成功状态
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * 错误状态
     */
    data class Error(
        val code: Int,
        val message: String,
        val exception: Throwable? = null
    ) : ApiResult<Nothing>()

    /**
     * 加载状态
     */
    data object Loading : ApiResult<Nothing>()
}

/**
 * 网络错误类型
 */
sealed class NetworkError(val message: String) {
    data object NoInternet : NetworkError("无网络连接，请检查网络设置")
    data object ConnectionTimeout : NetworkError("连接超时，请稍后重试")
    data object ServerError : NetworkError("服务器错误，请稍后重试")
    data object Unauthorized : NetworkError("登录已过期，请重新登录")
    data object NotFound : NetworkError("请求的资源不存在")
    data object BadRequest : NetworkError("请求参数错误")
    data object Unknown : NetworkError("未知错误，请稍后重试")

    class Custom(message: String) : NetworkError(message)
}

/**
 * 安全的 API 调用封装
 * 自动处理异常并转换为 ApiResult
 *
 * @param block 挂起函数，执行实际的 API 调用
 * @return ApiResult 封装的结果
 */
suspend fun <T> safeApiCall(
    block: suspend () -> Response<ApiResponseDto<T>>
): ApiResult<T> = withContext(Dispatchers.IO) {
    try {
        val response = block()
        when {
            response.isSuccessful -> {
                val body = response.body()
                if (body != null && body.isSuccess()) {
                    val data = body.data
                    if (data != null) {
                        ApiResult.Success(data)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        ApiResult.Success(Unit as T)
                    }
                } else {
                    ApiResult.Error(
                        code = body?.code ?: response.code(),
                        message = body?.message ?: "请求失败",
                    )
                }
            }
            else -> {
                val errorBody = response.errorBody()?.string()
                ApiResult.Error(
                    code = response.code(),
                    message = errorBody ?: response.message() ?: "请求失败",
                )
            }
        }
    } catch (e: UnknownHostException) {
        ApiResult.Error(
            code = -1,
            message = NetworkError.NoInternet.message,
            exception = e
        )
    } catch (e: SocketTimeoutException) {
        ApiResult.Error(
            code = -2,
            message = NetworkError.ConnectionTimeout.message,
            exception = e
        )
    } catch (e: IOException) {
        ApiResult.Error(
            code = -3,
            message = NetworkError.NoInternet.message,
            exception = e
        )
    } catch (e: HttpException) {
        val errorMessage = when (e.code()) {
            401 -> NetworkError.Unauthorized.message
            404 -> NetworkError.NotFound.message
            400 -> NetworkError.BadRequest.message
            in 500..599 -> NetworkError.ServerError.message
            else -> e.message ?: "HTTP 错误"
        }
        ApiResult.Error(
            code = e.code(),
            message = errorMessage,
            exception = e
        )
    } catch (e: Exception) {
        ApiResult.Error(
            code = -4,
            message = NetworkError.Unknown.message,
            exception = e
        )
    }
}

/**
 * 简化的 API 调用封装（不包装 ApiResponse）
 * 用于直接返回数据的 API
 *
 * @param block 挂起函数，执行实际的 API 调用
 * @return ApiResult 封装的结果
 */
suspend fun <T> safeCall(
    block: suspend () -> Response<T>
): ApiResult<T> = withContext(Dispatchers.IO) {
    try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(Unit as T)
            }
        } else {
            ApiResult.Error(
                code = response.code(),
                message = response.errorBody()?.string() ?: response.message() ?: "请求失败",
            )
        }
    } catch (e: UnknownHostException) {
        ApiResult.Error(
            code = -1,
            message = NetworkError.NoInternet.message,
            exception = e
        )
    } catch (e: SocketTimeoutException) {
        ApiResult.Error(
            code = -2,
            message = NetworkError.ConnectionTimeout.message,
            exception = e
        )
    } catch (e: IOException) {
        ApiResult.Error(
            code = -3,
            message = NetworkError.NoInternet.message,
            exception = e
        )
    } catch (e: HttpException) {
        val errorMessage = when (e.code()) {
            401 -> NetworkError.Unauthorized.message
            404 -> NetworkError.NotFound.message
            400 -> NetworkError.BadRequest.message
            in 500..599 -> NetworkError.ServerError.message
            else -> e.message ?: "HTTP 错误"
        }
        ApiResult.Error(
            code = e.code(),
            message = errorMessage,
            exception = e
        )
    } catch (e: Exception) {
        ApiResult.Error(
            code = -4,
            message = NetworkError.Unknown.message,
            exception = e
        )
    }
}

/**
 * 扩展函数：处理 ApiResult 的成功和失败情况
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) {
        action(data)
    }
    return this
}

/**
 * 扩展函数：处理 ApiResult 的错误情况
 */
inline fun <T> ApiResult<T>.onError(action: (code: Int, message: String) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) {
        action(code, message)
    }
    return this
}

/**
 * 扩展函数：获取成功数据或 null
 */
fun <T> ApiResult<T>.getOrNull(): T? {
    return (this as? ApiResult.Success)?.data
}

/**
 * 扩展函数：获取成功数据或默认值
 */
fun <T> ApiResult<T>.getOrDefault(defaultValue: T): T {
    return (this as? ApiResult.Success)?.data ?: defaultValue
}

/**
 * 扩展函数：获取错误信息
 */
fun <T> ApiResult<T>.errorMessage(): String? {
    return (this as? ApiResult.Error)?.message
}

/**
 * 扩展函数：检查是否成功
 */
fun <T> ApiResult<T>.isSuccess(): Boolean {
    return this is ApiResult.Success
}

/**
 * 扩展函数：检查是否错误
 */
fun <T> ApiResult<T>.isError(): Boolean {
    return this is ApiResult.Error
}
