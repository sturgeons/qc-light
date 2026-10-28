package com.example.qc_1;

import android.graphics.drawable.Drawable;

import java.io.File;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 开关控制页面
 * 分组控制命令的持续发送
 */
public class SwitchControlActivity extends AppCompatActivity {

    // 命令列表 - 复用CommandSendActivity的数据
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

    // 分组定义
    private static class Group {
        String name;
        int startIndex;
        int endIndex;
        
        Group(String name, int start, int end) {
            this.name = name;
            this.startIndex = start;
            this.endIndex = end;
        }
    }

    private static final Group[] GROUPS = {
            new Group("ADDR1-3", 0, 2),      // 左转1
            new Group("ADDR4-6", 3, 5),      // 左转2
            new Group("ADDR7-9", 6, 8),      // 右转1
            new Group("ADDR10-12", 9, 11),   // 右转2
            new Group("ADDR13-15", 12, 14),  // 左尾灯
            new Group("ADDR16-18", 15, 17),  // 右尾灯
            new Group("ADDR19-20", 18, 19)   // 后中灯
    };

    private static final byte[][] CMD_HEADER = new byte[CMD_LIST.length][];
    private static final byte[][] CMD_DATA = new byte[CMD_LIST.length][];
    private static final byte[] REAR_CENTER_FRAME;

    static {
        for (int i = 0; i < CMD_LIST.length; i++) {
            byte[] fullCmd = CMD_LIST[i];
            int totalLen;
            if (i == 2 || i == 8) {
                totalLen = 21;
            } else if (i <= 11) {
                totalLen = 19;
            } else {
                totalLen = 8;
            }
            byte[] header = new byte[4];
            System.arraycopy(fullCmd, 0, header, 0, 4);
            byte[] data = new byte[totalLen - 5];
            System.arraycopy(fullCmd, 5, data, 0, data.length);
            CMD_HEADER[i] = header;
            CMD_DATA[i] = data;
        }
        byte[] rearCmd = {
                (byte) 0x01, (byte) 0x10, (byte) 0x80, (byte) 0x01, (byte) 0x00, (byte) 0x05, (byte) 0x0A,
                (byte) 0x01, (byte) 0x08, (byte) 0x10, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x04,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA6, (byte) 0x71
        };
        REAR_CENTER_FRAME = LinBusModule.fromToomossWrite(rearCmd);
    }

    private SerialPortManager serialPortManager;
    private SendQueueManager sendQueueManager;
    private Handler mainHandler;
    private TextView tvLog;
    
    // 后中灯专用串口
    private SerialPortHelper rearCenterLightPort;
    private Thread rearCenterLightThread;
    
    // 每个分组的状态和线程
    private Map<Integer, Boolean> groupStates = new HashMap<>();
    private Map<Integer, Thread> groupThreads = new HashMap<>();
    private Map<Integer, View> groupButtons = new HashMap<>();
    private Map<Integer, View> groupContainers = new HashMap<>();
    
    // 计时器相关
    private Map<Integer, TextView> groupTimers = new HashMap<>();
    private Map<Integer, Long> groupStartTimes = new HashMap<>();
    private Map<Integer, Runnable> timerRunnables = new HashMap<>();
    
    // 队列统计显示
    private Runnable statsUpdateRunnable;
    private final Drawable[] styleOn = new Drawable[GROUPS.length];
    private final Drawable[] styleOff = new Drawable[GROUPS.length];
    private final ArrayDeque<String> logLines = new ArrayDeque<>();
    private final SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private volatile boolean pageAlive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_control);

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

        // 创建7个分组开关按钮
        int[] buttonIds = {
                R.id.btnGroup1, R.id.btnGroup2, R.id.btnGroup3,
                R.id.btnGroup4, R.id.btnGroup5, R.id.btnGroup6,
                R.id.btnGroup7  // 后中灯
        };
        
        int[] timerIds = {
                R.id.tvTimer1, R.id.tvTimer2, R.id.tvTimer3,
                R.id.tvTimer4, R.id.tvTimer5, R.id.tvTimer6,
                R.id.tvTimer7  // 后中灯
        };
        
        int[] containerIds = {
                R.id.containerGroup1, R.id.containerGroup2, R.id.containerGroup3,
                R.id.containerGroup4, R.id.containerGroup5, R.id.containerGroup6,
                R.id.containerGroup7  // 后中灯
        };

        for (int i = 0; i < buttonIds.length; i++) {
            View btn = findViewById(buttonIds[i]);
            TextView timer = findViewById(timerIds[i]);
            View container = findViewById(containerIds[i]);
            
            groupButtons.put(i, btn);
            groupTimers.put(i, timer);
            groupContainers.put(i, container);
            groupStates.put(i, false);
            
            final int groupIndex = i;
            
            int onRes;
            int offRes;
            if (i == 6) {
                onRes = R.drawable.tech_button_center_on;
                offRes = R.drawable.tech_button_center_off;
            } else if (i == 0 || i == 1 || i == 4) {
                onRes = R.drawable.tech_button_left_on;
                offRes = R.drawable.tech_button_left_off;
            } else {
                onRes = R.drawable.tech_button_right_on;
                offRes = R.drawable.tech_button_right_off;
            }
            styleOn[i] = ContextCompat.getDrawable(this, onRes).mutate();
            styleOff[i] = ContextCompat.getDrawable(this, offRes).mutate();
            container.setBackground(styleOff[i]);
            
            // 所有按钮都设置点击事件
            btn.setOnClickListener(v -> toggleGroup(groupIndex));
        }
    }

    private void initSerialPort() {
        serialPortManager = ((MyApplication) getApplication()).getSerialPortManager();
        if (serialPortManager == null) {
            showToast("串口管理器未初始化");
            finish();
            return;
        }
        
        // 初始化发送管理器（循环检测模式）
        sendQueueManager = new SendQueueManager(serialPortManager);
        sendQueueManager.start();
        updateLog("✅ 发送管理器已启动 (循环检测模式)");
        updateLog("⚡ 高频循环检测，无队列、无丢包");
        
        // 启动统计显示（每30秒更新一次）
        startStatisticsUpdater();
        
        updateLog("灯控走 RS485_1，后中灯走 RS485_2，两条路互不影响");
    }
    
    /**
     * 启动统计信息更新器
     */
    private void startStatisticsUpdater() {
        statsUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (sendQueueManager != null) {
                    // 获取统计信息
                    String stats = sendQueueManager.getStatistics();
                    
                    // 只在有发送活动时显示
                    if (sendQueueManager.getTotalSent() > 0) {
                        updateLog("📊 " + stats);
                    }
                    
                    // 继续定时更新（每30秒）
                    mainHandler.postDelayed(this, 30000);
                }
            }
        };
        mainHandler.postDelayed(statsUpdateRunnable, 30000); // 首次延迟30秒
    }

    /**
     * 切换分组状态。只改界面，串口打开和发送都放到后台。
     */
    private void toggleGroup(int groupIndex) {
        if (Boolean.TRUE.equals(groupStates.get(groupIndex))) {
            stopGroup(groupIndex);
        } else {
            startGroup(groupIndex);
        }
    }
    
    /**
     * 自动打开RS485（使用协议所需的波特率）
     * @return 是否成功
     */
    private boolean autoOpenRS485() {
        // 使用500000波特率（协议要求）
        final int PROTOCOL_BAUD_RATE = 500000;
        return serialPortManager.openRS485(PROTOCOL_BAUD_RATE, false);
    }

    /**
     * 开启分组持续发送（循环检测模式）
     */
    private void startGroup(int groupIndex) {
        Group group = GROUPS[groupIndex];
        groupStates.put(groupIndex, true);
        applyGroupStyle(groupIndex, true);
        startTimer(groupIndex);
        
        if (groupIndex == 6) {
            startRearCenterSender();
            updateLog(group.name + " 开启");
            return;
        }
        
        sendQueueManager.activateGroup(groupIndex);
        Thread running = groupThreads.get(groupIndex);
        if (running != null && running.isAlive()) {
            updateLog(group.name + " 开启");
            return;
        }
        
        updateLog(group.name + " 开启");
        
        Thread sendThread = new Thread(() -> {
            serialPortManager.disableRS485Read();
            while (pageAlive) {
                if (!Boolean.TRUE.equals(groupStates.get(groupIndex))) {
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException ignored) {
                    }
                    continue;
                }
                if (!ensureRs485Open()) {
                    onRs4851Unplugged();
                    continue;
                }
                int sendFail = 0;
                for (int i = group.startIndex; i <= group.endIndex; i++) {
                    if (!Boolean.TRUE.equals(groupStates.get(groupIndex))) {
                        break;
                    }
                    if (sendCommandDirect(groupIndex, i)) {
                        sendFail = 0;
                    } else if (++sendFail >= 3) {
                        onRs4851Unplugged();
                        break;
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }, "LoopDetect-" + groupIndex);
        
        groupThreads.put(groupIndex, sendThread);
        sendThread.start();
    }

    /**
     * 停止分组发送
     */
    private void stopGroup(int groupIndex) {
        groupStates.put(groupIndex, false);
        applyGroupStyle(groupIndex, false);
        stopTimer(groupIndex);
        if (groupIndex != 6) {
            sendQueueManager.deactivateGroup(groupIndex);
            Thread thread = groupThreads.get(groupIndex);
            if (thread != null) {
                thread.interrupt();
            }
        } else if (rearCenterLightThread != null) {
            rearCenterLightThread.interrupt();
        }
        updateLog(GROUPS[groupIndex].name + " 关闭");
    }

    private void applyGroupStyle(int groupIndex, boolean on) {
        View container = groupContainers.get(groupIndex);
        if (container == null) {
            return;
        }
        container.setBackground(on ? styleOn[groupIndex] : styleOff[groupIndex]);
        TextView timer = groupTimers.get(groupIndex);
        if (timer != null) {
            timer.setTextColor(on ? 0xFFF6F3EC : 0xFF6E7786);
        }
        View button = groupButtons.get(groupIndex);
        if (button instanceof TextView) {
            ((TextView) button).setTextColor(0xFFF6F3EC);
        }
    }

    private boolean ensureRs485Open() {
        if (!new File(SerialPortManager.RS485_DEVICE).exists()) {
            if (serialPortManager.isRS485Open()) {
                serialPortManager.closeRS485();
            }
            return false;
        }
        return serialPortManager.isRS485Open() || autoOpenRS485();
    }

    /**
     * RS485_1 断开：关掉这一路所有发送按钮并释放串口，下次点击会重新打开。
     */
    private synchronized void onRs4851Unplugged() {
        boolean anyOn = false;
        for (int i = 0; i < 6; i++) {
            if (Boolean.TRUE.equals(groupStates.get(i))) {
                anyOn = true;
            }
            groupStates.put(i, false);
            sendQueueManager.deactivateGroup(i);
        }
        serialPortManager.closeRS485();
        if (!anyOn) {
            return;
        }
        mainHandler.post(() -> {
            for (int i = 0; i < 6; i++) {
                applyGroupStyle(i, false);
                stopTimer(i);
            }
            showToast("RS485_1 已断开，可再次打开");
            updateLog("RS485_1 连接断开，发送已关闭");
        });
    }

    /**
     * RS485_2 断开：关掉后中灯并释放串口，下次点击会重新打开。
     */
    private synchronized void onRs4852Unplugged() {
        if (!Boolean.TRUE.equals(groupStates.get(6)) && rearCenterLightPort == null) {
            return;
        }
        groupStates.put(6, false);
        SerialPortHelper port = rearCenterLightPort;
        rearCenterLightPort = null;
        if (port != null) {
            port.close();
        }
        mainHandler.post(() -> {
            applyGroupStyle(6, false);
            stopTimer(6);
            showToast("后中灯连接已断开，可再次打开");
            updateLog("RS485_2 连接断开，后中灯已关闭");
        });
    }

    /**
     * 直接发送命令（循环检测模式）
     */
    private boolean sendCommandDirect(int groupId, int cmdIndex) {
        if (cmdIndex < 0 || cmdIndex >= CMD_HEADER.length) {
            return false;
        }
        return sendQueueManager.sendCommand(groupId, CMD_HEADER[cmdIndex], CMD_DATA[cmdIndex]);
    }

    /**
     * 后中灯走第二路 485。按钮状态已经先更新，打开串口和发送都在后台。
     */
    private void startRearCenterSender() {
        if (rearCenterLightThread != null && rearCenterLightThread.isAlive()) {
            return;
        }
        if (REAR_CENTER_FRAME == null) {
            groupStates.put(6, false);
            applyGroupStyle(6, false);
            stopTimer(6);
            showToast("后中灯报文转换失败");
            return;
        }
        rearCenterLightThread = new Thread(() -> {
            while (pageAlive) {
                if (!Boolean.TRUE.equals(groupStates.get(6))) {
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException ignored) {
                    }
                    continue;
                }
                if (!new File(SerialPortManager.RS485_2_DEVICE).exists()) {
                    onRs4852Unplugged();
                    continue;
                }
                SerialPortHelper port = rearCenterLightPort;
                if (port == null || !port.isOpen()) {
                    port = new SerialPortHelper();
                    boolean opened = port.open(SerialPortManager.RS485_2_DEVICE, 115200, true, false);
                    if (!opened) {
                        groupStates.put(6, false);
                        mainHandler.post(() -> {
                            applyGroupStyle(6, false);
                            stopTimer(6);
                            showToast("后中灯串口打开失败");
                        });
                        continue;
                    }
                    rearCenterLightPort = port;
                }
                try {
                    if (!port.send(REAR_CENTER_FRAME)) {
                        onRs4852Unplugged();
                        continue;
                    }
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // 关闭时被打断，回到循环里看开关状态
                }
            }
        }, "RearCenterLight-Thread");
        rearCenterLightThread.start();
    }

    /**
     * 启动计时器
     */
    private void startTimer(int groupIndex) {
        // 记录开始时间
        groupStartTimes.put(groupIndex, System.currentTimeMillis());
        
        // 创建计时器更新任务
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (groupStates.get(groupIndex)) {
                    updateTimerDisplay(groupIndex);
                    mainHandler.postDelayed(this, 1000); // 每秒更新一次
                }
            }
        };
        
        timerRunnables.put(groupIndex, timerRunnable);
        mainHandler.post(timerRunnable);
    }
    
    /**
     * 停止计时器
     */
    private void stopTimer(int groupIndex) {
        // 移除计时器更新任务
        Runnable timerRunnable = timerRunnables.get(groupIndex);
        if (timerRunnable != null) {
            mainHandler.removeCallbacks(timerRunnable);
            timerRunnables.remove(groupIndex);
        }
        
        // 重置计时器显示
        TextView timer = groupTimers.get(groupIndex);
        if (timer != null) {
            timer.setText("00:00");
        }
        
        groupStartTimes.remove(groupIndex);
    }
    
    /**
     * 更新计时器显示
     */
    private void updateTimerDisplay(int groupIndex) {
        Long startTime = groupStartTimes.get(groupIndex);
        if (startTime == null) return;
        
        long elapsedMillis = System.currentTimeMillis() - startTime;
        long elapsedSeconds = elapsedMillis / 1000;
        
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;
        
        TextView timer = groupTimers.get(groupIndex);
        if (timer != null) {
            String timeText;
            if (hours > 0) {
                timeText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            }
            timer.setText(timeText);
        }
    }
    
    /**
     * 添加日志
     */
    private void updateLog(String log) {
        mainHandler.post(() -> {
            logLines.addLast("[" + logTime.format(new Date()) + "] " + log);
            while (logLines.size() > 30) {
                logLines.removeFirst();
            }
            StringBuilder sb = new StringBuilder();
            for (String line : logLines) {
                sb.append(line).append('\n');
            }
            tvLog.setText(sb);
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        pageAlive = false;
        super.onDestroy();
        
        // 停止统计更新器
        if (statsUpdateRunnable != null) {
            mainHandler.removeCallbacks(statsUpdateRunnable);
        }
        
        for (int i = 0; i < GROUPS.length; i++) {
            groupStates.put(i, false);
        }
        for (Thread thread : groupThreads.values()) {
            if (thread != null) {
                thread.interrupt();
            }
        }
        if (rearCenterLightThread != null) {
            rearCenterLightThread.interrupt();
        }
        
        // 清理所有计时器回调
        for (Runnable runnable : timerRunnables.values()) {
            mainHandler.removeCallbacks(runnable);
        }
        timerRunnables.clear();
        
        // 关闭后中灯串口（如果存在）
        if (rearCenterLightPort != null) {
            rearCenterLightPort.close();
            rearCenterLightPort = null;
        }
        
        // 显示最终统计
        if (sendQueueManager != null) {
            updateLog("📊 最终统计: " + sendQueueManager.getStatistics());
            // 停止发送队列管理器
            sendQueueManager.stop();
        }
    }
}

