# =============================================================================
# 生鲜采购平台 - FreshProcurement Platform
# =============================================================================

## 项目简介

一个连接采购商与供应商的生鲜 B2B 撮合平台。

**核心功能**：
- 采购商发布需求 → 系统按城市+品类自动合并需求
- 供应商统一报价 → 合并展示，采购商选择最优报价
- 供应商按子订单分拣打包 → 单独发货到各采购商

## 技术栈

| 模块 | 技术 |
|------|------|
| Android 端 | Kotlin + Jetpack Compose + MVVM + Hilt + Retrofit |
| 后端 | Java 17 + Spring Boot 2.7 + Spring Data JPA |
| 数据库 | H2 (开发) / MySQL (生产) |
| 安全 | JWT + Spring Security + BCrypt |
| CI/CD | GitHub Actions |

## 项目结构

```
workspace/
├── FreshProcurementApp/          # Android 客户端
│   ├── .github/workflows/         # GitHub Actions
│   └── app/                     # App 模块
│
└── fresh-backend/               # Spring Boot 后端
    ├── .github/workflows/        # GitHub Actions
    ├── src/main/java/
    └── Dockerfile
```

## 快速开始

### 1. Fork 项目

点击 GitHub 页面右上角 **Fork** 按钮，Fork 到你自己的账号下。

### 2. 启用 GitHub Actions

Fork 完成后，Actions 会自动触发构建。
- 前往 **Actions** 页面查看构建状态
- 首次构建约需 5-10 分钟

### 3. 获取构建产物

#### Android APK
1. 构建完成后，进入 Actions 运行记录
2. 点击 `build-android` workflow
3. 下载 `app-debug-apk` artifact

#### 后端 JAR
1. 进入 Actions 运行记录
2. 点击 `build-backend` workflow
3. 下载 `backend-jar` artifact

### 4. 运行后端

```bash
# 直接运行 JAR
java -jar fresh-backend/build/libs/*.jar

# 或使用 Docker
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  your-docker-image:latest
```

### 5. 连接 Android 到后端

修改 `RetrofitClient.kt` 中的 BASE_URL 为后端实际地址：
```kotlin
private const val BASE_URL = "http://你的服务器IP:8080/"
```

## 测试账号

| 账号 | 手机号 | 密码 | 角色 |
|------|--------|------|------|
| 老王餐厅 | 13800138000 | 123456 | 采购商 |
| XX蔬菜批发 | 13900139000 | 123456 | 供应商 |

## CI/CD 流程

```
Push/PR
   ↓
┌─────────────────────────────────────────┐
│  Android CI                             │
│  ├─ Checkout                            │
│  ├─ Setup JDK 17                       │
│  ├─ Setup Android SDK                   │
│  ├─ Cache Gradle                        │
│  ├─ lintDebug (代码检查)                │
│  ├─ assembleDebug (构建 APK)            │
│  └─ Upload artifacts (APK)              │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Backend CI                             │
│  ├─ Checkout                            │
│  ├─ Setup JDK 17                        │
│  ├─ Cache Gradle                         │
│  ├─ test (单元测试)                     │
│  ├─ bootJar (构建 JAR)                  │
│  └─ Upload artifacts (JAR)               │
│                                         │
│  + Docker Build (仅 main 分支推送)       │
│  └─ Push to Docker Hub                  │
└─────────────────────────────────────────┘
```

## 配置 secrets (可选)

如需推送 Docker 镜像到 Docker Hub，在仓库 Settings → Secrets 中添加：

| Secret | 说明 |
|--------|------|
| `DOCKER_USERNAME` | Docker Hub 用户名 |
| `DOCKER_PASSWORD` | Docker Hub 密码或 Access Token |

## 环境变量说明

### 后端

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `default` | 激活的 Spring Profile |
| `JWT_SECRET` | (配置文件中的值) | JWT 签名密钥 |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:freshdb` | 数据库连接 |

### Android

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `BASE_URL` | `http://10.0.2.2:8080/` | API 地址（模拟器） |

## 分支策略

| 分支 | 说明 |
|------|------|
| `main` | 生产分支，发布版本 |
| `develop` | 开发分支，功能集成 |

## License

MIT License
