@echo off
chcp 65001 >nul
title 生鲜采购平台 - 一键部署脚本
color 0A

echo ============================================
echo    生鲜采购平台 后端一键部署
echo ============================================
echo.

:: ============================================
:: 第一步：检查 Java 环境
:: ============================================
echo [1/5] 检查 Java 环境...
java -version 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java！
    echo.
    echo 请先安装 JDK 17：
    echo   下载地址：https://adoptium.net/temurin/releases/?version=17
    echo   安装时勾选 "Set JAVA_HOME variable" 和 "Add to PATH"
    echo.
    pause
    exit /b 1
)
echo [OK] Java 已安装
echo.

:: ============================================
:: 第二步：下载并安装 MySQL
:: ============================================
echo [2/5] 检查 MySQL...
mysql --version 2>nul
if %errorlevel% neq 0 (
    echo [信息] 未检测到 MySQL，正在下载 MySQL 8.0 ...
    
    :: 下载 MySQL Installer
    curl -L -o "%TEMP%\mysql-installer.msi" "https://dev.mysql.com/get/Downloads/MySQLInstaller/mysql-installer-community-8.0.40.0.msi"
    
    if %errorlevel% neq 0 (
        echo [警告] 自动下载失败，请手动安装 MySQL 8.0
        echo   下载地址：https://dev.mysql.com/downloads/installer/
        echo   安装时请设置 root 密码为：FreshProcurement2024
        echo   安装完成后重新运行此脚本
        pause
        exit /b 1
    )
    
    echo [信息] 正在安装 MySQL（静默安装）...
    msiexec /i "%TEMP%\mysql-installer.msi" /qn INSTALLDIR="C:\Program Files\MySQL\MySQL Server 8.0" ^
        DATADIR="C:\ProgramData\MySQL\MySQL Server 8.0\Data" ^
        PORT=3306 ROOTPASSWORD=FreshProcurement2024
    
    :: 等待安装完成
    echo [信息] 等待 MySQL 安装完成（可能需要几分钟）...
    timeout /t 120 /nobreak >nul
    
    :: 添加 MySQL 到 PATH
    setx PATH "%PATH%;C:\Program Files\MySQL\MySQL Server 8.0\bin" >nul 2>&1
    
    echo [OK] MySQL 安装完成
) else (
    echo [OK] MySQL 已安装
)
echo.

:: ============================================
:: 第三步：启动 MySQL 并创建数据库
:: ============================================
echo [3/5] 配置 MySQL 数据库...

:: 启动 MySQL 服务
net start MySQL80 2>nul
if %errorlevel% neq 0 (
    net start MySQL 2>nul
)
if %errorlevel% neq 0 (
    echo [警告] 无法启动 MySQL 服务，请确认 MySQL 已正确安装
    pause
    exit /b 1
)
echo [OK] MySQL 服务已启动

:: 创建数据库
echo [信息] 创建数据库...
mysql -u root -pFreshProcurement2024 -e "CREATE DATABASE IF NOT EXISTS fresh_procurement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %errorlevel% neq 0 (
    echo [警告] 数据库创建失败，可能密码不同。请手动创建数据库 fresh_procurement
)
echo [OK] 数据库配置完成
echo.

:: ============================================
:: 第四步：部署后端应用
:: ============================================
echo [4/5] 部署后端应用...

:: 创建部署目录
set "DEPLOY_DIR=C:\FreshProcurement"
if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
if not exist "%DEPLOY_DIR%\logs" mkdir "%DEPLOY_DIR%\logs"

:: 检查 JAR 文件
if exist "%~dp0fresh-backend-1.0.0.jar" (
    copy /y "%~dp0fresh-backend-1.0.0.jar" "%DEPLOY_DIR%\fresh-backend.jar" >nul
    echo [OK] JAR 文件已复制到 %DEPLOY_DIR%
) else if exist "%DEPLOY_DIR%\fresh-backend.jar" (
    echo [OK] JAR 文件已存在
) else (
    echo [错误] 未找到 fresh-backend-1.0.0.jar
    echo 请将 JAR 文件放在脚本同目录下
    pause
    exit /b 1
)
echo.

:: ============================================
:: 第五步：启动后端服务
:: ============================================
echo [5/5] 启动后端服务...
echo.

:: 开放防火墙端口
netsh advfirewall firewall add rule name="FreshBackend" dir=in action=allow protocol=TCP localport=8080 >nul 2>&1

:: 启动应用
cd /d "%DEPLOY_DIR%"
echo [信息] 正在启动后端服务 (端口 8080)...
echo [信息] 启动日志：logs\startup.log
echo.
start /b "" java -jar fresh-backend.jar --spring.profiles.active=prod > logs\startup.log 2>&1

:: 等待启动
timeout /t 15 /nobreak >nul

:: 检查是否启动成功
curl -s http://localhost:8080/api/v1/categories >nul 2>&1
if %errorlevel% equ 0 (
    echo ============================================
    echo    部署成功！
    echo ============================================
    echo.
    echo    后端地址：http://localhost:8080
    echo    外网地址：http://113.46.154.10:8080
    echo    数据库：  fresh_procurement (MySQL)
    echo    日志目录：C:\FreshProcurement\logs
    echo.
    echo    常用命令：
    echo      查看日志：type C:\FreshProcurement\logs\startup.log
    echo      停止服务：taskkill /f /im java.exe
    echo      重启服务：重新运行此脚本
    echo.
) else (
    echo [警告] 服务可能还在启动中，请稍等片刻
    echo 查看日志：type C:\FreshProcurement\logs\startup.log
)

pause
