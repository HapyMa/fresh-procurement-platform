package com.fresh.procurement.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 加密的 Token 管理器实现
 * 使用 EncryptedSharedPreferences 安全存储敏感信息
 */
@Singleton
class EncryptedTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenManager {

    companion object {
        private const val FILE_NAME = "encrypted_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveAccessToken(token: String) {
        withContext(Dispatchers.IO) {
            encryptedPrefs.edit {
                putString(KEY_ACCESS_TOKEN, token)
            }
        }
    }

    override suspend fun saveRefreshToken(token: String) {
        withContext(Dispatchers.IO) {
            encryptedPrefs.edit {
                putString(KEY_REFRESH_TOKEN, token)
            }
        }
    }

    override suspend fun saveUserRole(role: String) {
        withContext(Dispatchers.IO) {
            encryptedPrefs.edit {
                putString(KEY_USER_ROLE, role)
            }
        }
    }

    override suspend fun saveUserId(userId: Long) {
        withContext(Dispatchers.IO) {
            encryptedPrefs.edit {
                putLong(KEY_USER_ID, userId)
            }
        }
    }

    override suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {
            encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
        }
    }

    override fun getAccessTokenSync(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun getRefreshTokenSync(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun getRefreshToken(): String? {
        return withContext(Dispatchers.IO) {
            encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
        }
    }

    override suspend fun getUserRole(): String? {
        return withContext(Dispatchers.IO) {
            encryptedPrefs.getString(KEY_USER_ROLE, null)
        }
    }

    override suspend fun getUserId(): Long? {
        return withContext(Dispatchers.IO) {
            val userId = encryptedPrefs.getLong(KEY_USER_ID, -1L)
            if (userId == -1L) null else userId
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            encryptedPrefs.edit {
                remove(KEY_ACCESS_TOKEN)
                remove(KEY_REFRESH_TOKEN)
                remove(KEY_USER_ROLE)
                remove(KEY_USER_ID)
            }
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
