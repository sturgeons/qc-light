package com.example.qc_1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 命令发送页面
 * 参照CanPage_ui.c实现
 * 通过RS485发送命令
 */
public class CommandSendActivity extends AppCompatActivity {

    // 命令列表 - 对应C代码中的cmd_list
    private static final byte[][] CMD_LIST = {
            // UART L Turn ADDR1
            {(byte)0x55, (byte)0x42, (byte)0x61, (byte)0xCE, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF, 
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, 
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART L Turn ADDR2
            {(byte)0x55, (byte)0x42, (byte)0x62, (byte)0x9E, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART L Turn ADDR3
            {(byte)0x55, (byte)0x42, (byte)0xE3, (byte)0x26, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x81},
            // UART L Turn ADDR4
            {(byte)0x55, (byte)0x42, (byte)0x64, (byte)0x3E, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART L Turn ADDR5
            {(byte)0x55, (byte)0x42, (byte)0x65, (byte)0xE2, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART R Turn ADDR6
            {(byte)0x55, (byte)0x42, (byte)0x66, (byte)0xB2, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART R Turn ADDR7
            {(byte)0x55, (byte)0x42, (byte)0x67, (byte)0x6E, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART R Turn ADDR8
            {(byte)0x55, (byte)0x42, (byte)0x68, (byte)0x4A, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART R Turn ADDR9
            {(byte)0x55, (byte)0x42, (byte)0xE9, (byte)0xF2, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x81},
            // UART R Turn ADDR10
            {(byte)0x55, (byte)0x42, (byte)0x6A, (byte)0xC6, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART R Turn ADDR11
            {(byte)0x55, (byte)0x42, (byte)0x6B, (byte)0x1A, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART R Turn ADDR12
            {(byte)0x55, (byte)0x42, (byte)0x6C, (byte)0x66, (byte)0xFC, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
             (byte)0xFF, (byte)0x0F, (byte)0x22},
            // UART L Tail ADDR13
            {(byte)0x55, (byte)0x7C, (byte)0x2D, (byte)0xA4, (byte)0xFC, (byte)0xFF, (byte)0x03, (byte)0xA0},
            // UART L Tail ADDR14
            {(byte)0x55, (byte)0x7C, (byte)0x2E, (byte)0xF4, (byte)0xFC, (byte)0xFF, (byte)0x03, (byte)0xA0},
            // UART L Tail ADDR15
            {(byte)0x55, (byte)0x7C, (byte)0x2F, (byte)0x28, (byte)0xFC, (byte)0xFF, (byte)0x03, (byte)0xA0},
            // UART L Tail ADDR16
            {(byte)0x55, (byte)0x7C, (byte)0x30, (byte)0xBC, (byte)0xFC, (byte)0xFF, (byte)0x03, (byte)0xA0},
            // UART L Tail ADDR17
            {(byte)0x55, (byte)0x7C, (byte)0x31, (byte)0x60, (byte)0xFC, (byte)0xFF, (byte)0x03, (byte)0xA0},
            // UART L Tail ADDR18
            {(byte)0x55, (byte)0x7C, (byte)0x32, (byte)0x30, (byte)0xFC, (byte)0xFF, (byte)0x03, (byte)0xA0},
            // MID Tail ADDR19
            {(byte)0x55, (byte)0x7C, (byte)0x33, (byte)0xEC, (byte)0xFC, (byte)0xFF, (byte)0x03, (byte)0xA0},
            // MID Tail ADDR20
            {(byte)0x55, (byte)0x7C, (byte)0x34, (byte)0x90, (byte)0xFC, (byte)0x00, (byte)0x00, (byte)0x1D}
    };

    // 命令名称
    private static final String[] CMD_NAMES = {
            "ADDR1-左转", "ADDR2-左转", "ADDR3-左转", "ADDR4-左转", "ADDR5-左转",
            "ADDR6-右转", "ADDR7-右转", "ADDR8-右转", "ADDR9-右转", "ADDR10-右转",
            "ADDR11-右转", "ADDR12-右转", "ADDR13-左尾", "ADDR14-左尾", "ADDR15-左尾",
            "ADDR16-左尾", "ADDR17-左尾", "ADDR18-左尾", "ADDR19-中尾", "ADDR20-中尾"
    };
    
    // 使用RS485发送

    private SerialPortManager serialPortManager;
    private TextView tvLog;
    private ScrollView scrollViewLog;
    private Handler mainHandler;
    private Button btnSendAll;
    private boolean isSending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_send);

        mainHandler = new Handler(Looper.getMainLooper());
        
        initViews();
        initSerialPort();
    }

    private void initViews() {
        tvLog = findViewById(R.id.tvLog);
        scrollViewLog = findViewById(R.id.scrollViewLog);
        btnSendAll = findViewById(R.id.btnSendAll);

        // 返回按钮
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 发送全部按钮
        btnSendAll.setOnClickListener(v -> sendAllCommands());

        // 清空日志
        findViewById(R.id.btnClearLog).setOnClickListener(v -> tvLog.setText(""));

        // 创建20个命令按钮
        int[] buttonIds = {
                R.id.btnCmd1, R.id.btnCmd2, R.id.btnCmd3, R.id.btnCmd4, R.id.btnCmd5,
                R.id.btnCmd6, R.id.btnCmd7, R.id.btnCmd8, R.id.btnCmd9, R.id.btnCmd10,
                R.id.btnCmd11, R.id.btnCmd12, R.id.btnCmd13, R.id.btnCmd14, R.id.btnCmd15,
                R.id.btnCmd16, R.id.btnCmd17, R.id.btnCmd18, R.id.btnCmd19, R.id.btnCmd20
        };

        for (int i = 0; i < buttonIds.length; i++) {
            Button btn = findViewById(buttonIds[i]);
            btn.setText(CMD_NAMES[i]);
            final int index = i;
            btn.setOnClickListener(v -> sendCommandMultiple(index, 100));
        }
    }

    private void initSerialPort() {
        serialPortManager = ((MyApplication) getApplication()).getSerialPortManager();
        if (serialPortManager == null) {
            showToast("串口管理器未初始化");
            finish();
            return;
        }
        
        // 检查RS485是否已打开
        if (!serialPortManager.isRS485Open()) {
            showToast("⚠️ RS485未打开，请先在主界面打开RS485串口");
        }
    }

    /**
     * 发送单个命令
     * 参照C代码的grf_sline_send协议：
     * 1. 前4字节为header
     * 2. 第5字节是0xFC（分隔符，不发送）
     * 3. 第6字节开始是data部分
     */
    private boolean sendCommand(int index) {
        if (index < 0 || index >= CMD_LIST.length) {
            return false;
        }

        byte[] fullCmd = CMD_LIST[index];
        int totalLen;

        // 根据命令索引确定总数据长度
        if (index == 2 || index == 8) {
            totalLen = 21; // ADDR3和ADDR9命令长度为21字节
        } else if (index >= 0 && index <= 11) {
            totalLen = 19; // UART L/R Turn命令长度为19字节
        } else {
            totalLen = 8; // UART L Tail和MID Tail命令长度为8字节
        }

        // 按照C代码分割数据：
        // header: 前4个字节
        byte[] header = new byte[4];
        System.arraycopy(fullCmd, 0, header, 0, 4);
        
        // data: 从第6个字节开始（跳过第5个字节0xFC）
        int dataLen = totalLen - 5; // 总长度 - 前5个字节
        byte[] data = new byte[dataLen];
        System.arraycopy(fullCmd, 5, data, 0, dataLen);

        // 使用特殊协议发送到RS485（不记录详细日志以减少资源消耗）
        return serialPortManager.sendToRS485WithProtocol(header, data);
    }

    /**
     * 发送命令多次
     */
    private void sendCommandMultiple(int index, int count) {
        // 检查RS485是否打开，如果没打开则自动打开
        if (!serialPortManager.isRS485Open()) {
            updateLogSimple("检测到RS485未打开，正在自动打开...");
            boolean success = autoOpenRS485();
            if (!success) {
                showToast("❌ RS485自动打开失败！");
                updateLogSimple("❌ RS485自动打开失败，请检查设备权限");
                return;
            }
            updateLogSimple("✅ RS485已自动打开 (波特率: 500000)");
            showToast("✅ RS485已自动打开");
        }
        
        if (isSending) {
            showToast("正在发送中，请稍候...");
            return;
        }

        isSending = true;
        btnSendAll.setEnabled(false);
        updateLogSimple("开始发送: " + CMD_NAMES[index] + " x" + count);
        
        new Thread(() -> {
            int successCount = 0;
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                if (sendCommand(index)) {
                    successCount++;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            int finalSuccessCount = successCount;
            
            mainHandler.post(() -> {
                isSending = false;
                btnSendAll.setEnabled(true);
                String result = String.format("完成 %s: %d/%d 成功 (耗时%dms)", 
                    CMD_NAMES[index], finalSuccessCount, count, duration);
                updateLogSimple(result);
                showToast(result);
            });
        }).start();
    }

    /**
     * 发送所有命令（循环50次）
     */
    private void sendAllCommands() {
        // 检查RS485是否打开，如果没打开则自动打开
        if (!serialPortManager.isRS485Open()) {
            updateLogSimple("检测到RS485未打开，正在自动打开...");
            boolean success = autoOpenRS485();
            if (!success) {
                showToast("❌ RS485自动打开失败！");
                updateLogSimple("❌ RS485自动打开失败，请检查设备权限");
                return;
            }
            updateLogSimple("✅ RS485已自动打开 (波特率: 500000)");
            showToast("✅ RS485已自动打开");
        }
        
        if (isSending) {
            showToast("正在发送中，请稍候...");
            return;
        }

        isSending = true;
        btnSendAll.setEnabled(false);
        int totalCommands = 20 * 50;
        updateLogSimple("开始发送全部命令: 20条 x 50次 = " + totalCommands + "条");
        
        new Thread(() -> {
            int successCount = 0;
            long startTime = System.currentTimeMillis();
            
            for (int loop = 0; loop < 50; loop++) {
                for (int i = 0; i < CMD_LIST.length; i++) {
                    if (sendCommand(i)) {
                        successCount++;
                    }
                    try {
                        Thread.sleep(1); // 全部发送时间隔短一些
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            int finalSuccessCount = successCount;
            
            mainHandler.post(() -> {
                isSending = false;
                btnSendAll.setEnabled(true);
                String result = String.format("全部完成: %d/%d 成功 (耗时%.1fs)", 
                    finalSuccessCount, totalCommands, duration / 1000.0);
                updateLogSimple(result);
                showToast(result);
            });
        }).start();
    }

    /**
     * 添加简单日志（只记录关键信息）
     */
    private void updateLogSimple(String log) {
        mainHandler.post(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String logLine = "[" + timestamp + "] " + log + "\n";
            
            String currentText = tvLog.getText().toString();
            
            // 限制日志条目（只保留最近50条）
            String[] lines = currentText.split("\n");
            if (lines.length > 50) {
                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 50; i < lines.length; i++) {
                    sb.append(lines[i]).append("\n");
                }
                currentText = sb.toString();
            }
            
            tvLog.setText(currentText + logLine);
            scrollViewLog.post(() -> scrollViewLog.fullScroll(View.FOCUS_DOWN));
        });
    }

    /**
     * 自动打开RS485（使用协议所需的波特率）
     * @return 是否成功
     */
    private boolean autoOpenRS485() {
        // 使用500000波特率（协议要求）
        final int PROTOCOL_BAUD_RATE = 500000;
        return serialPortManager.openRS485(PROTOCOL_BAUD_RATE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSending = false;
    }
}

