package com.shy.custum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


public class LbActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "hello";
    private TextView mSend_text_dis;
    private Button mBtn_send;
    private Socket mSocket;
    private PrintStream out;
    private ConnectThread mConnectThread;
    private String mip = "192.168.4.1";
    private int mport = 8089;
    private Button mBtn_turn;
    private BufferedReader mBufferedReader;
    private String readStrng;
    private String tempStrng;
    private myHandler mHandler = new myHandler();
    private Timer mTimer;
    private ReceiveDataTask mReceiveDataTask;
    private EditText editText_zb;
    private Button btn_zb;
    private Button button_tz;
    private Button button_tz1;
    private androidx.gridlayout.widget.GridLayout gridLayout1;
    private TextView lb_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lb);
        initView();

        //初始化表格
        gridViews();

        //地图上标注
        mapViews();

        // 读取状态值
        String statusText = StatusManager.getInstance().getStatusText();

        // 初始化 lb_textview
        lb_textview = findViewById(R.id.lb_textview);
        lb_textview.setText(statusText);
        btntzViews();
        btntzViews1();



    }




    @Override
    protected void onStart() {
        super.onStart();
        startTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }

    private class myHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1212:
                    updateGridLayoutData(readStrng);
                    mSend_text_dis.setText(readStrng);
                    break;
            }
        }

        private void updateGridLayoutData(String data) {
            // 解析数据并更新 GridLayout 中的数据
            String[] parts = data.split(":");
            // 提取值并赋给变量
            int n = Integer.parseInt(parts[0]);
            double a = Double.parseDouble(parts[1]);
            double b = Double.parseDouble(parts[2]);
            double c = Double.parseDouble(parts[3]);
            double d = Double.parseDouble(parts[4]);
            updateTextView(n, a, b, c,d);
        }

    }

    private void initView() {
        mSend_text_dis = findViewById(R.id.send_text_dis);
        mBtn_send = findViewById(R.id.btn_send);
        mBtn_turn = findViewById(R.id.btn_turn);


        mBtn_send.setOnClickListener(this);
        mBtn_turn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_turn:
                mip = "10.10.100.254"; // 将 IP 地址设置为定值
                mport = 8899; // 将端口号设置为定值

                if (mSocket == null || !mSocket.isConnected()) {
                    // 如果 mSocket 为空或者未连接到服务器
                    mConnectThread = new ConnectThread(mip, mport);
                    mConnectThread.start();
                } else {
                    // 如果已连接到服务器，则断开连接
                    try {
                        mSocket.close();
                        mSocket = null;
                        mBtn_turn.setText("连接");
                        Toast.makeText(this, "连接已断开", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_send:
                //final String str = mSend_text.getText().toString();
                final String str = lb_textview.getText().toString();
                if (str != null && out != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            out.print(str);
                            out.flush();
                        }
                    }).start();

                }
                break;
        }
    }

    private class ConnectThread extends Thread {
        private String ip;
        private int port;
        public ConnectThread(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
        @Override
        public void run() {
            try {
                mSocket = new Socket(ip, port);
                out = new PrintStream(mSocket.getOutputStream());
                mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtn_turn.setText("断开");
                        Toast.makeText(LbActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LbActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    private class ReceiveDataTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (mBufferedReader != null && (mBufferedReader.ready())) {
                    char[] readbuff = new char[30];
                    byte[] readByte = new byte[30];
                    mBufferedReader.read(readbuff, 0, readbuff.length);
                    tempStrng = String.valueOf(readbuff);
                    readByte = tempStrng.getBytes();
                    readStrng = new String(readByte, 0, readByte.length, "GB2312");
                    Message message = Message.obtain();
                    message.what = 1212;
                    mHandler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void startTimer() {
        Log.i(TAG, "startTimer:");
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mReceiveDataTask == null) {
            mReceiveDataTask = new ReceiveDataTask();
        }
        mTimer.schedule(mReceiveDataTask, 0, 10);
    }

    private void stopTimer() {
        Log.i(TAG, "stopTimer: ");
        if (mReceiveDataTask != null) {
            mReceiveDataTask.cancel();
            mReceiveDataTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    //初始化表格
    private void gridViews(){
        //任务列表
        gridLayout1 = findViewById(R.id.grid_layout1);
        gridLayout1.setColumnCount(4);
        gridLayout1.setRowCount(10);

        for (int i = 0; i < gridLayout1.getRowCount(); i++) {
            for (int j = 0; j < gridLayout1.getColumnCount(); j++) {
                TextView textView1 = new TextView(gridLayout1.getContext());

                // 使用 GridLayout.LayoutParams，而不是 android.widget.GridLayout.LayoutParams
                androidx.gridlayout.widget.GridLayout.LayoutParams params1 =
                        new androidx.gridlayout.widget.GridLayout.LayoutParams();

                // 设置行列下标，和比重
                params1.rowSpec = androidx.gridlayout.widget.GridLayout.spec(i, 1f);
                params1.columnSpec = androidx.gridlayout.widget.GridLayout.spec(j, 1f);

                // 设置文字颜色和背景颜色
                textView1.setTextColor(Color.BLACK);
                textView1.setBackgroundColor(Color.WHITE);

                // 居中显示
                textView1.setGravity(Gravity.CENTER);

                // 设置文字
                if (i == 0 && j == 0) {
                    textView1.setText("序号");
                } else if (i == 0 && j == 1) {
                    textView1.setText("坐标");
                } else if (i == 0 && j == 2) {
                    textView1.setText("水量");
                } else if (i == 0 && j == 3) {
                    textView1.setText("电量");
                }else {
                    //textView1.setText("(" + j + "," + i + ")");
                    //序号1
                    if (i == 1 && j == 0) {
                        textView1.setText("1");
                    } else if (i == 1 && j == 1) {
                        textView1.setText("(116.358103,39.961554)");
                    } else if (i == 1 && j == 2) {
                        textView1.setText("10%");
                    } else if (i == 1 && j == 3) {
                        textView1.setText("20%");
                    }

                    //序号2
                    if (i == 2 && j == 0) {
                        textView1.setText("2");
                    } else if (i == 2 && j == 1) {
                        textView1.setText("(116.558103,39.461554)");
                    } else if (i == 2 && j == 2) {
                        textView1.setText("70%");
                    } else if (i == 2 && j == 3) {
                        textView1.setText("90%");
                    }


                    //序号3
                    if (i == 3 && j == 0) {
                        textView1.setText("3");
                    } else if (i == 3 && j == 1) {
                        textView1.setText("(116.158103,39.161554)");
                    } else if (i == 3 && j == 2) {
                        textView1.setText("50%");
                    } else if (i == 3 && j == 3) {
                        textView1.setText("60%");
                    }


                }

                // 设置TextView的边距
                textView1.setPadding(1, 1, 1, 1);

                // 添加item
                gridLayout1.addView(textView1, params1);
            }
        }
    }

    private void updateTextView(int n, double a, double b, double c,double d) {
        // 找到对应行的 TextView 并更新数据
        for (int j = 0; j < gridLayout1.getColumnCount(); j++) {
            TextView textView = (TextView) gridLayout1.getChildAt((n * gridLayout1.getColumnCount()) + j);
            if (j == 0) {
                textView.setText(String.valueOf(n));
            } else if (j == 1) {
                textView.setText("(" + String.valueOf(a) + ", " + String.valueOf(b) + ")");
            } else if (j == 2) {
                textView.setText(String.valueOf(c)+ "%");
            } else if (j == 3) {
                textView.setText(String.valueOf(d)+ "%");
            }
        }
    }

    //地图上标注
    private void mapViews(){
        //获取按钮
        Button btn_zb = (Button) findViewById(R.id.btn_zb);
        // 获取editText_zb
        editText_zb = findViewById(R.id.editText_zb);

        //按钮进行监听
        btn_zb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取editText_zb中的文本
                String zbText = editText_zb.getText().toString();
                // 将zbText转换为数字
                int zbNumber = Integer.parseInt(zbText);

                // 获取gridLayout1中i=zbNumber+1，j=1的TextView
                TextView textViewToUpdate = (TextView) gridLayout1.getChildAt(zbNumber  * gridLayout1.getColumnCount() + 1);

                // 获取textViewToUpdate的内容
                String contentToUpdate = textViewToUpdate.getText().toString();

                //监听按钮，如果点击，就跳转
                Intent intent = new Intent();
                //前一个（MainActivity.this）是目前页面，后面一个是要跳转的下一个页面
                intent.setClass(LbActivity.this,ZbActivity.class);
                intent.putExtra("content", contentToUpdate);
                startActivity(intent);
            }
        });
    }
    private void btntzViews(){
        button_tz = findViewById(R.id.button_tz1);
        button_tz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建意图以启动目标Activity
                Intent intent = new Intent(LbActivity.this, MainActivity.class);
                // 开始目标Activity
                startActivity(intent);
            }
        });
    }
    private void btntzViews1(){
        button_tz1 = findViewById(R.id.button_tz2);
        button_tz1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LbActivity.this, "已在状态页面", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
