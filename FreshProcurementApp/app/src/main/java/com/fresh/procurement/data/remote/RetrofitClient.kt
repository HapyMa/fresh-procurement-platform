package com.fresh.procurement.data.remote

import com.fresh.procurement.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端
 */
object RetrofitClient {
    
    // 本地开发后端地址（真机测试时改为电脑的局域网IP，如 http://192.168.1.100:8080/）
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val TIMEOUT = 30L
    
    private var authToken: String? = null
    
    fun setToken(token: String?) {
        authToken = token
    }
    
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            readTimeout(TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            
            // 添加日志拦截器
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
            
            // 添加认证拦截器
            addInterceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    authToken?.let { token ->
                        addHeader("Authorization", "Bearer $token")
                    }
                    addHeader("Accept", "application/json")
                    addHeader("Content-Type", "application/json")
                }.build()
                chain.proceed(request)
            }
        }.build()
    }
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
