@echo off
setlocal EnableDelayedExpansion
title Fresh Procurement - Quick Start
color 0A

echo.
echo  ====================================================
echo     Fresh Procurement - Quick Start
echo  ====================================================
echo.

set "DEPLOY_DIR=C:\FreshProcurement"
set "MYSQL_PWD=FreshProcurement2024"
set "JAR_NAME=fresh-backend-1.0.0.jar"
set "GH_URL=https://github.com/HapyMa/fresh-procurement-platform/releases/download/v1.0.0/%JAR_NAME%"
set "JAR_FILE=%DEPLOY_DIR%\fresh-backend.jar"

if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

:: ============================================
:: STEP 1: Check Java
:: ============================================
echo  [1/4] Checking Java...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo        [FAIL] Java not found!
    pause
    exit /b 1
)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do echo        [OK] %%v
echo.

:: ============================================
:: STEP 2: Check MySQL
:: ============================================
echo  [2/4] Checking MySQL...
set "MYSQL_BIN="
for /d %%i in ("C:\Program Files\MySQL\MySQL Server 8*") do set "MYSQL_BIN=%%i\bin"
if "!MYSQL_BIN!"=="" (
    echo        [FAIL] MySQL not found!
    pause
    exit /b 1
)
echo        [OK] !MYSQL_BIN!

sc query MySQL80 | findstr "RUNNING" >nul 2>&1
if %errorlevel% neq 0 (
    echo        [INFO] Starting MySQL80...
    net start MySQL80 >nul 2>&1
)
echo        [OK] MySQL80 is running
echo.

:: ============================================
:: STEP 3: Reset Database
:: ============================================
echo  [3/4] Resetting database...
"!MYSQL_BIN!\mysql" -u root -p%MYSQL_PWD% -e "DROP DATABASE IF EXISTS fresh_procurement; CREATE DATABASE fresh_procurement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" >nul 2>&1
if !errorlevel! equ 0 (
    echo        [OK] Database ready
) else (
    echo        [FAIL] Database error!
    pause
    exit /b 1
)
echo.

:: ============================================
:: STEP 4: Download JAR and Start
:: ============================================
echo  [4/4] Downloading JAR (~45MB)...
echo.

taskkill /f /im java.exe >nul 2>&1

set "TMP_JAR=%DEPLOY_DIR%\fresh-backend-new.jar"
set "DL_OK=0"

set MIRROR[0]=https://ghfast.top/%GH_URL%
set MIRROR[1]=https://mirror.ghproxy.com/%GH_URL%
set MIRROR[2]=https://gh-proxy.com/%GH_URL%
set MIRROR[3]=https://gh.api.99988866.xyz/%GH_URL%
set MIRROR[4]=%GH_URL%

set /a IDX=0
:dlloop
if !IDX! geq 5 goto dldone
set "CURR=!MIRROR[%IDX%]!"
set /a IDX+=1
echo        Mirror !IDX!/5...
del /f "%TMP_JAR%" >nul 2>&1

powershell -Command ^
  "$url='!CURR!'; $out='%TMP_JAR%'; "^
  "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; "^
  "$wc = New-Object System.Net.WebClient; "^
  "$done = $false; "^
  "$wc.add_DownloadProgressChanged({ "^
  "  if($args[0].TotalBytesToReceive -gt 0) { "^
  "    $pct = [math]::Round($args[0].BytesReceived / $args[0].TotalBytesToReceive * 100); "^
  "    $recv = [math]::Round($args[0].BytesReceived / 1MB, 1); "^
  "    $total = [math]::Round($args[0].TotalBytesToReceive / 1MB, 1); "^
  "    $filled = [math]::Floor($pct / 5); "^
  "    $empty = 20 - $filled; "^
  "    $bar = ([char]9608).ToString() * $filled + ([char]9617).ToString() * $empty; "^
  "    Write-Host ('`r        [' + $bar + '] ' + $pct + '%% (' + $recv + 'MB/' + $total + 'MB)') -NoNewline; "^
  "  } "^
  "}); "^
  "$wc.add_DownloadFileCompleted({ $script:done = $true }); "^
  "$wc.DownloadFileAsync($url, $out); "^
  "while(-not $done) { Start-Sleep -Milliseconds 200 }; "^
  "Write-Host ''; "^
  "if(Test-Path $out) { exit 0 } else { exit 1 }"

if !errorlevel! equ 0 (
    if exist "%TMP_JAR%" (
        for %%A in ("%TMP_JAR%") do set "FSIZE=%%~zA"
        if !FSIZE! gtr 40000000 (
            set "DL_OK=1"
            goto dldone
        ) else (
            echo        [WARN] File too small, skipping...
        )
    )
)
echo        [FAIL] Mirror !IDX! failed
goto dlloop

:dldone
if "!DL_OK!"=="0" (
    echo.
    echo        [FAIL] All mirrors failed!
    pause
    exit /b 1
)

if exist "%JAR_FILE%" del /f "%JAR_FILE%" >nul 2>&1
ren "%TMP_JAR%" "fresh-backend.jar" >nul 2>&1
echo        [OK] Downloaded !FSIZE! bytes
echo.

echo  ====================================================
echo     Starting Backend...
echo     URL: http://113.46.154.10:8080
echo     Admin: admin / admin123
echo     Press Ctrl+C to stop
echo  ====================================================
echo.

cd /d "%DEPLOY_DIR%"
java -jar fresh-backend.jar --spring.profiles.active=prod

pause
