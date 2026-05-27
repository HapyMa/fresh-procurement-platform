package com.fresh.procurement.core.di

import android.content.Context
import com.fresh.procurement.BuildConfig
import com.fresh.procurement.core.network.AuthInterceptor
import com.fresh.procurement.core.network.CommonHeadersInterceptor
import com.fresh.procurement.core.network.TokenRefreshInterceptor
import com.fresh.procurement.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络模块
 * 提供 OkHttpClient、Retrofit、ApiService 等网络相关依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://113.46.154.10:8080/"
    private const val TIMEOUT_SECONDS = 30L
    private const val CACHE_SIZE = 10 * 1024 * 1024L // 10 MB
    private const val CACHE_DIR_NAME = "http_cache"

    /**
     * 提供 Http 缓存
     */
    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        return Cache(cacheDir, CACHE_SIZE)
    }

    /**
     * 提供日志拦截器
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * 提供 OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor,
        commonHeadersInterceptor: CommonHeadersInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // 设置超时
            connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

            // 设置缓存
            cache(cache)

            // 添加拦截器（注意顺序）
            // 1. 日志拦截器（最内层）
            addInterceptor(loggingInterceptor)

            // 2. 公共请求头拦截器
            addInterceptor(commonHeadersInterceptor)

            // 3. 认证拦截器
            addInterceptor(authInterceptor)

            // 4. Token 刷新拦截器（最外层，处理 401 响应）
            addInterceptor(tokenRefreshInterceptor)

            // 重试配置
            retryOnConnectionFailure(true)
        }.build()
    }

    /**
     * 提供 Retrofit
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * 提供 ApiService
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
