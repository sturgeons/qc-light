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
 * 隐藏调试页。LIN 10417 主机配置只在这里做，日常点灯页面不放这个按钮。
 */
public class DebugActivity extends AppCompatActivity {

    private static final String SERIAL_DEVICE = SerialPortManager.RS485_2_DEVICE;
    private static final int BAUD_RATE = 115200;

    private SerialPortHelper serialPort;
    private Handler mainHandler;
    private TextView tvLog;
    private volatile boolean linInitRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        mainHandler = new Handler(Looper.getMainLooper());
        tvLog = findViewById(R.id.tvLog);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnClearLog).setOnClickListener(v -> tvLog.setText(""));
        findViewById(R.id.btnInitLin).setOnClickListener(v -> initLinModule());

        initSerialPort();
    }

    private void initSerialPort() {
        try {
            if (serialPort != null && serialPort.isOpen()) {
                return;
            }
            serialPort = new SerialPortHelper();
            boolean opened = serialPort.open(SERIAL_DEVICE, BAUD_RATE, true);
            if (opened) {
                updateLog("串口已打开 (" + SERIAL_DEVICE + ")");
            } else {
                updateLog("串口打开失败 (" + SERIAL_DEVICE + ")");
                showToast("串口打开失败");
            }
        } catch (Exception e) {
            updateLog("串口初始化异常: " + e.getMessage());
            showToast("串口初始化失败");
        }
    }

    private void initLinModule() {
        if (linInitRunning) {
            showToast("正在初始化");
            return;
        }
        if (serialPort == null || !serialPort.isOpen()) {
            initSerialPort();
            if (serialPort == null || !serialPort.isOpen()) {
                showToast("串口未打开");
                return;
            }
        }
        linInitRunning = true;
        updateLog("开始初始化：LIN 10417、主机模式");
        new Thread(() -> {
            try {
                boolean ok = LinBusModule.ensureMaster10417(serialPort, this::updateLog);
                mainHandler.post(() -> showToast(ok ? "LIN 初始化完成" : "LIN 初始化未确认"));
            } finally {
                linInitRunning = false;
            }
        }, "LinInit").start();
    }

    private void updateLog(String log) {
        mainHandler.post(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String current = tvLog.getText().toString();
            String[] lines = current.split("\n");
            if (lines.length > 40) {
                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 40; i < lines.length; i++) {
                    if (!lines[i].isEmpty()) {
                        sb.append(lines[i]).append("\n");
                    }
                }
                current = sb.toString();
            }
            tvLog.setText(current + "[" + timestamp + "] " + log + "\n");
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }
}
