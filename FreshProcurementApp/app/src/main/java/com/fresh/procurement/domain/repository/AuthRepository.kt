package com.fresh.procurement.domain.repository

import com.fresh.procurement.domain.model.LoginResult
import com.fresh.procurement.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(phone: String, password: String): Result<LoginResult>
    suspend fun register(phone: String, password: String, nickname: String, userType: Int): Result<User>
    suspend fun logout(): Result<Unit>
    fun observeCurrentUser(): Flow<User?>
    suspend fun getCurrentUser(): User?
}
