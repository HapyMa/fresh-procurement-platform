package com.fresh.procurement.domain.usecase.buyer

import com.fresh.procurement.data.model.CreateDemandRequest
import com.fresh.procurement.domain.model.Demand
import com.fresh.procurement.domain.repository.DemandRepository
import com.fresh.procurement.domain.error.AppError
import com.fresh.procurement.domain.error.toAppError
import javax.inject.Inject

class CreateDemandUseCase @Inject constructor(
    private val demandRepository: DemandRepository
) {
    suspend operator fun invoke(params: Params): Result<Demand> {
        // 验证商品名称
        if (params.productName.isBlank()) {
            return Result.failure(AppError.ValidationError("productName"))
        }
        if (params.productName.length > 100) {
            return Result.failure(AppError.BusinessError("商品名称长度不能超过100个字符"))
        }

        // 验证数量
        if (params.quantity <= 0) {
            return Result.failure(AppError.BusinessError("数量必须大于0"))
        }
        if (params.quantity > 999999.99) {
            return Result.failure(AppError.BusinessError("数量超出限制"))
        }

        // 验证单位
        if (params.unit.isBlank()) {
            return Result.failure(AppError.ValidationError("unit"))
        }

        // 验证最高单价
        params.maxPrice?.let { maxPrice ->
            if (maxPrice <= 0) {
                return Result.failure(AppError.BusinessError("最高单价必须大于0"))
            }
        }

        // 验证分类ID
        if (params.categoryId <= 0) {
            return Result.failure(AppError.ValidationError("categoryId"))
        }

        // 验证收货地址ID
        if (params.deliveryAddressId <= 0) {
            return Result.failure(AppError.ValidationError("deliveryAddressId"))
        }

        // 验证配送日期
        if (params.deliveryDate.isNullOrBlank()) {
            return Result.failure(AppError.ValidationError("deliveryDate"))
        }

        val request = CreateDemandRequest(
            categoryId = params.categoryId,
            productName = params.productName,
            quantity = params.quantity,
            unit = params.unit,
            maxPrice = params.maxPrice,
            qualityRequirement = params.qualityRequirement,
            deliveryAddressId = params.deliveryAddressId,
            deliveryDate = params.deliveryDate,
            deliveryTimeSlot = params.deliveryTimeSlot,
            remark = params.remark
        )

        return try {
            demandRepository.createDemand(request).fold(
                onSuccess = { demand ->
                    Result.success(demand)
                },
                onFailure = { throwable ->
                    Result.failure(throwable.toAppError())
                }
            )
        } catch (e: Exception) {
            Result.failure(e.toAppError())
        }
    }

    data class Params(
        val categoryId: Long,
        val productName: String,
        val quantity: Double,
        val unit: String,
        val maxPrice: Double? = null,
        val qualityRequirement: String? = null,
        val deliveryAddressId: Long,
        val deliveryDate: String? = null,
        val deliveryTimeSlot: String? = null,
        val remark: String? = null
    )
}
