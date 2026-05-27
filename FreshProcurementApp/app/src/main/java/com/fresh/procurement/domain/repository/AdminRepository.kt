package com.fresh.procurement.domain.repository

import com.fresh.procurement.data.remote.dto.AdminDashboardDto
import com.fresh.procurement.data.remote.dto.AdminDemandItemDto
import com.fresh.procurement.data.remote.dto.AdminOrderStatsDto
import com.fresh.procurement.data.remote.dto.AdminQuoteItemDto
import com.fresh.procurement.data.remote.dto.AdminUserItemDto

/**
 * 管理员仓库接口
 * 提供管理员后台管理相关操作
 */
interface AdminRepository {

    /**
     * 获取管理员仪表盘数据
     * @return Result<AdminDashboardDto> 仪表盘数据
     */
    suspend fun getDashboard(): Result<AdminDashboardDto>

    /**
     * 获取用户列表
     * @param userType 用户类型筛选
     * @param status 状态筛选
     * @param page 页码
     * @param size 每页数量
     * @return Result<Pair<Int, List<AdminUserItemDto>>> 总数和用户列表
     */
    suspend fun getUsers(
        userType: Int? = null,
        status: Int? = null,
        page: Int = 1,
        size: Int = 20
    ): Result<Pair<Int, List<AdminUserItemDto>>>

    /**
     * 获取用户详情
     * @param userId 用户ID
     * @return Result<AdminUserItemDto> 用户详情
     */
    suspend fun getUserDetail(userId: Long): Result<AdminUserItemDto>

    /**
     * 切换用户状态（启用/禁用）
     * @param userId 用户ID
     * @return Result<AdminUserItemDto> 更新后的用户信息
     */
    suspend fun toggleUserStatus(userId: Long): Result<AdminUserItemDto>

    /**
     * 获取需求列表
     * @param status 状态筛选
     * @param page 页码
     * @param size 每页数量
     * @return Result<Pair<Int, List<AdminDemandItemDto>>> 总数和需求列表
     */
    suspend fun getDemands(
        status: Int? = null,
        page: Int = 1,
        size: Int = 20
    ): Result<Pair<Int, List<AdminDemandItemDto>>>

    /**
     * 获取需求详情
     * @param demandId 需求ID
     * @return Result<AdminDemandItemDto> 需求详情
     */
    suspend fun getDemandDetail(demandId: Long): Result<AdminDemandItemDto>

    /**
     * 取消需求
     * @param demandId 需求ID
     * @return Result<AdminDemandItemDto> 更新后的需求
     */
    suspend fun cancelDemand(demandId: Long): Result<AdminDemandItemDto>

    /**
     * 获取报价列表
     * @param status 状态筛选
     * @param page 页码
     * @param size 每页数量
     * @return Result<Pair<Int, List<AdminQuoteItemDto>>> 总数和报价列表
     */
    suspend fun getQuotes(
        status: Int? = null,
        page: Int = 1,
        size: Int = 20
    ): Result<Pair<Int, List<AdminQuoteItemDto>>>

    /**
     * 获取订单统计数据
     * @return Result<AdminOrderStatsDto> 订单统计
     */
    suspend fun getOrderStats(): Result<AdminOrderStatsDto>
}
