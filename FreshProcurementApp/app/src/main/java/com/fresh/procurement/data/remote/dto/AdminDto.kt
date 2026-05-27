package com.fresh.procurement.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 管理员仪表盘数据 DTO
 */
data class AdminDashboardDto(
    @SerializedName("totalUsers")
    val totalUsers: Long,
    @SerializedName("totalBuyers")
    val totalBuyers: Long,
    @SerializedName("totalSuppliers")
    val totalSuppliers: Long,
    @SerializedName("totalDemands")
    val totalDemands: Long,
    @SerializedName("totalOrders")
    val totalOrders: Long,
    @SerializedName("totalQuotes")
    val totalQuotes: Long,
    @SerializedName("totalAmount")
    val totalAmount: Double,
    @SerializedName("pendingDemands")
    val pendingDemands: Long,
    @SerializedName("recentUsers")
    val recentUsers: List<AdminUserItemDto>
)

/**
 * 管理员用户列表项 DTO
 */
data class AdminUserItemDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    @SerializedName("userType")
    val userType: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * 管理员用户列表响应 DTO
 */
data class AdminUserListResponseDto(
    @SerializedName("total")
    val total: Int,
    @SerializedName("users")
    val users: List<AdminUserItemDto>
)

/**
 * 管理员需求列表项 DTO
 */
data class AdminDemandItemDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("groupId")
    val groupId: Long?,
    @SerializedName("buyerId")
    val buyerId: Long?,
    @SerializedName("categoryId")
    val categoryId: Long?,
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
    val deliveryAddressId: Long?,
    @SerializedName("deliveryDate")
    val deliveryDate: String?,
    @SerializedName("deliveryTimeSlot")
    val deliveryTimeSlot: String?,
    @SerializedName("remark")
    val remark: String?,
    @SerializedName("status")
    val status: Int,
    @SerializedName("selectedQuoteId")
    val selectedQuoteId: Long?,
    @SerializedName("dealPrice")
    val dealPrice: Double?,
    @SerializedName("dealTotalAmount")
    val dealTotalAmount: Double?,
    @SerializedName("supplierId")
    val supplierId: Long?,
    @SerializedName("packStatus")
    val packStatus: Int?,
    @SerializedName("actualWeight")
    val actualWeight: Double?,
    @SerializedName("packRemark")
    val packRemark: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("buyerName")
    val buyerName: String?,
    @SerializedName("supplierName")
    val supplierName: String?,
    @SerializedName("categoryName")
    val categoryName: String?,
    @SerializedName("cityName")
    val cityName: String?
)

/**
 * 管理员需求列表响应 DTO
 */
data class AdminDemandListResponseDto(
    @SerializedName("total")
    val total: Int,
    @SerializedName("demands")
    val demands: List<AdminDemandItemDto>
)

/**
 * 管理员报价列表项 DTO
 */
data class AdminQuoteItemDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("groupId")
    val groupId: Long?,
    @SerializedName("supplierId")
    val supplierId: Long?,
    @SerializedName("unitPrice")
    val unitPrice: Double?,
    @SerializedName("totalAmount")
    val totalAmount: Double?,
    @SerializedName("validHours")
    val validHours: Int?,
    @SerializedName("expireAt")
    val expireAt: String?,
    @SerializedName("remark")
    val remark: String?,
    @SerializedName("status")
    val status: Int,
    @SerializedName("selectedAt")
    val selectedAt: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("supplierName")
    val supplierName: String?,
    @SerializedName("productName")
    val productName: String?,
    @SerializedName("cityName")
    val cityName: String?
)

/**
 * 管理员报价列表响应 DTO
 */
data class AdminQuoteListResponseDto(
    @SerializedName("total")
    val total: Int,
    @SerializedName("quotes")
    val quotes: List<AdminQuoteItemDto>
)

/**
 * 管理员订单统计 DTO
 */
data class AdminOrderStatsDto(
    @SerializedName("statusDistribution")
    val statusDistribution: Map<String, Long>?
)
