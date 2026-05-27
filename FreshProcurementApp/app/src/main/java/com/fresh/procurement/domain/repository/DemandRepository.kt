package com.fresh.procurement.domain.repository

import com.fresh.procurement.domain.model.Demand
import com.fresh.procurement.domain.model.DemandGroup

/**
 * 需求仓库接口
 * 提供采购商需求相关操作
 */
interface DemandRepository {

    /**
     * 发布需求
     * @param categoryId 商品分类ID
     * @param productName 商品名称
     * @param quantity 数量
     * @param unit 单位
     * @param maxPrice 最高价格
     * @param qualityRequirement 质量要求
     * @param deliveryAddressId 收货地址ID
     * @param deliveryDate 期望送达日期
     * @param deliveryTimeSlot 送达时间段
     * @param remark 备注
     * @return Result<Demand> 创建的需求
     */
    suspend fun createDemand(
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
    ): Result<Demand>

    /**
     * 获取采购商的需求列表
     * @param page 页码
     * @param size 每页数量
     * @param status 需求状态筛选
     * @return Result<Pair<Int, List<Demand>>> 总数和需求列表
     */
    suspend fun getBuyerDemands(
        page: Int = 1,
        size: Int = 20,
        status: Int? = null
    ): Result<Pair<Int, List<Demand>>>

    /**
     * 获取需求详情
     * @param demandId 需求ID
     * @return Result<Demand> 需求详情
     */
    suspend fun getDemandDetail(demandId: Long): Result<Demand>

    /**
     * 选择报价
     * @param demandId 需求ID
     * @param quoteId 报价ID
     * @return Result<Demand> 更新后的需求
     */
    suspend fun selectQuote(demandId: Long, quoteId: Long): Result<Demand>

    /**
     * 确认收货
     * @param demandId 需求ID
     * @param params 确认收货参数
     * @return Result<Unit>
     */
    suspend fun confirmReceipt(
        demandId: Long,
        params: Map<String, Any>
    ): Result<Unit>

    /**
     * 获取供应商的需求合并组列表
     * @param page 页码
     * @param size 每页数量
     * @param city 城市筛选
     * @return Result<Pair<Int, List<DemandGroup>>> 总数和合并组列表
     */
    suspend fun getDemandGroups(
        page: Int = 1,
        size: Int = 20,
        city: String? = null
    ): Result<Pair<Int, List<DemandGroup>>>

    /**
     * 获取需求合并组详情
     * @param groupId 合并组ID
     * @return Result<DemandGroupDetailData> 合并组详情
     */
    suspend fun getDemandGroupDetail(groupId: Long): Result<DemandGroupDetailData>

    /**
     * 获取供应商订单列表
     * @param page 页码
     * @param size 每页数量
     * @param status 状态筛选
     * @return Result<Pair<Int, List<Demand>>> 总数和订单列表
     */
    suspend fun getSupplierOrders(
        page: Int = 1,
        size: Int = 20,
        status: Int? = null
    ): Result<Pair<Int, List<Demand>>>
}

/**
 * 需求合并组详情数据
 */
data class DemandGroupDetailData(
    val groupId: Long,
    val productName: String,
    val city: String,
    val totalQuantity: Double,
    val unit: String?,
    val status: Int,
    val demands: List<DemandGroupItemData>
)

/**
 * 合并组中的需求项数据
 */
data class DemandGroupItemData(
    val demandId: Long,
    val buyerId: Long,
    val buyerName: String,
    val quantity: Double,
    val maxPrice: Double?,
    val qualityRequirement: String?,
    val deliveryAddress: String?,
    val deliveryDate: String?,
    val deliveryTimeSlot: String?
)
