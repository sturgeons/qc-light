package com.example.qc_1;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * 串口管理类
 * 管理RS485和UART两个串口
 */
public class SerialPortManager {
    private static final String TAG = "SerialPortManager";
    
    // 设备路径
    public static final String RS485_DEVICE = "/dev/ttyS2";
    public static final String UART_DEVICE = "/dev/ttyUSB0";
    
    private SerialPortHelper rs485Port;
    private SerialPortHelper uartPort;
    
    private Handler mainHandler;
    private DataCallback dataCallback;
    
    public interface DataCallback {
        void onRS485DataReceived(String data);
        void onUartDataReceived(String data);
        void onError(String error);
    }
    
    public SerialPortManager() {
        mainHandler = new Handler(Looper.getMainLooper());
        rs485Port = new SerialPortHelper();
        uartPort = new SerialPortHelper();
    }
    
    /**
     * 打开RS485串口
     */
    public boolean openRS485(int baudRate) {
        try {
            boolean success = rs485Port.open(RS485_DEVICE, baudRate);
            if (success) {
                rs485Port.setOnDataReceiveListener(new SerialPortHelper.OnDataReceiveListener() {
                    @Override
                    public void onDataReceived(byte[] buffer, int size) {
                        String data = bytesToHexString(buffer, size);
                        String text = "HEX: " + data + "\nASCII: " + new String(buffer, 0, size);
                        
                        mainHandler.post(() -> {
                            if (dataCallback != null) {
                                dataCallback.onRS485DataReceived(text);
                            }
                        });
                    }
                });
                Log.i(TAG, "RS485打开成功，波特率: " + baudRate);
            } else {
                notifyError("RS485打开失败");
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "RS485打开异常", e);
            notifyError("RS485打开异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 打开UART串口
     */
    public boolean openUart(int baudRate) {
        try {
            boolean success = uartPort.open(UART_DEVICE, baudRate);
            if (success) {
                uartPort.setOnDataReceiveListener(new SerialPortHelper.OnDataReceiveListener() {
                    @Override
                    public void onDataReceived(byte[] buffer, int size) {
                        String data = bytesToHexString(buffer, size);
                        String text = "HEX: " + data + "\nASCII: " + new String(buffer, 0, size);
                        
                        mainHandler.post(() -> {
                            if (dataCallback != null) {
                                dataCallback.onUartDataReceived(text);
                            }
                        });
                    }
                });
                Log.i(TAG, "UART打开成功，波特率: " + baudRate);
            } else {
                notifyError("UART打开失败");
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "UART打开异常", e);
            notifyError("UART打开异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 关闭RS485
     */
    public void closeRS485() {
        if (rs485Port != null) {
            rs485Port.close();
            Log.i(TAG, "RS485已关闭");
        }
    }
    
    /**
     * 关闭UART
     */
    public void closeUart() {
        if (uartPort != null) {
            uartPort.close();
            Log.i(TAG, "UART已关闭");
        }
    }
    
    /**
     * 关闭所有串口
     */
    public void closeAll() {
        closeRS485();
        closeUart();
    }
    
    /**
     * 设置数据回调
     */
    public void setDataCallback(DataCallback callback) {
        this.dataCallback = callback;
    }
    
    /**
     * 检查RS485是否打开
     */
    public boolean isRS485Open() {
        return rs485Port != null && rs485Port.isOpen();
    }
    
    /**
     * 检查UART是否打开
     */
    public boolean isUartOpen() {
        return uartPort != null && uartPort.isOpen();
    }
    
    /**
     * 向RS485发送数据
     * 使用synchronized确保多线程环境下串口访问的安全性
     */
    public synchronized boolean sendToRS485(byte[] data) {
        if (rs485Port != null && rs485Port.isOpen()) {
            return rs485Port.send(data);
        }
        return false;
    }
    
    /**
     * 按照完整协议发送数据到RS485
     * 新协议要求：
     * 1. 363636波特率发送0x00 break信号
     * 2. 切换到500000波特率发送header
     * 3. 等待从机回复0xFC
     * 4. 发送data数据
     * 
     * 使用synchronized确保多线程环境下串口访问的安全性
     */
    public synchronized boolean sendToRS485WithProtocol(byte[] header, byte[] data) {
        if (rs485Port == null || !rs485Port.isOpen()) {
            Log.e(TAG, "RS485未打开");
            return false;
        }
        
        try {
            // 步骤1: 切换到363636波特率（无校验）
            Log.i(TAG, "切换波特率到363636(无校验)发送break信号");
            if (!rs485Port.reconfigureBaudRate(363636, 0)) { // 0 = 无校验
                Log.e(TAG, "无法切换到363636波特率");
                return false;
            }
            
            // 步骤2: 发送break信号 0x00
            byte[] breakSignal = {0x00};
            rs485Port.send(breakSignal);
            Log.i(TAG, "已发送break信号: 0x00");
            
            // 短暂延迟
            Thread.sleep(1);
            
            // 步骤3: 切换到500000波特率（偶校验）
            Log.i(TAG, "切换波特率到500000(偶校验)发送header");
            if (!rs485Port.reconfigureBaudRate(500000, 2)) { // 2 = 偶校验
                Log.e(TAG, "无法切换到500000波特率");
                return false;
            }
            
            // 步骤4: 发送header
            if (header != null && header.length > 0) {
                rs485Port.send(header);
                Log.i(TAG, "已发送header: " + bytesToHexString(header, header.length));
            }
            
            // 步骤5: 等待从机回复0xFC
//            Log.i(TAG, "等待从机回复0xFC...");
//            byte[] response = new byte[1];
//            int bytesRead = rs485Port.readWithTimeout(response, 1); // 100ms超时
//
//            if (bytesRead > 0 && response[0] == (byte)0xFC) {
//                Log.i(TAG, "收到从机回复: 0xFC");
//            } else {
//                Log.w(TAG, "未收到从机回复0xFC或超时，继续发送data");
//            }
            
            // 短暂延迟
            Thread.sleep(1);
            
            // 步骤6: 发送data
            if (data != null && data.length > 0) {
                rs485Port.send(data);
                Log.i(TAG, "已发送data: " + bytesToHexString(data, data.length));
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "按协议发送失败", e);
            return false;
        }
    }
    
    /**
     * 向UART发送数据
     * 使用synchronized确保多线程环境下串口访问的安全性
     */
    public synchronized boolean sendToUart(byte[] data) {
        if (uartPort != null && uartPort.isOpen()) {
            return uartPort.send(data);
        }
        return false;
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHexString(byte[] bytes, int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }
    
    /**
     * 通知错误
     */
    private void notifyError(String error) {
        mainHandler.post(() -> {
            if (dataCallback != null) {
                dataCallback.onError(error);
            }
        });
    }
}

