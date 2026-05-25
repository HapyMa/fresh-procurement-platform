package com.fresh.procurement.data.model

import com.google.gson.annotations.SerializedName

/**
 * 需求状态
 */
enum class DemandStatus(val value: Int, val text: String) {
    DRAFT(0, "待发布"),
    PENDING_MERGE(1, "待合并"),
    MERGING(2, "合并中"),
    QUOTING(3, "报价中"),
    WAITING_SELECT(4, "待选择"),
    WAITING_SHIP(5, "待发货"),
    SHIPPED(6, "已发货"),
    DELIVERED(7, "已签收"),
    COMPLETED(8, "已完成"),
    CANCELLED(9, "已取消");
    
    companion object {
        fun fromValue(value: Int) = values().find { it.value == value } ?: DRAFT
    }
}

/**
 * 打包状态
 */
enum class PackStatus(val value: Int, val text: String) {
    PENDING(0, "待分拣"),
    SORTING(1, "分拣中"),
    PACKED(2, "已打包"),
    SHIPPED(3, "已发货");
    
    companion object {
        fun fromValue(value: Int) = values().find { it.value == value } ?: PENDING
    }
}

/**
 * 收货地址信息
 */
data class DeliveryAddress(
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
 * 采购商需求（子订单）
 */
data class Demand(
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
    val deliveryAddress: DeliveryAddress?,
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
) {
    fun getStatusEnum() = DemandStatus.fromValue(status)
    fun getPackStatusEnum() = packStatus?.let { PackStatus.fromValue(it) } ?: PackStatus.PENDING
    fun getStatusText() = getStatusEnum().text
    fun getPackStatusText() = getPackStatusEnum().text
}

/**
 * 需求列表响应
 */
data class DemandListResponse(
    @SerializedName("total")
    val total: Int,
    @SerializedName("list")
    val list: List<Demand>
)

/**
 * 发布需求请求
 */
data class CreateDemandRequest(
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
 * 选择报价请求
 */
data class SelectQuoteRequest(
    @SerializedName("quoteId")
    val quoteId: Long
)
