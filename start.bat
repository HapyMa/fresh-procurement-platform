@echo off
setlocal EnableDelayedExpansion
title Fresh Procurement - Final Setup
color 0A

echo.
echo  ============================================
echo     Fresh Procurement - Final Setup
echo  ============================================
echo.

set "DEPLOY_DIR=C:\FreshProcurement"
set "MYSQL_PWD=FreshProcurement2024"
set "MYSQL_BIN="
for /d %%i in ("C:\Program Files\MySQL\MySQL Server 8*") do set "MYSQL_BIN=%%i\bin"
if "!MYSQL_BIN!"=="" for /d %%i in ("C:\Program Files (x86)\MySQL\MySQL Server 8*") do set "MYSQL_BIN=%%i\bin"

if "!MYSQL_BIN!"=="" (
    echo  [FAIL] MySQL not found!
    pause
    exit /b 1
)

echo  [OK] MySQL found: !MYSQL_BIN!
echo.

:: ============================================
:: Step 1: Check MySQL Service
:: ============================================
echo  [1/3] Checking MySQL service...
sc query MySQL80 | findstr "RUNNING" >nul 2>&1
if %errorlevel% equ 0 (
    echo       [OK] MySQL80 is running
) else (
    echo       [INFO] Starting MySQL80...
    net start MySQL80 >nul 2>&1
    if %errorlevel% neq 0 (
        echo       [FAIL] Cannot start MySQL80!
        pause
        exit /b 1
    )
    echo       [OK] MySQL80 started
)
echo.

:: ============================================
:: Step 2: Create Database
:: ============================================
echo  [2/3] Creating database...
timeout /t 2 /nobreak >nul
"!MYSQL_BIN!\mysql" -u root -p!MYSQL_PWD! -e "CREATE DATABASE IF NOT EXISTS fresh_procurement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" >nul 2>&1
if %errorlevel% equ 0 (
    echo       [OK] Database 'fresh_procurement' created
) else (
    echo       [WARN] Failed with password, trying without password...
    "!MYSQL_BIN!\mysql" -u root -e "CREATE DATABASE IF NOT EXISTS fresh_procurement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" >nul 2>&1
    if %errorlevel% equ 0 (
        echo       [OK] Database created
    ) else (
        echo       [FAIL] Cannot create database! Check MySQL password.
        pause
        exit /b 1
    )
)
echo.

:: ============================================
:: Step 3: Start Backend
:: ============================================
echo  [3/3] Starting backend...
if not exist "%DEPLOY_DIR%\fresh-backend.jar" (
    echo       [FAIL] Backend JAR not found at %DEPLOY_DIR%\fresh-backend.jar
    pause
    exit /b 1
)

echo       [OK] Found fresh-backend.jar
echo.
echo  ============================================
echo     Starting Backend Service...
echo     Press Ctrl+C to stop
echo     URL: http://113.46.154.10:8080
echo  ============================================
echo.

cd /d "%DEPLOY_DIR%"
java -jar fresh-backend.jar --spring.profiles.active=prod

pause
