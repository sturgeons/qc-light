#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <android/log.h>
#include <string.h>
#include <sys/ioctl.h>
#include <errno.h>

#define TAG "SerialPort"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// 非标准波特率（灯控 break 使用 363636）走 termios2 / BOTHER
#ifndef BOTHER
#define BOTHER 0010000
#endif
#ifndef CBAUD
#define CBAUD 0010017
#endif

#ifndef TIOCGRS485
#define TIOCGRS485 0x542E
#endif
#ifndef TIOCSRS485
#define TIOCSRS485 0x542F
#endif
#define SER_RS485_ENABLED        (1 << 0)
#define SER_RS485_RTS_ON_SEND    (1 << 1)
#define SER_RS485_RTS_AFTER_SEND (1 << 2)

struct serial_rs485 {
    unsigned int flags;
    unsigned int delay_rts_before_send;
    unsigned int delay_rts_after_send;
    unsigned int padding[5];
};

static bool lookupStandardBaud(jint baudRate, speed_t *speed) {
    switch (baudRate) {
        case 0: *speed = B0; return true;
        case 50: *speed = B50; return true;
        case 75: *speed = B75; return true;
        case 110: *speed = B110; return true;
        case 134: *speed = B134; return true;
        case 150: *speed = B150; return true;
        case 200: *speed = B200; return true;
        case 300: *speed = B300; return true;
        case 600: *speed = B600; return true;
        case 1200: *speed = B1200; return true;
        case 1800: *speed = B1800; return true;
        case 2400: *speed = B2400; return true;
        case 4800: *speed = B4800; return true;
        case 9600: *speed = B9600; return true;
        case 19200: *speed = B19200; return true;
        case 38400: *speed = B38400; return true;
        case 57600: *speed = B57600; return true;
        case 115200: *speed = B115200; return true;
        case 230400: *speed = B230400; return true;
        case 460800: *speed = B460800; return true;
        case 500000: *speed = B500000; return true;
        case 576000: *speed = B576000; return true;
        case 921600: *speed = B921600; return true;
        case 1000000: *speed = B1000000; return true;
        case 1152000: *speed = B1152000; return true;
        case 1500000: *speed = B1500000; return true;
        case 2000000: *speed = B2000000; return true;
        case 2500000: *speed = B2500000; return true;
        case 3000000: *speed = B3000000; return true;
        case 3500000: *speed = B3500000; return true;
        case 4000000: *speed = B4000000; return true;
        default: return false;
    }
}

static void applyParity(tcflag_t *cflag, jint parity) {
    switch (parity) {
        case 1: // ODD
            *cflag |= PARENB;
            *cflag |= PARODD;
            break;
        case 2: // EVEN
            *cflag |= PARENB;
            *cflag &= ~PARODD;
            break;
        case 0:
        default:
            *cflag &= ~PARENB;
            break;
    }
}

static bool setCustomBaudRate(int fd, jint baudRate, jint parity) {
    struct termios2 tio;
    memset(&tio, 0, sizeof(tio));
    if (ioctl(fd, TCGETS2, &tio) != 0) {
        LOGE("TCGETS2 failed: %s", strerror(errno));
        return false;
    }
    applyParity(&tio.c_cflag, parity);
    tio.c_cflag &= ~CBAUD;
    tio.c_cflag |= BOTHER;
    tio.c_ispeed = baudRate;
    tio.c_ospeed = baudRate;
    if (ioctl(fd, TCSETS2, &tio) != 0) {
        LOGE("TCSETS2 failed, baud %d: %s", baudRate, strerror(errno));
        return false;
    }
    LOGI("Custom baud rate set: %d", baudRate);
    return true;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_qc_11_SerialPortHelper_nativeOpen(
        JNIEnv *env,
        jobject thiz,
        jstring path,
        jint baudRate,
        jint dataBits,
        jint stopBits,
        jint parity) {

    const char *pathStr = env->GetStringUTFChars(path, nullptr);
    LOGI("Opening serial port: %s, baudrate: %d", pathStr, baudRate);

    // 打开串口设备
    int fd = open(pathStr, O_RDWR | O_NOCTTY | O_NDELAY);
    if (fd == -1) {
        LOGE("Failed to open device: %s, error: %s", pathStr, strerror(errno));
        env->ReleaseStringUTFChars(path, pathStr);
        return nullptr;
    }

    // 设置为阻塞模式
    if (fcntl(fd, F_SETFL, 0) < 0) {
        LOGE("Failed to set blocking mode");
        close(fd);
        env->ReleaseStringUTFChars(path, pathStr);
        return nullptr;
    }

    // 获取并设置串口参数
    struct termios options;
    if (tcgetattr(fd, &options) != 0) {
        LOGE("Failed to get serial port attributes");
        close(fd);
        env->ReleaseStringUTFChars(path, pathStr);
        return nullptr;
    }

    // 标准波特率先写入 termios；363636 等非标准值稍后用 BOTHER 覆盖
    speed_t speed = B9600;
    bool standardBaud = lookupStandardBaud(baudRate, &speed);
    cfsetispeed(&options, speed);
    cfsetospeed(&options, speed);

    // 设置数据位
    options.c_cflag &= ~CSIZE;
    switch (dataBits) {
        case 5:
            options.c_cflag |= CS5;
            break;
        case 6:
            options.c_cflag |= CS6;
            break;
        case 7:
            options.c_cflag |= CS7;
            break;
        case 8:
        default:
            options.c_cflag |= CS8;
            break;
    }

    // 设置奇偶校验位
    switch (parity) {
        case 0: // NONE
            options.c_cflag &= ~PARENB;
            break;
        case 1: // ODD
            options.c_cflag |= PARENB;
            options.c_cflag |= PARODD;
            break;
        case 2: // EVEN
            options.c_cflag |= PARENB;
            options.c_cflag &= ~PARODD;
            break;
        default:
            options.c_cflag &= ~PARENB;
            break;
    }

    // 设置停止位
    if (stopBits == 2) {
        options.c_cflag |= CSTOPB;
    } else {
        options.c_cflag &= ~CSTOPB;
    }

    // 原始模式
    options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
    options.c_oflag &= ~OPOST;
    options.c_iflag &= ~(IXON | IXOFF | IXANY | INLCR | ICRNL | IGNCR);
    
    // 启用接收和本地模式
    options.c_cflag |= (CLOCAL | CREAD);

    // 设置超时
    options.c_cc[VTIME] = 1; // 0.1秒
    options.c_cc[VMIN] = 0;

    // 应用设置
    tcflush(fd, TCIFLUSH);
    if (tcsetattr(fd, TCSANOW, &options) != 0) {
        LOGE("Failed to set serial port attributes");
        close(fd);
        env->ReleaseStringUTFChars(path, pathStr);
        return nullptr;
    }

    if (!standardBaud && !setCustomBaudRate(fd, baudRate, parity)) {
        LOGE("Failed to set custom baud rate: %d", baudRate);
        close(fd);
        env->ReleaseStringUTFChars(path, pathStr);
        return nullptr;
    }

    env->ReleaseStringUTFChars(path, pathStr);

    // 创建FileDescriptor对象
    jclass fileDescriptorClass = env->FindClass("java/io/FileDescriptor");
    jmethodID constructorId = env->GetMethodID(fileDescriptorClass, "<init>", "()V");
    jobject fileDescriptor = env->NewObject(fileDescriptorClass, constructorId);

    // 设置文件描述符
    jfieldID descriptorId = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    env->SetIntField(fileDescriptor, descriptorId, fd);

    LOGI("Serial port opened successfully, fd: %d", fd);
    return fileDescriptor;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_qc_11_SerialPortHelper_nativeClose(
        JNIEnv *env,
        jobject thiz) {

    jclass serialPortClass = env->GetObjectClass(thiz);
    jfieldID fdFieldId = env->GetFieldID(serialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jobject fileDescriptor = env->GetObjectField(thiz, fdFieldId);

    if (fileDescriptor != nullptr) {
        jclass fileDescriptorClass = env->GetObjectClass(fileDescriptor);
        jfieldID descriptorId = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
        jint fd = env->GetIntField(fileDescriptor, descriptorId);

        if (fd > 0) {
            close(fd);
            LOGI("Serial port closed, fd: %d", fd);
        }
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_qc_11_SerialPortHelper_nativeSetBaudRate(
        JNIEnv *env,
        jobject thiz,
        jobject fileDescriptor,
        jint baudRate,
        jint parity) {

    if (fileDescriptor == nullptr) {
        LOGE("FileDescriptor is null");
        return JNI_FALSE;
    }

    jclass fileDescriptorClass = env->GetObjectClass(fileDescriptor);
    jfieldID descriptorId = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    jint fd = env->GetIntField(fileDescriptor, descriptorId);

    if (fd <= 0) {
        LOGE("Invalid file descriptor");
        return JNI_FALSE;
    }

    struct termios options;
    if (tcgetattr(fd, &options) != 0) {
        LOGE("Failed to get serial port attributes");
        return JNI_FALSE;
    }

    speed_t speed = B9600;
    bool standardBaud = lookupStandardBaud(baudRate, &speed);
    if (!standardBaud) {
        if (!setCustomBaudRate(fd, baudRate, parity)) {
            LOGE("Failed to set custom baud rate to %d", baudRate);
            return JNI_FALSE;
        }
        LOGI("Baud rate changed to: %d, parity: %d", baudRate, parity);
        return JNI_TRUE;
    }

    cfsetispeed(&options, speed);
    cfsetospeed(&options, speed);
    applyParity(&options.c_cflag, parity);

    tcflush(fd, TCIFLUSH);
    if (tcsetattr(fd, TCSANOW, &options) != 0) {
        LOGE("Failed to set baud rate to %d with parity %d", baudRate, parity);
        return JNI_FALSE;
    }

    LOGI("Baud rate changed to: %d, parity: %d", baudRate, parity);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_qc_11_SerialPortHelper_nativeEnableRs485(
        JNIEnv *env,
        jobject thiz,
        jobject fileDescriptor) {

    if (fileDescriptor == nullptr) {
        LOGE("FileDescriptor is null");
        return JNI_FALSE;
    }

    jclass fileDescriptorClass = env->GetObjectClass(fileDescriptor);
    jfieldID descriptorId = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    jint fd = env->GetIntField(fileDescriptor, descriptorId);
    if (fd <= 0) {
        LOGE("Invalid file descriptor");
        return JNI_FALSE;
    }

    struct serial_rs485 rs485;
    memset(&rs485, 0, sizeof(rs485));
    if (ioctl(fd, TIOCGRS485, &rs485) == 0) {
        rs485.flags |= SER_RS485_ENABLED;
        if ((rs485.flags & (SER_RS485_RTS_ON_SEND | SER_RS485_RTS_AFTER_SEND)) == 0) {
            rs485.flags |= SER_RS485_RTS_ON_SEND;
        }
    } else {
        LOGI("TIOCGRS485 unsupported (%s), applying default RS485 flags", strerror(errno));
        memset(&rs485, 0, sizeof(rs485));
        rs485.flags = SER_RS485_ENABLED | SER_RS485_RTS_ON_SEND;
    }

    if (ioctl(fd, TIOCSRS485, &rs485) != 0) {
        LOGE("TIOCSRS485 failed: %s", strerror(errno));
        return JNI_FALSE;
    }

    LOGI("RS485 mode enabled, flags=0x%x", rs485.flags);
    return JNI_TRUE;
}
