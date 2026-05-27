package com.fresh.procurement.data.repository

import com.fresh.procurement.data.model.*
import com.fresh.procurement.data.remote.ApiService
import com.fresh.procurement.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun login(phone: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(phone, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    // 保存token
                    RetrofitClient.setToken(body.data.token)
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "登录失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        phone: String,
        password: String,
        userType: Int,
        nickname: String?
    ): Result<LoginResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(phone, password, userType, nickname)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess() == true && body.data != null) {
                    RetrofitClient.setToken(body.data.token)
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "注册失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        RetrofitClient.setToken(null)
    }
}
