package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SpecificHistoryTrackActivity extends Activity {
    private static final int DELAY = 1000;
    private static String START = "start";
    private static String PLAYING = "playing";
    private static String PAUSE = "pause";
    public static List<TracksManager.TrackPoint> trackDataList;
    private String status;
    private String startPoint;
    private String endPoint;

    private Button btn_play;
    private Button btn_speed;

    private Marker markerMobile;

    private LatLng southwest;
    private LatLng northeast;

    private BaiduMap mBaiduMap;

    private Overlay tracksOverlay;
    private int playOrder = 0;
    public static MapView mMapView;
    private SeekBar seekBar;
    private int Progress;
    private boolean btnPlayClicked;
    private SettingManager settingManager;

    private Handler playHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handleKey key = handleKey.values()[msg.what];
            switch (key) {
                case CHANGE_POINT:
                    try {
//                        Log.i(TAG, "playOrder:" + playOrder);
                        if ((int) msg.obj < trackDataList.size()) {
                            markerMobile.setPosition(trackDataList.get((int) msg.obj).point);
                            SetSeekbar((int)msg.obj);
                            playLocateMobile((int) msg.obj);
                            if((int) msg.obj == (trackDataList.size()-1)){
//                                btnPause.setVisibility(View.INVISIBLE);
//                                btnPlay.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };


    //播放线程消息类型
    enum handleKey {
        CHANGE_POINT,
        SET_MARKER
    }

    /**
     * marker从一个点跳转到另外一个点
     */
    private void playLocateMobile(int track) {
        if (mBaiduMap == null) return;
        LatLng p2;
        if (track == 0) {
            p2 = trackDataList.get(track + 1).point;
            markerMobile.setPosition(p2);
        } else if ((track + 1) == (trackDataList.size())) {
            //不运动
            playOrder = 0;
            return;
        } else {
            p2 = trackDataList.get(track + 1).point;
            markerMobile.setPosition(p2);
        }
        playOrder += 1;
        sendPlayMessage(DELAY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this);

        Intent intent = getIntent();
        startPoint = intent.getStringExtra("startPoint");
        endPoint = intent.getStringExtra("endPoint");
//        int size = trackDataList.size();
        setContentView(R.layout.activity_specific_history_track);

        initViews();
        initEvents();
    }



    private void initViews(){
        settingManager = SettingManager.getInstance();

        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("历史轨迹");
        Button btn_back = (Button)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpecificHistoryTrackActivity.this.finish();
            }
        });

        mMapView = (MapView)findViewById(R.id.bmapView);
//        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        TextView tv_CarName = (TextView)findViewById(R.id.tv_CarName);
        TextView tv_startPoint = (TextView)findViewById(R.id.tv_startPoint);
        TextView tv_endPoint = (TextView)findViewById(R.id.tv_endPoint);
        TextView tv_routeTime = (TextView)findViewById(R.id.tv_routeTime);
        btn_play = (Button)findViewById(R.id.btn_play);
        btn_speed = (Button)findViewById(R.id.btn_speed);
        seekBar = (SeekBar)findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                Progress = progressValue;
                if (progressValue == (trackDataList.size() - 1)) {
                    btnPlayClicked = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playHandler.removeMessages(handleKey.CHANGE_POINT.ordinal());
                playOrder = Progress;
                LatLng p2 = trackDataList.get(Progress).point;
                markerMobile.setPosition(p2);

                if (btnPlayClicked) {
                    android.os.Message msg = android.os.Message.obtain();
                    msg.what = handleKey.CHANGE_POINT.ordinal();
                    msg.obj = playOrder;
                    playHandler.sendMessage(msg);
                }
            }
        });

        tv_CarName.setText("车辆名称:" + settingManager.getCarName(settingManager.getIMEI()));
        tv_startPoint.setText("起始位置:" + startPoint);
        tv_endPoint.setText("终点位置:" + endPoint);

        long time1 = trackDataList.get(0).time.getTime();
        long time2 = trackDataList.get(trackDataList.size()-1).time.getTime();

        // Calculate difference in milliseconds
        long diff = time2 - time1;
        // Difference in seconds
        long diffSec = diff / 1000;

        if(diffSec/3600 == 0){
            //小于1h
            int min = (int)diffSec/60;
            tv_routeTime.setText("历时:"+min+"分钟");
        }
        else{
            int hour = (int)diffSec/3600;
            int min = (int)(diffSec-hour*3600)/60;
            tv_routeTime.setText("历时:"+hour+"小时"+min+"分钟");
        }


        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPlayClicked = true;
                if (Progress == (trackDataList.size() - 1)) {
                    Progress = 0;
                }

                playOrder = Progress;
                Message msg = Message.obtain();
                msg.what = handleKey.CHANGE_POINT.ordinal();
                msg.obj = playOrder;
                playHandler.sendMessage(msg);
            }
        });
        status = START;
        seekBar.setMax(trackDataList.size()-1);
    }

    private void initEvents(){
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        //构建MarkerOption，用于在地图上添加Marker
        MarkerOptions option = new MarkerOptions()
                .position(trackDataList.get(0).point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        markerMobile = (Marker) mBaiduMap.addOverlay(option);
        markerMobile.setPosition(trackDataList.get(0).point);

        //让轨迹在中间
        findMinMaxLatlan(trackDataList);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(bounds);
        mBaiduMap.setMapStatus(u);

        drawLine();
    }

    /**
     * 划线
     */
    private void drawLine() {
//        mBaiduMap.clear();
        ArrayList<LatLng> points = new ArrayList<>();
        for (TracksManager.TrackPoint tp : trackDataList) {
            points.add(tp.point);
        }
        //构建用户绘制多边形的Option对象
        OverlayOptions polylineOption = new PolylineOptions()
                .points(points)
                .width(5)
                .color(0xAA00FF00);
        //在地图上添加多边形Option，用于显示
        tracksOverlay = mBaiduMap.addOverlay(polylineOption);
    }

    /**
     * 进入历史轨迹播放模式
     */

    private void findMinMaxLatlan(List<TracksManager.TrackPoint> IN_trackDataList){
        double latitude_min = IN_trackDataList.get(0).point.latitude;
        double latitude_max = latitude_min;
        double longitude_min = IN_trackDataList.get(0).point.longitude;
        double longitude_max = longitude_min;
        double latitude;
        double longitude;
        for(int i=0;i<IN_trackDataList.size();i++){
            latitude = IN_trackDataList.get(i).point.latitude;
            if(latitude_min>latitude){
                latitude_min = latitude;
            }
            if(latitude_max<latitude){
                latitude_max = latitude;
            }

            longitude = IN_trackDataList.get(i).point.longitude;
            if(longitude_min>longitude){
                longitude_min=longitude;
            }
            if(longitude_max<longitude){
                longitude_max = longitude;
            }
        }
        southwest = new LatLng(latitude_min,longitude_min);
        northeast = new LatLng(latitude_max,longitude_max);
    }

    /**
     * 发送播放消息
     */
    private void sendPlayMessage(int delay) {
        Message msg = Message.obtain();
        msg.what = handleKey.CHANGE_POINT.ordinal();
        msg.obj = playOrder;
        playHandler.sendMessageDelayed(msg, delay);
    }

    private void SetSeekbar(int progress)
    {
        seekBar.setProgress(progress + 1);
    }

}
