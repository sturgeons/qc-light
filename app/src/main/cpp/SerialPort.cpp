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

static speed_t getBaudRate(jint baudRate) {
    switch (baudRate) {
        case 0: return B0;
        case 50: return B50;
        case 75: return B75;
        case 110: return B110;
        case 134: return B134;
        case 150: return B150;
        case 200: return B200;
        case 300: return B300;
        case 600: return B600;
        case 1200: return B1200;
        case 1800: return B1800;
        case 2400: return B2400;
        case 4800: return B4800;
        case 9600: return B9600;
        case 19200: return B19200;
        case 38400: return B38400;
        case 57600: return B57600;
        case 115200: return B115200;
        case 230400: return B230400;
        case 460800: return B460800;
        case 500000: return B500000;
        case 576000: return B576000;
        case 921600: return B921600;
        case 1000000: return B1000000;
        case 1152000: return B1152000;
        case 1500000: return B1500000;
        case 2000000: return B2000000;
        case 2500000: return B2500000;
        case 3000000: return B3000000;
        case 3500000: return B3500000;
        case 4000000: return B4000000;
        default: return B9600;
    }
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

    // 设置波特率
    speed_t speed = getBaudRate(baudRate);
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

    // 设置新的波特率
    speed_t speed = getBaudRate(baudRate);
    cfsetispeed(&options, speed);
    cfsetospeed(&options, speed);

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

    // 应用新设置
    tcflush(fd, TCIFLUSH);
    if (tcsetattr(fd, TCSANOW, &options) != 0) {
        LOGE("Failed to set baud rate to %d with parity %d", baudRate, parity);
        return JNI_FALSE;
    }

    LOGI("Baud rate changed to: %d, parity: %d", baudRate, parity);
    return JNI_TRUE;
}
