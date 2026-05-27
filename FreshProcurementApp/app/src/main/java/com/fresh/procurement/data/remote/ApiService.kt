package com.fresh.procurement.data.remote

import com.fresh.procurement.data.remote.dto.AdminDashboardDto
import com.fresh.procurement.data.remote.dto.AdminDemandItemDto
import com.fresh.procurement.data.remote.dto.AdminDemandListResponseDto
import com.fresh.procurement.data.remote.dto.AdminOrderStatsDto
import com.fresh.procurement.data.remote.dto.AdminQuoteListResponseDto
import com.fresh.procurement.data.remote.dto.AdminUserItemDto
import com.fresh.procurement.data.remote.dto.AdminUserListResponseDto
import com.fresh.procurement.data.remote.dto.ApiResponseDto
import com.fresh.procurement.data.remote.dto.CreateDemandRequestDto
import com.fresh.procurement.data.remote.dto.CreateQuoteRequestDto
import com.fresh.procurement.data.remote.dto.DemandDto
import com.fresh.procurement.data.remote.dto.DemandGroupDetailDto
import com.fresh.procurement.data.remote.dto.DemandGroupListResponseDto
import com.fresh.procurement.data.remote.dto.DemandListResponseDto
import com.fresh.procurement.data.remote.dto.LoginRequestDto
import com.fresh.procurement.data.remote.dto.LoginResponseDto
import com.fresh.procurement.data.remote.dto.CompletePackRequestDto
import com.fresh.procurement.data.remote.dto.PackRecordDto
import com.fresh.procurement.data.remote.dto.QuoteDto
import com.fresh.procurement.data.remote.dto.QuoteListResponseDto
import com.fresh.procurement.data.remote.dto.RegisterRequestDto
import com.fresh.procurement.data.remote.dto.SelectQuoteRequestDto
import com.fresh.procurement.data.remote.dto.ShipRequestDto
import com.fresh.procurement.data.remote.dto.UserAddressDto
import com.fresh.procurement.data.remote.dto.UserDto
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API 接口定义
 * 使用 DTO 进行网络请求和响应
 */
interface ApiService {

    // ==================== 认证模块 ====================

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<ApiResponseDto<LoginResponseDto>>

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<ApiResponseDto<LoginResponseDto>>

    // ==================== 用户模块 ====================

    @GET("api/v1/user/profile")
    suspend fun getUserProfile(): Response<ApiResponseDto<UserDto>>

    @GET("api/v1/user/addresses")
    suspend fun getUserAddresses(
        @Query("addressType") addressType: Int? = null
    ): Response<ApiResponseDto<List<UserAddressDto>>>

    @POST("api/v1/user/addresses")
    suspend fun addAddress(
        @Body address: UserAddressDto
    ): Response<ApiResponseDto<UserAddressDto>>

    @DELETE("api/v1/user/addresses/{addressId}")
    suspend fun deleteAddress(
        @Path("addressId") addressId: Long
    ): Response<ApiResponseDto<Unit>>

    // ==================== 采购商模块 ====================

    @POST("api/v1/buyer/demands")
    suspend fun createDemand(
        @Body request: CreateDemandRequestDto
    ): Response<ApiResponseDto<DemandDto>>

    @GET("api/v1/buyer/demands")
    suspend fun getBuyerDemands(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("status") status: Int? = null
    ): Response<ApiResponseDto<DemandListResponseDto>>

    @GET("api/v1/buyer/demands/{demandId}")
    suspend fun getDemandDetail(
        @Path("demandId") demandId: Long
    ): Response<ApiResponseDto<DemandDto>>

    @POST("api/v1/buyer/demands/{demandId}/select-quote")
    suspend fun selectQuote(
        @Path("demandId") demandId: Long,
        @Body request: SelectQuoteRequestDto
    ): Response<ApiResponseDto<DemandDto>>

    @POST("api/v1/buyer/demands/{demandId}/confirm-receipt")
    suspend fun confirmReceipt(
        @Path("demandId") demandId: Long,
        @Body params: Map<String, Any>
    ): Response<ApiResponseDto<Unit>>

    // ==================== 供应商模块 ====================

    @GET("api/v1/supplier/demand-groups")
    suspend fun getDemandGroups(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("city") city: String? = null
    ): Response<ApiResponseDto<DemandGroupListResponseDto>>

    @GET("api/v1/supplier/demand-groups/{groupId}")
    suspend fun getDemandGroupDetail(
        @Path("groupId") groupId: Long
    ): Response<ApiResponseDto<DemandGroupDetailDto>>

    @POST("api/v1/supplier/demand-groups/{groupId}/quotes")
    suspend fun createQuote(
        @Path("groupId") groupId: Long,
        @Body request: CreateQuoteRequestDto
    ): Response<ApiResponseDto<QuoteDto>>

    @GET("api/v1/supplier/quotes")
    suspend fun getMyQuotes(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("status") status: Int? = null
    ): Response<ApiResponseDto<QuoteListResponseDto>>

    @GET("api/v1/supplier/orders")
    suspend fun getSupplierOrders(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("status") status: Int? = null
    ): Response<ApiResponseDto<DemandListResponseDto>>

    // ==================== 分拣打包模块 ====================

    @GET("api/v1/supplier/pack/pending-list")
    suspend fun getPendingPackList(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<DemandListResponseDto>>

    @POST("api/v1/supplier/pack/{demandId}/start")
    suspend fun startPacking(
        @Path("demandId") demandId: Long
    ): Response<ApiResponseDto<PackRecordDto>>

    @POST("api/v1/supplier/pack/{demandId}/complete")
    suspend fun completePacking(
        @Path("demandId") demandId: Long,
        @Body request: CompletePackRequestDto
    ): Response<ApiResponseDto<PackRecordDto>>

    @GET("api/v1/supplier/pack/{demandId}/record")
    suspend fun getPackRecord(
        @Path("demandId") demandId: Long
    ): Response<ApiResponseDto<PackRecordDto>>

    @POST("api/v1/supplier/ship/{demandId}")
    suspend fun shipOrder(
        @Path("demandId") demandId: Long,
        @Body request: ShipRequestDto
    ): Response<ApiResponseDto<Unit>>

    // ==================== 公共模块 ====================

    @GET("api/v1/categories")
    suspend fun getCategories(
        @Query("parentId") parentId: Long = 0
    ): Response<ApiResponseDto<List<CategoryDto>>>

    // ==================== 管理员模块 ====================

    @GET("api/v1/admin/dashboard")
    suspend fun getAdminDashboard(): Response<ApiResponseDto<AdminDashboardDto>>

    @GET("api/v1/admin/users")
    suspend fun getAdminUsers(
        @Query("userType") userType: Int? = null,
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<AdminUserListResponseDto>>

    @GET("api/v1/admin/users/{userId}")
    suspend fun getAdminUserDetail(
        @Path("userId") userId: Long
    ): Response<ApiResponseDto<AdminUserItemDto>>

    @PUT("api/v1/admin/users/{userId}/toggle-status")
    suspend fun toggleUserStatus(
        @Path("userId") userId: Long
    ): Response<ApiResponseDto<AdminUserItemDto>>

    @GET("api/v1/admin/demands")
    suspend fun getAdminDemands(
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<AdminDemandListResponseDto>>

    @GET("api/v1/admin/demands/{demandId}")
    suspend fun getAdminDemandDetail(
        @Path("demandId") demandId: Long
    ): Response<ApiResponseDto<AdminDemandItemDto>>

    @PUT("api/v1/admin/demands/{demandId}/cancel")
    suspend fun cancelAdminDemand(
        @Path("demandId") demandId: Long
    ): Response<ApiResponseDto<AdminDemandItemDto>>

    @GET("api/v1/admin/quotes")
    suspend fun getAdminQuotes(
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<AdminQuoteListResponseDto>>

    @GET("api/v1/admin/order-stats")
    suspend fun getAdminOrderStats(): Response<ApiResponseDto<AdminOrderStatsDto>>
}

/**
 * 商品分类 DTO
 */
data class CategoryDto(
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

