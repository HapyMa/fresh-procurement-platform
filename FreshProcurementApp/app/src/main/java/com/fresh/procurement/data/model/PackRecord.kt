package com.fresh.procurement.data.model

import com.google.gson.annotations.SerializedName

/**
 * 打包记录
 */
data class PackRecord(
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
    val packages: List<PackageInfo>?
) {
    fun getStatusEnum() = PackStatus.fromValue(status)
}

/**
 * 包裹信息
 */
data class PackageInfo(
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
 * 完成打包请求
 */
data class CompletePackRequest(
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

/**
 * 发货请求
 */
data class ShipRequest(
    @SerializedName("packageIds")
    val packageIds: List<Long>,
    @SerializedName("logisticsType")
    val logisticsType: Int,
    @SerializedName("logisticsCompany")
    val logisticsCompany: String?,
    @SerializedName("trackingNo")
    val trackingNo: String?,
    @SerializedName("estimatedArrival")
    val estimatedArrival: String?
)

/**
 * 物流类型
 */
enum class LogisticsType(val value: Int) {
    THIRD_PARTY(1),
    SELF_DELIVERY(2),
    BUYER_PICKUP(3)
}
