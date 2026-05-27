package com.fresh.procurement.domain.usecase.auth

import com.fresh.procurement.domain.error.AppError
import com.fresh.procurement.domain.error.toAppError
import com.fresh.procurement.domain.model.LoginResult
import com.fresh.procurement.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, password: String): Result<LoginResult> {
        // 验证输入
        if (phone.isBlank()) {
            return Result.failure(AppError.ValidationError("phone"))
        }
        if (password.isBlank()) {
            return Result.failure(AppError.ValidationError("password"))
        }
        if (!phone.matches(Regex("^1[3-9]\\d{9}$"))) {
            return Result.failure(AppError.BusinessError("请输入正确的手机号"))
        }
        if (password.length < 6) {
            return Result.failure(AppError.BusinessError("密码长度不能少于6位"))
        }

        return try {
            authRepository.login(phone, password).fold(
                onSuccess = { loginResult ->
                    Result.success(loginResult)
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
