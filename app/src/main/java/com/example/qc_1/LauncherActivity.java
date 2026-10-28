package com.example.qc_1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 启动页面 - 主界面
 * 提供CX483和U625两个控制页面的入口
 */
public class LauncherActivity extends AppCompatActivity {

    private int titleTapCount;
    private long lastTitleTapAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        initViews();
    }

    private void initViews() {
        // CX483控制页面按钮（车后灯控制）
        findViewById(R.id.btnOpenCX483).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LauncherActivity.this, SwitchControlActivity.class);
                startActivity(intent);
            }
        });

        // U625格栅灯控制页面按钮
        findViewById(R.id.btnOpenU625).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LauncherActivity.this, U625ControlActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnCallSupport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:19242828258"));
                startActivity(dial);
            }
        });

        findViewById(R.id.tvConsoleTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = SystemClock.uptimeMillis();
                if (now - lastTitleTapAt > 1000) {
                    titleTapCount = 0;
                }
                lastTitleTapAt = now;
                titleTapCount++;
                if (titleTapCount >= 3) {
                    titleTapCount = 0;
                    startActivity(new Intent(LauncherActivity.this, DebugActivity.class));
                }
            }
        });
    }
}

