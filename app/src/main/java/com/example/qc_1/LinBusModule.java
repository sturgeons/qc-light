package com.example.qc_1;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * LR-802T：RS485 转 LIN。
 * RS485 侧保持出厂 115200 8N1、ID=1。
 * LIN 侧设为 10417、主机模式，由模块把主机发送帧转成 LIN。
 */
public final class LinBusModule {
    private static final String TAG = "LinBusModule";

    public static final byte RS485_ID = 0x01;
    /** 手册命令 0x02 的取值 3：10417 */
    private static final byte LIN_BAUD_10417 = 0x03;
    /** 手册命令 0x03 的取值 0：主机模式 */
    private static final byte MODE_MASTER = 0x00;
    /** 主机发送时模块自己算校验和 */
    private static final byte CHECKSUM_PLACEHOLDER = 0x00;
    /** 0x01 标准校验，0x02 增强校验 */
    public static final byte CHECKSUM_CLASSIC = 0x01;
    public static final byte CHECKSUM_ENHANCED = 0x02;

    private static final byte[] CMD_LIN_BAUD = {
            RS485_ID, 0x3C, 0x02, LIN_BAUD_10417
    };
    private static final byte[] CMD_MASTER = {
            RS485_ID, 0x3C, 0x03, MODE_MASTER
    };

    private static final Object LOCK = new Object();
    private static volatile boolean configured = false;

    public interface Logger {
        void log(String message);
    }

    private LinBusModule() {
    }

    /**
     * 把模块设为 LIN 10417、主机模式。只在初始化按钮里调用。
     * 手册：3C 配置断电保存，指示灯快闪后模块自行软重启，重启后生效。应用不需要给模块断电。
     */
    public static boolean ensureMaster10417(SerialPortHelper port, Logger logger) {
        if (port == null || !port.isOpen()) {
            log(logger, "❌ LIN模块串口未打开，无法设置 10417 主机模式");
            return false;
        }
        synchronized (LOCK) {
            try {
                String ver = queryVersion(port, 700);
                if (ver != null && isTargetConfig(ver)) {
                    configured = true;
                    log(logger, "✅ LIN模块已是 10417 主机模式");
                    return true;
                }
                if (ver != null) {
                    log(logger, "当前模块配置与目标不一致，开始改写");
                } else {
                    log(logger, "未读到版本信息，按手册写入 LIN 10417 主机模式");
                }

                log(logger, "📤 设置 LIN 波特率 10417: 01 3C 02 03");
                boolean baudOk = waitConfigAck(port, CMD_LIN_BAUD, (byte) 0x02, 800, logger);
                // 参数断电保存、重启生效；波特率指令后留出重启时间再写下一条
                Thread.sleep(1500);

                log(logger, "📤 设置主机模式: 01 3C 03 00");
                boolean modeOk = false;
                for (int i = 0; i < 3 && !modeOk; i++) {
                    modeOk = waitConfigAck(port, CMD_MASTER, (byte) 0x03, 800, logger);
                    if (!modeOk) {
                        Thread.sleep(500);
                    }
                }
                // 模式指令会让模块指示灯快闪并自行软重启，这里只等待它起来
                log(logger, "模块正在自行软重启，不用断电，等它起来后再发灯控");
                Thread.sleep(2000);

                configured = baudOk && modeOk;
                if (configured) {
                    log(logger, "✅ LIN 已设为 10417、主机模式");
                } else {
                    log(logger, "⚠️ LIN 配置未收到成功回复（波特率="
                            + (baudOk ? "成功" : "未确认")
                            + "，主机模式=" + (modeOk ? "成功" : "未确认") + "）");
                }
                return configured;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log(logger, "❌ LIN模块配置被中断");
                return false;
            } finally {
                port.setOnDataReceiveListener(null);
            }
        }
    }

    /**
     * 主机模式发送。固定 14 字节：01 3A 长度 ID 数据8字节 校验和 校验方式。
     * 数据不足 8 字节补 0，校验和填 0，由模块计算。
     */
    public static byte[] buildMasterFrame(int linId, byte[] data, int dataLen, byte checksumMode) {
        return buildMasterFrame(linId, data, dataLen, checksumMode, 0);
    }

    private static byte[] buildMasterFrame(int linId, byte[] data, int dataLen, byte checksumMode, int dataOffset) {
        byte[] frame = new byte[14];
        int len = data == null ? 0 : Math.max(0, Math.min(dataLen, 8));
        frame[0] = RS485_ID;
        frame[1] = 0x3A;
        frame[2] = (byte) len;
        frame[3] = (byte) (linId & 0x3F);
        if (data != null && len > 0) {
            System.arraycopy(data, dataOffset, frame, 4, len);
        }
        frame[12] = CHECKSUM_PLACEHOLDER;
        frame[13] = checksumMode;
        return frame;
    }

    public static boolean sendMaster(SerialPortHelper port, int linId, byte[] data, int dataLen, byte checksumMode) {
        return port != null && port.send(buildMasterFrame(linId, data, dataLen, checksumMode));
    }

    /**
     * 图莫斯 1.4「LIN写数据」转成 485 转 LIN 主机发送帧。
     * 原帧：地址 10 通道 LIN_ID 寄存器数 字节数 校验模式 长度 数据 CRC16
     * 校验模式 0 标准 → 0x01，1 增强 → 0x02。校验和填 0，由模块计算。
     */
    public static byte[] fromToomossWrite(byte[] frame) {
        if (frame == null || frame.length < 11 || (frame[1] & 0xFF) != 0x10) {
            return null;
        }
        int dataLen = frame[8] & 0xFF;
        if (dataLen > 8 || frame.length < 9 + dataLen + 2) {
            return null;
        }
        int linId = frame[3] & 0x3F;
        int checkMode = frame[7] & 0xFF;
        byte checksumMode = checkMode == 0 ? CHECKSUM_CLASSIC : CHECKSUM_ENHANCED;
        return buildMasterFrame(linId, frame, dataLen, checksumMode, 9);
    }

    private static boolean isTargetConfig(String ver) {
        String text = ver.toLowerCase();
        boolean baudOk = text.contains("lin_baund:10417") || text.contains("lin_baud:10417");
        boolean masterOk = text.contains("lin_mode:master");
        return baudOk && masterOk;
    }

    private static String queryVersion(SerialPortHelper port, int timeoutMs) throws InterruptedException {
        byte[] request = "VER\r\n".getBytes(StandardCharsets.US_ASCII);
        byte[] response = exchange(port, request, timeoutMs, data -> {
            String text = new String(data, StandardCharsets.US_ASCII).toLowerCase();
            return text.contains("lin_mode");
        });
        if (response.length == 0) {
            return null;
        }
        String text = new String(response, StandardCharsets.US_ASCII);
        Log.i(TAG, "VER: " + text.replace('\r', ' ').replace('\n', ' '));
        if (!text.toLowerCase().contains("lin_mode")) {
            return null;
        }
        return text;
    }

    private static boolean waitConfigAck(SerialPortHelper port, byte[] cmd, byte command, int timeoutMs, Logger logger)
            throws InterruptedException {
        byte[] response = exchange(port, cmd, timeoutMs, data -> findStatus(data, command) >= 0);
        int status = findStatus(response, command);
        if (response.length > 0) {
            log(logger, "模块回复: " + toHex(response, response.length));
        }
        if (status == 0x00) {
            return true;
        }
        if (status > 0) {
            log(logger, "❌ 配置失败，状态=0x" + String.format("%02X", status));
        } else {
            log(logger, "⚠️ 未收到 01 2C " + String.format("%02X", command) + " 00");
        }
        return false;
    }

    /** @return 配置状态字节；没有对应回复时返回 -1 */
    private static int findStatus(byte[] data, byte command) {
        for (int i = 0; i + 3 < data.length; i++) {
            if (data[i] == RS485_ID && data[i + 1] == 0x2C && data[i + 2] == command) {
                return data[i + 3] & 0xFF;
            }
        }
        return -1;
    }

    private interface Ready {
        boolean test(byte[] data);
    }

    private static byte[] exchange(SerialPortHelper port, byte[] request, int timeoutMs, Ready ready)
            throws InterruptedException {
        ByteArrayOutputStream acc = new ByteArrayOutputStream();
        port.setOnDataReceiveListener((buffer, size) -> {
            synchronized (acc) {
                acc.write(buffer, 0, size);
                acc.notifyAll();
            }
        });
        if (!port.send(request)) {
            return new byte[0];
        }
        long deadline = System.currentTimeMillis() + timeoutMs;
        synchronized (acc) {
            while (true) {
                byte[] snapshot = acc.toByteArray();
                if (ready.test(snapshot)) {
                    return snapshot;
                }
                long remain = deadline - System.currentTimeMillis();
                if (remain <= 0) {
                    return snapshot;
                }
                acc.wait(remain);
            }
        }
    }

    private static void log(Logger logger, String message) {
        Log.i(TAG, message);
        if (logger != null) {
            logger.log(message);
        }
    }

    public static String toHex(byte[] data) {
        if (data == null) {
            return "";
        }
        return toHex(data, data.length);
    }

    private static String toHex(byte[] data, int size) {
        StringBuilder sb = new StringBuilder();
        int n = Math.min(size, data.length);
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(String.format("%02X", data[i]));
        }
        return sb.toString();
    }
}
