package com.fresh.procurement.domain.usecase.admin

import com.fresh.procurement.data.model.AdminUserListResponse
import com.fresh.procurement.domain.repository.AdminRepository
import com.fresh.procurement.domain.error.AppError
import com.fresh.procurement.domain.error.toAppError
import com.fresh.procurement.domain.model.UserType
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(
        userType: UserType? = null,
        status: Int? = null,
        page: Int = 1,
        size: Int = 20
    ): Result<AdminUserListResponse> {
        // 验证分页参数
        if (page < 1) {
            return Result.failure(AppError.ValidationError("page"))
        }
        if (size < 1 || size > 100) {
            return Result.failure(AppError.ValidationError("size"))
        }

        // 验证状态参数
        status?.let {
            if (it !in 0..1) {
                return Result.failure(AppError.ValidationError("status"))
            }
        }

        return try {
            adminRepository.getUsers(
                userType = userType?.value,
                status = status,
                page = page,
                size = size
            ).fold(
                onSuccess = { (total, users) ->
                    Result.success(AdminUserListResponse(total = total, users = users))
                },
                onFailure = { throwable ->
                    Result.failure(throwable.toAppError())
                }
            )
        } catch (e: Exception) {
            Result.failure(e.toAppError())
        }
    }
}
