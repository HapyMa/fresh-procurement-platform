package com.fresh.procurement.domain.usecase.admin

import com.fresh.procurement.data.model.AdminDashboard
import com.fresh.procurement.data.repository.AdminRepository
import com.fresh.procurement.domain.error.toAppError
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): Result<AdminDashboard> {
        return try {
            adminRepository.getDashboard().fold(
                onSuccess = { dashboard ->
                    Result.success(dashboard)
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
