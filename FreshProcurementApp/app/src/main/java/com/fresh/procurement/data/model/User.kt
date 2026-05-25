package com.fresh.procurement.data.model

import com.google.gson.annotations.SerializedName

/**
 * 用户类型
 */
enum class UserType(val value: Int) {
    BUYER(1),
    SUPPLIER(2),
    BOTH(3)
}

/**
 * 用户认证状态
 */
enum class VerifyStatus(val value: Int) {
    UNVERIFIED(0),
    PENDING(1),
    VERIFIED(2),
    REJECTED(3)
}

/**
 * 用户基础信息
 */
data class User(
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
) {
    fun getUserTypeEnum(): UserType = UserType.values().find { it.value == userType } ?: UserType.BUYER
    fun getVerifyStatusEnum(): VerifyStatus = VerifyStatus.values().find { it.value == verifyStatus } ?: VerifyStatus.UNVERIFIED
}

/**
 * 登录响应
 */
data class LoginResponse(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("token")
    val token: String,
    @SerializedName("expireAt")
    val expireAt: String
)

/**
 * 用户地址
 */
data class UserAddress(
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
