package com.fresh.procurement.domain.model

enum class UserType(val value: Int) {
    BUYER(1),
    SUPPLIER(2),
    ADMIN(3);

    companion object {
        fun fromValue(value: Int): UserType = values().find { it.value == value } ?: BUYER
    }
}

data class User(
    val id: Long,
    val phone: String,
    val nickname: String,
    val avatarUrl: String?,
    val userType: UserType,
    val status: Int,
    val createdAt: String?,
    val updatedAt: String?
) {
    fun isActive(): Boolean = status == 1
    fun isAdmin(): Boolean = userType == UserType.ADMIN
    fun isBuyer(): Boolean = userType == UserType.BUYER
    fun isSupplier(): Boolean = userType == UserType.SUPPLIER
}

data class LoginResult(
    val user: User,
    val token: String,
    val expireAt: String
)
