@echo off
chcp 65001 >nul
echo ================================
echo 串口设备权限设置工具
echo ================================
echo.

echo 检查设备连接...
adb devices
echo.

echo 正在设置串口设备权限...
echo.

echo [1] 设置 RS485 (/dev/ttyS2) 权限
adb shell "su -c 'chmod 666 /dev/ttyS2'"
if %ERRORLEVEL% EQU 0 (
    echo ✅ RS485 权限设置成功
) else (
    echo ❌ RS485 权限设置失败 - 可能需要root权限
)

echo.
echo [2] 设置 UART (/dev/ttyUSB0) 权限
adb shell "su -c 'chmod 666 /dev/ttyUSB0'"
if %ERRORLEVEL% EQU 0 (
    echo ✅ UART 权限设置成功
) else (
    echo ❌ UART 权限设置失败 - 可能需要root权限
)

echo.
echo [3] 检查设备文件状态
echo.
echo RS485 (/dev/ttyS2):
adb shell "ls -l /dev/ttyS2"
echo.
echo UART (/dev/ttyUSB0):
adb shell "ls -l /dev/ttyUSB0"

echo.
echo 完成！
pause

