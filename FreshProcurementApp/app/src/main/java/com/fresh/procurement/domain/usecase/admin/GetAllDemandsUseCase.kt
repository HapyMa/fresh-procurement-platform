package com.fresh.procurement.domain.usecase.admin

import com.fresh.procurement.data.model.AdminDemandListResponse
import com.fresh.procurement.domain.repository.AdminRepository
import com.fresh.procurement.domain.error.AppError
import com.fresh.procurement.domain.error.toAppError
import com.fresh.procurement.domain.model.DemandStatus
import javax.inject.Inject

class GetAllDemandsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(
        status: DemandStatus? = null,
        page: Int = 1,
        size: Int = 20
    ): Result<AdminDemandListResponse> {
        // 验证分页参数
        if (page < 1) {
            return Result.failure(AppError.ValidationError("page"))
        }
        if (size < 1 || size > 100) {
            return Result.failure(AppError.ValidationError("size"))
        }

        return try {
            adminRepository.getDemands(
                status = status?.value,
                page = page,
                size = size
            ).fold(
                onSuccess = { response ->
                    Result.success(response)
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
