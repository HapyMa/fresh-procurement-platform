package com.fresh.procurement.domain.model

enum class DemandStatus(val value: Int, val label: String) {
    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    MERGED(3, "已合并"),
    QUOTED(4, "已报价"),
    CONFIRMED(5, "已确认"),
    PACKED(6, "已分拣"),
    SHIPPED(7, "已发货"),
    COMPLETED(8, "已完成"),
    CANCELLED(9, "已取消");

    companion object {
        fun fromValue(value: Int): DemandStatus = values().find { it.value == value } ?: PENDING
    }
}

data class Demand(
    val demandId: Long,
    val buyerId: Long,
    val buyerNickname: String?,
    val categoryId: Long,
    val categoryName: String?,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val maxPrice: Double?,
    val qualityRequirement: String?,
    val deliveryAddressId: Long?,
    val deliveryAddress: String?,
    val deliveryDate: String?,
    val deliveryTimeSlot: String?,
    val status: DemandStatus,
    val groupId: Long?,
    val createdAt: String?,
    val updatedAt: String?
)

data class DemandGroup(
    val groupId: Long,
    val categoryId: Long,
    val categoryName: String?,
    val productName: String,
    val city: String,
    val totalQuantity: Double,
    val unit: String,
    val mergeDeadline: String,
    val status: Int,
    val createdAt: String?,
    val quoteCount: Int = 0,
    val minPrice: Double? = null
)
