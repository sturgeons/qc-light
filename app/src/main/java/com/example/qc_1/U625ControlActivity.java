package com.example.qc_1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * U625格栅灯控制页面
 * 通过 RS485_2 发送控制指令。
 */
public class U625ControlActivity extends AppCompatActivity {

    // U625控制指令定义
    private static final byte[] CMD_HIGH_BRIGHT = {  // 格栅灯高亮
            (byte)0x01, (byte)0x10, (byte)0x80, (byte)0x01, (byte)0x00, (byte)0x05, (byte)0x0A,
            (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x40, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xB9, (byte)0x9C
    };

    private static final byte[] CMD_ALL_OFF = {  // 关闭所有格栅灯
            (byte)0x01, (byte)0x10, (byte)0x80, (byte)0x01, (byte)0x00, (byte)0x05, (byte)0x0A,
            (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xB7, (byte)0x5C
    };

    private static final byte[] CMD_LEFT_LOW = {  // 左侧格栅灯低亮
            (byte)0x01, (byte)0x10, (byte)0x80, (byte)0x01, (byte)0x00, (byte)0x05, (byte)0x0A,
            (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xB6, (byte)0xA0
    };

    private static final byte[] CMD_RIGHT_LOW = {  // 右侧格栅灯低亮
            (byte)0x01, (byte)0x10, (byte)0x80, (byte)0x01, (byte)0x00, (byte)0x05, (byte)0x0A,
            (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xB6, (byte)0xE4
    };

    private static final byte[] CMD_BOTH_LOW = {  // 左右格栅灯低亮
            (byte)0x01, (byte)0x10, (byte)0x80, (byte)0x01, (byte)0x00, (byte)0x05, (byte)0x0A,
            (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xB7, (byte)0x18
    };

    private static final byte[] CMD_FADE_IN = {  // 格栅灯渐亮
            (byte)0x01, (byte)0x10, (byte)0x80, (byte)0x01, (byte)0x00, (byte)0x05, (byte)0x0A,
            (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x01, (byte)0x01, (byte)0x00, (byte)0x04,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xA7, (byte)0x7D
    };

    private static final byte[] CMD_FADE_OUT = {  // 格栅灯渐灭
            (byte)0x01, (byte)0x10, (byte)0x80, (byte)0x01, (byte)0x00, (byte)0x05, (byte)0x0A,
            (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x04,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xB7, (byte)0xBD
    };

    private SerialPortHelper serialPort;
    private Handler mainHandler;
    private TextView tvLog;

    // 原 232（/dev/ttyUSB1）已作废，改走第二路 485
    private static final String SERIAL_DEVICE = SerialPortManager.RS485_2_DEVICE;
    private static final int BAUD_RATE = 115200;
    private static final int SEND_DELAY_MS = 50; // 两次发送之间的延迟（毫秒）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_u625_control);

        mainHandler = new Handler(Looper.getMainLooper());
        
        initViews();
        initSerialPort();
    }

    private void initViews() {
        tvLog = findViewById(R.id.tvLog);

        // 返回按钮
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 清空日志
        findViewById(R.id.btnClearLog).setOnClickListener(v -> tvLog.setText(""));

        // 格栅灯高亮
        findViewById(R.id.btnHighBright).setOnClickListener(v -> 
            sendCommand("格栅灯高亮", CMD_HIGH_BRIGHT));

        // 关闭所有格栅灯
        findViewById(R.id.btnAllOff).setOnClickListener(v -> 
            sendCommand("关闭所有格栅灯", CMD_ALL_OFF));

        // 左侧格栅灯低亮
        findViewById(R.id.btnLeftLow).setOnClickListener(v -> 
            sendCommand("左侧格栅灯低亮", CMD_LEFT_LOW));

        // 右侧格栅灯低亮
        findViewById(R.id.btnRightLow).setOnClickListener(v -> 
            sendCommand("右侧格栅灯低亮", CMD_RIGHT_LOW));

        // 左右格栅灯低亮
        findViewById(R.id.btnBothLow).setOnClickListener(v -> 
            sendCommand("左右格栅灯低亮", CMD_BOTH_LOW));

        // 格栅灯渐亮
        findViewById(R.id.btnFadeIn).setOnClickListener(v -> 
            sendCommand("格栅灯渐亮", CMD_FADE_IN));

        // 格栅灯渐灭
        findViewById(R.id.btnFadeOut).setOnClickListener(v -> 
            sendCommand("格栅灯渐灭", CMD_FADE_OUT));
    }

    private void initSerialPort() {
        try {
            serialPort = new SerialPortHelper();
            boolean opened = serialPort.open(SERIAL_DEVICE, BAUD_RATE, true);
            
            if (opened) {
                updateLog("✅ 串口已打开 (" + SERIAL_DEVICE + ", " + BAUD_RATE + ", RS485, NONE, 1bit)");
                showToast("✅ 串口已打开");
            } else {
                updateLog("❌ 串口打开失败 (" + SERIAL_DEVICE + ")");
                updateLog("⚠️ 部分功能可能无法使用");
                showToast("⚠️ 串口打开失败");
            }
        } catch (Exception e) {
            updateLog("❌ 串口初始化异常: " + e.getMessage());
            showToast("❌ 串口初始化失败");
        }
    }

    /**
     * 发送指令（发送两次）
     */
    private void sendCommand(String commandName, byte[] command) {
        if (serialPort == null || !serialPort.isOpen()) {
            updateLog("❌ 串口未打开，尝试自动打开...");
            initSerialPort();
            
            if (serialPort == null || !serialPort.isOpen()) {
                showToast("❌ 串口未打开，指令发送失败");
                return;
            }
        }

        // 在后台线程中发送指令
        new Thread(() -> {
            try {
                byte[] frame = LinBusModule.fromToomossWrite(command);
                if (frame == null) {
                    updateLog("❌ 图莫斯报文无法转换成485主机帧");
                    mainHandler.post(() -> showToast("❌ 报文转换失败"));
                    return;
                }
                updateLog("📤 发送指令: " + commandName);
                updateLog("485主机帧: " + LinBusModule.toHex(frame));
                
                // 第一次发送
                boolean success1 = serialPort.send(frame);
                if (success1) {
                    updateLog("✅ 第1次发送成功 (" + frame.length + "字节)");
                } else {
                    updateLog("❌ 第1次发送失败");
                }
                
                // 延迟
                Thread.sleep(SEND_DELAY_MS);
                
                // 第二次发送
                boolean success2 = serialPort.send(frame);
                if (success2) {
                    updateLog("✅ 第2次发送成功 (" + frame.length + "字节)");
                } else {
                    updateLog("❌ 第2次发送失败");
                }
                
                if (success1 && success2) {
                    mainHandler.post(() -> showToast("✅ " + commandName + " 指令已发送"));
                    updateLog("🎉 " + commandName + " 指令发送完成");
                } else {
                    mainHandler.post(() -> showToast("⚠️ " + commandName + " 指令发送异常"));
                }
                
            } catch (InterruptedException e) {
                updateLog("❌ 发送指令被中断");
            } catch (Exception e) {
                updateLog("❌ 发送指令异常: " + e.getMessage());
                mainHandler.post(() -> showToast("❌ 发送失败"));
            }
        }, "U625-SendCommand").start();
    }

    /**
     * 添加日志
     */
    private void updateLog(String log) {
        mainHandler.post(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String logLine = "[" + timestamp + "] " + log + "\n";
            
            String currentText = tvLog.getText().toString();
            
            // 限制日志条目
            String[] lines = currentText.split("\n");
            if (lines.length > 50) {
                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 50; i < lines.length; i++) {
                    sb.append(lines[i]).append("\n");
                }
                currentText = sb.toString();
            }
            
            tvLog.setText(currentText + logLine);
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 关闭串口
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
            updateLog("✅ 串口已关闭");
        }
    }
}

