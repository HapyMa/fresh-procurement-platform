package com.fresh.procurement.data.model

import com.google.gson.annotations.SerializedName

// Admin Dashboard
data class AdminDashboard(
    @SerializedName("totalUsers") val totalUsers: Long,
    @SerializedName("totalBuyers") val totalBuyers: Long,
    @SerializedName("totalSuppliers") val totalSuppliers: Long,
    @SerializedName("totalDemands") val totalDemands: Long,
    @SerializedName("totalOrders") val totalOrders: Long,
    @SerializedName("totalQuotes") val totalQuotes: Long,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("pendingDemands") val pendingDemands: Long,
    @SerializedName("recentUsers") val recentUsers: List<AdminUserItem>
)

// Admin User Item (for dashboard recent users and user list)
data class AdminUserItem(
    @SerializedName("id") val id: Long,
    @SerializedName("phone") val phone: String,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("userType") val userType: Int,
    @SerializedName("status") val status: Int,
    @SerializedName("createdAt") val createdAt: String
) {
    fun getUserTypeText(): String = when (userType) {
        1 -> "采购商"
        2 -> "供应商"
        3 -> "管理员"
        else -> "未知"
    }
    fun isActive(): Boolean = status == 1
}

// Admin User List Response
data class AdminUserListResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("users") val users: List<AdminUserItem>
)

// Admin Demand Item (enriched with names)
data class AdminDemandItem(
    @SerializedName("id") val id: Long,
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("buyerId") val buyerId: Long?,
    @SerializedName("categoryId") val categoryId: Long?,
    @SerializedName("productName") val productName: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unit") val unit: String?,
    @SerializedName("maxPrice") val maxPrice: Double?,
    @SerializedName("qualityRequirement") val qualityRequirement: String?,
    @SerializedName("deliveryAddressId") val deliveryAddressId: Long?,
    @SerializedName("deliveryDate") val deliveryDate: String?,
    @SerializedName("deliveryTimeSlot") val deliveryTimeSlot: String?,
    @SerializedName("remark") val remark: String?,
    @SerializedName("status") val status: Int,
    @SerializedName("selectedQuoteId") val selectedQuoteId: Long?,
    @SerializedName("dealPrice") val dealPrice: Double?,
    @SerializedName("dealTotalAmount") val dealTotalAmount: Double?,
    @SerializedName("supplierId") val supplierId: Long?,
    @SerializedName("packStatus") val packStatus: Int?,
    @SerializedName("actualWeight") val actualWeight: Double?,
    @SerializedName("packRemark") val packRemark: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("buyerName") val buyerName: String?,
    @SerializedName("supplierName") val supplierName: String?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("cityName") val cityName: String?
) {
    fun getStatusText(): String = when (status) {
        0 -> "待报价"
        3 -> "已报价"
        5 -> "已选报价"
        6 -> "已发货"
        7 -> "已收货"
        9 -> "已取消"
        else -> "未知"
    }
}

// Admin Demand List Response
data class AdminDemandListResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("demands") val demands: List<AdminDemandItem>
)

// Admin Quote Item
data class AdminQuoteItem(
    @SerializedName("id") val id: Long,
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("supplierId") val supplierId: Long?,
    @SerializedName("unitPrice") val unitPrice: Double?,
    @SerializedName("totalAmount") val totalAmount: Double?,
    @SerializedName("validHours") val validHours: Int?,
    @SerializedName("expireAt") val expireAt: String?,
    @SerializedName("remark") val remark: String?,
    @SerializedName("status") val status: Int,
    @SerializedName("selectedAt") val selectedAt: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("supplierName") val supplierName: String?,
    @SerializedName("productName") val productName: String?,
    @SerializedName("cityName") val cityName: String?
) {
    fun getStatusText(): String = when (status) {
        0 -> "待选中"
        1 -> "已选中"
        else -> "未知"
    }
}

// Admin Quote List Response
data class AdminQuoteListResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("quotes") val quotes: List<AdminQuoteItem>
)

// Admin Order Stats
data class AdminOrderStats(
    @SerializedName("statusDistribution") val statusDistribution: Map<String, Long>?
)
