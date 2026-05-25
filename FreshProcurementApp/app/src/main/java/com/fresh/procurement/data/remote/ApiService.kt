package com.fresh.procurement.data.remote

import com.fresh.procurement.data.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

/**
 * API 接口定义
 */
interface ApiService {

    // ==================== 认证模块 ====================
    
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<LoginResponse>>
    
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
    
    // ==================== 用户模块 ====================
    
    @GET("api/v1/user/profile")
    suspend fun getUserProfile(): Response<ApiResponse<User>>
    
    @GET("api/v1/user/addresses")
    suspend fun getUserAddresses(
        @Query("addressType") addressType: Int? = null
    ): Response<ApiResponse<List<UserAddress>>>
    
    @POST("api/v1/user/addresses")
    suspend fun addAddress(@Body address: UserAddress): Response<ApiResponse<UserAddress>>
    
    @DELETE("api/v1/user/addresses/{addressId}")
    suspend fun deleteAddress(@Path("addressId") addressId: Long): Response<ApiResponse<Unit>>
    
    // ==================== 采购商模块 ====================
    
    @POST("api/v1/buyer/demands")
    suspend fun createDemand(@Body request: CreateDemandRequest): Response<ApiResponse<Demand>>
    
    @GET("api/v1/buyer/demands")
    suspend fun getBuyerDemands(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("status") status: Int? = null
    ): Response<ApiResponse<DemandListResponse>>
    
    @GET("api/v1/buyer/demands/{demandId}")
    suspend fun getDemandDetail(@Path("demandId") demandId: Long): Response<ApiResponse<Demand>>
    
    @POST("api/v1/buyer/demands/{demandId}/select-quote")
    suspend fun selectQuote(
        @Path("demandId") demandId: Long,
        @Body request: SelectQuoteRequest
    ): Response<ApiResponse<Demand>>
    
    @POST("api/v1/buyer/demands/{demandId}/confirm-receipt")
    suspend fun confirmReceipt(
        @Path("demandId") demandId: Long,
        @Body params: Map<String, Any>
    ): Response<ApiResponse<Unit>>
    
    // ==================== 供应商模块 ====================
    
    @GET("api/v1/supplier/demand-groups")
    suspend fun getDemandGroups(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("city") city: String? = null
    ): Response<ApiResponse<DemandGroupListResponse>>
    
    @GET("api/v1/supplier/demand-groups/{groupId}")
    suspend fun getDemandGroupDetail(@Path("groupId") groupId: Long): Response<ApiResponse<DemandGroupDetail>>
    
    @POST("api/v1/supplier/demand-groups/{groupId}/quotes")
    suspend fun createQuote(
        @Path("groupId") groupId: Long,
        @Body request: CreateQuoteRequest
    ): Response<ApiResponse<Quote>>
    
    @GET("api/v1/supplier/quotes")
    suspend fun getMyQuotes(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("status") status: Int? = null
    ): Response<ApiResponse<QuoteListResponse>>
    
    @GET("api/v1/supplier/orders")
    suspend fun getSupplierOrders(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("status") status: Int? = null
    ): Response<ApiResponse<DemandListResponse>>
    
    // ==================== 分拣打包模块 ====================
    
    @GET("api/v1/supplier/pack/pending-list")
    suspend fun getPendingPackList(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<DemandListResponse>>
    
    @POST("api/v1/supplier/pack/{demandId}/start")
    suspend fun startPacking(@Path("demandId") demandId: Long): Response<ApiResponse<PackRecord>>
    
    @POST("api/v1/supplier/pack/{demandId}/complete")
    suspend fun completePacking(
        @Path("demandId") demandId: Long,
        @Body request: CompletePackRequest
    ): Response<ApiResponse<PackRecord>>
    
    @GET("api/v1/supplier/pack/{demandId}/record")
    suspend fun getPackRecord(@Path("demandId") demandId: Long): Response<ApiResponse<PackRecord>>
    
    @POST("api/v1/supplier/ship/{demandId}")
    suspend fun shipOrder(
        @Path("demandId") demandId: Long,
        @Body request: ShipRequest
    ): Response<ApiResponse<Unit>>
    
    // ==================== 公共模块 ====================
    
    @GET("api/v1/categories")
    suspend fun getCategories(@Query("parentId") parentId: Long = 0): Response<ApiResponse<List<Category>>>
}

/**
 * 商品分类
 */
data class Category(
    @SerializedName("id")
    val id: Long,
    @SerializedName("parentId")
    val parentId: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("iconUrl")
    val iconUrl: String?,
    @SerializedName("sortOrder")
    val sortOrder: Int,
    @SerializedName("status")
    val status: Int
)
