package com.example.qc_1;

import android.graphics.drawable.Drawable;
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
            
            // 设置初始样式（根据左右侧不同，第7个是后中灯-使用右侧样式）
            boolean isLeftSide = (i == 0 || i == 1 || i == 4); // 0,1,4 是左侧
            if (isLeftSide) {
                container.setBackground(ContextCompat.getDrawable(this, R.drawable.tech_button_left_off));
            } else {
                container.setBackground(ContextCompat.getDrawable(this, R.drawable.tech_button_right_off));
            }
            
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
        
        // 检查RS485是否已打开
        if (!serialPortManager.isRS485Open()) {
            showToast("⚠️ RS485未打开，请先在主界面打开RS485串口");
        }
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
     * 切换分组状态
     */
    private void toggleGroup(int groupIndex) {
        // 检查RS485是否打开，如果没打开则自动打开
        if (!serialPortManager.isRS485Open()) {
            updateLog("检测到RS485未打开，正在自动打开...");
            boolean success = autoOpenRS485();
            if (!success) {
                showToast("❌ RS485自动打开失败！");
                updateLog("❌ RS485自动打开失败，请检查设备权限");
                return;
            }
            updateLog("✅ RS485已自动打开 (波特率: 500000)");
            showToast("✅ RS485已自动打开");
        }

        boolean currentState = groupStates.get(groupIndex);
        
        if (currentState) {
            // 当前是开启状态，点击后关闭
            stopGroup(groupIndex);
        } else {
            // 当前是关闭状态，点击后开启
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
        return serialPortManager.openRS485(PROTOCOL_BAUD_RATE);
    }

    /**
     * 开启分组持续发送（循环检测模式）
     */
    private void startGroup(int groupIndex) {
        Group group = GROUPS[groupIndex];
        groupStates.put(groupIndex, true);
        
        // 第7组（groupIndex=6）后中灯使用特殊处理
        if (groupIndex == 6) {
            startRearCenterLight();
            return;
        }
        
        // ⚡ 激活分组（允许发送）
        sendQueueManager.activateGroup(groupIndex);
        
        // 判断左右侧（左侧：0,1,4  右侧：2,3,5,6）
        boolean isLeftSide = (groupIndex == 0 || groupIndex == 1 || groupIndex == 4);
        
        // 更新容器样式
        View container = groupContainers.get(groupIndex);
        if (container != null) {
            if (isLeftSide) {
                container.setBackground(ContextCompat.getDrawable(this, R.drawable.tech_button_left_on));
            } else {
                container.setBackground(ContextCompat.getDrawable(this, R.drawable.tech_button_right_on));
            }
        }
        
        // 启动计时器
        startTimer(groupIndex);
        
        updateLog(group.name + " 开启 " + (isLeftSide ? "🔵" : "🔴"));
        
        // 创建高频循环检测线程
        Thread sendThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            long sentCount = 0;
            
            while (groupStates.get(groupIndex)) {
                // 高频循环：遍历该组的所有命令
                for (int i = group.startIndex; i <= group.endIndex; i++) {
                    // 实时检查开关状态
                    if (!groupStates.get(groupIndex)) {
                        break; // 开关已关闭，立即退出
                    }
                    
                    // 直接发送命令（无队列）
                    boolean success = sendCommandDirect(groupIndex, i);
                    if (success) {
                        sentCount++;
                    }
                    
                    // 微小延迟，避免CPU占用过高（可调整）
                    try {
                        Thread.sleep(1); // 1ms延迟，实现高频发送
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            
            // 发送结束，更新统计
            long duration = System.currentTimeMillis() - startTime;
            long finalCount = sentCount;
            mainHandler.post(() -> {
                updateLog(String.format("%s 停止 (发送%d条, 耗时%.1fs)", 
                    group.name, finalCount, duration / 1000.0));
            });
        }, "LoopDetect-" + groupIndex);
        
        groupThreads.put(groupIndex, sendThread);
        sendThread.start();
    }

    /**
     * 停止分组发送
     */
    private void stopGroup(int groupIndex) {
        Group group = GROUPS[groupIndex];
        
        // 第7组（groupIndex=6）后中灯使用特殊处理
        if (groupIndex == 6) {
            stopRearCenterLight();
            return;
        }
        
        // ⚡ 立即停用分组（循环会自动检测并停止）
        groupStates.put(groupIndex, false);
        sendQueueManager.deactivateGroup(groupIndex);
        
        // 判断左右侧（左侧：0,1,4  右侧：2,3,5,6）
        boolean isLeftSide = (groupIndex == 0 || groupIndex == 1 || groupIndex == 4);
        
        // 更新容器样式
        View container = groupContainers.get(groupIndex);
        if (container != null) {
            if (isLeftSide) {
                container.setBackground(ContextCompat.getDrawable(this, R.drawable.tech_button_left_off));
            } else {
                container.setBackground(ContextCompat.getDrawable(this, R.drawable.tech_button_right_off));
            }
        }
        
        // 停止计时器
        stopTimer(groupIndex);
        
        // 等待线程结束（循环检测会立即响应状态变化）
        Thread thread = groupThreads.get(groupIndex);
        if (thread != null) {
            try {
                thread.interrupt();
                thread.join(500); // 最多等待0.5秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 直接发送命令（循环检测模式）
     */
    private boolean sendCommandDirect(int groupId, int cmdIndex) {
        if (cmdIndex < 0 || cmdIndex >= CMD_LIST.length) {
            return false;
        }

        byte[] fullCmd = CMD_LIST[cmdIndex];
        int totalLen;

        // 根据命令索引确定总数据长度
        if (cmdIndex == 2 || cmdIndex == 8) {
            totalLen = 21;  // ADDR3 和 ADDR9 是21字节
        } else if (cmdIndex >= 0 && cmdIndex <= 11) {
            totalLen = 19;  // ADDR1-12 是19字节
        } else {
            totalLen = 8;   // ADDR13-20 是8字节
        }

        // 分割数据
        byte[] header = new byte[4];
        System.arraycopy(fullCmd, 0, header, 0, 4);
        
        int dataLen = totalLen - 5;
        byte[] data = new byte[dataLen];
        System.arraycopy(fullCmd, 5, data, 0, dataLen);

        // 直接发送（无队列）
        return sendQueueManager.sendCommand(groupId, header, data);
    }
    
    /**
     * 启动后中灯（使用专用串口 /dev/ttyUSB1）
     */
    private void startRearCenterLight() {
        // 打开后中灯专用串口
        try {
            rearCenterLightPort = new SerialPortHelper();
            // 波特率: 115200 (用户提到11520，应该是115200)
            // 校验位: NONE (0)
            // 数据位: 8
            // 停止位: 1
            boolean opened = rearCenterLightPort.open("/dev/ttyUSB1", 115200);
            
            if (!opened) {
                updateLog("❌ 后中灯串口打开失败 (/dev/ttyUSB1)");
                showToast("❌ 后中灯串口打开失败");
                groupStates.put(6, false);
                return;
            }
            
            updateLog("✅ 后中灯串口已打开 (/dev/ttyUSB1, 115200, NONE, 1bit)");
            
            // 更新容器样式 (后中灯使用右侧样式)
            View container = groupContainers.get(6);
            if (container != null) {
                container.setBackground(ContextCompat.getDrawable(this, R.drawable.tech_button_right_on));
            }
            
            // 启动计时器
            startTimer(6);
            
            // 后中灯命令数据
            final byte[] rearCenterLightCmd = new byte[] {
                (byte)0x01, (byte)0x10, (byte)0x80, (byte)0x01, (byte)0x00, (byte)0x05, (byte)0x0A, 
                (byte)0x01, (byte)0x08, (byte)0x10, (byte)0x01, (byte)0x01, (byte)0x00, (byte)0x04, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xA6, (byte)0x71
            };
            
            // 创建循环发送线程
            rearCenterLightThread = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                long sentCount = 0;
                
                while (groupStates.get(6)) {
                    try {
                        // 发送命令
                        boolean success = rearCenterLightPort.send(rearCenterLightCmd);
                        if (success) {
                            sentCount++;
                        }
                        
                        // 延迟，避免发送过快
                        Thread.sleep(10); // 10ms延迟
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                
                // 发送结束，更新统计
                long duration = System.currentTimeMillis() - startTime;
                long finalCount = sentCount;
                mainHandler.post(() -> {
                    updateLog(String.format("后中灯停止 (发送%d条, 耗时%.1fs)", 
                        finalCount, duration / 1000.0));
                });
            }, "RearCenterLight-Thread");
            
            rearCenterLightThread.start();
            updateLog("🟢 后中灯开启，循环发送...");
            
        } catch (Exception e) {
            updateLog("❌ 后中灯启动异常: " + e.getMessage());
            groupStates.put(6, false);
        }
    }
    
    /**
     * 停止后中灯
     */
    private void stopRearCenterLight() {
        // 停止状态
        groupStates.put(6, false);
        
        // 停止线程
        if (rearCenterLightThread != null && rearCenterLightThread.isAlive()) {
            try {
                rearCenterLightThread.interrupt();
                rearCenterLightThread.join(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rearCenterLightThread = null;
        }
        
        // 关闭串口
        if (rearCenterLightPort != null) {
            rearCenterLightPort.close();
            rearCenterLightPort = null;
            updateLog("✅ 后中灯串口已关闭");
        }
        
        // 更新容器样式
        View container = groupContainers.get(6);
        if (container != null) {
            container.setBackground(ContextCompat.getDrawable(this, R.drawable.tech_button_right_off));
        }
        
        // 停止计时器
        stopTimer(6);
        
        updateLog("🔴 后中灯关闭");
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
            String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String logLine = "[" + timestamp + "] " + log + "\n";
            
            String currentText = tvLog.getText().toString();
            
            // 限制日志条目
            String[] lines = currentText.split("\n");
            if (lines.length > 30) {
                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 30; i < lines.length; i++) {
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
        
        // 停止统计更新器
        if (statsUpdateRunnable != null) {
            mainHandler.removeCallbacks(statsUpdateRunnable);
        }
        
        // 停止所有发送线程和计时器
        for (int i = 0; i < GROUPS.length; i++) {
            if (groupStates.get(i)) {
                stopGroup(i);
            }
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

