package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.avos.avoscloud.LogUtil;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.bean.WeatherBean;
import com.xunce.electrombile.protocol.JsonKeys;
import com.xunce.electrombile.utils.device.VibratorUtil;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.JSONUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

public class SwitchFragment extends BaseFragment implements OnGetGeoCoderResultListener {

    private static String TAG = "SwitchFragment";
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private Context m_context;
    private boolean alarmState = false;
    //缓存view
    private View rootView;
    private Button btnAlarmState;
    //textview 设置当前位置
    private TextView switch_fragment_tvLocation;
    private LocationTVClickedListener locationTVClickedListener;
    //设置时出现的进度框
    // private ProgressDialog setAlarmDialog;
    private TextView tvWeather;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        m_context = activity;
        try {
            locationTVClickedListener = (LocationTVClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);


        mLocationClient = new LocationClient(m_context);     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();
        mLocationClient.start();
        //设置报警进度框初始化
        waitDialog.setMessage("正在设置，请稍后......");
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnAlarmState = (Button) getActivity().findViewById(R.id.btn_AlarmState);

        tvWeather = (TextView) getActivity().findViewById(R.id.weather);

        switch_fragment_tvLocation = (TextView) getActivity().findViewById(R.id.switch_fragment_tvLocation);
        switch_fragment_tvLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                locationTVClickedListener.locationTVClicked();
            }
        });
        if (setManager.getAlarmFlag()) {
            showNotification("安全宝防盗系统已启动");
            openStateAlarmBtn();
        } else {
            closeStateAlarmBtn();
        }
        btnAlarmState.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alarmState) {
                    if (!setManager.getIMEI().isEmpty()) {
                        if (NetworkUtils.isNetworkConnected(m_context)) {
                            //关闭报警
                            //等状态设置成功之后再改变按钮的显示状态，并且再更改标志位等的保存。
                            cancelNotification();
                            FragmentActivity.pushService.sendMessage1(mCenter.cmdFenceOff());
                            waitDialog.show();
                            timeHandler.sendEmptyMessageDelayed(JsonKeys.TIME_OUT, JsonKeys.TIME_OUT_VALUE);
                            //test
//                            FragmentActivity.pushService.sendMessage1(mCenter.cmdFenceOff());
//                            FragmentActivity.pushService.sendMessage1(mCenter.cmdFenceOn());
//                            FragmentActivity.pushService.sendMessage1(mCenter.cmdSeekOn());
//                            FragmentActivity.pushService.sendMessage1(mCenter.cmdSeekOff());
//                            FragmentActivity.pushService.sendMessage1(mCenter.cmdFenceGet());
//                            FragmentActivity.pushService.sendMessage1(mCenter.cmdWhere());

                        } else {
                            ToastUtils.showShort(m_context, "网络连接失败");
                        }
                    } else {
                        ToastUtils.showShort(m_context, "请等待设备绑定");
                    }
                } else {
                    if (NetworkUtils.isNetworkConnected(m_context)) {
                        //打开报警
                        if (!setManager.getIMEI().isEmpty()) {
                            //等状态设置成功之后再改变按钮的显示状态，并且再更改标志位等的保存。
                            cancelNotification();
                            VibratorUtil.Vibrate(getActivity(), 700);
                            FragmentActivity.pushService.sendMessage1(mCenter.cmdFenceOn());
                            waitDialog.show();
                            timeHandler.sendEmptyMessageDelayed(JsonKeys.TIME_OUT, JsonKeys.TIME_OUT_VALUE);
                        } else {
                            ToastUtils.showShort(m_context, "请先绑定设备");
                        }
                    } else {
                        ToastUtils.showShort(m_context, "网络连接失败");
                    }
                }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        if (setManager.getAlarmFlag()) {
            openStateAlarmBtn();
        } else {
            closeStateAlarmBtn();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.switch_fragment, container, false);
        }
        return rootView;
    }

    public void reverserGeoCedec(LatLng pCenter) {
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(pCenter));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        LogUtil.log.i("进入位置设置:" + result.getAddress());
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }
        switch_fragment_tvLocation.setText(result.getAddress().trim());
    }

    //显示常驻通知栏
    public void showNotification(String text) {
        NotificationManager notificationManager = (NotificationManager) m_context.getSystemService(getActivity().
                getApplicationContext()
                .NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.mipmap.ic_launcher, "安全宝", System.currentTimeMillis());
        //下面这句用来自定义通知栏
        //notification.contentView = new RemoteViews(getPackageName(),R.layout.notification);
        Intent intent = new Intent(m_context, FragmentActivity.class);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        PendingIntent contextIntent = PendingIntent.getActivity(m_context, 0, intent, 0);
        notification.setLatestEventInfo(m_context, "安全宝", text, contextIntent);
        notificationManager.notify(R.string.app_name, notification);
    }

    //取消显示常驻通知栏
    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) m_context.getSystemService(getActivity().
                getApplicationContext()
                .NOTIFICATION_SERVICE);
        notificationManager.cancel(R.string.app_name);
    }

    public void msgSuccessArrived() {
        if (setManager.getAlarmFlag()) {
            showNotification("安全宝防盗系统已启动");
            openStateAlarmBtn();
        } else {
            showNotification("安全宝防盗系统已关闭");
            VibratorUtil.Vibrate(getActivity(), 500);
            closeStateAlarmBtn();
        }
    }

    @Override
    public void onDestroy() {
        m_context = null;
        mSearch = null;
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    private void httpGetWeather(String city) {
        city = city.replace("市", "");
        Log.e(TAG, "city：" + city);
        city = StringUtils.encode(city);
        Log.e(TAG, "city" + city);
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET,
                "http://apistore.baidu.com/microservice/weather?cityname=" + city,
                new RequestCallBack<String>() {
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        Log.e(TAG, "onLoading");
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, StringUtils.decodeUnicode(responseInfo.result));
                        String originData = StringUtils.decodeUnicode(responseInfo.result);
                        WeatherBean data = new WeatherBean();
                        parseWeatherErr(data, originData);
                    }

                    private void parseWeatherErr(WeatherBean data, String originData) {
                        data.errNum = JSONUtils.ParseJSON(originData, "errNum");
                        data.errMsg = JSONUtils.ParseJSON(originData, "errMsg");
                        if ("0".equals(data.errNum) && "success".equals(data.errMsg)) {
                            data.retData = JSONUtils.ParseJSON(originData, "retData");
                            parseRetData(data.retData, data);
                        } else {
                            Log.e(TAG, "fail to get Weather info");
                        }
                    }

                    private void parseRetData(String originData, WeatherBean data) {
                        data.city = JSONUtils.ParseJSON(originData, "city");
                        data.time = JSONUtils.ParseJSON(originData, "time");
                        data.weather = JSONUtils.ParseJSON(originData, "weather");
                        data.temp = JSONUtils.ParseJSON(originData, "temp");
                        data.l_tmp = JSONUtils.ParseJSON(originData, "l_tmp");
                        data.h_tmp = JSONUtils.ParseJSON(originData, "h_tmp");
                        data.WD = JSONUtils.ParseJSON(originData, "WD");
                        data.WS = JSONUtils.ParseJSON(originData, "WS");
                        setWeather(data);
                    }

                    private void setWeather(WeatherBean data) {
                        tvWeather.setText("城市：" + data.city +
                                "更新时间：" + data.time +
                                "天气状况：" + data.weather +
                                "气温：" + data.temp +
                                "最低气温：" + data.l_tmp +
                                "最高气温：" + data.h_tmp +
                                "风向：" + data.WD +
                                "风速：" + data.WS);
                    }

                    @Override
                    public void onStart() {
                        Log.i(TAG, "start");
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.i(TAG, "失败");
                    }
                });
    }

    //点击打开报警按钮时按钮样式的响应操作
    public void openStateAlarmBtn() {
        alarmState = true;
        btnAlarmState.setText("关闭小安保");
        btnAlarmState.setBackgroundResource(R.drawable.btn_switch_selector_2);
    }

    //点击关闭报警按钮时按钮样式的响应操作
    public void closeStateAlarmBtn() {
        alarmState = false;
        btnAlarmState.setText("开启小安保");
        btnAlarmState.setBackgroundResource(R.drawable.btn_switch_selector_1);
    }

    public interface LocationTVClickedListener {
        void locationTVClicked();
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                String city = location.getCity();
                httpGetWeather(city);
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                String city = location.getCity();
                httpGetWeather(city);
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                Log.i(TAG, "离线定位成功，离线定位结果也是有效的");
                String city = location.getCity();
                httpGetWeather(city);
            } else {
                Log.e(TAG, "服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            }
        }
    }


}
