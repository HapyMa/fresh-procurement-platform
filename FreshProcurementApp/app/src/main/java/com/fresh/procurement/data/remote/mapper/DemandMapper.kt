package com.fresh.procurement.data.remote.mapper

import com.fresh.procurement.data.remote.dto.DeliveryAddressDto
import com.fresh.procurement.data.remote.dto.DemandDto
import com.fresh.procurement.data.remote.dto.DemandGroupDto
import com.fresh.procurement.domain.model.Demand
import com.fresh.procurement.domain.model.DemandGroup
import com.fresh.procurement.domain.model.DemandStatus

/**
 * 需求相关 DTO 到 Domain Model 的转换器
 */
object DemandMapper {

    /**
     * 将 DemandDto 转换为 Domain 层的 Demand
     */
    fun DemandDto.toDomain(): Demand {
        return Demand(
            demandId = this.demandId,
            buyerId = 0, // 需要从其他字段获取或后续填充
            buyerNickname = this.buyerName,
            categoryId = 0, // 需要从其他字段获取或后续填充
            categoryName = null,
            productName = this.productName,
            quantity = this.quantity,
            unit = this.unit ?: "",
            maxPrice = this.maxPrice,
            qualityRequirement = this.qualityRequirement,
            deliveryAddressId = null,
            deliveryAddress = this.deliveryAddress?.toDomainString(),
            deliveryDate = this.deliveryDate,
            deliveryTimeSlot = this.deliveryTimeSlot,
            status = DemandStatus.fromValue(this.status),
            groupId = this.groupId,
            createdAt = this.createdAt,
            updatedAt = null
        )
    }

    /**
     * 将 DeliveryAddressDto 转换为地址字符串
     */
    private fun DeliveryAddressDto.toDomainString(): String {
        return buildString {
            append(province)
            append(city)
            district?.let { append(it) }
            append(detail)
        }
    }

    /**
     * 将 DemandDto 列表转换为 Domain 层的 Demand 列表
     */
    fun List<DemandDto>.toDomainList(): List<Demand> {
        return this.map { it.toDomain() }
    }

    /**
     * 将 DemandGroupDto 转换为 Domain 层的 DemandGroup
     */
    fun DemandGroupDto.toDomain(): DemandGroup {
        return DemandGroup(
            groupId = this.groupId,
            categoryId = this.categoryId,
            categoryName = null, // 需要从其他字段获取
            productName = this.productName,
            city = this.city,
            totalQuantity = this.totalQuantity,
            unit = this.unit ?: "",
            mergeDeadline = this.mergeDeadline ?: "",
            status = this.status,
            createdAt = null,
            quoteCount = this.quoteCount ?: 0,
            minPrice = null
        )
    }

    /**
     * 将 DemandGroupDto 列表转换为 Domain 层的 DemandGroup 列表
     */
    fun List<DemandGroupDto>.toDomainGroupList(): List<DemandGroup> {
        return this.map { it.toDomain() }
    }
}
