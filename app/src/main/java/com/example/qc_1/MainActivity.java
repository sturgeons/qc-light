package com.example.qc_1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SerialPortManager serialPortManager;
    
    // UI组件
    private Spinner spinnerRS485BaudRate;
    private Spinner spinnerUartBaudRate;
    private Button btnOpenRS485;
    private Button btnCloseRS485;
    private Button btnOpenUart;
    private Button btnCloseUart;
    private Button btnClearRS485;
    private Button btnClearUart;
    private TextView tvRS485Data;
    private TextView tvUartData;
    private TextView tvRS485Status;
    private TextView tvUartStatus;
    private ScrollView scrollViewRS485;
    private ScrollView scrollViewUart;
    
    // 波特率选项
    private final int[] baudRates = {1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400, 460800, 500000};
    private int selectedRS485BaudRate = 9600;
    private int selectedUartBaudRate = 9600;
    
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
    
    // 最大显示字符数（防止内存溢出和ANR）
    private static final int MAX_TEXT_LENGTH = 50000;
    
    // UI更新节流机制
    private Handler uiUpdateHandler;
    private StringBuilder rs485Buffer = new StringBuilder();
    private StringBuilder uartBuffer = new StringBuilder();
    private Runnable rs485UpdateRunnable;
    private Runnable uartUpdateRunnable;
    private static final int UI_UPDATE_DELAY = 100; // 100ms更新一次UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initUIUpdateHandler();
        initSerialPort();
        initViews();
    }
    
    private void initUIUpdateHandler() {
        uiUpdateHandler = new Handler(Looper.getMainLooper());
        
        // RS485数据更新任务
        rs485UpdateRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (rs485Buffer) {
                    if (rs485Buffer.length() > 0) {
                        updateRS485TextView(rs485Buffer.toString());
                        rs485Buffer.setLength(0);
                    }
                }
            }
        };
        
        // UART数据更新任务
        uartUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (uartBuffer) {
                    if (uartBuffer.length() > 0) {
                        updateUartTextView(uartBuffer.toString());
                        uartBuffer.setLength(0);
                    }
                }
            }
        };
    }
    
    private void initViews() {
        // RS485控件
        spinnerRS485BaudRate = findViewById(R.id.spinnerRS485BaudRate);
        btnOpenRS485 = findViewById(R.id.btnOpenRS485);
        btnCloseRS485 = findViewById(R.id.btnCloseRS485);
        btnClearRS485 = findViewById(R.id.btnClearRS485);
        tvRS485Data = findViewById(R.id.tvRS485Data);
        tvRS485Status = findViewById(R.id.tvRS485Status);
        scrollViewRS485 = findViewById(R.id.scrollViewRS485);
        
        // UART控件
        spinnerUartBaudRate = findViewById(R.id.spinnerUartBaudRate);
        btnOpenUart = findViewById(R.id.btnOpenUart);
        btnCloseUart = findViewById(R.id.btnCloseUart);
        btnClearUart = findViewById(R.id.btnClearUart);
        tvUartData = findViewById(R.id.tvUartData);
        tvUartStatus = findViewById(R.id.tvUartStatus);
        scrollViewUart = findViewById(R.id.scrollViewUart);
        
        // 设置波特率选择器
        setupBaudRateSpinner(spinnerRS485BaudRate, true);
        setupBaudRateSpinner(spinnerUartBaudRate, false);
        
        // 设置按钮监听器
        btnOpenRS485.setOnClickListener(v -> openRS485());
        btnCloseRS485.setOnClickListener(v -> closeRS485());
        btnOpenUart.setOnClickListener(v -> openUart());
        btnCloseUart.setOnClickListener(v -> closeUart());
        btnClearRS485.setOnClickListener(v -> tvRS485Data.setText(""));
        btnClearUart.setOnClickListener(v -> tvUartData.setText(""));
        
        // 命令发送页面按钮
        findViewById(R.id.btnOpenCommandSend).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CommandSendActivity.class);
            startActivity(intent);
        });
        
        // 开关控制页面按钮
        findViewById(R.id.btnOpenSwitchControl).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SwitchControlActivity.class);
            startActivity(intent);
        });
        
        updateButtonStates();
    }
    
    private void setupBaudRateSpinner(Spinner spinner, boolean isRS485) {
        String[] baudRateStrings = new String[baudRates.length];
        for (int i = 0; i < baudRates.length; i++) {
            baudRateStrings[i] = String.valueOf(baudRates[i]);
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, baudRateStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        // 默认选择9600
        spinner.setSelection(3);
        
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isRS485) {
                    selectedRS485BaudRate = baudRates[position];
                } else {
                    selectedUartBaudRate = baudRates[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    private void initSerialPort() {
        serialPortManager = ((MyApplication) getApplication()).getSerialPortManager();
        serialPortManager.setDataCallback(new SerialPortManager.DataCallback() {
            @Override
            public void onRS485DataReceived(String data) {
                appendRS485Data(data);
            }

            @Override
            public void onUartDataReceived(String data) {
                appendUartData(data);
            }

            @Override
            public void onError(String error) {
                showToast(error);
            }
        });
    }
    
    private void openRS485() {
        boolean success = serialPortManager.openRS485(selectedRS485BaudRate);
        if (success) {
            showToast("RS485已打开，波特率: " + selectedRS485BaudRate);
            tvRS485Status.setText("状态: 已连接 (" + selectedRS485BaudRate + " bps)");
            tvRS485Status.setTextColor(0xFF4CAF50); // 绿色
        } else {
            showToast("RS485打开失败");
            tvRS485Status.setText("状态: 未连接");
            tvRS485Status.setTextColor(0xFFF44336); // 红色
        }
        updateButtonStates();
    }
    
    private void closeRS485() {
        serialPortManager.closeRS485();
        showToast("RS485已关闭");
        tvRS485Status.setText("状态: 未连接");
        tvRS485Status.setTextColor(0xFF9E9E9E); // 灰色
        updateButtonStates();
    }
    
    private void openUart() {
        boolean success = serialPortManager.openUart(selectedUartBaudRate);
        if (success) {
            showToast("UART已打开，波特率: " + selectedUartBaudRate);
            tvUartStatus.setText("状态: 已连接 (" + selectedUartBaudRate + " bps)");
            tvUartStatus.setTextColor(0xFF4CAF50); // 绿色
        } else {
            showToast("UART打开失败");
            tvUartStatus.setText("状态: 未连接");
            tvUartStatus.setTextColor(0xFFF44336); // 红色
        }
        updateButtonStates();
    }
    
    private void closeUart() {
        serialPortManager.closeUart();
        showToast("UART已关闭");
        tvUartStatus.setText("状态: 未连接");
        tvUartStatus.setTextColor(0xFF9E9E9E); // 灰色
        updateButtonStates();
    }
    
    private void appendRS485Data(String data) {
        String timestamp = timeFormat.format(new Date());
        String newLine = "[" + timestamp + "] " + data + "\n\n";
        
        synchronized (rs485Buffer) {
            rs485Buffer.append(newLine);
        }
        
        // 移除之前的更新任务，然后延迟执行新任务（节流）
        uiUpdateHandler.removeCallbacks(rs485UpdateRunnable);
        uiUpdateHandler.postDelayed(rs485UpdateRunnable, UI_UPDATE_DELAY);
    }
    
    private void updateRS485TextView(String newData) {
        String currentText = tvRS485Data.getText().toString();
        
        // 如果当前文本太长，删除前面的内容
        if (currentText.length() > MAX_TEXT_LENGTH) {
            int cutPosition = currentText.length() - MAX_TEXT_LENGTH / 2;
            // 找到下一个换行符，保持数据完整性
            int newlinePos = currentText.indexOf("\n\n", cutPosition);
            if (newlinePos > 0) {
                currentText = currentText.substring(newlinePos + 2);
            }
        }
        
        tvRS485Data.setText(currentText + newData);
        
        // 自动滚动到底部
        scrollViewRS485.post(() -> scrollViewRS485.fullScroll(View.FOCUS_DOWN));
    }
    
    private void appendUartData(String data) {
        String timestamp = timeFormat.format(new Date());
        String newLine = "[" + timestamp + "] " + data + "\n\n";
        
        synchronized (uartBuffer) {
            uartBuffer.append(newLine);
        }
        
        // 移除之前的更新任务，然后延迟执行新任务（节流）
        uiUpdateHandler.removeCallbacks(uartUpdateRunnable);
        uiUpdateHandler.postDelayed(uartUpdateRunnable, UI_UPDATE_DELAY);
    }
    
    private void updateUartTextView(String newData) {
        String currentText = tvUartData.getText().toString();
        
        // 如果当前文本太长，删除前面的内容
        if (currentText.length() > MAX_TEXT_LENGTH) {
            int cutPosition = currentText.length() - MAX_TEXT_LENGTH / 2;
            // 找到下一个换行符，保持数据完整性
            int newlinePos = currentText.indexOf("\n\n", cutPosition);
            if (newlinePos > 0) {
                currentText = currentText.substring(newlinePos + 2);
            }
        }
        
        tvUartData.setText(currentText + newData);
        
        // 自动滚动到底部
        scrollViewUart.post(() -> scrollViewUart.fullScroll(View.FOCUS_DOWN));
    }
    
    private void updateButtonStates() {
        if (serialPortManager == null) {
            return;
        }
        
        boolean rs485Open = serialPortManager.isRS485Open();
        boolean uartOpen = serialPortManager.isUartOpen();
        
        btnOpenRS485.setEnabled(!rs485Open);
        btnCloseRS485.setEnabled(rs485Open);
        spinnerRS485BaudRate.setEnabled(!rs485Open);
        
        btnOpenUart.setEnabled(!uartOpen);
        btnCloseUart.setEnabled(uartOpen);
        spinnerUartBaudRate.setEnabled(!uartOpen);
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理Handler回调，防止内存泄漏
        if (uiUpdateHandler != null) {
            uiUpdateHandler.removeCallbacks(rs485UpdateRunnable);
            uiUpdateHandler.removeCallbacks(uartUpdateRunnable);
        }
        
        if (serialPortManager != null) {
            serialPortManager.closeAll();
        }
    }
}