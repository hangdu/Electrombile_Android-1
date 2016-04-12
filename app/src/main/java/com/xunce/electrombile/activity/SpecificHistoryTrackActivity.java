package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
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
    private static final int DELAY1 = 1000;
    private static final int DELAY2 = 800;
    private static final int DELAY5 = 500;
    private static int DELAY = DELAY1;
    private static String START = "start";
    private static String PLAYING = "playing";
    private static String PAUSE = "pause";
    private String status;

    public static List<TracksManager.TrackPoint> trackDataList;

    private String startPoint;
    private String endPoint;
    private int miles;

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
    private SettingManager settingManager;
    private InfoWindow mInfoWindow;
    private View markerView;
    private TextView tv_pointTime;

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
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case BOUND_REFRESH:
                    LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();
                    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);
                    mBaiduMap.setMapStatus(mMapStatusUpdate);
                    break;
            }
        }
    };

    //播放线程消息类型
    enum handleKey {
        CHANGE_POINT,
        BOUND_REFRESH
    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this);

        Intent intent = getIntent();
        startPoint = intent.getStringExtra("startPoint");
        endPoint = intent.getStringExtra("endPoint");
        miles = Integer.parseInt(intent.getStringExtra("miles"));
        setContentView(R.layout.activity_specific_history_track);

        initViews();
        initEvents();
    }

    private void initViews(){
        settingManager = SettingManager.getInstance();

        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("历史轨迹");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpecificHistoryTrackActivity.this.finish();
            }
        });

        mMapView = (MapView)findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        TextView tv_CarName = (TextView)findViewById(R.id.tv_CarName);
        TextView tv_startPoint = (TextView)findViewById(R.id.tv_startPoint);
        TextView tv_endPoint = (TextView)findViewById(R.id.tv_endPoint);
        TextView tv_routeTime = (TextView)findViewById(R.id.tv_routeTime);
        btn_play = (Button)findViewById(R.id.btn_play);

        btn_speed = (Button)findViewById(R.id.btn_speed);
        btn_speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(DELAY == DELAY1){
                    DELAY = DELAY2;
                    //还需要修改贴图
                }
                else if(DELAY == DELAY2){
                    DELAY = DELAY5;
                    //还需要修改贴图
                }
                else if(DELAY == DELAY5){
                    DELAY = DELAY1;
                    //还需要修改贴图
                }
            }
        });

        seekBar = (SeekBar)findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                Progress = progressValue;
                if (progressValue == (trackDataList.size() - 1)) {
                    status = START;
                    btn_play.setBackgroundResource(R.drawable.btn_play1);
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
                if(status.equals(PLAYING)){
                    Message msg = Message.obtain();
                    msg.what = handleKey.CHANGE_POINT.ordinal();
                    msg.obj = playOrder;
                    playHandler.sendMessage(msg);
                }
            }
        });

        tv_CarName.setText("车辆名称:" + settingManager.getCarName(settingManager.getIMEI())+"miles:"+miles);
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
                if(status.equals(START)){
                    status = PLAYING;
                    btn_play.setBackgroundResource(R.drawable.img_pause);
                    Progress = 0;
                    playOrder = 0;
                    Message msg = Message.obtain();
                    msg.what = handleKey.CHANGE_POINT.ordinal();
                    msg.obj = playOrder;
                    playHandler.sendMessage(msg);
                }
                else if(status.equals(PLAYING)){
                    status = PAUSE;
                    btn_play.setBackgroundResource(R.drawable.btn_play1);
                    //怎么可以暂停呢????
                    Message msg = Message.obtain();
                    msg.what = handleKey.CHANGE_POINT.ordinal();
                    playHandler.removeMessages(msg.what);
                }
                else if(status.equals(PAUSE)){
                    status = PLAYING;
                    btn_play.setBackgroundResource(R.drawable.img_pause);
                    Message msg = Message.obtain();
                    msg.what = handleKey.CHANGE_POINT.ordinal();
                    msg.obj = playOrder;
                    playHandler.sendMessage(msg);
                }
            }
        });
        status = START;

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        markerView = inflater.inflate(R.layout.view_marker, null);

        tv_pointTime = (TextView)markerView.findViewById(R.id.tv_updateTime);
        SimpleDateFormat sdfWithSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdfWithSecond.format(trackDataList.get(0).time);
        String[] strs = time.split(" ");
        tv_pointTime.setText(strs[1]);

        TextView tv_speedname = (TextView)markerView.findViewById(R.id.tv_statusName);
        tv_speedname.setText("速度:");
        TextView tv_speed = (TextView)markerView.findViewById(R.id.tv_statuse);

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

        BitmapDescriptor bitmap_startpoint = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_startpoint);
        option = new MarkerOptions()
                .position(trackDataList.get(0).point)
                .icon(bitmap_startpoint);
        mBaiduMap.addOverlay(option);


        BitmapDescriptor bitmap_endpoint = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_endpoint);
        option = new MarkerOptions()
                .position(trackDataList.get(trackDataList.size()-1).point)
                .icon(bitmap_endpoint);
        mBaiduMap.addOverlay(option);


        //让轨迹在中间
        findMinMaxLatlan();
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        drawLine();

        mInfoWindow = new InfoWindow(markerView,trackDataList.get(0).point, -100);
        mBaiduMap.showInfoWindow(mInfoWindow);

        Message msg = Message.obtain();
        msg.what = handleKey.BOUND_REFRESH.ordinal();
        playHandler.sendMessageDelayed(msg,300);
    }

    /**
     * 划线
     */
    private void drawLine() {
        ArrayList<LatLng> points = new ArrayList<>();
        for (TracksManager.TrackPoint tp : trackDataList) {
            points.add(tp.point);
        }
        //构建用户绘制多边形的Option对象
        OverlayOptions polylineOption = new PolylineOptions()
                .points(points)
                .width(10)
                .color(0xAA00FF00);
        //在地图上添加多边形Option，用于显示
        tracksOverlay = mBaiduMap.addOverlay(polylineOption);
    }

    /**
     * 进入历史轨迹播放模式
     */
    private void findMinMaxLatlan(){
        double latitude_min = trackDataList.get(0).point.latitude;
        double latitude_max = latitude_min;
        double longitude_min = trackDataList.get(0).point.longitude;
        double longitude_max = longitude_min;
        double latitude;
        double longitude;
        for(int i=0;i<trackDataList.size();i++){
            latitude = trackDataList.get(i).point.latitude;
            if(latitude_min>latitude){
                latitude_min = latitude;
            }
            if(latitude_max<latitude){
                latitude_max = latitude;
            }

            longitude = trackDataList.get(i).point.longitude;
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

    /**
     * marker从一个点跳转到另外一个点
     */
    private void playLocateMobile(int track) {
        if (mBaiduMap == null) return;
        LatLng p2;

        if((track + 1) == (trackDataList.size())){
            //不运动
            playOrder = 0;
            return;
        }

        else if(track == 0){
            p2 = trackDataList.get(track + 1).point;
            markerMobile.setPosition(p2);
        }
        else{
            p2 = trackDataList.get(track + 1).point;
            markerMobile.setPosition(p2);
        }

        mInfoWindow = new InfoWindow(markerView,p2, -100);
        SimpleDateFormat sdfWithSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdfWithSecond.format(trackDataList.get(track + 1).time);
        String[] strs = time.split(" ");
        tv_pointTime.setText(strs[1]);
        mBaiduMap.showInfoWindow(mInfoWindow);
        playOrder += 1;

        sendPlayMessage(DELAY);
    }
}
