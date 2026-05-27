package com.fresh.procurement.data.remote.mapper

import com.fresh.procurement.data.remote.dto.LoginResponseDto
import com.fresh.procurement.data.remote.dto.UserDto
import com.fresh.procurement.domain.model.LoginResult
import com.fresh.procurement.domain.model.User
import com.fresh.procurement.domain.model.UserType

/**
 * 认证相关 DTO 到 Domain Model 的转换器
 */
object AuthMapper {

    /**
     * 将 UserDto 转换为 Domain 层的 User
     */
    fun UserDto.toDomain(): User {
        return User(
            id = this.userId,
            phone = this.phone,
            nickname = this.nickname ?: "",
            avatarUrl = this.avatarUrl,
            userType = UserType.fromValue(this.userType),
            status = this.verifyStatus,
            createdAt = this.createdAt,
            updatedAt = null
        )
    }

    /**
     * 将 LoginResponseDto 转换为 Domain 层的 LoginResult
     * 需要配合 User 信息一起使用
     */
    fun LoginResponseDto.toDomain(user: User): LoginResult {
        return LoginResult(
            user = user,
            token = this.token,
            expireAt = this.expireAt
        )
    }

    /**
     * 将 LoginResponseDto 和 UserDto 一起转换为 Domain 层的 LoginResult
     */
    fun mapToLoginResult(
        loginResponseDto: LoginResponseDto,
        userDto: UserDto
    ): LoginResult {
        val user = userDto.toDomain()
        return loginResponseDto.toDomain(user)
    }

    /**
     * 将 UserDto 列表转换为 Domain 层的 User 列表
     */
    fun List<UserDto>.toDomainList(): List<User> {
        return this.map { it.toDomain() }
    }
}
