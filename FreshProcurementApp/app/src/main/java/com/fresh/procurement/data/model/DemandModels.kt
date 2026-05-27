package com.fresh.procurement.data.model

import com.fresh.procurement.domain.model.Demand

/**
 * 创建需求请求（Domain 层使用的数据模型）
 */
data class CreateDemandRequest(
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

/**
 * 需求列表响应（Domain 层使用的数据模型）
 */
data class DemandListResponse(
    val total: Int,
    val list: List<Demand>
)
