@echo off
setlocal
title Fresh Procurement - Verify Installation
color 0A

echo.
echo  ====================================================
echo     Fresh Procurement - Installation Verify
echo  ====================================================
echo.

set "DEPLOY_DIR=C:\FreshProcurement"
set "MYSQL_PWD=FreshProcurement2024"

set "ALL_OK=1"

:: ============================================
:: Check 1: Java
:: ============================================
echo  [1/6] Checking Java...
where java >nul 2>&1
if %errorlevel% equ 0 (
    java -version 2>&1 | findstr "17" >nul
    if %errorlevel% equ 0 (
        echo       [OK] Java 17 is installed
        java -version 2>&1 | findstr "version"
    ) else (
        echo       [WARN] Java found but not version 17
        set "ALL_OK=0"
    )
) else (
    echo       [FAIL] Java not found!
    set "ALL_OK=0"
)
echo.

:: ============================================
:: Check 2: MySQL Service
:: ============================================
echo  [2/6] Checking MySQL Service...
sc query MySQL80 >nul 2>&1
if %errorlevel% equ 0 (
    sc query MySQL80 | findstr "RUNNING" >nul
    if %errorlevel% equ 0 (
        echo       [OK] MySQL80 service is running
    ) else (
        echo       [WARN] MySQL80 service exists but not running
        echo              Try: net start MySQL80
    )
) else (
    sc query MySQL >nul 2>&1
    if %errorlevel% equ 0 (
        sc query MySQL | findstr "RUNNING" >nul
        if %errorlevel% equ 0 (
            echo       [OK] MySQL service is running
        ) else (
            echo       [WARN] MySQL service exists but not running
            echo              Try: net start MySQL
        )
    ) else (
        echo       [FAIL] MySQL service not found!
        set "ALL_OK=0"
    )
)
echo.

:: ============================================
:: Check 3: MySQL Connection
:: ============================================
echo  [3/6] Checking MySQL Connection...
for /d %%i in ("%ProgramFiles%\MySQL\MySQL Server 8*") do set "PATH=%PATH%;%%i\bin"
for /d %%i in ("%ProgramFiles(x86)%\MySQL\MySQL Server 8*") do set "PATH=%PATH%;%%i\bin"

mysql -u root -p%MYSQL_PWD% -e "SELECT 1;" >nul 2>&1
if %errorlevel% equ 0 (
    echo       [OK] MySQL connection successful
    
    echo  [4/6] Checking Database...
    mysql -u root -p%MYSQL_PWD% -e "SHOW DATABASES LIKE 'fresh_procurement';" 2>nul | findstr "fresh_procurement" >nul
    if %errorlevel% equ 0 (
        echo       [OK] Database 'fresh_procurement' exists
    ) else (
        echo       [WARN] Database 'fresh_procurement' not found!
        echo              Run: mysql -u root -p
        echo              CREATE DATABASE fresh_procurement;
        set "ALL_OK=0"
    )
) else (
    echo       [FAIL] Cannot connect to MySQL!
    echo              Check password or run MySQL installer to reset password.
    set "ALL_OK=0"
)
echo.

:: ============================================
:: Check 4: Backend JAR
:: ============================================
echo  [5/6] Checking Backend JAR...
if exist "%DEPLOY_DIR%\fresh-backend.jar" (
    for %%A in ("%DEPLOY_DIR%\fresh-backend.jar") do echo       [OK] Found: %%~zA bytes
) else (
    echo       [FAIL] Backend JAR not found!
    echo              Expected: %DEPLOY_DIR%\fresh-backend.jar
    set "ALL_OK=0"
)
echo.

:: ============================================
:: Check 5: Backend Service
:: ============================================
echo  [6/6] Checking Backend Service...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/categories' -UseBasicParsing -TimeoutSec 3; Write-Host '       [OK] Backend is running on port 8080'; exit 0 } catch { exit 1 }" >nul 2>&1
if %errorlevel% equ 0 (
    echo       [OK] API test passed
) else (
    echo       [WARN] Backend not responding on port 8080
    echo              Try: cd /d %DEPLOY_DIR% ^&^& java -jar fresh-backend.jar
    set "ALL_OK=0"
)
echo.

:: ============================================
:: Summary
:: ============================================
echo  ====================================================
if %ALL_OK% equ 1 (
    echo.
    echo     ALL CHECKS PASSED!
    echo.
    echo     Your backend is ready!
    echo     URL: http://113.46.154.10:8080
    echo     Admin: admin / admin123
    echo.
) else (
    echo.
    echo     SOME CHECKS FAILED
    echo     Please review the errors above.
    echo.
)
echo  ====================================================
echo.

:: Show helpful commands
echo  Useful Commands:
echo    Start MySQL:   net start MySQL80
echo    Start Backend: cd /d %DEPLOY_DIR% ^&^& java -jar fresh-backend.jar
necho    View Logs:     type %DEPLOY_DIR%\logs\startup.log
echo    MySQL Shell:   mysql -u root -p%MYSQL_PWD%
echo.

pause
