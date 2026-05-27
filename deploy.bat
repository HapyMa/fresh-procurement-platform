@echo off
setlocal
title Fresh Procurement - Auto Deploy
color 0A

echo.
echo  ====================================================
echo     Fresh Procurement Platform - Auto Deploy v6.0
echo     MySQL MSI installer support
echo  ====================================================
echo.

set "DEPLOY_DIR=C:\FreshProcurement"
set "MYSQL_PWD=FreshProcurement2024"

set "RELEASE=https://github.com/HapyMa/fresh-procurement-platform/releases/download/v1.0.0"
set "JDK_URL=https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13+11/OpenJDK17U-jdk_x64_windows_hotspot_17.0.13_11.zip"
set "JAR_URL=%RELEASE%/fresh-backend-1.0.0.jar"

if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
if not exist "%DEPLOY_DIR%\downloads" mkdir "%DEPLOY_DIR%\downloads"
if not exist "%DEPLOY_DIR%\logs" mkdir "%DEPLOY_DIR%\logs"

:: ============================================
:: STEP 1: JDK 17
:: ============================================
echo.
echo  ====================================================
echo   STEP 1/5 : Install JDK 17
echo  ====================================================
echo.

where java >nul 2>&1
if %errorlevel% equ 0 (
    java -version 2>&1 | findstr "17" >nul
    if %errorlevel% equ 0 (
        echo  [OK] Java 17 already installed, skipping...
        goto step2
    )
)

echo  [INFO] Installing JDK 17 via winget...
winget install EclipseAdoptium.Temurin.17.JDK --accept-package-agreements --accept-source-agreements --silent >nul 2>&1
if %errorlevel% equ 0 (
    echo  [OK] JDK 17 installed via winget!
    for /d %%i in ("%LocalAppData%\Programs\Eclipse Adoptium\jdk-17*") do set "PATH=%%i\bin;%PATH%"
    goto step2
)

echo  [WARN] winget failed. Downloading from GitHub...
powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.DownloadFile('%JDK_URL%', '%DEPLOY_DIR%\downloads\jdk17.zip')"
if not exist "%DEPLOY_DIR%\downloads\jdk17.zip" (
    echo  [ERROR] JDK download failed!
    pause >nul
    exit /b 1
)
echo  [INFO] Extracting JDK...
powershell -Command "Expand-Archive -Path '%DEPLOY_DIR%\downloads\jdk17.zip' -DestinationPath '%DEPLOY_DIR%\jdk17' -Force"
for /d %%i in ("%DEPLOY_DIR%\jdk17\jdk-17*") do set "JDK_HOME=%%i"
if not defined JDK_HOME set "JDK_HOME=%DEPLOY_DIR%\jdk17\jdk-17.0.13+11"
set "PATH=%JDK_HOME%\bin;%PATH%"
echo  [OK] JDK 17 installed!
echo.

:step2
:: ============================================
:: STEP 2: MySQL 8.0 (MSI)
:: ============================================
echo  ====================================================
echo   STEP 2/5 : Install MySQL 8.0
echo  ====================================================
echo.

where mysql >nul 2>&1
if %errorlevel% equ 0 (
    echo  [OK] MySQL already installed, skipping...
    goto step3
)

:: Find MySQL MSI file
set "MSI_FILE="
if exist "%DEPLOY_DIR%\downloads\mysql*.msi" (
    for %%f in ("%DEPLOY_DIR%\downloads\mysql*.msi") do set "MSI_FILE=%%f"
)
if exist "%USERPROFILE%\Downloads\mysql*.msi" (
    for %%f in ("%USERPROFILE%\Downloads\mysql*.msi") do if not defined MSI_FILE set "MSI_FILE=%%f"
)
if exist "%USERPROFILE%\Desktop\mysql*.msi" (
    for %%f in ("%USERPROFILE%\Desktop\mysql*.msi") do if not defined MSI_FILE set "MSI_FILE=%%f"
)

if not defined MSI_FILE (
    echo  [ERROR] MySQL MSI file not found!
    echo.
    echo  Please put your mysql-installer-community-8.0.x.msi in one of:
    echo    - %DEPLOY_DIR%\downloads\
    echo    - %USERPROFILE%\Downloads\
    echo    - %USERPROFILE%\Desktop\
    echo.
    echo  Then re-run this script.
    pause >nul
    exit /b 1
)

echo  [INFO] Found MySQL MSI: %MSI_FILE%
echo  [INFO] Installing MySQL 8.0 (silent, ~3 minutes)...
echo.

msiexec /i "%MSI_FILE%" /quiet /norestart

echo  [INFO] Waiting for MySQL to be ready...
set /a cnt=0
:waitmysql
set /a cnt+=1
if %cnt% gtr 60 goto mysqltimeout
set /a "pct=cnt*100/60"
<nul set /p "=  Installing... [%pct%%]  "
sc query MySQL80 >nul 2>&1
if %errorlevel% equ 0 goto mysqlok
sc query MySQL >nul 2>&1
if %errorlevel% equ 0 goto mysqlok
timeout /t 5 /nobreak >nul
echo.
goto waitmysql
:mysqlok
echo.
echo  [OK] MySQL installed!

:: Add MySQL to PATH
for /d %%i in ("%ProgramFiles%\MySQL\MySQL Server 8*") do set "PATH=%PATH%;%%i\bin"
for /d %%i in ("%ProgramFiles(x86)%\MySQL\MySQL Server 8*") do set "PATH=%PATH%;%%i\bin"

:: Start MySQL
echo  [INFO] Starting MySQL service...
net start MySQL80 >nul 2>&1
if %errorlevel% neq 0 net start MySQL >nul 2>&1

:: Set root password (MSI may have set a default password or none)
echo  [INFO] Setting root password...
mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '%MYSQL_PWD%'; FLUSH PRIVILEGES;" 2>nul
if %errorlevel% neq 0 (
    echo  [WARN] Could not set password automatically.
    echo  [INFO] You may need to set the password manually after installation.
)
echo  [OK] MySQL 8.0 ready! Password: %MYSQL_PWD%
echo.
goto step3

:mysqltimeout
echo.
echo  [WARN] MySQL install timeout.
echo  [INFO] MySQL may still be installing in background.
echo  [INFO] If the next step fails, check MySQL service status.
echo.

:step3
:: ============================================
:: STEP 3: Create Database
:: ============================================
echo  ====================================================
echo   STEP 3/5 : Configure Database
echo  ====================================================
echo.

:: Try to start MySQL if not running
net start MySQL80 >nul 2>&1
if %errorlevel% neq 0 net start MySQL >nul 2>&1

echo  [INFO] Creating database: fresh_procurement ...

mysql -u root -p%MYSQL_PWD% -e "CREATE DATABASE IF NOT EXISTS fresh_procurement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %errorlevel% equ 0 (
    echo  [OK] Database created!
) else (
    echo.
    echo  [WARN] Auto create failed!
    echo  [INFO] MySQL root password may not be: %MYSQL_PWD%
    echo.
    echo  Please run manually:
    echo    mysql -u root -p
    echo    (enter your MySQL root password)
    echo    CREATE DATABASE fresh_procurement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    echo    exit;
    echo.
    echo  Press any key to continue...
    pause >nul
)
echo.

:: ============================================
:: STEP 4: Backend JAR
:: ============================================
echo  ====================================================
echo   STEP 4/5 : Download Backend
echo  ====================================================
echo.

if exist "%DEPLOY_DIR%\fresh-backend.jar" (
    echo  [OK] Backend already exists, skipping...
    goto step5
)

echo  [INFO] Downloading backend (~45MB)...
powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.DownloadFile('%JAR_URL%', '%DEPLOY_DIR%\fresh-backend.jar')"
if not exist "%DEPLOY_DIR%\fresh-backend.jar" (
    echo  [ERROR] Download failed!
    pause >nul
    exit /b 1
)
echo  [OK] Backend downloaded!
echo.

:: ============================================
:: STEP 5: Start Service
:: ============================================
:step5
echo  ====================================================
echo   STEP 5/5 : Start Backend Service
echo  ====================================================
echo.

netsh advfirewall firewall add rule name="FreshBackend 8080" dir=in action=allow protocol=TCP localport=8080 >nul 2>&1
taskkill /f /im java.exe >nul 2>&1
timeout /t 2 /nobreak >nul

cd /d "%DEPLOY_DIR%"
start /b "" java -jar fresh-backend.jar --spring.profiles.active=prod > logs\startup.log 2>&1

echo  [INFO] Waiting for startup...
set /a cnt=0
:waitstart
set /a cnt+=1
if %cnt% gtr 30 goto starttimeout
set /a "pct=cnt*100/30"
<nul set /p "=  Starting... [%pct%%]  "
powershell -Command "try { Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/categories' -UseBasicParsing -TimeoutSec 2 | Out-Null; exit 0 } catch { exit 1 }" >nul 2>&1
if %errorlevel% equ 0 goto started
timeout /t 1 /nobreak >nul
echo.
goto waitstart
:started
echo.
echo.
echo  ====================================================
echo.
echo     SUCCESS! Backend is running!
echo.
echo  ====================================================
echo.
echo  URL:        http://113.46.154.10:8080
echo  Admin:      admin / admin123
echo  Deploy Dir: %DEPLOY_DIR%
echo  Log:        %DEPLOY_DIR%\logs\startup.log
echo.
echo  Stop:  taskkill /f /im java.exe
echo  Stop DB: net stop MySQL80
echo.
echo  ====================================================
goto done

:starttimeout
echo.
echo  [WARN] Service not responding yet.
echo  Check log: type %DEPLOY_DIR%\logs\startup.log
echo.

:done
echo.
pause
