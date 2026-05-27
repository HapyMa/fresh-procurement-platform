package com.fresh.procurement.domain.error

import com.fresh.procurement.R
import retrofit2.HttpException
import java.io.IOException

open class AppError(val messageResId: Int, override val message: String = "") : Exception(message) {
    // 网络错误
    class NetworkError(val code: Int) : AppError(R.string.error_network, "网络请求失败: $code")
    class NoInternet : AppError(R.string.error_no_internet, "网络不可用")
    class Timeout : AppError(R.string.error_timeout, "请求超时")

    // 服务器错误
    class ServerError(serverMessage: String) : AppError(R.string.error_server, serverMessage)
    class Unauthorized : AppError(R.string.error_unauthorized, "未授权")
    class Forbidden : AppError(R.string.error_forbidden, "无权限")
    class NotFound : AppError(R.string.error_not_found, "资源不存在")

    // 业务错误
    class ValidationError(field: String) : AppError(R.string.error_validation, "验证失败: $field")
    class BusinessError(bizMessage: String) : AppError(R.string.error_business, bizMessage)

    // 未知错误
    class Unknown(detail: String) : AppError(R.string.error_unknown, detail)
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
