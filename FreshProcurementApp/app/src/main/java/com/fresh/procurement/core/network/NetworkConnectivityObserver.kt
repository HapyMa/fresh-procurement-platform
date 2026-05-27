package com.fresh.procurement.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络连接状态观察者
 * 监听网络状态变化并提供实时连接状态流
 */
@Singleton
class NetworkConnectivityObserver @Inject constructor(
    @ApplicationContext context: Context
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * 网络连接状态
     */
    enum class Status {
        AVAILABLE,
        UNAVAILABLE,
        LOSING,
        LOST
    }

    /**
     * 观察网络连接状态变化
     * @return 返回网络状态变化的 Flow
     */
    fun observe(): Flow<Status> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                launch { send(Status.AVAILABLE) }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                launch { send(Status.LOSING) }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                launch { send(Status.LOST) }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                launch { send(Status.UNAVAILABLE) }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // 发送初始状态
        val currentStatus = if (isNetworkAvailable()) Status.AVAILABLE else Status.UNAVAILABLE
        launch { send(currentStatus) }

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    /**
     * 检查当前网络是否可用
     * @return 如果网络可用返回 true
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * 检查当前是否使用 WiFi 网络
     * @return 如果使用 WiFi 返回 true
     */
    fun isWifiConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * 检查当前是否使用移动数据网络
     * @return 如果使用移动数据返回 true
     */
    fun isCellularConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
