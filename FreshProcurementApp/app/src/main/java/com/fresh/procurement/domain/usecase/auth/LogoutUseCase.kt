package com.fresh.procurement.domain.usecase.auth

import com.fresh.procurement.domain.error.toAppError
import com.fresh.procurement.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.logout().fold(
                onSuccess = {
                    Result.success(Unit)
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
