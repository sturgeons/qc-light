package com.example.qc_1;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口通信帮助类
 */
public class SerialPortHelper {
    private static final String TAG = "SerialPortHelper";
    
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private ReadThread mReadThread;
    private String mDevicePath;
    private int mBaudRate;
    private boolean isOpen = false;
    
    private OnDataReceiveListener mOnDataReceiveListener;
    
    public interface OnDataReceiveListener {
        void onDataReceived(byte[] buffer, int size);
    }
    
    /**
     * 打开串口
     * @param device 设备路径，如 /dev/ttyS2
     * @param baudRate 波特率，如 9600, 115200
     * @return 是否成功
     */
    public boolean open(String device, int baudRate) {
        mDevicePath = device;
        mBaudRate = baudRate;
        
        try {
            // 检查串口设备文件是否存在
            File deviceFile = new File(device);
            if (!deviceFile.exists()) {
                Log.e(TAG, "设备不存在: " + device);
                return false;
            }
            
            // 打开串口
            mFd = nativeOpen(device, baudRate, 8, 1, 0);
            if (mFd == null) {
                Log.e(TAG, "打开串口失败: " + device);
                return false;
            }
            
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
            
            // 启动读取线程
            mReadThread = new ReadThread();
            mReadThread.start();
            
            isOpen = true;
            Log.i(TAG, "串口打开成功: " + device + ", 波特率: " + baudRate);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "打开串口异常", e);
            close();
            return false;
        }
    }
    
    /**
     * 关闭串口
     */
    public void close() {
        if (mReadThread != null) {
            mReadThread.interrupt();
            mReadThread = null;
        }
        
        if (mFileInputStream != null) {
            try {
                mFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileInputStream = null;
        }
        
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileOutputStream = null;
        }
        
        if (mFd != null) {
            nativeClose();
            mFd = null;
        }
        
        isOpen = false;
        Log.i(TAG, "串口已关闭: " + mDevicePath);
    }
    
    /**
     * 发送数据
     */
    public boolean send(byte[] data) {
        if (!isOpen || mFileOutputStream == null) {
            Log.e(TAG, "串口未打开");
            return false;
        }
        
        try {
            mFileOutputStream.write(data);
            mFileOutputStream.flush();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "发送数据失败", e);
            return false;
        }
    }
    
    /**
     * 设置数据接收监听器
     */
    public void setOnDataReceiveListener(OnDataReceiveListener listener) {
        mOnDataReceiveListener = listener;
    }
    
    /**
     * 是否已打开
     */
    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * 重新配置波特率（不关闭串口）
     * @param baudRate 新的波特率
     * @return 是否成功
     */
    public boolean reconfigureBaudRate(int baudRate) {
        return reconfigureBaudRate(baudRate, 0); // 默认无校验
    }
    
    /**
     * 重新配置波特率和奇偶校验（不关闭串口）
     * @param baudRate 新的波特率
     * @param parity 奇偶校验 (0=NONE, 1=ODD, 2=EVEN)
     * @return 是否成功
     */
    public boolean reconfigureBaudRate(int baudRate, int parity) {
        if (!isOpen || mFd == null) {
            Log.e(TAG, "串口未打开，无法重新配置");
            return false;
        }
        
        try {
            boolean success = nativeSetBaudRate(mFd, baudRate, parity);
            if (success) {
                mBaudRate = baudRate;
                String parityStr = (parity == 0 ? "无" : (parity == 1 ? "奇" : "偶")) + "校验";
                Log.i(TAG, "波特率已切换到: " + baudRate + ", " + parityStr);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "切换波特率失败", e);
            return false;
        }
    }
    
    /**
     * 读取数据（阻塞，带超时）
     * @param buffer 缓冲区
     * @param timeout 超时时间（毫秒）
     * @return 读取的字节数，-1表示超时或错误
     */
    public int readWithTimeout(byte[] buffer, int timeout) {
        if (!isOpen || mFileInputStream == null) {
            return -1;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            int bytesRead = 0;
            
            while (bytesRead == 0 && (System.currentTimeMillis() - startTime) < timeout) {
                if (mFileInputStream.available() > 0) {
                    bytesRead = mFileInputStream.read(buffer);
                    return bytesRead;
                }
                Thread.sleep(1);
            }
            
            return -1; // 超时
        } catch (Exception e) {
            Log.e(TAG, "读取数据异常", e);
            return -1;
        }
    }
    
    /**
     * 读取线程
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int size;
            
            while (!isInterrupted() && isOpen) {
                try {
                    if (mFileInputStream == null) {
                        break;
                    }
                    
                    size = mFileInputStream.read(buffer);
                    if (size > 0 && mOnDataReceiveListener != null) {
                        byte[] data = new byte[size];
                        System.arraycopy(buffer, 0, data, 0, size);
                        mOnDataReceiveListener.onDataReceived(data, size);
                    }
                } catch (IOException e) {
                    if (!isInterrupted()) {
                        Log.e(TAG, "读取数据异常", e);
                    }
                    break;
                }
            }
        }
    }
    
    // JNI本地方法
    private native FileDescriptor nativeOpen(String path, int baudRate, int dataBits, int stopBits, int parity);
    private native void nativeClose();
    private native boolean nativeSetBaudRate(FileDescriptor fd, int baudRate, int parity);
    
    static {
        try {
            System.loadLibrary("serial_port");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "加载native库失败，使用模拟模式", e);
        }
    }
}

