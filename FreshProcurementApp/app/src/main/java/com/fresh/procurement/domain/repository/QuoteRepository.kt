package com.fresh.procurement.domain.repository

import com.fresh.procurement.data.remote.dto.QuoteDto

/**
 * 报价仓库接口
 * 提供供应商报价相关操作
 */
interface QuoteRepository {

    /**
     * 提交报价
     * @param groupId 需求合并组ID
     * @param unitPrice 单价
     * @param validHours 报价有效时长（小时）
     * @param remark 备注
     * @return Result<QuoteDto> 创建的报价
     */
    suspend fun createQuote(
        groupId: Long,
        unitPrice: Double,
        validHours: Int = 24,
        remark: String? = null
    ): Result<QuoteDto>

    /**
     * 获取我的报价列表
     * @param page 页码
     * @param size 每页数量
     * @param status 状态筛选
     * @return Result<Pair<Int, List<QuoteDto>>> 总数和报价列表
     */
    suspend fun getMyQuotes(
        page: Int = 1,
        size: Int = 20,
        status: Int? = null
    ): Result<Pair<Int, List<QuoteDto>>>
}
