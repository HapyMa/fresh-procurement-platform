package com.fresh.procurement.data.repository

import com.fresh.procurement.data.model.*
import com.fresh.procurement.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getPendingPackList(page: Int, size: Int): Result<DemandListResponse> {
        return try {
            val response = apiService.getPendingPackList(page, size)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun startPacking(demandId: Long): Result<PackRecord> {
        return try {
            val response = apiService.startPacking(demandId)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
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
    ): Result<PackRecord> {
        return try {
            val request = CompletePackRequest(
                actualQuantity = actualQuantity,
                actualWeight = actualWeight,
                grade = grade,
                qualityCheck = qualityCheck,
                packageCount = packageCount,
                packageType = packageType,
                labelCode = labelCode,
                remark = remark
            )
            val response = apiService.completePacking(demandId, request)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPackRecord(demandId: Long): Result<PackRecord> {
        return try {
            val response = apiService.getPackRecord(demandId)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun shipOrder(
        demandId: Long,
        packageIds: List<Long>,
        logisticsType: Int,
        logisticsCompany: String?,
        trackingNo: String?,
        estimatedArrival: String?
    ): Result<Unit> {
        return try {
            val request = ShipRequest(
                packageIds = packageIds,
                logisticsType = logisticsType,
                logisticsCompany = logisticsCompany,
                trackingNo = trackingNo,
                estimatedArrival = estimatedArrival
            )
            val response = apiService.shipOrder(demandId, request)
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
