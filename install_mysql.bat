@echo off
setlocal EnableDelayedExpansion
title Install MySQL Server 8.0
color 0A

echo.
echo  ============================================
echo     Install MySQL Server 8.0 (ZIP)
echo  ============================================
echo.

set "MYSQL_HOME=C:\MySQL"
set "MYSQL_DATA=C:\MySQL\data"
set "MYSQL_PWD=FreshProcurement2024"
set "ZIP_URL=https://github.com/HapyMa/fresh-procurement-platform/releases/download/v1.0.0/mysql-8.0.44-winx64.zip"
set "ZIP_FILE=%TEMP%\mysql-8.0.44-winx64.zip"

:: Check if already installed
sc query MySQL80 >nul 2>&1
if %errorlevel% equ 0 (
    sc query MySQL80 | findstr "RUNNING" >nul 2>&1
    if %errorlevel% equ 0 (
        echo  [OK] MySQL80 is already running!
        goto :done
    )
)

:: Stop existing service if any
net stop MySQL80 >nul 2>&1
taskkill /f /im mysqld.exe >nul 2>&1

:: Clean old install
if exist "%MYSQL_HOME%" (
    echo  [INFO] Removing old MySQL directory...
    rmdir /s /q "%MYSQL_HOME%" >nul 2>&1
)

echo  [1/6] Downloading MySQL 8.0.44 (244MB)...
echo  [INFO] Please wait, this may take a few minutes...
echo.

powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.DownloadFile('%ZIP_URL%', '%ZIP_FILE%')"

if not exist "%ZIP_FILE%" (
    echo  [FAIL] Download failed!
    echo  [INFO] Please download manually from:
    echo  %ZIP_URL%
    pause
    exit /b 1
)

for %%A in ("%ZIP_FILE%") do echo  [OK] Downloaded: %%~zA bytes
echo.

echo  [2/6] Extracting MySQL...
powershell -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath 'C:\' -Force"
if not exist "%MYSQL_HOME%\bin\mysqld.exe" (
    echo  [FAIL] Extraction failed! mysqld.exe not found
    pause
    exit /b 1
)
echo  [OK] Extracted to %MYSQL_HOME%
echo.

echo  [3/6] Creating config file...
> "%MYSQL_HOME%\my.ini" (
    echo [mysqld]
    echo basedir=%MYSQL_HOME%
    echo datadir=%MYSQL_DATA%
    echo port=3306
    echo max_connections=200
    echo character-set-server=utf8mb4
    echo collation-server=utf8mb4_unicode_ci
    echo default-storage-engine=INNODB
    echo max_allowed_packet=64M
    echo.
    echo [client]
    echo port=3306
    echo default-character-set=utf8mb4
)
echo  [OK] Config created
echo.

echo  [4/6] Initializing MySQL...
"%MYSQL_HOME%\bin\mysqld" --initialize-insecure --console
if %errorlevel% neq 0 (
    echo  [FAIL] Initialization failed!
    pause
    exit /b 1
)
echo  [OK] MySQL initialized
echo.

echo  [5/6] Installing MySQL service...
"%MYSQL_HOME%\bin\mysqld" --install MySQL80 --defaults-file="%MYSQL_HOME%\my.ini"
if %errorlevel% neq 0 (
    echo  [WARN] Install failed, removing and retrying...
    "%MYSQL_HOME%\bin\mysqld" --remove MySQL80 >nul 2>&1
    "%MYSQL_HOME%\bin\mysqld" --install MySQL80 --defaults-file="%MYSQL_HOME%\my.ini"
)
echo  [OK] Service installed
echo.

echo  [6/6] Starting MySQL...
net start MySQL80
if %errorlevel% neq 0 (
    echo  [FAIL] Could not start MySQL!
    pause
    exit /b 1
)
echo  [OK] MySQL started!
echo.

:: Wait for MySQL to be ready
timeout /t 3 /nobreak >nul

:: Set root password
echo  Setting root password...
"%MYSQL_HOME%\bin\mysql" -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '%MYSQL_PWD%'; FLUSH PRIVILEGES;" >nul 2>&1
if %errorlevel% equ 0 (
    echo  [OK] Password set to: %MYSQL_PWD%
) else (
    echo  [WARN] Could not set password
)

:: Create database
echo  Creating database...
"%MYSQL_HOME%\bin\mysql" -u root -p%MYSQL_PWD% -e "CREATE DATABASE IF NOT EXISTS fresh_procurement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" >nul 2>&1
if %errorlevel% equ 0 (
    echo  [OK] Database 'fresh_procurement' created
) else (
    echo  [WARN] Could not create database
)

:: Add to system PATH
echo.
echo  Adding MySQL to system PATH...
powershell -Command "[Environment]::SetEnvironmentVariable('Path', [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';%MYSQL_HOME%\bin', 'Machine')"
echo  [OK] PATH updated (restart CMD to take effect)

:: Cleanup
del /f "%ZIP_FILE%" >nul 2>&1

:done
echo.
echo  ============================================
echo     MySQL Installation Complete!
echo  ============================================
echo.
echo  Install Path: %MYSQL_HOME%
echo  Data Path:    %MYSQL_DATA%
echo  Port:         3306
echo  Root Password: %MYSQL_PWD%
echo  Database:     fresh_procurement
echo.
echo  Next: Run verify.bat to check
echo.
pause
