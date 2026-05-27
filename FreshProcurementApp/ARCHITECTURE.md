# FreshProcurementApp 架构设计文档

## 1. 概述

### 1.1 项目背景
FreshProcurementApp 是一个生鲜采购平台 Android 应用，支持三种用户角色：采购商、供应商和管理员。

### 1.2 设计目标
- **安全性**：HTTPS 传输、Token 加密存储
- **可维护性**：Clean Architecture 分层架构
- **可测试性**：依赖注入、接口隔离
- **用户体验**：统一状态管理、优雅的错误处理

### 1.3 技术栈
- **UI**：Jetpack Compose + Material3
- **架构**：MVVM + Clean Architecture
- **依赖注入**：Hilt
- **网络**：Retrofit2 + OkHttp3
- **状态管理**：StateFlow
- **本地存储**：DataStore + EncryptedSharedPreferences

---

## 2. 架构设计

### 2.1 Clean Architecture 分层

```
┌─────────────────────────────────────────────────────────────┐
│                        Presentation Layer                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │  Screen  │  │ ViewModel│  │   State  │  │  Event   │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                          Domain Layer                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ UseCase  │  │  Model   │  │Repository│  │  Error   │    │
│  │  (业务)   │  │  (领域)   │  │Interface │  │  Type    │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                           Data Layer                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │Repository│  │   API    │  │  Local   │  │  Mapper  │    │
│  │Impl      │  │ Service  │  │ Storage  │  │          │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 模块职责

| 层级 | 职责 | 包含内容 |
|------|------|----------|
| **Presentation** | UI 展示、用户交互 | Screen、ViewModel、State、Event |
| **Domain** | 业务逻辑、领域模型 | UseCase、Domain Model、Repository Interface |
| **Data** | 数据获取、存储实现 | Repository Impl、API、Local Storage、DTO |

---

## 3. 核心设计

### 3.1 统一状态管理

```kotlin
// 通用 UI 状态密封类
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val error: AppError) : UiState<Nothing>()
}

// 使用示例
class BuyerHomeViewModel @Inject constructor(
    private val getBuyerDemandsUseCase: GetBuyerDemandsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<BuyerHomeData>>(UiState.Loading)
    val uiState: StateFlow<UiState<BuyerHomeData>> = _uiState.asStateFlow()
    
    fun loadDemands() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getBuyerDemandsUseCase()
                .onSuccess { data -> _uiState.value = UiState.Success(data) }
                .onFailure { error -> _uiState.value = UiState.Error(error.toAppError()) }
        }
    }
}
```

### 3.2 错误处理体系

```kotlin
// 应用错误类型
sealed class AppError(val messageResId: Int) {
    // 网络错误
    class NetworkError(code: Int) : AppError(R.string.error_network)
    class NoInternet : AppError(R.string.error_no_internet)
    class Timeout : AppError(R.string.error_timeout)
    
    // 服务器错误
    class ServerError(message: String) : AppError(R.string.error_server)
    class Unauthorized : AppError(R.string.error_unauthorized)
    class Forbidden : AppError(R.string.error_forbidden)
    
    // 业务错误
    class ValidationError(field: String) : AppError(R.string.error_validation)
    class NotFound : AppError(R.string.error_not_found)
    
    // 未知错误
    class Unknown(message: String) : AppError(R.string.error_unknown)
}

// 异常转换扩展
fun Throwable.toAppError(): AppError = when (this) {
    is IOException -> AppError.NoInternet()
    is HttpException -> when (code()) {
        401 -> AppError.Unauthorized()
        403 -> AppError.Forbidden()
        404 -> AppError.NotFound()
        in 500..599 -> AppError.ServerError(message())
        else -> AppError.NetworkError(code())
    }
    is JsonParseException -> AppError.ServerError("数据解析失败")
    else -> AppError.Unknown(message ?: "未知错误")
}
```

### 3.3 网络层设计

```kotlin
// 网络状态监听
interface NetworkConnectivityObserver {
    fun observe(): Flow<Status>
    enum class Status { Available, Unavailable, Losing, Lost }
}

// 安全的 API 调用
suspend fun <T> safeApiCall(
    connectivityObserver: NetworkConnectivityObserver,
    apiCall: suspend () -> T
): Result<T> {
    // 检查网络状态
    // 添加重试机制
    // 统一异常转换
}

// Retrofit 配置
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenManager: TokenManager,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
```

### 3.4 Token 管理

```kotlin
// Token 管理接口
interface TokenManager {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    fun observeToken(): Flow<String?>
}

// 加密存储实现
class EncryptedTokenManager @Inject constructor(
    @ApplicationContext context: Context
) : TokenManager {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // 实现 TokenManager 接口...
}
```

### 3.5 Repository 基类

```kotlin
// Repository 基类
abstract class BaseRepository(
    private val connectivityObserver: NetworkConnectivityObserver
) {
    protected suspend fun <T> safeCall(
        apiCall: suspend () -> T
    ): Result<T> = safeApiCall(connectivityObserver, apiCall)
    
    protected fun <T, R> Response<ApiResponse<T>>.handleResponse(
        mapper: (T) -> R
    ): Result<R> {
        return if (isSuccessful) {
            body()?.let { response ->
                if (response.isSuccess() && response.data != null) {
                    Result.success(mapper(response.data))
                } else {
                    Result.failure(AppError.ServerError(response.message))
                }
            } ?: Result.failure(AppError.ServerError("响应为空"))
        } else {
            Result.failure(AppError.NetworkError(code()))
        }
    }
}

// 具体实现
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    connectivityObserver: NetworkConnectivityObserver,
    private val tokenManager: TokenManager
) : BaseRepository(connectivityObserver), AuthRepository {
    
    override suspend fun login(phone: String, password: String): Result<User> {
        return safeCall {
            apiService.login(LoginRequest(phone, password))
        }.map { response ->
            tokenManager.saveToken(response.token)
            response.toDomainModel()
        }
    }
}
```

---

## 4. 目录结构

```
com.fresh.procurement/
├── FreshApplication.kt
├── MainActivity.kt
│
├── common/                          # 公共组件
│   ├── components/                  # 共享 UI 组件
│   │   ├── LoadingButton.kt
│   │   ├── ErrorView.kt
│   │   ├── EmptyView.kt
│   │   ├── InfoRow.kt
│   │   └── StatusChip.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── utils/
│       ├── DateUtils.kt
│       └── ValidationUtils.kt
│
├── core/                            # 核心基础设施
│   ├── di/                          # 依赖注入模块
│   │   ├── NetworkModule.kt
│   │   ├── DatabaseModule.kt
│   │   └── RepositoryModule.kt
│   ├── network/                     # 网络相关
│   │   ├── ApiService.kt
│   │   ├── NetworkConnectivityObserver.kt
│   │   ├── SafeApiCall.kt
│   │   └── AuthInterceptor.kt
│   └── security/                    # 安全相关
│       └── TokenManager.kt
│
├── domain/                          # 领域层
│   ├── model/                       # 领域模型
│   │   ├── User.kt
│   │   ├── Demand.kt
│   │   ├── DemandGroup.kt
│   │   ├── Quote.kt
│   │   └── PackRecord.kt
│   ├── repository/                  # Repository 接口
│   │   ├── AuthRepository.kt
│   │   ├── DemandRepository.kt
│   │   ├── QuoteRepository.kt
│   │   └── AdminRepository.kt
│   ├── usecase/                     # 用例
│   │   ├── auth/
│   │   │   ├── LoginUseCase.kt
│   │   │   ├── RegisterUseCase.kt
│   │   │   └── LogoutUseCase.kt
│   │   ├── buyer/
│   │   │   ├── GetBuyerDemandsUseCase.kt
│   │   │   ├── CreateDemandUseCase.kt
│   │   │   └── CancelDemandUseCase.kt
│   │   ├── supplier/
│   │   │   ├── GetDemandGroupsUseCase.kt
│   │   │   ├── SubmitQuoteUseCase.kt
│   │   │   └── PackOrderUseCase.kt
│   │   └── admin/
│   │       ├── GetDashboardDataUseCase.kt
│   │       ├── GetAllUsersUseCase.kt
│   │       └── GetAllDemandsUseCase.kt
│   └── error/                       # 错误类型
│       └── AppError.kt
│
├── data/                            # 数据层
│   ├── remote/                      # 远程数据源
│   │   ├── dto/                     # 数据传输对象
│   │   │   ├── AuthDto.kt
│   │   │   ├── DemandDto.kt
│   │   │   └── AdminDto.kt
│   │   └── mapper/                  # DTO 转换器
│   │       ├── AuthMapper.kt
│   │       └── DemandMapper.kt
│   ├── local/                       # 本地数据源
│   │   └── datastore/
│   │       └── UserPreferences.kt
│   └── repository/                  # Repository 实现
│       ├── BaseRepository.kt
│       ├── AuthRepositoryImpl.kt
│       ├── DemandRepositoryImpl.kt
│       └── AdminRepositoryImpl.kt
│
├── presentation/                    # 展示层
│   ├── navigation/
│   │   ├── Screen.kt
│   │   └── AppNavigation.kt
│   │
│   ├── auth/                        # 认证模块
│   │   ├── AuthViewModel.kt
│   │   ├── AuthState.kt
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt
│   │
│   ├── buyer/                       # 采购商模块
│   │   ├── home/
│   │   │   ├── BuyerHomeViewModel.kt
│   │   │   ├── BuyerHomeState.kt
│   │   │   └── BuyerHomeScreen.kt
│   │   ├── demand/
│   │   │   ├── CreateDemandViewModel.kt
│   │   │   └── CreateDemandScreen.kt
│   │   └── detail/
│   │       ├── DemandDetailViewModel.kt
│   │       └── DemandDetailScreen.kt
│   │
│   ├── supplier/                    # 供应商模块
│   │   ├── home/
│   │   │   ├── SupplierHomeViewModel.kt
│   │   │   ├── SupplierHomeState.kt
│   │   │   └── SupplierHomeScreen.kt
│   │   ├── quote/
│   │   │   ├── QuoteViewModel.kt
│   │   │   └── SubmitQuoteScreen.kt
│   │   └── pack/
│   │       ├── PackViewModel.kt
│   │       ├── PackOrderScreen.kt
│   │       └── ShipOrderScreen.kt
│   │
│   └── admin/                       # 管理模块
│       ├── dashboard/
│       │   ├── AdminDashboardViewModel.kt
│       │   ├── AdminDashboardState.kt
│       │   └── AdminDashboardScreen.kt
│       ├── users/
│       │   ├── AdminUsersViewModel.kt
│       │   ├── AdminUsersState.kt
│       │   ├── AdminUsersScreen.kt
│       │   └── AdminUserDetailScreen.kt
│       └── demands/
│           ├── AdminDemandsViewModel.kt
│           ├── AdminDemandsState.kt
│           ├── AdminDemandsScreen.kt
│           └── AdminDemandDetailScreen.kt
│
└── widget/                          # 小组件（可选）
    └── QuickActionWidget.kt
```

---

## 5. 关键设计决策

### 5.1 为什么使用 UseCase？

| 优势 | 说明 |
|------|------|
| **单一职责** | 每个 UseCase 只做一件事 |
| **可测试** | 业务逻辑与框架解耦 |
| **可复用** | 多个 ViewModel 可共享 UseCase |
| **可组合** | UseCase 可组合成复杂业务逻辑 |

### 5.2 为什么使用 UiState<T>？

| 优势 | 说明 |
|------|------|
| **类型安全** | 编译时确保状态完整性 |
| ** exhaustive** | when 表达式强制处理所有状态 |
| **简洁** | 一个状态变量管理所有 UI 状态 |

### 5.3 为什么使用 EncryptedSharedPreferences？

| 方案 | 安全性 | 复杂度 | 推荐度 |
|------|--------|--------|--------|
| SharedPreferences | ❌ 明文存储 | 低 | ⭐ |
| DataStore | ❌ 明文存储 | 中 | ⭐⭐ |
| EncryptedSharedPreferences | ✅ AES256 加密 | 中 | ⭐⭐⭐⭐⭐ |
| Keystore + 自定义加密 | ✅ 最高安全 | 高 | ⭐⭐⭐⭐ |

---

## 6. 开发规范

### 6.1 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 类名 | PascalCase | `UserRepository`, `LoginUseCase` |
| 函数名 | camelCase | `getUserById()`, `handleLogin()` |
| 常量 | SCREAMING_SNAKE_CASE | `BASE_URL`, `TIMEOUT_SECONDS` |
| 资源文件 | snake_case | `ic_launcher.png`, `strings.xml` |

### 6.2 代码组织原则

1. **单一职责**：一个类只做一件事
2. **依赖倒置**：依赖接口而非实现
3. **最小暴露**：使用 `internal` 限制模块可见性
4. **不可变性**：优先使用 `val` 和不可变集合

### 6.3 错误处理规范

```kotlin
// ✅ 正确：统一错误处理
viewModelScope.launch {
    _uiState.value = UiState.Loading
    useCase()
        .onSuccess { data -> _uiState.value = UiState.Success(data) }
        .onFailure { error -> _uiState.value = UiState.Error(error.toAppError()) }
}

// ❌ 错误：分散的错误处理
try {
    val data = api.getData()
    _uiState.value = UiState.Success(data)
} catch (e: Exception) {
    when (e) {
        is IOException -> // 处理 IO
        is HttpException -> // 处理 HTTP
    }
}
```

---

## 7. 后续优化方向

1. **分页加载**：使用 Paging 3 实现无限滚动
2. **离线模式**：Room 数据库缓存
3. **图片优化**：Coil 图片加载缓存
4. **性能监控**：Firebase Performance
5. **崩溃报告**：Firebase Crashlytics

---

## 8. 附录

### 8.1 依赖版本

```kotlin
// 核心
composeBom = "2023.10.01"
hilt = "2.48"
retrofit = "2.9.0"
okhttp = "4.12.0"
coroutines = "1.7.3"

// 安全
securityCrypto = "1.1.0-alpha06"

// 存储
datastore = "1.0.0"
```

### 8.2 参考文档

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Jetpack Compose Best Practices](https://developer.android.com/jetpack/compose/api-best-practices)
