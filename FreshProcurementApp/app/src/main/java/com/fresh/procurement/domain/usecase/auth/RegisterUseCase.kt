package com.fresh.procurement.domain.usecase.auth

import com.fresh.procurement.domain.error.AppError
import com.fresh.procurement.domain.error.toAppError
import com.fresh.procurement.domain.model.User
import com.fresh.procurement.domain.model.UserType
import com.fresh.procurement.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        phone: String,
        password: String,
        nickname: String,
        userType: UserType
    ): Result<User> {
        // 验证手机号
        if (phone.isBlank()) {
            return Result.failure(AppError.ValidationError("phone"))
        }
        if (!phone.matches(Regex("^1[3-9]\\d{9}$"))) {
            return Result.failure(AppError.BusinessError("请输入正确的手机号"))
        }

        // 验证密码
        if (password.isBlank()) {
            return Result.failure(AppError.ValidationError("password"))
        }
        if (password.length < 6) {
            return Result.failure(AppError.BusinessError("密码长度不能少于6位"))
        }
        if (password.length > 20) {
            return Result.failure(AppError.BusinessError("密码长度不能超过20位"))
        }

        // 验证昵称
        if (nickname.isBlank()) {
            return Result.failure(AppError.ValidationError("nickname"))
        }
        if (nickname.length < 2 || nickname.length > 20) {
            return Result.failure(AppError.BusinessError("昵称长度应在2-20个字符之间"))
        }

        // 验证用户类型
        if (userType == UserType.ADMIN) {
            return Result.failure(AppError.Forbidden())
        }

        return try {
            authRepository.register(
                phone = phone,
                password = password,
                nickname = nickname,
                userType = userType.value
            ).fold(
                onSuccess = { user ->
                    Result.success(user)
                },
                onFailure = { throwable ->
                    Result.failure(throwable.toAppError())
                }
            )
        } catch (e: Exception) {
            Result.failure(e.toAppError())
        }
    }
}
