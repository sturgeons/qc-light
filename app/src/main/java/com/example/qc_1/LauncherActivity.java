package com.example.qc_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 启动页面 - 主界面
 * 提供CX483和U625两个控制页面的入口
 */
public class LauncherActivity extends AppCompatActivity {

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
    }
}

