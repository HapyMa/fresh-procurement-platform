package com.fresh.procurement.data.repository

import com.fresh.procurement.data.model.*
import com.fresh.procurement.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemandRepository @Inject constructor(
    private val apiService: ApiService
) {
    // ==================== 采购商操作 ====================
    
    suspend fun createDemand(request: CreateDemandRequest): Result<Demand> {
        return try {
            val response = apiService.createDemand(request)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBuyerDemands(page: Int, size: Int, status: Int?): Result<DemandListResponse> {
        return try {
            val response = apiService.getBuyerDemands(page, size, status)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDemandDetail(demandId: Long): Result<Demand> {
        return try {
            val response = apiService.getDemandDetail(demandId)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun selectQuote(demandId: Long, quoteId: Long): Result<Demand> {
        return try {
            val response = apiService.selectQuote(demandId, SelectQuoteRequest(quoteId))
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun confirmReceipt(demandId: Long, actualWeight: Double?, remark: String?): Result<Unit> {
        return try {
            val params = mutableMapOf<String, Any>()
            actualWeight?.let { params["actualWeight"] = it }
            remark?.let { params["remark"] = it }
            val response = apiService.confirmReceipt(demandId, params)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== 供应商操作 ====================
    
    suspend fun getDemandGroups(page: Int, size: Int, city: String?): Result<DemandGroupListResponse> {
        return try {
            val response = apiService.getDemandGroups(page, size, city)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDemandGroupDetail(groupId: Long): Result<DemandGroupDetail> {
        return try {
            val response = apiService.getDemandGroupDetail(groupId)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSupplierOrders(page: Int, size: Int, status: Int?): Result<DemandListResponse> {
        return try {
            val response = apiService.getSupplierOrders(page, size, status)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun <T> handleResponse(response: retrofit2.Response<ApiResponse<T>>): Result<T> {
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
