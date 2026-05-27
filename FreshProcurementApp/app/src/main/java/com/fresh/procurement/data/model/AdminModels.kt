package com.fresh.procurement.data.model

import com.fresh.procurement.data.remote.dto.AdminDashboardDto
import com.fresh.procurement.data.remote.dto.AdminDemandItemDto
import com.fresh.procurement.data.remote.dto.AdminUserItemDto

/**
 * 管理员仪表盘数据（Domain 层使用的数据模型）
 */
typealias AdminDashboard = AdminDashboardDto

/**
 * 管理员用户列表响应（Domain 层使用的数据模型）
 */
data class AdminUserListResponse(
    val total: Int,
    val users: List<AdminUserItemDto>
)

/**
 * 管理员需求列表响应（Domain 层使用的数据模型）
 */
data class AdminDemandListResponse(
    val total: Int,
    val demands: List<AdminDemandItemDto>
)
