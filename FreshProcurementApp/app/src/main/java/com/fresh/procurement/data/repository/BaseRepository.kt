package com.fresh.procurement.data.repository

import com.fresh.procurement.data.remote.dto.ApiException
import com.fresh.procurement.data.remote.dto.ApiResponseDto
import com.fresh.procurement.domain.error.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 基础 Repository 类
 * 封装通用网络请求逻辑和错误处理
 */
abstract class BaseRepository {

    /**
     * 执行网络请求并返回 Result 包装的结果
     *
     * @param call 网络请求 lambda
     * @return Result<T> 成功时包含数据，失败时包含异常
     */
    protected suspend fun <T> safeApiCall(
        call: suspend () -> Response<ApiResponseDto<T>>
    ): Result<T> {
        return try {
            val response = call()
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * 执行网络请求并返回 Flow 流
     *
     * @param call 网络请求 lambda
     * @return Flow<T> 成功时发射数据，失败时抛出异常
     */
    protected fun <T> safeApiFlow(
        call: suspend () -> Response<ApiResponseDto<T>>
    ): Flow<T> = flow {
        val response = call()
        val result = handleResponse(response)
        result.fold(
            onSuccess = { emit(it) },
            onFailure = { throw it }
        )
    }.flowOn(Dispatchers.IO)

    /**
     * 处理 Retrofit 响应
     */
    private fun <T> handleResponse(
        response: Response<ApiResponseDto<T>>
    ): Result<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.isSuccess()) {
                        body.data?.let {
                            Result.success(it)
                        } ?: Result.failure(AppError.ServerError("响应数据为空"))
                    } else {
                        Result.failure(
                            AppError.BusinessError(body.message)
                        )
                    }
                } else {
                    Result.failure(AppError.ServerError("响应体为空"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(
                    AppError.ServerError(
                        errorBody ?: "HTTP ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * 将异常映射为 AppError
     */
    protected fun mapException(throwable: Throwable): Throwable {
        return when (throwable) {
            is SocketTimeoutException -> AppError.Timeout()
            is IOException -> AppError.NoInternet()
            is HttpException -> mapHttpException(throwable)
            is ApiException -> AppError.BusinessError(throwable.message)
            else -> AppError.Unknown(throwable.message ?: "未知错误")
        }
    }

    /**
     * 映射 HTTP 异常
     */
    private fun mapHttpException(e: HttpException): AppError {
        return when (e.code()) {
            401 -> AppError.Unauthorized()
            403 -> AppError.Forbidden()
            404 -> AppError.NotFound()
            in 500..599 -> AppError.ServerError(e.message())
            else -> AppError.NetworkError(e.code())
        }
    }

    /**
     * 处理空响应体的情况（如删除操作）
     */
    protected suspend fun safeApiCallEmpty(
        call: suspend () -> Response<ApiResponseDto<Unit>>
    ): Result<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        AppError.BusinessError(body?.message ?: "操作失败")
                    )
                }
            } else {
                Result.failure(
                    AppError.ServerError("HTTP ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }
}
