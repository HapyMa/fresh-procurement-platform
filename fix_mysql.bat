@echo off
setlocal EnableDelayedExpansion
title Fix MySQL Installation
color 0A

echo.
echo  ============================================
echo     Fix MySQL Installation
echo  ============================================
echo.

:: Find MySQL installation
set "MYSQL_BASE="
for /d %%i in ("C:\Program Files\MySQL\MySQL Server 8*") do set "MYSQL_BASE=%%i"
if "!MYSQL_BASE!"=="" for /d %%i in ("C:\Program Files (x86)\MySQL\MySQL Server 8*") do set "MYSQL_BASE=%%i"
if "!MYSQL_BASE!"=="" for /d %%i in ("C:\Program Files\MySQL\*") do set "MYSQL_BASE=%%i"
if "!MYSQL_BASE!"=="" for /d %%i in ("C:\Program Files (x86)\MySQL\*") do set "MYSQL_BASE=%%i"

if "!MYSQL_BASE!"=="" (
    echo  [FAIL] MySQL installation not found!
    echo.
    echo  Searching in common locations...
    echo.
    if exist "C:\Program Files\MySQL" (
        echo  Found: C:\Program Files\MySQL
        dir /b "C:\Program Files\MySQL"
    )
    if exist "C:\Program Files (x86)\MySQL" (
        echo  Found: C:\Program Files (x86)\MySQL
        dir /b "C:\Program Files (x86)\MySQL"
    )
    echo.
    echo  Please tell me the MySQL installation path.
    pause
    exit /b 1
)

echo  [OK] Found MySQL at: !MYSQL_BASE!
echo.

:: Add to PATH
set "PATH=!MYSQL_BASE!\bin;%PATH%"

:: Check if service exists
sc query MySQL80 >nul 2>&1
if %errorlevel% equ 0 (
    echo  [INFO] MySQL80 service already exists
    goto :start_service
)

echo  [1/4] Creating MySQL service...
"!MYSQL_BASE!\bin\mysqld" --install MySQL80 >nul 2>&1
if %errorlevel% neq 0 (
    echo  [WARN] Service creation failed, trying alternative...
    mysqld --install MySQL80 >nul 2>&1
)

echo.
echo  [2/4] Initializing MySQL data directory...
if not exist "!MYSQL_BASE!\data" (
    "!MYSQL_BASE!\bin\mysqld" --initialize-insecure --console >nul 2>&1
    echo  [OK] MySQL initialized
) else (
    echo  [OK] Data directory already exists
)

:start_service
echo.
echo  [3/4] Starting MySQL service...
net start MySQL80 >nul 2>&1
if %errorlevel% neq 0 (
    net start MySQL >nul 2>&1
    if %errorlevel% neq 0 (
        echo  [WARN] Could not start service automatically.
        echo         Please start MySQL manually from Services.msc
    ) else (
        echo  [OK] MySQL service started
    )
) else (
    echo  [OK] MySQL80 service started
)

echo.
echo  [4/4] Setting root password...
timeout /t 2 /nobreak >nul

mysql -u root -e "SELECT 1;" >nul 2>&1
if %errorlevel% equ 0 (
    mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'FreshProcurement2024';" >nul 2>&1
    echo  [OK] Root password set
) else (
    mysql -u root -pFreshProcurement2024 -e "SELECT 1;" >nul 2>&1
    if %errorlevel% equ 0 (
        echo  [OK] Password already set
    ) else (
        echo  [WARN] Cannot connect to MySQL
    )
)

echo.
mysql -u root -pFreshProcurement2024 -e "CREATE DATABASE IF NOT EXISTS fresh_procurement;" >nul 2>&1
if %errorlevel% equ 0 (
    echo  [OK] Database created
) else (
    echo  [WARN] Could not create database
)

echo.
echo  ============================================
echo     MySQL Fix Complete
echo  ============================================
echo.
pause
