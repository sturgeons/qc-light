@echo off
chcp 65001 >nul
echo ================================
echo 串口测试程序日志查看器
echo 按 Ctrl+C 退出
echo ================================
echo.

adb logcat -c
adb logcat -s SerialPort:I SerialPortHelper:I SerialPortManager:I MainActivity:I *:E

