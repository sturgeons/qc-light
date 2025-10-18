package com.example.qc_1;

import android.app.Application;

public class MyApplication extends Application {
    private SerialPortManager serialPortManager;

    @Override
    public void onCreate() {
        super.onCreate();
        serialPortManager = new SerialPortManager();
    }

    public SerialPortManager getSerialPortManager() {
        return serialPortManager;
    }
}

