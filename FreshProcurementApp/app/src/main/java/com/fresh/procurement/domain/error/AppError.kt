package com.fresh.procurement.domain.error

import com.fresh.procurement.R
import retrofit2.HttpException
import java.io.IOException

sealed class AppError(val messageResId: Int) {
    // 网络错误
    class NetworkError(val code: Int) : AppError(R.string.error_network)
    class NoInternet : AppError(R.string.error_no_internet)
    class Timeout : AppError(R.string.error_timeout)

    // 服务器错误
    class ServerError(val message: String) : AppError(R.string.error_server)
    class Unauthorized : AppError(R.string.error_unauthorized)
    class Forbidden : AppError(R.string.error_forbidden)
    class NotFound : AppError(R.string.error_not_found)

    // 业务错误
    class ValidationError(val field: String) : AppError(R.string.error_validation)
    class BusinessError(val bizMessage: String) : AppError(R.string.error_business)

    // 未知错误
    class Unknown(val detail: String) : AppError(R.string.error_unknown)
}

fun Throwable.toAppError(): AppError = when (this) {
    is IOException -> AppError.NoInternet()
    is HttpException -> when (code()) {
        401 -> AppError.Unauthorized()
        403 -> AppError.Forbidden()
        404 -> AppError.NotFound()
        in 500..599 -> AppError.ServerError(message())
        else -> AppError.NetworkError(code())
    }
    else -> AppError.Unknown(message ?: "未知错误")
}
