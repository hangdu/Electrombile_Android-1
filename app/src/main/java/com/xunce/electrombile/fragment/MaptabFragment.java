package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.LogUtil;
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
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.BindingActivity2;
import com.xunce.electrombile.activity.FindCarActivity;
import com.xunce.electrombile.activity.TestddActivity;
import com.xunce.electrombile.manager.TracksManager.TrackPoint;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

public class MaptabFragment extends BaseFragment implements OnGetGeoCoderResultListener {
    //保存数据所需要的最短距离
    private static final double MIN_DISTANCE = 100;
//    private static final int DELAY = 1000;
    public static MapView mMapView;
    private static String TAG = "MaptabFragment:";
    public TrackPoint currentTrack;

    //电动车标志
    private Marker markerMobile;
    private Marker markerPerson;
    //轨迹图层
    private TextView tvUpdateTime;
    private InfoWindow mInfoWindow;
    private View markerView;

    //dialogs
    private Dialog networkDialog;
    private Dialog didDialog;
    private BaiduMap mBaiduMap;
    //缓存布局
    private View rootView;
    private TextView tv_CarName;
    private TextView tv_CarPosition;
    private GeoCoder mSearch = null;
    private BroadcastReceiver MyBroadcastReceiver;
    private View titleView;
    private TextView titleTextView;
    private RelativeLayout btn_back;
    private LinearLayout layout_FindMode_up;
    private Button btn_arriveNearby;
    private RelativeLayout ll_historyAndlocate;
    private TextView tv_FindModeCarName;
    private TextView tv_FindModeCarPosition;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public static final String status_LocateCar = "LOCATECAR";
    public static final String status_FindCar = "FINDCAR";

    public String status = status_LocateCar;

    public Boolean LostCarSituation = false;

    private TextView dialog_tv_LastCarLocation;
    private TextView dialog_tv_LastLocateTime;
    private TextView dialog_tv_status;
    private Button btn_LostCarnextstep;
    private String status_GPSornot;
    private TextView tv_GPSornot;


    private Handler playHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                TrackPoint trackPoint = (TrackPoint) msg.obj;
                mInfoWindow = new InfoWindow(markerView, trackPoint.point, -100);
                SimpleDateFormat sdfWithSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                tvUpdateTime.setText(sdfWithSecond.format(trackPoint.time));
                if(LostCarSituation){
                    dialog_tv_LastLocateTime.setText(sdfWithSecond.format(trackPoint.time));
                }
                mBaiduMap.showInfoWindow(mInfoWindow);
                //设置车辆位置  填到textview中
                mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(trackPoint.point));
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(m_context);
        currentTrack = new TrackPoint(new Date(), 0, 0);
        LayoutInflater inflater = (LayoutInflater) m_context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //定位成功的时候出现在电动车图标上面的那个...
        markerView = inflater.inflate(R.layout.view_marker, null);
        tvUpdateTime = (TextView) markerView.findViewById(R.id.tv_updateTime);
        didDialog = new AlertDialog.Builder(m_context).setMessage(R.string.bindErrorSet)
                .setTitle(R.string.bindSet)
                .setPositiveButton(R.string.goBind, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent;
                        intent = new Intent(m_context, BindingActivity2.class);
                        m_context.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();

        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        MyBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.app.bc.test");
        m_context.registerReceiver(MyBroadcastReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            View view = inflater.inflate(R.layout.map_fragment, container, false);
            initView(view);
            rootView = view;
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        log.info("onViewCreated");
    }


    public void InitCarLocation(){
        if (NetworkUtils.checkNetwork(m_context)) return;
        //检查是否绑定
        if (checkBind()) return;

        if (mBaiduMap != null) {
            m_context.timeHandler.sendEmptyMessageDelayed(ProtocolConstants.TIME_OUT, ProtocolConstants.TIME_OUT_VALUE);
            updateLocation();
            com.orhanobut.logger.Logger.i("InitCarLocation", "发送cmd_location");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.setVisibility(View.VISIBLE);
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    @Override
    public void onDestroy() {
        mBaiduMap.clear();
        mMapView.onDestroy();
        mMapView = null;

        m_context.unregisterReceiver(MyBroadcastReceiver);
        m_context = null;
        mSearch = null;
        super.onDestroy();
    }

    @Override
    public void onDetach(){
        super.onDetach();
    }

    /**
     * 初始化view
     *
     * @param v view
     */
    private void initView(View v) {
        titleView = v.findViewById(R.id.ll_button) ;
        titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setVisibility(View.INVISIBLE);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToMapFragmentUI();
            }
        });

        tv_GPSornot = (TextView)v.findViewById(R.id.tv_GPSornot);

        mMapView = (MapView) v.findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                return true;
            }
        });

        layout_FindMode_up = (LinearLayout)v.findViewById(R.id.layout_FindMode_up);
        btn_arriveNearby = (Button)v.findViewById(R.id.btn_arriveNearby);
        btn_arriveNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(m_context, FindActivity.class);
                Intent intent = new Intent(m_context, FindCarActivity.class);
                startActivity(intent);
            }
        });
        ll_historyAndlocate = (RelativeLayout)v.findViewById(R.id.ll_historyAndlocate);
        tv_FindModeCarName = (TextView)v.findViewById(R.id.tv_FindModeCarName);
        tv_FindModeCarPosition = (TextView)v.findViewById(R.id.tv_FindModeCarPosition);

        //定位电动车按钮
        Button btnLocation = (Button) v.findViewById(R.id.btn_location);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //检查网络
                if (NetworkUtils.checkNetwork(m_context)) return;
                //检查是否绑定
                if (checkBind()) return;

                if (mBaiduMap != null) {
                    m_context.showWaitDialog();
                    m_context.timeHandler.sendEmptyMessageDelayed(ProtocolConstants.TIME_OUT, ProtocolConstants.TIME_OUT_VALUE);
                    updateLocation();
                }
            }
        });

        //历史记录按钮
        Button btnRecord = (Button) v.findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //检查网络
                if (NetworkUtils.checkNetwork(m_context)) return;
                //检查是否绑定
                if (checkBind()) return;
                Intent intent = new Intent(m_context, TestddActivity.class);
                startActivity(intent);
            }
        });

        //按钮容器
        tv_CarName = (TextView) v.findViewById(R.id.tv_CarName);
        tv_CarName.setText("车辆名称:"+setManager.getIMEI());
        tv_CarPosition = (TextView) v.findViewById(R.id.tv_CarPosition);

        //精确找车
        Button find_car = (Button) v.findViewById(R.id.find_car);
        find_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检查网络
                if (NetworkUtils.checkNetwork(m_context)) return;
                //检查是否绑定
                if (checkBind()) return;
                //出现找车导航页面1
                findCarGuide1();
            }
        });

        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        //构建MarkerOption，用于在地图上添加Marker
        LatLng point = new LatLng(30.5171, 114.4392);
        MarkerOptions option2 = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        markerMobile = (Marker) mBaiduMap.addOverlay(option2);

        titleTextView.setText("地图");
        layout_FindMode_up.setVisibility(View.INVISIBLE);
        btn_arriveNearby.setVisibility(View.INVISIBLE);
        btn_back.setVisibility(View.INVISIBLE);
        ll_historyAndlocate.setVisibility(View.VISIBLE);


        HideInfowindow();
        setCarname();
    }

    //找车模式的UI
    private void ToFindCarModeUI(){
        status = status_FindCar;
        titleTextView.setText("车辆位置");

        layout_FindMode_up.setVisibility(View.VISIBLE);
        btn_arriveNearby.setVisibility(View.VISIBLE);
        btn_back.setVisibility(View.VISIBLE);
        ll_historyAndlocate.setVisibility(View.INVISIBLE);


        tv_FindModeCarName.setText("车辆名称:" + setManager.getIMEI());
        InitCarLocation();
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_person);
        //构建MarkerOption，用于在地图上添加Marker
        LatLng point = new LatLng(30.5171, 114.4392);
        MarkerOptions option2 = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        markerPerson = (Marker) mBaiduMap.addOverlay(option2);

        //怎么判断车在室内还是在室外呢???
        tv_GPSornot.setText(status_GPSornot);

        //获取到手机的位置
        getPhoneLocation();
    }

    //根据车的位置和人的位置  需要对地图进行缩放
    private void scaleMap(){
        LatLng p1 = markerMobile.getPosition();
        LatLng p2 = markerPerson.getPosition();

        double latitude_min;
        double latitude_max;
        double longitude_min;
        double longitude_max;
        if(p1.latitude<p2.latitude){
            latitude_min = p1.latitude;
            latitude_max = p2.latitude;
        }
        else{
            latitude_min = p2.latitude;
            latitude_max = p1.latitude;
        }

        if(p1.longitude<p2.longitude){
            longitude_min = p1.longitude;
            longitude_max = p2.longitude;
        }
        else{
            longitude_min = p2.longitude;
            longitude_max = p1.longitude;
        }

        LatLng southwest = new LatLng(latitude_min,longitude_min);
        LatLng northeast = new LatLng(latitude_max,longitude_max);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }

    private void getPhoneLocation(){
        // Acquire a reference to the system Location Manager
        if(locationManager == null){
            locationManager = (LocationManager) m_context.getSystemService(Context.LOCATION_SERVICE);
        }

        // Define a listener that responds to location updates
        if(locationListener == null){
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    LatLng oldPoint = new LatLng(location.getLatitude(), location.getLongitude());
                    LatLng bdPoint = mCenter.convertPoint(oldPoint);
                    markerPerson.setPosition(bdPoint);
                    scaleMap();
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };
        }

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 1, locationListener);
    }

    private void ToMapFragmentUI(){
        status = status_LocateCar;
        titleTextView.setText("地图");

        layout_FindMode_up.setVisibility(View.INVISIBLE);
        btn_arriveNearby.setVisibility(View.INVISIBLE);

        btn_back.setVisibility(View.INVISIBLE);
        ll_historyAndlocate.setVisibility(View.VISIBLE);

        //人的marker去掉
        markerPerson.remove();
        //地图缩放
        MarkerLocationCenter(markerMobile.getPosition());

        //解除gps位置监听
        locationManager.removeUpdates(locationListener);

        LostCarSituation = false;
    }


    private void findCarGuide1(){
        final LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_findcar_guide1, null);
        final Dialog dialog = new Dialog(m_context, R.style.Translucent_NoTitle_white);

        Button btn_startFindcar = (Button)view.findViewById(R.id.btn_startFindcar);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);

        //获取屏幕的宽度
        WindowManager m = m_context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75); // 宽度设置为屏幕的0.65

        btn_startFindcar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                LostCarSituation = true;
                findCarGuide2(dialog_width);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.addContentView(view, new LinearLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }

    private void findCarGuide2(int dialog_width){
        final LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_findcar_guide2, null);
        final Dialog dialog = new Dialog(m_context, R.style.Translucent_NoTitle_white);

        btn_LostCarnextstep = (Button)view.findViewById(R.id.btn_nextstep);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);

        TextView dialog_tv_carName = (TextView)view.findViewById(R.id.dialog_tv_carName);
        dialog_tv_LastCarLocation = (TextView)view.findViewById(R.id.dialog_tv_LastCarLocation);
        dialog_tv_LastLocateTime = (TextView)view.findViewById(R.id.dialog_tv_LastLocateTime);
        dialog_tv_status = (TextView)view.findViewById(R.id.dialog_tv_status);

        dialog_tv_carName.setText("车辆名称:"+setManager.getCarName(setManager.getIMEI()));
//        dialog_tv_LastCarLocation.setText(tv_CarPosition.getText());

        if ((m_context).mac != null ){
            (m_context).sendMessage(m_context, mCenter.cmdWhere(), setManager.getIMEI());
        }

        btn_LostCarnextstep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ToFindCarModeUI();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.addContentView(view, new LinearLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }


    //把电动车的位置放在地图中间
    private void MarkerLocationCenter(LatLng point){
        markerMobile.setPosition(point);

        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(18)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }

    /**
     * 将地图中心移到某点
     */
    public void locateMobile(TrackPoint track) {
        if (mBaiduMap == null) return;
        mBaiduMap.hideInfoWindow();
        markerMobile.setPosition(track.point);
        MarkerLocationCenter(track.point);

        Message msg = Message.obtain();
        msg.obj = track;
        playHandler.sendMessage(msg);
        refreshTrack(track);
    }

    /**
     * 更新当前轨迹
     */
    private void refreshTrack(TrackPoint track) {
        currentTrack = track;
        m_context.dismissWaitDialog();
    }

    /**
     * 获取最新的位置
     */
    public void updateLocation() {
        tv_CarPosition.setText("车辆位置:");
        if ((m_context).mac != null ){
            (m_context).sendMessage(m_context, mCenter.cmdWhere(), setManager.getIMEI());
        }

    }

    /**
     * 检查是否已经绑定设备
     *
     * @return true绑定设备 false 未绑定
     */
    private boolean checkBind() {
        if (setManager.getIMEI().isEmpty()) {
            didDialog.show();
            return true;
        }
        return false;
    }

    public void setCarname()
    {
        tv_CarName.setText("车辆名称:" + setManager.getCarName(setManager.getIMEI()));

    }

    public void HideInfowindow()
    {
        if(mInfoWindow != null){
            mBaiduMap.hideInfoWindow();
        }
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        LogUtil.log.i("进入位置设置:" + result.getAddress());
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }
        String reverseGeoCodeResult = result.getAddress();
        //对这个字符串做一定的处理  去掉省和市
        if(reverseGeoCodeResult.contains("市")){
            String[] strings = reverseGeoCodeResult.split("市");
            reverseGeoCodeResult = strings[1];
        }

        tv_CarPosition.setText("车辆位置:" + reverseGeoCodeResult);
        tv_FindModeCarPosition.setText("车辆位置:" + reverseGeoCodeResult);

        if(LostCarSituation){
            dialog_tv_LastCarLocation.setText(reverseGeoCodeResult);
        }
    }

    public void caseLostCarSituationOffline(){
        dialog_tv_status.setText("设备不在线");
        btn_LostCarnextstep.setVisibility(View.INVISIBLE);
    }

    public void caseLostCarSituationWaiting(){
        dialog_tv_status.setText("设备在室内");
        status_GPSornot = "设备在室内";
    }

    public void caseLostCarSituationSuccess(){
        dialog_tv_status.setText("设备在室外,定位成功");
        status_GPSornot = "设备在室外,定位成功";
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        //接收到广播会被自动调用
        @Override
        public void onReceive (Context context, Intent intent) {
            if(intent.getStringExtra("KIND").equals("CHANGECARNICKNAME")){
                setCarname();
            }
            else if(intent.getStringExtra("KIND").equals("OTHER")){
                HideInfowindow();
                setCarname();
                InitCarLocation();

                //如果是找车界面  需要切换到locateCar界面
                if(status.equals(status_FindCar)){
                    ToMapFragmentUI();
                    status  = status_LocateCar;
                }
            }
        }
    }
}

