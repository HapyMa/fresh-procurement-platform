package com.fresh.procurement.data.repository

import com.fresh.procurement.data.remote.ApiService
import com.fresh.procurement.data.remote.dto.CreateDemandRequestDto
import com.fresh.procurement.data.remote.dto.SelectQuoteRequestDto
import com.fresh.procurement.data.remote.mapper.DemandMapper.toDomain
import com.fresh.procurement.data.remote.mapper.DemandMapper.toDomainGroupList
import com.fresh.procurement.data.remote.mapper.DemandMapper.toDomainList
import com.fresh.procurement.domain.model.Demand
import com.fresh.procurement.domain.model.DemandGroup
import com.fresh.procurement.domain.repository.DemandGroupDetailData
import com.fresh.procurement.domain.repository.DemandGroupItemData
import com.fresh.procurement.domain.repository.DemandRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 需求仓库实现类
 * 实现 Domain 层的 DemandRepository 接口
 */
@Singleton
class DemandRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : BaseRepository(), DemandRepository {

    /**
     * 发布需求
     */
    override suspend fun createDemand(
        categoryId: Long,
        productName: String,
        quantity: Double,
        unit: String?,
        maxPrice: Double?,
        qualityRequirement: String?,
        deliveryAddressId: Long,
        deliveryDate: String?,
        deliveryTimeSlot: String?,
        remark: String?
    ): Result<Demand> {
        return safeApiCall {
            apiService.createDemand(
                CreateDemandRequestDto(
                    categoryId = categoryId,
                    productName = productName,
                    quantity = quantity,
                    unit = unit,
                    maxPrice = maxPrice,
                    qualityRequirement = qualityRequirement,
                    deliveryAddressId = deliveryAddressId,
                    deliveryDate = deliveryDate,
                    deliveryTimeSlot = deliveryTimeSlot,
                    remark = remark
                )
            )
        }.map { it.toDomain() }
    }

    /**
     * 获取采购商的需求列表
     */
    override suspend fun getBuyerDemands(
        page: Int,
        size: Int,
        status: Int?
    ): Result<Pair<Int, List<Demand>>> {
        return safeApiCall {
            apiService.getBuyerDemands(page = page, size = size, status = status)
        }.map { response ->
            Pair(response.total, response.list.toDomainList())
        }
    }

    /**
     * 获取需求详情
     */
    override suspend fun getDemandDetail(demandId: Long): Result<Demand> {
        return safeApiCall {
            apiService.getDemandDetail(demandId)
        }.map { it.toDomain() }
    }

    /**
     * 选择报价
     */
    override suspend fun selectQuote(demandId: Long, quoteId: Long): Result<Demand> {
        return safeApiCall {
            apiService.selectQuote(
                demandId,
                SelectQuoteRequestDto(quoteId = quoteId)
            )
        }.map { it.toDomain() }
    }

    /**
     * 确认收货
     */
    override suspend fun confirmReceipt(
        demandId: Long,
        params: Map<String, Any>
    ): Result<Unit> {
        return safeApiCallEmpty {
            apiService.confirmReceipt(demandId, params)
        }
    }

    /**
     * 获取供应商的需求合并组列表
     */
    override suspend fun getDemandGroups(
        page: Int,
        size: Int,
        city: String?
    ): Result<Pair<Int, List<DemandGroup>>> {
        return safeApiCall {
            apiService.getDemandGroups(page = page, size = size, city = city)
        }.map { response ->
            Pair(response.total, response.list.toDomainGroupList())
        }
    }

    /**
     * 获取需求合并组详情
     */
    override suspend fun getDemandGroupDetail(groupId: Long): Result<DemandGroupDetailData> {
        return safeApiCall {
            apiService.getDemandGroupDetail(groupId)
        }.map { detail ->
            DemandGroupDetailData(
                groupId = detail.groupId,
                productName = detail.productName,
                city = detail.city,
                totalQuantity = detail.totalQuantity,
                unit = detail.unit,
                status = detail.status,
                demands = detail.demands.map { item ->
                    DemandGroupItemData(
                        demandId = item.demandId,
                        buyerId = item.buyerId,
                        buyerName = item.buyerName,
                        quantity = item.quantity,
                        maxPrice = item.maxPrice,
                        qualityRequirement = item.qualityRequirement,
                        deliveryAddress = item.deliveryAddress?.let { addr ->
                            buildString {
                                append(addr.province)
                                append(addr.city)
                                addr.district?.let { append(it) }
                                append(addr.detail)
                            }
                        },
                        deliveryDate = item.deliveryDate,
                        deliveryTimeSlot = item.deliveryTimeSlot
                    )
                }
            )
        }
    }

    /**
     * 获取供应商订单列表
     */
    override suspend fun getSupplierOrders(
        page: Int,
        size: Int,
        status: Int?
    ): Result<Pair<Int, List<Demand>>> {
        return safeApiCall {
            apiService.getSupplierOrders(page = page, size = size, status = status)
        }.map { response ->
            Pair(response.total, response.list.toDomainList())
        }
    }
}
