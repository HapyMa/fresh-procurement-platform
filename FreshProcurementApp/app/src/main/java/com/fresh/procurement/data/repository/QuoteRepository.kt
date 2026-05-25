package com.fresh.procurement.data.repository

import com.fresh.procurement.data.model.*
import com.fresh.procurement.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun createQuote(groupId: Long, unitPrice: Double, validHours: Int, remark: String?): Result<Quote> {
        return try {
            val response = apiService.createQuote(
                groupId,
                CreateQuoteRequest(unitPrice, validHours, remark)
            )
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMyQuotes(page: Int, size: Int, status: Int?): Result<QuoteListResponse> {
        return try {
            val response = apiService.getMyQuotes(page, size, status)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun <T> handleResponse(response: Response<ApiResponse<T>>): Result<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body?.isSuccess() == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "请求失败"))
            }
        } else {
            Result.failure(Exception("网络请求失败: ${response.code()}"))
        }
    }
}
