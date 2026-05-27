package com.fresh.procurement.data.model

import com.fresh.procurement.data.remote.dto.ApiResponseDto

/**
 * API 响应类型别名
 * 将旧代码中引用的 ApiResponse 映射到 ApiResponseDto
 */
typealias ApiResponse<T> = ApiResponseDto<T>
