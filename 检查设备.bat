@echo off
chcp 65001 >nul
echo ================================
echo 串口设备检查工具
echo ================================
echo.

echo [1] 检查ADB设备连接
adb devices
echo.

echo [2] 检查所有串口设备
echo.
adb shell "ls -l /dev/tty*"
echo.

echo [3] 检查RS485设备 (/dev/ttyS2)
adb shell "ls -l /dev/ttyS2 2>&1"
if %ERRORLEVEL% EQU 0 (
    echo ✅ RS485 设备存在
) else (
    echo ❌ RS485 设备不存在
)
echo.

echo [4] 检查UART设备 (/dev/ttyUSB0)
adb shell "ls -l /dev/ttyUSB0 2>&1"
if %ERRORLEVEL% EQU 0 (
    echo ✅ UART 设备存在
) else (
    echo ❌ UART 设备不存在
)
echo.

echo [5] 检查所有USB设备
echo.
adb shell "ls -l /dev/ttyUSB*"
echo.

echo [6] 检查串口驱动信息
echo.
adb shell "cat /proc/tty/driver/serial"
echo.

echo [7] 检查应用是否已安装
adb shell "pm list packages | grep qc_1"
if %ERRORLEVEL% EQU 0 (
    echo ✅ 应用已安装
) else (
    echo ❌ 应用未安装
)
echo.

echo 完成！
pause

