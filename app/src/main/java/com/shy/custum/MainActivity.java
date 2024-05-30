package com.shy.custum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shy.rockerview.MyRockerView;
import com.shy.rockerview.MyRockerView.OnAngleChangeListener;
import com.shy.rockerview.MyRockerView.OnDistanceLevelListener;
import com.shy.rockerview.MyRockerView.OnShakeListener;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SeekBar seekBar;
    private TextView txt_cur;

    private SeekBar seekBar2;
    private TextView txt_cur2;
    private SeekBar seekBar3;
    private TextView txt_cur3;
    private Switch switchButton;
    private Switch switchButton2;
    private Switch switchButton3;
    private Switch switchButton5;
    private Button button_tz;
    private Button button_tz1;

    private TextView txtStatus;
    private double angleFromRockerView = 0.0;
    private int levelFromRockerView = 0;

    private Button mBtn_send;
    private Socket mSocket;
    private PrintStream out;
    private MainActivity.ConnectThread mConnectThread;
    private String mip = "192.168.4.1";
    private int mport = 8089;
    private Button mBtn_turn;
    private BufferedReader mBufferedReader;
    private String readStrng;
    private String tempStrng;
    private MainActivity.myHandler mHandler = new MainActivity.myHandler();
    private Timer mTimer;
    private MainActivity.ReceiveDataTask mReceiveDataTask;
    private EditText editText_zb;
    private Button btn_zb;

    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = findViewById(R.id.txt_status);
        //摇杆
        rockerViews();
        //吸附调节进度条
        seekbarViews1();
        //车速进度条
        seekbarViews2();
        //清洗模块转速进度条
        seekbarViews3();

        //全体急停
        switchViews5();
        //当前急停
        switchViews();
        //模式切换
        switchViews2();
        //水泵开关
        switchViews3();
        //跳转页面
        btntzViews();
        btntzViews1();

        //机器人选择
        spinnerViews();

        btnturnViews();
        btnsendViews();


    }

    private void spinnerViews(){
        spinner = findViewById(R.id.spinner);

        // 创建一个数组列表用于存放下拉列表中的选项
        List<String> options = new ArrayList<>();
        options.add("robot1");
        options.add("robot2");
        options.add("robot3");
        options.add("robot4");
        options.add("robot5");
        options.add("robot6");
        options.add("robot7");
        options.add("robot8");
        options.add("robot9");
        options.add("robot10");

        // 创建一个 ArrayAdapter 作为 Spinner 的适配器，将选项数据与 Spinner 绑定
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);

        // 设置下拉列表的样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 将适配器设置给 Spinner
        spinner.setAdapter(adapter);

        // 设置选择监听器，监听用户选择的项
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 获取用户选择的项
                String selectedItem = (String) parent.getItemAtPosition(position);
                // 在这里执行根据选择项进行的操作
                Toast.makeText(MainActivity.this, "你选择了：" + selectedItem, Toast.LENGTH_SHORT).show();
                updateStatusText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 当用户未选择任何项时触发
            }
        });

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
                    break;
            }
        }

    }
    private void btnturnViews(){
        mBtn_turn = findViewById(R.id.btn_turn);
        mBtn_turn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mip = "10.10.100.254"; // 将 IP 地址设置为定值
                mport = 8899; // 将端口号设置为定值

                if (mSocket == null || !mSocket.isConnected()) {
                    // 如果 mSocket 为空或者未连接到服务器
                    mConnectThread = new MainActivity.ConnectThread(mip, mport);
                    mConnectThread.start();
                } else {
                    // 如果已连接到服务器，则断开连接
                    try {
                        mSocket.close();
                        mSocket = null;
                        mBtn_turn.setText("连接");
                        Toast.makeText(MainActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void btnsendViews(){
        mBtn_send = findViewById(R.id.btn_send);
        mBtn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个Handler对象
                final Handler handler = new Handler();
                // 创建一个Runnable对象，用于发送数据
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 获取文本内容
                        final String str = txtStatus.getText().toString();
                        if (str != null && out != null) {
                            // 在新线程中发送数据
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    out.print(str);
                                    out.flush();
                                }
                            }).start();
                        }
                        // 延迟1秒后再次执行该Runnable
                        handler.postDelayed(this, 300);
                    }
                };
                // 第一次执行Runnable，延迟1秒后开始执行
                handler.postDelayed(runnable, 300);

                // 新建的按钮
                Button stopButton = findViewById(R.id.stop_button);
                stopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 移除之前的Runnable，停止发送数据
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
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
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
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
            mReceiveDataTask = new MainActivity.ReceiveDataTask();
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

    private void rockerViews() {
        MyRockerView mRockerViewXY = findViewById(R.id.rockerXY_View);
        TextView directionXY_Text = findViewById(R.id.directionXY_Text);
        TextView angleXY_Text = findViewById(R.id.angleXY_Text);
        TextView levelXY_Text = findViewById(R.id.levelXY_Text);

        mRockerViewXY.setOnShakeListener(MyRockerView.DirectionMode.DIRECTION_8, new OnShakeListener() {
            @Override
            public void onStart() {}

            @Override
            public void direction(MyRockerView.Direction direction) {
                String directionXY = "";
                if (direction == MyRockerView.Direction.DIRECTION_CENTER) {
                    directionXY = "当前方向：中心";
                } else if (direction == MyRockerView.Direction.DIRECTION_DOWN) {
                    directionXY = "当前方向：下";
                } else if (direction == MyRockerView.Direction.DIRECTION_LEFT) {
                    directionXY = "当前方向：左";
                } else if (direction == MyRockerView.Direction.DIRECTION_UP) {
                    directionXY = "当前方向：上";
                } else if (direction == MyRockerView.Direction.DIRECTION_RIGHT) {
                    directionXY = "当前方向：右";
                } else if (direction == MyRockerView.Direction.DIRECTION_DOWN_LEFT) {
                    directionXY = "当前方向：左下";
                } else if (direction == MyRockerView.Direction.DIRECTION_DOWN_RIGHT) {
                    directionXY = "当前方向：右下";
                } else if (direction == MyRockerView.Direction.DIRECTION_UP_LEFT) {
                    directionXY = "当前方向：左上";
                } else if (direction == MyRockerView.Direction.DIRECTION_UP_RIGHT) {
                    directionXY = "当前方向：右上";
                }
                Log.e(TAG, "XY轴" + directionXY);
                Log.e(TAG, "-----------------------------------------------");
                directionXY_Text.setText(directionXY);
            }

            @Override
            public void onFinish() {
                double angle1 = 0.0;
                String angleXY = "当前角度：" + angle1;
                //Log.e(TAG, "XY轴" + angleXY);
                angleXY_Text.setText(angleXY);
                angleFromRockerView = angle1;
                int level1 = 0;
                String levelXY = "当前距离级别：" + level1;
                //Log.e(TAG, "XY轴" + levelXY);
                levelXY_Text.setText(levelXY);
                levelFromRockerView = level1;
                updateStatusText();
            }
        });

        mRockerViewXY.setOnAngleChangeListener(new OnAngleChangeListener() {
            @Override
            public void onStart() {}

            @Override
            public void angle(double angle) {
                String angleXY = "当前角度：" + angle;
                Log.e(TAG, "XY轴" + angleXY);
                angleXY_Text.setText(angleXY);
                angleFromRockerView = angle;
                updateStatusText();
            }

            @Override
            public void onFinish() {}
        });

        mRockerViewXY.setOnDistanceLevelListener(new OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                String levelXY = "当前距离级别：" + level;
                Log.e(TAG, "XY轴" + levelXY);
                levelXY_Text.setText(levelXY);
                levelFromRockerView = level;
                updateStatusText();
            }
        });
    }

    //吸附调节进度条
    private void seekbarViews1() {
        seekBar = findViewById(R.id.seekBar);
        txt_cur = findViewById(R.id.txt_cur);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txt_cur.setText("吸附调节:" + progress + "  / 100 ");
                updateStatusText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //车速进度条
    private void seekbarViews2() {
        seekBar2 = findViewById(R.id.seekBar2);
        txt_cur2 = findViewById(R.id.txt_cur2);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txt_cur2.setText("车速:" + progress + "  / 100 ");
                updateStatusText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //清洗模块转速进度条
    private void seekbarViews3() {
        seekBar3 = findViewById(R.id.seekBar3);
        txt_cur3 = findViewById(R.id.txt_cur3);
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txt_cur3.setText("清洗模块转速:" + progress + "  / 100 ");
                updateStatusText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //全体急停开关
    private void switchViews5(){
        switchButton5 = findViewById(R.id.switchButton5);
        switchButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "全体急停开关开启", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "全体急停开关关闭", Toast.LENGTH_SHORT).show();
                }
                updateStatusText();
            }
        });
    }
    //当前急停开关
    private void switchViews(){
        switchButton = findViewById(R.id.switchButton);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "当前急停开关开启", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "当前急停开关关闭", Toast.LENGTH_SHORT).show();
                }
                updateStatusText();
            }
        });
    }


    //模式切换
    private void switchViews2(){
        switchButton2 = findViewById(R.id.switchButton2);
        switchButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "手动模式", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "自动模式", Toast.LENGTH_SHORT).show();
                }
                updateStatusText();
            }
        });
    }

    //水泵开关
    private void switchViews3(){
        switchButton3 = findViewById(R.id.switchButton3);
        switchButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "水泵开启", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "水泵关闭", Toast.LENGTH_SHORT).show();
                }
                updateStatusText();
            }
        });
    }

    private void updateStatusText() {
        // 获取 Spinner 当前选择的索引
        int selectedPosition = spinner.getSelectedItemPosition();

        // 根据选择的索引确定要显示的值
        String selectedItemValue = String.valueOf(selectedPosition + 1);
        // Get values from seekBar, switchButton, and rockerView
        int progress = seekBar.getProgress();
        int progress2 = seekBar2.getProgress();
        int progress3 = seekBar3.getProgress();
        int switchStatus = switchButton.isChecked() ? 1 : 0;
        int switchStatus2 = switchButton2.isChecked() ? 1 : 0;
        int switchStatus3 = switchButton3.isChecked() ? 1 : 0;
        int switchStatus5 = switchButton5.isChecked() ? 1 : 0;
        double angle = angleFromRockerView; // Replace with actual angle value from rockerView
        int distanceLevel = levelFromRockerView; // Replace with actual distance level value from rockerView

        // Update TextView with formatted text
        String statusText = selectedItemValue+ ":" +switchStatus5 +":" +switchStatus +  ":" +switchStatus2 + ":" +switchStatus3 + ":" +progress + ":" +progress2 + ":" +progress3 + ":" +angle + ":" + distanceLevel;
        // 在需要换行的地方添加 \n
        statusText += "\r\n";
        txtStatus.setText(statusText);
        // 更新单例对象的值
        StatusManager.getInstance().setStatusText(statusText);
    }

    private void btntzViews(){
        button_tz = findViewById(R.id.button_tz2);
        button_tz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建意图以启动目标Activity
                Intent intent = new Intent(MainActivity.this, LbActivity.class);
                // 开始目标Activity
                startActivity(intent);
            }
        });
    }

    private void btntzViews1(){
        button_tz1 = findViewById(R.id.button_tz1);
        button_tz1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "已在控制页面", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
