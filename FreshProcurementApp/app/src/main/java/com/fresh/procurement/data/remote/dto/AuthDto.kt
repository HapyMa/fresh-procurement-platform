package com.fresh.procurement.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 登录请求 DTO
 */
data class LoginRequestDto(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String
)

/**
 * 注册请求 DTO
 */
data class RegisterRequestDto(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("userType")
    val userType: Int,
    @SerializedName("nickname")
    val nickname: String?
)

/**
 * 登录响应 DTO
 */
data class LoginResponseDto(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("token")
    val token: String,
    @SerializedName("expireAt")
    val expireAt: String,
    @SerializedName("userType")
    val userType: Int
)

/**
 * 用户信息 DTO
 */
data class UserDto(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    @SerializedName("userType")
    val userType: Int,
    @SerializedName("verifyStatus")
    val verifyStatus: Int,
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * 用户地址 DTO
 */
data class UserAddressDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("addressType")
    val addressType: Int,
    @SerializedName("contactName")
    val contactName: String?,
    @SerializedName("contactPhone")
    val contactPhone: String?,
    @SerializedName("province")
    val province: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("district")
    val district: String?,
    @SerializedName("detailAddress")
    val detailAddress: String,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("isDefault")
    val isDefault: Int
)
