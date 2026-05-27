package com.fresh.procurement.data.repository

import com.fresh.procurement.data.remote.ApiService
import com.fresh.procurement.data.remote.dto.AdminDashboardDto
import com.fresh.procurement.data.remote.dto.AdminDemandItemDto
import com.fresh.procurement.data.remote.dto.AdminOrderStatsDto
import com.fresh.procurement.data.remote.dto.AdminQuoteItemDto
import com.fresh.procurement.data.remote.dto.AdminUserItemDto
import com.fresh.procurement.domain.repository.AdminRepository
import javax.inject.Inject

/**
 * 管理员仓库实现类
 * 实现 Domain 层的 AdminRepository 接口
 */
class AdminRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : BaseRepository(), AdminRepository {

    /**
     * 获取管理员仪表盘数据
     */
    override suspend fun getDashboard(): Result<AdminDashboardDto> {
        return safeApiCall {
            apiService.getAdminDashboard()
        }
    }

    /**
     * 获取用户列表
     */
    override suspend fun getUsers(
        userType: Int?,
        status: Int?,
        page: Int,
        size: Int
    ): Result<Pair<Int, List<AdminUserItemDto>>> {
        return safeApiCall {
            apiService.getAdminUsers(
                userType = userType,
                status = status,
                page = page,
                size = size
            )
        }.map { response ->
            Pair(response.total, response.users)
        }
    }

    /**
     * 获取用户详情
     */
    override suspend fun getUserDetail(userId: Long): Result<AdminUserItemDto> {
        return safeApiCall {
            apiService.getAdminUserDetail(userId)
        }
    }

    /**
     * 切换用户状态（启用/禁用）
     */
    override suspend fun toggleUserStatus(userId: Long): Result<AdminUserItemDto> {
        return safeApiCall {
            apiService.toggleUserStatus(userId)
        }
    }

    /**
     * 获取需求列表
     */
    override suspend fun getDemands(
        status: Int?,
        page: Int,
        size: Int
    ): Result<Pair<Int, List<AdminDemandItemDto>>> {
        return safeApiCall {
            apiService.getAdminDemands(
                status = status,
                page = page,
                size = size
            )
        }.map { response ->
            Pair(response.total, response.demands)
        }
    }

    /**
     * 获取需求详情
     */
    override suspend fun getDemandDetail(demandId: Long): Result<AdminDemandItemDto> {
        return safeApiCall {
            apiService.getAdminDemandDetail(demandId)
        }
    }

    /**
     * 取消需求
     */
    override suspend fun cancelDemand(demandId: Long): Result<AdminDemandItemDto> {
        return safeApiCall {
            apiService.cancelAdminDemand(demandId)
        }
    }

    /**
     * 获取报价列表
     */
    override suspend fun getQuotes(
        status: Int?,
        page: Int,
        size: Int
    ): Result<Pair<Int, List<AdminQuoteItemDto>>> {
        return safeApiCall {
            apiService.getAdminQuotes(
                status = status,
                page = page,
                size = size
            )
        }.map { response ->
            Pair(response.total, response.quotes)
        }
    }

    /**
     * 获取订单统计数据
     */
    override suspend fun getOrderStats(): Result<AdminOrderStatsDto> {
        return safeApiCall {
            apiService.getAdminDashboard()
        }.map { dashboard ->
            AdminOrderStatsDto(
                statusDistribution = mapOf(
                    "totalOrders" to dashboard.totalOrders,
                    "pendingDemands" to dashboard.pendingDemands,
                    "totalAmount" to dashboard.totalAmount.toLong()
                )
            )
        }
    }
}
