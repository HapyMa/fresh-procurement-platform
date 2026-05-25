@echo off
setlocal enabledelayedexpansion
title Fresh Procurement Platform - Deploy
color 0A

echo.
echo  ========================================
echo    Fresh Procurement - Auto Deploy
echo  ========================================
echo.

set "DEPLOY_DIR=C:\FreshProcurement"
set "JAR_URL=https://github.com/HapyMa/fresh-procurement-platform/releases/download/v1.0.0/fresh-backend-1.0.0.jar"
set "JDK_URL=https://aka.ms/download-jdk/microsoft-jdk-17.0.13-windows-x64.msi"
set "MYSQL_URL=https://dev.mysql.com/get/Downloads/MySQLInstaller/mysql-installer-community-8.0.40.0.msi"
set "MYSQL_PWD=FreshProcurement2024"

if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
if not exist "%DEPLOY_DIR%\downloads" mkdir "%DEPLOY_DIR%\downloads"
if not exist "%DEPLOY_DIR%\logs" mkdir "%DEPLOY_DIR%\logs"

:: ============================================
:: STEP 1: Install JDK 17
:: ============================================
echo  [1/5] Checking Java...
java -version 2>nul | findstr "17" >nul
if %errorlevel% equ 0 (
    echo  [OK] Java 17 already installed
    goto :step2
)

echo  [DOWNLOAD] JDK 17 (~160MB)...
curl -L -o "%DEPLOY_DIR%\downloads\jdk17.msi" "%JDK_URL%"
if %errorlevel% neq 0 (
    echo  [ERROR] JDK download failed!
    pause
    exit /b 1
)

echo  [INSTALL] JDK 17 (silent, ~1 min)...
msiexec /i "%DEPLOY_DIR%\downloads\jdk17.msi" ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome /quiet /norestart
timeout /t 60 /nobreak >nul

set "PATH=%ProgramFiles%\Microsoft\jdk-17.0.13+11\bin;%PATH%"

java -version 2>nul | findstr "17" >nul
if %errorlevel% equ 0 (
    echo  [OK] JDK 17 installed
) else (
    echo  [WARN] JDK install may have failed, trying to continue...
)
echo.

:step2
:: ============================================
:: STEP 2: Install MySQL 8.0
:: ============================================
echo  [2/5] Checking MySQL...
mysql --version 2>nul
if %errorlevel% equ 0 (
    echo  [OK] MySQL already installed
    goto :step3
)

echo  [DOWNLOAD] MySQL 8.0 (~400MB, please wait)...
curl -L -o "%DEPLOY_DIR%\downloads\mysql-installer.msi" "%MYSQL_URL%"
if %errorlevel% neq 0 (
    echo  [ERROR] MySQL download failed!
    pause
    exit /b 1
)

echo  [INSTALL] MySQL 8.0 (silent, 3-5 min, DO NOT close this window)...
msiexec /i "%DEPLOY_DIR%\downloads\mysql-installer.msi" /quiet /norestart

echo  [WAIT] Waiting for MySQL install to complete...
set "MYSQL_READY=0"
for /l %%i in (1,1,36) do (
    if "!MYSQL_READY!"=="0" (
        sc query MySQL80 >nul 2>&1
        if !errorlevel! equ 0 set "MYSQL_READY=1"
        sc query MySQL >nul 2>&1
        if !errorlevel! equ 0 set "MYSQL_READY=1"
        if "!MYSQL_READY!"=="0" timeout /t 10 /nobreak >nul
    )
)

for /d %%i in ("%ProgramFiles%\MySQL\MySQL Server 8*") do (
    set "MYSQL_BIN=%%i\bin"
)
if defined MYSQL_BIN (
    set "PATH=!PATH!;!MYSQL_BIN!"
)

mysql --version 2>nul
if %errorlevel% equ 0 (
    echo  [OK] MySQL installed
) else (
    echo  [WARN] MySQL may not be fully installed
    echo         If next step fails, install MySQL 8.0 manually
    echo         Download: https://dev.mysql.com/downloads/installer/
    echo         Set root password to: %MYSQL_PWD%
)
echo.

:step3
:: ============================================
:: STEP 3: Create Database
:: ============================================
echo  [3/5] Configuring database...

net start MySQL80 >nul 2>&1
if %errorlevel% neq 0 net start MySQL >nul 2>&1

mysql -u root -p%MYSQL_PWD% -e "CREATE DATABASE IF NOT EXISTS fresh_procurement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %errorlevel% equ 0 (
    echo  [OK] Database fresh_procurement created
) else (
    echo.
    echo  [IMPORTANT] Auto database creation failed!
    echo.
    echo  Possible reasons:
    echo    1. MySQL root password is not %MYSQL_PWD%
    echo    2. MySQL service not running
    echo.
    echo  Please run manually in CMD:
    echo    mysql -u root -p
    echo    CREATE DATABASE fresh_procurement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    echo    exit;
    echo.
    echo  After creating database, press any key to continue...
    pause >nul
)
echo.

:: ============================================
:: STEP 4: Download Backend JAR
:: ============================================
echo  [4/5] Downloading backend (45MB)...

if exist "%DEPLOY_DIR%\fresh-backend.jar" (
    echo  [OK] Backend JAR already exists, skipping download
    goto :step5
)

curl -L -o "%DEPLOY_DIR%\fresh-backend.jar" "%JAR_URL%"
if %errorlevel% neq 0 (
    echo  [ERROR] Backend download failed!
    echo  Manual download: %JAR_URL%
    pause
    exit /b 1
)
echo  [OK] Backend JAR downloaded
echo.

:: ============================================
:: STEP 5: Start Service
:: ============================================
:step5
echo  [5/5] Starting backend service...

netsh advfirewall firewall add rule name="FreshBackend 8080" dir=in action=allow protocol=TCP localport=8080 >nul 2>&1
echo  [OK] Firewall port 8080 opened

taskkill /f /im java.exe >nul 2>&1
timeout /t 2 /nobreak >nul

cd /d "%DEPLOY_DIR%"
echo  [START] Backend service starting...
start /b "" java -jar fresh-backend.jar --spring.profiles.active=prod > logs\startup.log 2>&1

echo  [WAIT] Waiting for startup (~20 sec)...
timeout /t 20 /nobreak >nul

curl -s http://localhost:8080/api/v1/categories >nul 2>&1
if %errorlevel% equ 0 (
    echo.
    echo  ========================================
    echo     SUCCESS! Deploy complete!
    echo  ========================================
    echo.
    echo    URL:      http://113.46.154.10:8080
    echo    Admin:    admin / admin123
    echo.
    echo    Dir:      %DEPLOY_DIR%
    echo    Log:      %DEPLOY_DIR%\logs\startup.log
    echo.
    echo    Commands:
    echo      View log:  type %DEPLOY_DIR%\logs\startup.log
    echo      Stop:      taskkill /f /im java.exe
    echo      Restart:   re-run this script
    echo.
) else (
    echo.
    echo  [WARN] Service may still be starting, or failed to start
    echo  Check log: type %DEPLOY_DIR%\logs\startup.log
    echo.
    echo  === Last 20 lines of log ===
    powershell -Command "Get-Content '%DEPLOY_DIR%\logs\startup.log' -Tail 20" 2>nul
    echo  ================================
    echo.
)

pause
