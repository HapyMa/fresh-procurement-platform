package com.fresh.procurement.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 打包记录 DTO
 */
data class PackRecordDto(
    @SerializedName("packRecordId")
    val packRecordId: Long,
    @SerializedName("demandId")
    val demandId: Long,
    @SerializedName("plannedQuantity")
    val plannedQuantity: Double,
    @SerializedName("actualQuantity")
    val actualQuantity: Double?,
    @SerializedName("actualWeight")
    val actualWeight: Double?,
    @SerializedName("weightDeviation")
    val weightDeviation: Double?,
    @SerializedName("grade")
    val grade: String?,
    @SerializedName("qualityCheck")
    val qualityCheck: Int?,
    @SerializedName("packageCount")
    val packageCount: Int?,
    @SerializedName("packageType")
    val packageType: String?,
    @SerializedName("labelCode")
    val labelCode: String?,
    @SerializedName("photos")
    val photos: List<String>?,
    @SerializedName("remark")
    val remark: String?,
    @SerializedName("status")
    val status: Int,
    @SerializedName("packedAt")
    val packedAt: String?,
    @SerializedName("packages")
    val packages: List<PackageInfoDto>?
)

/**
 * 包裹信息 DTO
 */
data class PackageInfoDto(
    @SerializedName("packageId")
    val packageId: Long,
    @SerializedName("packageNo")
    val packageNo: String,
    @SerializedName("labelCode")
    val labelCode: String?,
    @SerializedName("weight")
    val weight: Double?
)

/**
 * 完成打包请求 DTO
 */
data class CompletePackRequestDto(
    @SerializedName("actualQuantity")
    val actualQuantity: Double,
    @SerializedName("actualWeight")
    val actualWeight: Double,
    @SerializedName("grade")
    val grade: String?,
    @SerializedName("qualityCheck")
    val qualityCheck: Int,
    @SerializedName("packageCount")
    val packageCount: Int,
    @SerializedName("packageType")
    val packageType: String?,
    @SerializedName("labelCode")
    val labelCode: String,
    @SerializedName("remark")
    val remark: String?
)
