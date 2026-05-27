package com.fresh.procurement.core.di

import com.fresh.procurement.core.security.EncryptedTokenManager
import com.fresh.procurement.core.security.TokenManager
import com.fresh.procurement.data.repository.AdminRepository
import com.fresh.procurement.data.repository.AuthRepository
import com.fresh.procurement.data.repository.DemandRepository
import com.fresh.procurement.data.repository.PackRepository
import com.fresh.procurement.data.repository.QuoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 模块
 * 绑定 Repository 和 TokenManager 接口实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * 绑定 TokenManager 接口到 EncryptedTokenManager 实现
     */
    @Binds
    @Singleton
    abstract fun bindTokenManager(
        encryptedTokenManager: EncryptedTokenManager
    ): TokenManager

    /**
     * 绑定 AuthRepository
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepository: AuthRepository
    ): AuthRepository

    /**
     * 绑定 AdminRepository
     */
    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepository: AdminRepository
    ): AdminRepository

    /**
     * 绑定 DemandRepository
     */
    @Binds
    @Singleton
    abstract fun bindDemandRepository(
        demandRepository: DemandRepository
    ): DemandRepository

    /**
     * 绑定 QuoteRepository
     */
    @Binds
    @Singleton
    abstract fun bindQuoteRepository(
        quoteRepository: QuoteRepository
    ): QuoteRepository

    /**
     * 绑定 PackRepository
     */
    @Binds
    @Singleton
    abstract fun bindPackRepository(
        packRepository: PackRepository
    ): PackRepository
}
