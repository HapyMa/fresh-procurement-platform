package com.fresh.procurement.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 通用 API 响应包装 DTO
 */
data class ApiResponseDto<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T?
) {
    /**
     * 判断响应是否成功
     */
    fun isSuccess(): Boolean = code == 200

    /**
     * 获取成功数据，如果失败则抛出异常
     */
    fun getOrThrow(): T {
        if (!isSuccess()) {
            throw ApiException(code, message)
        }
        return data ?: throw ApiException(code, "响应数据为空")
    }
}

/**
 * API 异常类
 */
class ApiException(
    val code: Int,
    override val message: String
) : Exception(message)

/**
 * 分页请求参数 DTO
 */
data class PageParamsDto(
    val page: Int = 1,
    val size: Int = 20
)
