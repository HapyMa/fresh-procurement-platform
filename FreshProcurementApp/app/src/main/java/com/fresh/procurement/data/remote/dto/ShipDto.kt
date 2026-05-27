package com.fresh.procurement.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 发货请求 DTO
 */
data class ShipRequestDto(
    @SerializedName("logisticsCompany")
    val logisticsCompany: String,
    @SerializedName("trackingNo")
    val trackingNo: String
)
