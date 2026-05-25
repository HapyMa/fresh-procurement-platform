# GitHub 上传指南

## 方法一：使用 GitHub 网页上传（最简单）

### 步骤 1：在 GitHub 上创建仓库

1. 登录 [GitHub](https://github.com)
2. 点击右上角 **+** → **New repository**
3. 填写仓库名称：`fresh-procurement-platform`
4. 选择 **Private**（私有）或 **Public**（公开）
5. 点击 **Create repository**

### 步骤 2：上传文件

在创建好的仓库页面，点击 **uploading an existing file**，将 `workspace` 文件夹下的所有内容拖拽上传：

```
FreshProcurementApp/
├── app/
├── gradlew
├── gradlew.bat
├── gradle/
├── .github/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties

fresh-backend/
├── src/
├── gradlew
├── gradlew.bat
├── gradle/
├── build.gradle
├── settings.gradle
├── Dockerfile
├── .github/
└── gradle/

README.md
.gitignore
.gitattributes
```

### 步骤 3：提交并查看 Actions

1. 点击 **Commit changes**
2. 进入 **Actions** 标签页
3. 等待构建完成（约 5-10 分钟）
4. 点击构建记录 → 下载 APK

---

## 方法二：使用 Git 命令行

### 前提条件

已安装 Git：[https://git-scm.com/downloads](https://git-scm.com/downloads)

### 步骤 1：初始化 Git 仓库

```bash
cd workspace
git init
git add .
git commit -m "Initial commit: 生鲜采购平台 v1.0"
```

### 步骤 2：连接远程仓库

```bash
git remote add origin https://github.com/你的用户名/fresh-procurement-platform.git
git branch -M main
git push -u origin main
```

### 步骤 3：查看 Actions

```bash
# 查看构建状态
gh run list

# 查看构建详情
gh run view <run-id>

# 下载 APK
gh run download <run-id> -n app-debug-apk -D ./apk-output/
```

---

## 方法三：使用 GitHub CLI

```bash
# 安装 GitHub CLI
# macOS: brew install gh
# Windows: winget install GitHub.cli

# 登录
gh auth login

# 创建仓库并推送
gh repo create fresh-procurement-platform --public --push
```

---

## 构建完成后的操作

### 1. 下载 APK

1. 进入仓库 → **Actions** 页面
2. 点击 `build-android` workflow 的运行记录
3. 在 **Artifacts** 区域，点击 `app-debug-apk` 下载

### 2. 部署后端

构建完成后，下载 `backend-jar` artifact：

```bash
java -jar fresh-backend/build/libs/fresh-backend-1.0.0.jar
```

后端启动后访问：`http://localhost:8080`

### 3. 连接 Android 到后端

修改 `RetrofitClient.kt`：

```kotlin
// 真机测试（需要手机和电脑在同一局域网）
private const val BASE_URL = "http://192.168.1.100:8080/"

// 模拟器测试（10.0.2.2 = 模拟器的宿主机地址）
private const val BASE_URL = "http://10.0.2.2:8080/"
```

### 4. 配置 Docker 推送（可选）

如需自动构建 Docker 镜像，在仓库 Settings → **Secrets and variables** → **Actions** 中添加：

| Name | Value |
|------|-------|
| `DOCKER_USERNAME` | 你的 Docker Hub 用户名 |
| `DOCKER_PASSWORD` | 你的 Docker Hub 密码或 Access Token |

---

## 常见问题

### Q: Actions 显示失败怎么办？

1. 点击失败的 workflow 运行记录
2. 查看 **build-android** 或 **build-backend** 的日志
3. 常见错误：
   - 网络超时 → 重试
   - 依赖下载失败 → 检查网络
   - 权限问题 → 检查 secrets 配置

### Q: 如何触发重新构建？

- 推送新的 commit
- 进入 Actions → 点击触发器 → **Re-run all jobs**

### Q: 如何查看构建历史？

仓库 → **Insights** → **Traffic** → **Views** 或 **Actions**
