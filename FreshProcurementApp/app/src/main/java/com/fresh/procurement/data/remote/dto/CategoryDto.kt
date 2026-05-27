package com.fresh.procurement.data.remote.dto

import com.google.gson.annotations.SerializedName

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
