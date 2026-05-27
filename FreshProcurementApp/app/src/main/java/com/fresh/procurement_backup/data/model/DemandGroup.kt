package com.fresh.procurement.data.model

import com.google.gson.annotations.SerializedName

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
 * 需求合并组
 */
data class DemandGroup(
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
) {
    fun getStatusEnum() = GroupStatus.fromValue(status)
    fun getStatusText() = getStatusEnum().text
}

/**
 * 合并组详情
 */
data class DemandGroupDetail(
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
    val demands: List<GroupDemandItem>
)

/**
 * 合并组中的需求项
 */
data class GroupDemandItem(
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
    val deliveryAddress: DeliveryAddress?,
    @SerializedName("deliveryDate")
    val deliveryDate: String?,
    @SerializedName("deliveryTimeSlot")
    val deliveryTimeSlot: String?
)

/**
 * 合并组列表响应
 */
data class DemandGroupListResponse(
    @SerializedName("total")
    val total: Int,
    @SerializedName("list")
    val list: List<DemandGroup>
)
