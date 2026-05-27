package com.fresh.procurement.core.di

import com.fresh.procurement.core.security.EncryptedTokenManager
import com.fresh.procurement.core.security.TokenManager
import com.fresh.procurement.data.repository.AdminRepositoryImpl
import com.fresh.procurement.data.repository.AuthRepositoryImpl
import com.fresh.procurement.data.repository.DemandRepositoryImpl
import com.fresh.procurement.data.repository.PackRepositoryImpl
import com.fresh.procurement.data.repository.QuoteRepositoryImpl
import com.fresh.procurement.domain.repository.AdminRepository
import com.fresh.procurement.domain.repository.AuthRepository
import com.fresh.procurement.domain.repository.DemandRepository
import com.fresh.procurement.domain.repository.PackRepository
import com.fresh.procurement.domain.repository.QuoteRepository
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
     * 绑定 AuthRepository 接口到 AuthRepositoryImpl 实现
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * 绑定 AdminRepository 接口到 AdminRepositoryImpl 实现
     */
    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository

    /**
     * 绑定 DemandRepository 接口到 DemandRepositoryImpl 实现
     */
    @Binds
    @Singleton
    abstract fun bindDemandRepository(
        demandRepositoryImpl: DemandRepositoryImpl
    ): DemandRepository

    /**
     * 绑定 QuoteRepository 接口到 QuoteRepositoryImpl 实现
     */
    @Binds
    @Singleton
    abstract fun bindQuoteRepository(
        quoteRepositoryImpl: QuoteRepositoryImpl
    ): QuoteRepository

    /**
     * 绑定 PackRepository 接口到 PackRepositoryImpl 实现
     */
    @Binds
    @Singleton
    abstract fun bindPackRepository(
        packRepositoryImpl: PackRepositoryImpl
    ): PackRepository
}
