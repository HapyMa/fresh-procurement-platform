package com.fresh.procurement.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 报价状态
 */
enum class QuoteStatus(val value: Int, val text: String) {
    PENDING(0, "待审核"),
    VALID(1, "有效"),
    EXPIRED(2, "已过期"),
    REVOKED(3, "已撤销"),
    SELECTED(4, "已选中");

    companion object {
        fun fromValue(value: Int) = values().find { it.value == value } ?: PENDING
    }
}

/**
 * 报价信息 DTO
 */
data class QuoteDto(
    @SerializedName("quoteId")
    val quoteId: Long,
    @SerializedName("groupId")
    val groupId: Long,
    @SerializedName("supplierId")
    val supplierId: Long,
    @SerializedName("supplierName")
    val supplierName: String?,
    @SerializedName("supplierScore")
    val supplierScore: Double?,
    @SerializedName("unitPrice")
    val unitPrice: Double,
    @SerializedName("totalAmount")
    val totalAmount: Double?,
    @SerializedName("validHours")
    val validHours: Int,
    @SerializedName("expireAt")
    val expireAt: String?,
    @SerializedName("remark")
    val remark: String?,
    @SerializedName("status")
    val status: Int
)

/**
 * 提交报价请求 DTO
 */
data class CreateQuoteRequestDto(
    @SerializedName("unitPrice")
    val unitPrice: Double,
    @SerializedName("validHours")
    val validHours: Int = 24,
    @SerializedName("remark")
    val remark: String?
)

/**
 * 报价列表响应 DTO
 */
data class QuoteListResponseDto(
    @SerializedName("total")
    val total: Int,
    @SerializedName("list")
    val list: List<QuoteDto>
)
