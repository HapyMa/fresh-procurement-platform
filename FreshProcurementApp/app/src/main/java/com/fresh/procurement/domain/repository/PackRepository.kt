package com.fresh.procurement.domain.repository

import com.fresh.procurement.data.remote.dto.PackRecordDto

/**
 * 分拣打包仓库接口
 * 提供供应商分拣打包相关操作
 */
interface PackRepository {

    /**
     * 获取待分拣列表
     * @param page 页码
     * @param size 每页数量
     * @return Result<PendingPackListData> 待分拣列表数据
     */
    suspend fun getPendingPackList(
        page: Int = 1,
        size: Int = 20
    ): Result<PendingPackListData>

    /**
     * 开始分拣
     * @param demandId 需求ID
     * @return Result<PackRecordDto> 分拣记录
     */
    suspend fun startPacking(demandId: Long): Result<PackRecordDto>

    /**
     * 完成分拣
     * @param demandId 需求ID
     * @param actualQuantity 实际数量
     * @param actualWeight 实际重量
     * @param grade 等级
     * @param qualityCheck 质检结果
     * @param packageCount 包裹数量
     * @param packageType 包装类型
     * @param labelCode 标签码
     * @param remark 备注
     * @return Result<PackRecordDto> 分拣记录
     */
    suspend fun completePacking(
        demandId: Long,
        actualQuantity: Double,
        actualWeight: Double,
        grade: String?,
        qualityCheck: Int,
        packageCount: Int,
        packageType: String?,
        labelCode: String,
        remark: String?
    ): Result<PackRecordDto>

    /**
     * 获取分拣记录
     * @param demandId 需求ID
     * @return Result<PackRecordDto> 分拣记录
     */
    suspend fun getPackRecord(demandId: Long): Result<PackRecordDto>

    /**
     * 发货
     * @param demandId 需求ID
     * @param logisticsCompany 物流公司
     * @param trackingNo 物流单号
     * @return Result<Unit>
     */
    suspend fun shipOrder(
        demandId: Long,
        logisticsCompany: String,
        trackingNo: String
    ): Result<Unit>
}

/**
 * 待分拣列表数据
 */
data class PendingPackListData(
    val total: Int,
    val demands: List<PendingPackItemData>
)

/**
 * 待分拣项数据
 */
data class PendingPackItemData(
    val demandId: Long,
    val productName: String,
    val quantity: Double,
    val unit: String?,
    val status: Int,
    val buyerName: String?,
    val dealPrice: Double?,
    val createdAt: String
)
