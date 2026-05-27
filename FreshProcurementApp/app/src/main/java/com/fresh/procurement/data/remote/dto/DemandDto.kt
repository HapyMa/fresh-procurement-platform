package com.fresh.procurement.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 收货地址信息 DTO
 */
data class DeliveryAddressDto(
    @SerializedName("province")
    val province: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("district")
    val district: String?,
    @SerializedName("detail")
    val detail: String,
    @SerializedName("lng")
    val longitude: Double?,
    @SerializedName("lat")
    val latitude: Double?
)

/**
 * 需求（子订单）DTO
 */
data class DemandDto(
    @SerializedName("demandId")
    val demandId: Long,
    @SerializedName("groupId")
    val groupId: Long?,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("quantity")
    val quantity: Double,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("maxPrice")
    val maxPrice: Double?,
    @SerializedName("qualityRequirement")
    val qualityRequirement: String?,
    @SerializedName("deliveryAddress")
    val deliveryAddress: DeliveryAddressDto?,
    @SerializedName("deliveryDate")
    val deliveryDate: String?,
    @SerializedName("deliveryTimeSlot")
    val deliveryTimeSlot: String?,
    @SerializedName("remark")
    val remark: String?,
    @SerializedName("status")
    val status: Int,
    @SerializedName("packStatus")
    val packStatus: Int?,
    @SerializedName("buyerName")
    val buyerName: String?,
    @SerializedName("dealPrice")
    val dealPrice: Double?,
    @SerializedName("dealTotalAmount")
    val dealTotalAmount: Double?,
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * 需求列表响应 DTO
 */
data class DemandListResponseDto(
    @SerializedName("total")
    val total: Int,
    @SerializedName("list")
    val list: List<DemandDto>
)

/**
 * 发布需求请求 DTO
 */
data class CreateDemandRequestDto(
    @SerializedName("categoryId")
    val categoryId: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("quantity")
    val quantity: Double,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("maxPrice")
    val maxPrice: Double?,
    @SerializedName("qualityRequirement")
    val qualityRequirement: String?,
    @SerializedName("deliveryAddressId")
    val deliveryAddressId: Long,
    @SerializedName("deliveryDate")
    val deliveryDate: String?,
    @SerializedName("deliveryTimeSlot")
    val deliveryTimeSlot: String?,
    @SerializedName("remark")
    val remark: String?
)

/**
 * 选择报价请求 DTO
 */
data class SelectQuoteRequestDto(
    @SerializedName("quoteId")
    val quoteId: Long
)

/**
 * 合并组状态
 */
enum class GroupStatus(val value: Int, val text: String) {
    PENDING(0, "待合并"),
    MERGING(1, "合并中"),
    QUOTING(2, "报价中"),
    DEAL_DONE(3, "已成交"),
    CLOSED(4, "已关闭");

    companion object {
        fun fromValue(value: Int) = values().find { it.value == value } ?: PENDING
    }
}

/**
 * 需求合并组 DTO
 */
data class DemandGroupDto(
    @SerializedName("groupId")
    val groupId: Long,
    @SerializedName("categoryId")
    val categoryId: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("totalQuantity")
    val totalQuantity: Double,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("demandCount")
    val demandCount: Int,
    @SerializedName("mergeDeadline")
    val mergeDeadline: String?,
    @SerializedName("quoteCount")
    val quoteCount: Int?,
    @SerializedName("status")
    val status: Int
)

/**
 * 合并组详情 DTO
 */
data class DemandGroupDetailDto(
    @SerializedName("groupId")
    val groupId: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("totalQuantity")
    val totalQuantity: Double,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("status")
    val status: Int,
    @SerializedName("demands")
    val demands: List<GroupDemandItemDto>
)

/**
 * 合并组中的需求项 DTO
 */
data class GroupDemandItemDto(
    @SerializedName("demandId")
    val demandId: Long,
    @SerializedName("buyerId")
    val buyerId: Long,
    @SerializedName("buyerName")
    val buyerName: String,
    @SerializedName("quantity")
    val quantity: Double,
    @SerializedName("maxPrice")
    val maxPrice: Double?,
    @SerializedName("qualityRequirement")
    val qualityRequirement: String?,
    @SerializedName("deliveryAddress")
    val deliveryAddress: DeliveryAddressDto?,
    @SerializedName("deliveryDate")
    val deliveryDate: String?,
    @SerializedName("deliveryTimeSlot")
    val deliveryTimeSlot: String?
)

/**
 * 合并组列表响应 DTO
 */
data class DemandGroupListResponseDto(
    @SerializedName("total")
    val total: Int,
    @SerializedName("list")
    val list: List<DemandGroupDto>
)
