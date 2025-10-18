@echo off
chcp 65001 >nul
echo ================================
echo RS485/UART 串口测试程序
echo 编译和安装脚本
echo ================================
echo.

echo [1/4] 清理旧的构建文件...
call gradlew.bat clean

echo.
echo [2/4] 开始编译项目...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 编译失败！请检查错误信息。
    pause
    exit /b 1
)

echo.
echo [3/4] 检查设备连接...
adb devices

echo.
echo [4/4] 安装应用到设备...
adb install -r app\build\outputs\apk\debug\app-debug.apk

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 安装失败！请确保设备已连接并开启USB调试。
    pause
    exit /b 1
)

echo.
echo ✅ 应用安装成功！
echo.
echo 是否启动应用？(Y/N)
set /p choice=

if /i "%choice%"=="Y" (
    echo 正在启动应用...
    adb shell am start -n com.example.qc_1/.MainActivity
    echo.
    echo 是否查看日志？(Y/N)
    set /p logchoice=
    if /i "%logchoice%"=="Y" (
        echo 按 Ctrl+C 退出日志查看...
        adb logcat -s SerialPort:I SerialPortHelper:I SerialPortManager:I MainActivity:I *:E
    )
)

echo.
echo 完成！
pause

