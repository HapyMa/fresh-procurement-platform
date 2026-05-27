package com.fresh.procurement.domain.usecase.buyer

import com.fresh.procurement.data.model.DemandListResponse
import com.fresh.procurement.data.repository.DemandRepository
import com.fresh.procurement.domain.error.AppError
import com.fresh.procurement.domain.error.toAppError
import com.fresh.procurement.domain.model.DemandStatus
import javax.inject.Inject

class GetBuyerDemandsUseCase @Inject constructor(
    private val demandRepository: DemandRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        size: Int = 20,
        status: DemandStatus? = null
    ): Result<DemandListResponse> {
        // 验证分页参数
        if (page < 1) {
            return Result.failure(AppError.ValidationError("page"))
        }
        if (size < 1 || size > 100) {
            return Result.failure(AppError.ValidationError("size"))
        }

        return try {
            demandRepository.getBuyerDemands(
                page = page,
                size = size,
                status = status?.value
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
