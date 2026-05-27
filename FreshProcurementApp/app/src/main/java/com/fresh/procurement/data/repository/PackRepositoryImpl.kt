package com.fresh.procurement.data.repository

import com.fresh.procurement.data.remote.ApiService
import com.fresh.procurement.data.remote.dto.CompletePackRequestDto
import com.fresh.procurement.data.remote.dto.PackRecordDto
import com.fresh.procurement.data.remote.dto.ShipRequestDto
import com.fresh.procurement.domain.repository.PackRepository
import com.fresh.procurement.domain.repository.PendingPackItemData
import com.fresh.procurement.domain.repository.PendingPackListData
import javax.inject.Inject

/**
 * 分拣打包仓库实现类
 * 实现 Domain 层的 PackRepository 接口
 */
class PackRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : BaseRepository(), PackRepository {

    /**
     * 获取待分拣列表
     */
    override suspend fun getPendingPackList(
        page: Int,
        size: Int
    ): Result<PendingPackListData> {
        return safeApiCall {
            apiService.getPendingPackList(page = page, size = size)
        }.map { response ->
            PendingPackListData(
                total = response.total,
                demands = response.list.map { demand ->
                    PendingPackItemData(
                        demandId = demand.demandId,
                        productName = demand.productName,
                        quantity = demand.quantity,
                        unit = demand.unit,
                        status = demand.status,
                        buyerName = demand.buyerName,
                        dealPrice = demand.dealPrice,
                        createdAt = demand.createdAt
                    )
                }
            )
        }
    }

    /**
     * 开始分拣
     */
    override suspend fun startPacking(demandId: Long): Result<PackRecordDto> {
        return safeApiCall {
            apiService.startPacking(demandId)
        }
    }

    /**
     * 完成分拣
     */
    override suspend fun completePacking(
        demandId: Long,
        actualQuantity: Double,
        actualWeight: Double,
        grade: String?,
        qualityCheck: Int,
        packageCount: Int,
        packageType: String?,
        labelCode: String,
        remark: String?
    ): Result<PackRecordDto> {
        return safeApiCall {
            apiService.completePacking(
                demandId,
                CompletePackRequestDto(
                    actualQuantity = actualQuantity,
                    actualWeight = actualWeight,
                    grade = grade,
                    qualityCheck = qualityCheck,
                    packageCount = packageCount,
                    packageType = packageType,
                    labelCode = labelCode,
                    remark = remark
                )
            )
        }
    }

    /**
     * 获取分拣记录
     */
    override suspend fun getPackRecord(demandId: Long): Result<PackRecordDto> {
        return safeApiCall {
            apiService.getPackRecord(demandId)
        }
    }

    /**
     * 发货
     */
    override suspend fun shipOrder(
        demandId: Long,
        logisticsCompany: String,
        trackingNo: String
    ): Result<Unit> {
        return safeApiCallEmpty {
            apiService.shipOrder(
                demandId,
                ShipRequestDto(
                    logisticsCompany = logisticsCompany,
                    trackingNo = trackingNo
                )
            )
        }
    }
}
