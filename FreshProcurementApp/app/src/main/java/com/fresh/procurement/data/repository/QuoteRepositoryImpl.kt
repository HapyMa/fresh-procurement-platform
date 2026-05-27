package com.fresh.procurement.data.repository

import com.fresh.procurement.data.remote.ApiService
import com.fresh.procurement.data.remote.dto.CreateQuoteRequestDto
import com.fresh.procurement.data.remote.dto.QuoteDto
import com.fresh.procurement.domain.repository.QuoteRepository
import javax.inject.Inject

/**
 * 报价仓库实现类
 * 实现 Domain 层的 QuoteRepository 接口
 */
class QuoteRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : BaseRepository(), QuoteRepository {

    /**
     * 提交报价
     */
    override suspend fun createQuote(
        groupId: Long,
        unitPrice: Double,
        validHours: Int,
        remark: String?
    ): Result<QuoteDto> {
        return safeApiCall {
            apiService.createQuote(
                groupId,
                CreateQuoteRequestDto(
                    unitPrice = unitPrice,
                    validHours = validHours,
                    remark = remark
                )
            )
        }
    }

    /**
     * 获取我的报价列表
     */
    override suspend fun getMyQuotes(
        page: Int,
        size: Int,
        status: Int?
    ): Result<Pair<Int, List<QuoteDto>>> {
        return safeApiCall {
            apiService.getMyQuotes(page = page, size = size, status = status)
        }.map { response ->
            Pair(response.total, response.list)
        }
    }
}
