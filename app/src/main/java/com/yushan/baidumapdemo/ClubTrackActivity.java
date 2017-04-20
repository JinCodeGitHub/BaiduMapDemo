package com.yushan.baidumapdemo;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by beiyong on 2017-4-12.
 */

public class ClubTrackActivity extends Activity implements View.OnClickListener {


    private TextView tv_start_track;
    private LinearLayout ll_pause;
    private TextView tv_over_track;
    private TextView tv_pause_track;
    private RelativeLayout rl_all_screen;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private String startButton = "start";
    private List<LatLng> line = new ArrayList<>();
    private List<LatLng> points_tem = new ArrayList<>();

    private static final double EARTH_RADIUS = 6378137.0;
    // 定时器相关，定时检查GPS是否开启（这里只须检查mLocationClient是否启动）
    Handler handler = new Handler();
    private boolean isPause;
    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;
    private boolean startTrack;
    private Dialog trackDialog;
    private LatLng dot;
    private PolylineOptions options;
    private boolean setDot;
    private boolean setDotSuc;
    private boolean isStopLocClient;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_club_track);

        // 启动计时器(每10秒检测一次)
        handler.postDelayed(new MyRunable(), 10000);

        initView();
        initMap();
        initLocation();
//        refreshActivityView();
    }

    private void initView() {

        tv_start_track = (TextView) findViewById(R.id.tv_start_track);
        tv_start_track.setOnClickListener(this);
        ll_pause = (LinearLayout) findViewById(R.id.ll_pause);

        tv_over_track = (TextView) findViewById(R.id.tv_over_track);
        tv_over_track.setOnClickListener(this);
        tv_pause_track = (TextView) findViewById(R.id.tv_pause_track);
        tv_pause_track.setOnClickListener(this);

        rl_all_screen = (RelativeLayout) findViewById(R.id.rl_all_screen);

        mMapView = (MapView) findViewById(R.id.b_mapView);

    }

    private void refreshActivityView() {


//      改变地图状态  40.051558,116.314941  29.806651,121.606983
//        LatLng cenpt = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        LatLng cenpt = new LatLng(40.051558, 116.314941);
        MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(14).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.animateMapStatus(mMapStatusUpdate);

        OverlayOptions ooA = new MarkerOptions().position(cenpt).icon(BitmapDescriptorFactory
                .fromResource(R.drawable.club_track_location))
                .zIndex(10).draggable(true);
        mBaiduMap.addOverlay(ooA);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_start_track:
                tv_start_track.setVisibility(View.GONE);
                ll_pause.setVisibility(View.VISIBLE);

                startTrack = true;
                if (!mLocationClient.isStarted()) {
                    mLocationClient.start();
                }

                break;
            case R.id.tv_over_track:
                try {
                    ClubTrackDialog();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tv_pause_track:
                setDot = true;
                if (isPause == true) {
                    drawEnd(line);
                    if (setDotSuc == true) {
                        changeState(isPause);
                        startTrack = false;
                        tv_pause_track.setText("继续");
                        mLocationClient.stop();
                    }
                } else {

                    tv_pause_track.setText("暂停");
                    line.clear();
                    changeState(isPause);
                }
                break;

        }
    }


    private Boolean changeState(Boolean bln) {
        isPause = !bln;
        return isPause;
    }

    private void initMap() {
//      获取地图控件引用
        mMapView.removeViewAt(2);
//      不显示缩放比例尺
        mMapView.showZoomControls(false);
//      百度地图
        mBaiduMap = mMapView.getMap();
    }

    private void initLocation() {
//      定位客户端的设置
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationListener = new MyLocationListener();
//      注册监听
        mLocationClient.registerLocationListener(mLocationListener);
//      配置定位
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);
        mLocationClient.setLocOption(option);

    }

    public void ClubTrackDialog() throws Exception {

        View view = View.inflate(ClubTrackActivity.this, R.layout.layout_track_dialog, null);
        TextView tv_track_content = (TextView) view.findViewById(R.id.tv_track_content);
        TextView tv_track_ok = (TextView) view.findViewById(R.id.tv_track_ok);
        TextView tv_track_cancel = (TextView) view.findViewById(R.id.tv_track_cancel);

        tv_track_content.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_track_content.setText("确定要关闭轨迹吗？");


        DialogListener questionnaireListener = new DialogListener();
        tv_track_cancel.setOnClickListener(questionnaireListener);
        tv_track_ok.setOnClickListener(questionnaireListener);

        trackDialog = new Dialog(ClubTrackActivity.this, R.style.trackDialog_style);
        trackDialog.setContentView(view);
        trackDialog.setCanceledOnTouchOutside(false);
        trackDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        Window dialogWindow = trackDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = this.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.6); // 高度设置为屏幕的0.6
        lp.height = (int) (d.heightPixels * 0.205); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);

        trackDialog.show();
    }

    private class DialogListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_track_cancel:
                    dimissDialog();
                    break;
                case R.id.tv_track_ok:
                    setDot = false;
                    drawEnd(line);
                    startTrack = false;
                    line.clear();
                    mLocationClient.stop();
                    tv_start_track.setVisibility(View.VISIBLE);
                    ll_pause.setVisibility(View.GONE);

                    dimissDialog();
                    break;
            }
        }
    }

    private void dimissDialog() {

        if (trackDialog != null) {
            trackDialog.dismiss();
            trackDialog = null;
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {



        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();

            mBaiduMap.setMyLocationData(locData);

            if (isFirst == true){
                LatLng cenpt = new LatLng(location.getLatitude(), location.getLongitude());
//                LatLng cenpt = new LatLng(40.051558, 116.314941);
                MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(14).build();
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                mBaiduMap.animateMapStatus(mMapStatusUpdate);
                isFirst = false;
            }

//            distance = getDistance(location.getLongitude(), location.getLatitude(), 116.314941, 40.051558);
            int code = location.getLocType();
            ToastUtil.showCenter(getApplicationContext(), "key= " + code);
            drawTrackLine(location);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    }

    private void drawTrackLine(BDLocation location) {

        dot = new LatLng(location.getLatitude(), location.getLongitude());
        if (startTrack == true) {
            line.add(dot);
            if (line.size() == 1) {
                // 这里绘制起点
                drawStart(line);
                if (isPause != true) {
                    ToastUtil.showCenter(ClubTrackActivity.this, "轨迹已开启");
                }

            } else if (line.size() > 3) {
                points_tem = line.subList(line.size() - 2, line.size());

                options = new PolylineOptions().color(0xfe12b7f5).width(10)
                        .points(points_tem);
                mBaiduMap.addOverlay(options);
            }
        }
    }


    /**
     * 绘制起点，取前n个点坐标的平均值绘制起点
     *
     * @param points2
     */
    public void drawStart(List<LatLng> points2) {
        double myLat = 0.0;
        double myLng = 0.0;

        for (LatLng ll : points2) {
            myLat += ll.latitude;
            myLng += ll.longitude;
        }
        LatLng avePoint = new LatLng(myLat / points2.size(), myLng
                / points2.size());
        line.add(avePoint);
        OverlayOptions ooA;
        if (setDot == false) {
            ooA = new MarkerOptions().position(avePoint).icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.club_track_start))
                    .zIndex(4).draggable(true);
        } else {
            ooA = new DotOptions().center(avePoint).color(0xfe12b7f5).radius(15);
        }
        mBaiduMap.addOverlay(ooA);
    }

    /**
     * 绘制终点。
     *
     * @param points2
     */
    protected void drawEnd(List<LatLng> points2) {
        double myLat = 0.0;
        double myLng = 0.0;
        if (points2.size() >= 2) {
            for (int i = points2.size() - 2; i < points2.size(); i++) {
                LatLng ll = points2.get(i);
                myLat += ll.latitude;
                myLng += ll.longitude;

            }
            LatLng avePoint = new LatLng(myLat / 2, myLng / 2);

            OverlayOptions ooA;
            if (setDot == false) {
                ooA = new MarkerOptions().position(dot).icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.club_track_close))
                        .zIndex(4).draggable(true);
            } else {
                ooA = new DotOptions().center(avePoint).color(0xfe12b7f5).radius(15);
                mBaiduMap.addOverlay(ooA);
            }
            setDotSuc = true;
            mBaiduMap.addOverlay(ooA);
        } else {

            if (isPause == false && setDot == false) {
                OverlayOptions ooA = new MarkerOptions().position(dot).icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.club_track_close))
                        .zIndex(4).draggable(true);

                setDotSuc = true;
                mBaiduMap.addOverlay(ooA);

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//      开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (mLocationClient != null) {
            if (!mLocationClient.isStarted()) {
                mLocationClient.start();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//      关闭定位
        mBaiduMap.setMyLocationEnabled(false);
        if (mLocationClient != null) {
            if (mLocationClient.isStarted()) {
                mLocationClient.stop();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStopLocClient = true;
        // 退出时销毁定位
        mLocationClient.stop();
        dimissDialog();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        if (mMapView != null) {
            mMapView.onDestroy();
            mMapView = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        if (mMapView != null) {
            mMapView.onResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    class MyRunable implements Runnable {

        public void run() {

            if (!mLocationClient.isStarted()) {
                mLocationClient.start();
            }

            if (!isStopLocClient) {
                handler.postDelayed(this, 3000);
            }

        }

    }
}

