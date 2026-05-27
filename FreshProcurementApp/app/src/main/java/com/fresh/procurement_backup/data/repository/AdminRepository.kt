package com.fresh.procurement.data.repository

import com.fresh.procurement.data.model.*
import com.fresh.procurement.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getDashboard(): Result<AdminDashboard> {
        return try {
            val response = apiService.getAdminDashboard()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "获取仪表盘失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUsers(userType: Int?, status: Int?, page: Int, size: Int): Result<AdminUserListResponse> {
        return try {
            val response = apiService.getAdminUsers(userType, status, page, size)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "获取用户列表失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserDetail(userId: Long): Result<AdminUserItem> {
        return try {
            val response = apiService.getAdminUserDetail(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "获取用户详情失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleUserStatus(userId: Long): Result<AdminUserItem> {
        return try {
            val response = apiService.toggleUserStatus(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "操作失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDemands(status: Int?, page: Int, size: Int): Result<AdminDemandListResponse> {
        return try {
            val response = apiService.getAdminDemands(status, page, size)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "获取需求列表失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDemandDetail(demandId: Long): Result<AdminDemandItem> {
        return try {
            val response = apiService.getAdminDemandDetail(demandId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "获取需求详情失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelDemand(demandId: Long): Result<AdminDemandItem> {
        return try {
            val response = apiService.cancelAdminDemand(demandId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "取消需求失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getQuotes(status: Int?, page: Int, size: Int): Result<AdminQuoteListResponse> {
        return try {
            val response = apiService.getAdminQuotes(status, page, size)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "获取报价列表失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOrderStats(): Result<AdminOrderStats> {
        return try {
            val response = apiService.getAdminOrderStats()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "获取订单统计失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
