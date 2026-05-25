package com.fresh.procurement.data.model

import com.google.gson.annotations.SerializedName

/**
 * 通用API响应包装
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T?
) {
    fun isSuccess() = code == 200
}

/**
 * 分页请求参数
 */
data class PageParams(
    val page: Int = 1,
    val size: Int = 20
)

/**
 * 登录请求
 */
data class LoginRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String
)

/**
 * 注册请求
 */
data class RegisterRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("userType")
    val userType: Int,
    @SerializedName("nickname")
    val nickname: String?
)
