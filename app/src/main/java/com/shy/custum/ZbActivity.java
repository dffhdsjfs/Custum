package com.shy.custum;


import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;


public class ZbActivity extends AppCompatActivity implements MKOfflineMapListener{

    private TextView textView11;
    private TextView textView22;

    MapView mMapView;
    GeoCoder geoCoder;

    EditText etLongitude;
    EditText etLatitude;
    EditText etAddress;
    EditText etCityName;
    Button btnDownload;
    Button btnP2M;

    private MKOfflineMap mOfflineMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.setAgreePrivacy(getApplicationContext(), true);
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_zb);


        // 获取textView1
        textView11 = findViewById(R.id.textView11);

        // 获取textView2
        textView22 = findViewById(R.id.textView22);

        // 获取传递过来的内容
        Intent intent = getIntent();
        String content = intent.getStringExtra("content");


        // 去除首尾的括号并分割content字符串，以逗号为分隔符
        String[] parts = content.substring(1, content.length() - 1).split(",");

        // 设置textView1和textView2的文本
        if (parts.length == 2) {
            textView11.setText(parts[0].trim()); // 设置数字1到textView1
            textView22.setText(parts[1].trim()); // 设置数字2到textView2
        } else {
            // 如果content不符合预期格式，给出错误提示或者默认值
            textView11.setText("未知"); // 设置默认值到textView1
            textView22.setText("未知"); // 设置默认值到textView2
        }
        mMapView = (MapView)findViewById(R.id.bmapView);


        etCityName = (EditText)findViewById(R.id.editTextCityName);
        btnDownload = (Button)findViewById(R.id.buttonDownload);
        btnP2M = (Button)findViewById(R.id.buttonP2M);




        // 离线地图初始化
        mOfflineMap = new MKOfflineMap();
        mOfflineMap.init(this);


        // 初始化离线地图
        mOfflineMap = new MKOfflineMap();
        mOfflineMap.init(this);



        // 将经纬度坐标点显示到地图
        btnP2M.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                double longitude = Double.parseDouble(textView11.getText().toString());
                double latitude = Double.parseDouble(textView22.getText().toString());
                BaiduMap map = mMapView.getMap();   // 获取BaiduMap对象

                // 清除地图上的所有覆盖物，包括之前的标记
                map.clear();

                map.setMyLocationEnabled(true);
                // 设置当前位置
                MyLocationData.Builder builder = new MyLocationData.Builder();
                MyLocationData location = builder.accuracy(10).direction(100).longitude(longitude).latitude(latitude).build();
                map.setMyLocationData(location);
                // 动画显示
                LatLng ll = new LatLng(location.latitude, location.longitude);
                MapStatus.Builder statusBuilder = new MapStatus.Builder();
                statusBuilder.target(ll).zoom(18.0f);
                map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(statusBuilder.build()));

                // 构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);

                // 构建MarkerOption，用于在地图上添加Marker
                MarkerOptions options = new MarkerOptions()
                        .position(ll)
                        .icon(bitmap);

                // 在地图上添加Marker，并显示
                map.addOverlay(options);

                map.setMyLocationEnabled(false);
            }
        });

        // 离线地图下载
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = etCityName.getText().toString();
                if (cityName.isEmpty()) {
                    Toast.makeText(ZbActivity.this, "请输入城市名称", Toast.LENGTH_SHORT).show();
                    return;
                }

                int cityId = getCityIdByName(cityName);
                if (cityId != -1) {
                    // 开始下载地图
                    mOfflineMap.start(cityId);
                    Toast.makeText(ZbActivity.this, "开始下载离线地图: " + cityName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ZbActivity.this, "无法找到该城市: " + cityName, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    // 根据城市名称获取城市ID
    private int getCityIdByName(String cityName) {
        // 这里应该实现获取城市ID的逻辑，以下是示例代码
        for (MKOLSearchRecord record : mOfflineMap.getOfflineCityList()) {
            if (record.cityName.equals(cityName)) {
                return record.cityID;
            }
        }
        return -1;
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
                MKOLUpdateElement update = mOfflineMap.getUpdateInfo(state);
                if (update != null) {
                    if (update.ratio == 100) {
                        // 下载完成时显示提示信息
                        Log.d("OfflineMap", "离线地图已下载完成");
                    } else {
                        // 显示下载进度
                        Log.d("OfflineMap", "下载进度：" + update.ratio + "%");
                    }
                }
                break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                Log.d("OfflineMap", "有新的离线地图包");
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                Log.d("OfflineMap", "离线地图版本更新");
                break;
            default:
                break;
        }
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mOfflineMap.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

}
