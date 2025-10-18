package com.example.qc_1;

import android.util.Log;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发送管理器
 * 循环检测方式：不使用队列，通过高频循环检测开关状态来发送数据
 * 优点：无队列溢出、无丢包、响应快、逻辑简单
 */
public class SendQueueManager {
    private static final String TAG = "SendManager";
    
    private SerialPortManager serialPortManager;
    
    // 记录当前活跃的分组（用于快速检查发送权限）
    private Set<Integer> activeGroups = ConcurrentHashMap.newKeySet();
    
    // 统计信息
    private volatile long totalSent = 0;
    private volatile long lastReportTime = System.currentTimeMillis();
    private volatile long lastReportCount = 0;
    
    public SendQueueManager(SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }
    
    /**
     * 启动（保持接口兼容，实际不需要启动线程）
     */
    public synchronized void start() {
        Log.i(TAG, "✅ 发送管理器已启动 (循环检测模式)");
    }
    
    /**
     * 停止（保持接口兼容）
     */
    public synchronized void stop() {
        activeGroups.clear();
        Log.i(TAG, "📊 最终统计: 总发送=" + totalSent);
    }
    
    /**
     * 发送命令（同步方式，直接发送）
     * @param groupId 开关组ID
     * @param header 命令头部
     * @param data 命令数据
     * @return 是否成功发送
     */
    public boolean sendCommand(int groupId, byte[] header, byte[] data) {
        // 检查分组是否活跃
        if (!activeGroups.contains(groupId)) {
            return false;
        }
        
        // 直接发送（同步方式，串口已有synchronized保护）
        boolean success = serialPortManager.sendToRS485WithProtocol(header, data);
        
        if (success) {
            totalSent++;
            
            // 每10000次输出一次速率统计
            if (totalSent % 10000 == 0) {
                long now = System.currentTimeMillis();
                long duration = now - lastReportTime;
                long count = totalSent - lastReportCount;
                if (duration > 0) {
                    double speed = count * 1000.0 / duration;
                    Log.i(TAG, String.format("📊 发送: %d条 | 速率: %.1f条/秒", totalSent, speed));
                }
                lastReportTime = now;
                lastReportCount = totalSent;
            }
        }
        
        return success;
    }
    
    /**
     * 激活分组（允许发送）
     * @param groupId 分组ID
     */
    public void activateGroup(int groupId) {
        activeGroups.add(groupId);
        Log.i(TAG, "✅ 分组 " + groupId + " 已激活");
    }
    
    /**
     * 停用分组（禁止发送）
     * @param groupId 分组ID
     */
    public void deactivateGroup(int groupId) {
        activeGroups.remove(groupId);
        Log.i(TAG, "⏸️ 分组 " + groupId + " 已停用");
    }
    
    /**
     * 检查分组是否活跃
     * @param groupId 分组ID
     * @return 是否活跃
     */
    public boolean isGroupActive(int groupId) {
        return activeGroups.contains(groupId);
    }
    
    /**
     * 清空所有待发送命令（保持接口兼容，循环模式无需清空）
     * @return 清除的命令数量（始终返回0）
     */
    public int clearAllCommands() {
        // 循环模式不需要清空队列
        return 0;
    }
    
    /**
     * 获取队列统计信息（简化版）
     */
    public String getStatistics() {
        long now = System.currentTimeMillis();
        long duration = now - lastReportTime;
        long count = totalSent - lastReportCount;
        double speed = duration > 0 ? count * 1000.0 / duration : 0;
        return String.format("总发送:%d | 当前速率:%.1f条/秒", totalSent, speed);
    }
    
    /**
     * 获取当前队列使用率（循环模式始终返回0）
     */
    public int getQueueUsagePercent() {
        return 0;
    }
    
    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        totalSent = 0;
        lastReportTime = System.currentTimeMillis();
        lastReportCount = 0;
        Log.i(TAG, "统计信息已重置");
    }
    
    /**
     * 获取总发送数量
     */
    public long getTotalSent() {
        return totalSent;
    }
    
    /**
     * 获取总丢弃数量（循环模式无丢弃）
     */
    public long getTotalDropped() {
        return 0;
    }
    
    /**
     * 检查是否正在运行（保持接口兼容）
     */
    public boolean isRunning() {
        return true;
    }
}
