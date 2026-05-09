@echo off
setlocal enabledelayedexpansion
title EasyMD - Build APK

echo ============================================================
echo  EasyMD Android - 编译 APK
echo ============================================================
echo.

:: ── 1. 检测 JAVA_HOME ───────────────────────────────────────
if "%JAVA_HOME%"=="" (
    echo [警告] JAVA_HOME 未设置，尝试使用 PATH 中的 java...
    where java >nul 2>&1
    if errorlevel 1 (
        echo [错误] 未找到 Java。请安装 JDK 17+ 或设置 JAVA_HOME。
        pause & exit /b 1
    )
)

:: ── 2. 检测 Android SDK ──────────────────────────────────────
if not exist "local.properties" (
    echo [信息] 未找到 local.properties，正在创建...
    set "SDK_PATH=%LOCALAPPDATA%\Android\Sdk"
    if not exist "!SDK_PATH!" (
        echo [错误] 未找到 Android SDK。
        echo        请先安装 Android Studio：https://developer.android.com/studio
        echo        或手动创建 local.properties 并设置 sdk.dir 路径。
        pause & exit /b 1
    )
    :: Escape backslashes for properties file
    set "SDK_ESCAPED=!SDK_PATH:\=\\!"
    echo sdk.dir=!SDK_ESCAPED!> local.properties
    echo [信息] local.properties 已生成：sdk.dir=!SDK_PATH!
)

echo.
echo [步骤 1/2] 编译 Debug APK（首次运行会下载依赖，约需 5~15 分钟）...
echo.

call gradlew.bat assembleDebug --stacktrace 2>&1
if errorlevel 1 (
    echo.
    echo [错误] 编译失败，请查看上方错误信息。
    pause & exit /b 1
)

:: ── 3. 安装 ─────────────────────────────────────────────────
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
if not exist "%APK_PATH%" (
    echo [错误] APK 未生成：%APK_PATH%
    pause & exit /b 1
)

echo.
echo ============================================================
echo  编译成功！APK 路径：
echo  %~dp0%APK_PATH%
echo ============================================================
echo.

:: Check adb
where adb >nul 2>&1
if errorlevel 1 (
    echo [提示] 未找到 adb，跳过自动安装。
    echo        请手动将 APK 传输到手机并安装。
    echo        或将 Android SDK\platform-tools 加入 PATH 后重新运行。
    start "" "%~dp0%APK_PATH%"
    pause & exit /b 0
)

:: Check device
adb devices 2>&1 | findstr /v "List" | findstr "device" >nul
if errorlevel 1 (
    echo [提示] 未检测到已连接的设备。
    echo        请确认：
    echo          1. 手机已通过 USB 连接
    echo          2. 手机已开启「开发者选项」→「USB 调试」
    echo          3. 手机屏幕上点击「允许调试」
    echo.
    echo        APK 已保存至：%~dp0%APK_PATH%
    echo        您可以手动将此文件传到手机安装。
    pause & exit /b 0
)

echo [步骤 2/2] 安装 APK 到手机...
adb install -r "%APK_PATH%"
if errorlevel 1 (
    echo [错误] 安装失败，请检查手机是否允许安装未知来源 APK。
    pause & exit /b 1
)

echo.
echo [完成] 简墨已成功安装到手机！
adb shell monkey -p com.easymd -c android.intent.category.LAUNCHER 1 >nul 2>&1
echo       应用已自动启动。
pause
