package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
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
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.activity.SwitchManagedCar;
import com.xunce.electrombile.bean.WeatherBean;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.device.VibratorUtil;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.JSONUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.utils.useful.StringUtils;
import com.xunce.electrombile.view.MyHorizontalScrollView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SwitchFragment extends BaseFragment implements OnGetGeoCoderResultListener {
    private static String TAG = "SwitchFragment";
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private boolean alarmState = false;
    //缓存view
    private View rootView;
    private LocationTVClickedListener locationTVClickedListener;
    private Button ChangeAutobike;
    private Button TodayWeather;
    private ImageView headImage;
    private MyHorizontalScrollView myHorizontalScrollView;
    private TextView BindedCarIMEI;
    private ArrayList<String> OtherCar;
    private SwitchManagedCar switchManagedCar;
    private Dialog waitDialog;
    String WeatherData;
    Button btnAlarmState1;
    private static int Count = 0;
    public Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismissWaitDialog();
            ToastUtils.showShort(m_context, "指令下发失败，请检查网络和设备工作是否正常。");
        }
    };

    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1://处理侧滑的message
                    myHorizontalScrollView.UpdateListview();
                    break;
                case 2:
                    cancelWaitTimeOut();
                    break;
                case 3:
                    msgSuccessArrived();
                    break;
                case 4:
                    openStateAlarmBtn();
                    break;
                case 5:
                    closeStateAlarmBtn();
                    break;

            }
            return;
        }
    };


    public void alarmStatusChange() {
        Count++;
        if(1 == Count)
        {
            (m_context).receiver.setAlarmHandler(mhandler);
        }

        if (alarmState) {
            if (!setManager.getIMEI().isEmpty()) {
                if (NetworkUtils.isNetworkConnected(m_context)) {
                    //关闭报警
                    //等状态设置成功之后再改变按钮的显示状态，并且再更改标志位等的保存。
                    cancelNotification();
                    (m_context).sendMessage(m_context, mCenter.cmdFenceOff(), setManager.getIMEI());
                    showWaitDialog();
                    timeHandler.sendEmptyMessageDelayed(ProtocolConstants.TIME_OUT, ProtocolConstants.TIME_OUT_VALUE);

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
                    VibratorUtil.Vibrate(m_context, 700);
                    (m_context).sendMessage(m_context, mCenter.cmdFenceOn(), setManager.getIMEI());
                    showWaitDialog();
                    timeHandler.sendEmptyMessageDelayed(ProtocolConstants.TIME_OUT, ProtocolConstants.TIME_OUT_VALUE);
                } else {
                    ToastUtils.showShort(m_context, "请先绑定设备");
                }
            } else {
                ToastUtils.showShort(m_context, "网络连接失败");
            }
        }
    }

    public void showWaitDialog() {
        LayoutInflater inflater = (LayoutInflater) (m_context).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_wait, null);
        Animation animation = AnimationUtils.loadAnimation(m_context, R.anim.alpha);
        view.findViewById(R.id.iv).startAnimation(animation);
        waitDialog = new Dialog(m_context, R.style.Translucent_NoTitle_trans);
        waitDialog.addContentView(view, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        waitDialog.setContentView(view);
        waitDialog.setCancelable(false);
        waitDialog.show();
        WindowManager.LayoutParams params = waitDialog.getWindow().getAttributes();
        params.y = -156;
        waitDialog.getWindow().setAttributes(params);
    }

    /**
     * 取消显示等待框
     */
    public void dismissWaitDialog() {
        if (waitDialog != null) {
            waitDialog.dismiss();
        }
    }

    /**
     * 取消等待框的显示
     */
    public void cancelWaitTimeOut() {
        if (waitDialog != null) {
            dismissWaitDialog();
            timeHandler.removeMessages(ProtocolConstants.TIME_OUT);
        }
    }


    //取消显示常驻通知栏
    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) (m_context).getSystemService(m_context
                .NOTIFICATION_SERVICE);
        notificationManager.cancel(R.string.app_name);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //???
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
        initView();
      }



    //点击打开报警按钮时按钮样式的响应操作
    public void openStateAlarmBtn() {
        alarmState = true;
        btnAlarmState1.setText("防盗关闭");
        btnAlarmState1.setBackgroundResource(R.drawable.btn_switch_selector_2);
    }

    //点击关闭报警按钮时按钮样式的响应操作
    public void closeStateAlarmBtn() {
        alarmState = false;
        btnAlarmState1.setText("防盗开启");
        btnAlarmState1.setBackgroundResource(R.drawable.btn_switch_selector_1);
    }

    public void msgSuccessArrived() {
        if (setManager.getAlarmFlag()) {
            showNotification("安全宝防盗系统已启动");
            openStateAlarmBtn();
        } else {
            showNotification("安全宝防盗系统已关闭");
            VibratorUtil.Vibrate(m_context, 500);
            closeStateAlarmBtn();
        }
    }

    //显示常驻通知栏
    public void showNotification(String text) {
        NotificationManager notificationManager = (NotificationManager) (m_context).getSystemService(
                m_context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(m_context, FragmentActivity.class);
        PendingIntent contextIntent = PendingIntent.getActivity(m_context, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(m_context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("安全宝")
                .setWhen(System.currentTimeMillis())
                .setTicker("安全宝正在设置~")
                .setOngoing(true)
                .setContentText(text)
                .setContentIntent(contextIntent)
                .build();
        notificationManager.notify(R.string.app_name, notification);
    }

    private void initView() {
        //防盗开关的初始化
//        if (setManager.getAlarmFlag()) {
//            showNotification("安全宝防盗系统已启动");
//            openStateAlarmBtn();
//        } else {
//            closeStateAlarmBtn();
//        }

        btnAlarmState1 = (Button) getActivity().findViewById(R.id.btn_AlarmState1);
        btnAlarmState1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmStatusChange();

            }
        });

        BindedCarIMEI = (TextView)getActivity().findViewById(R.id.menutext1);

        OtherCar = new ArrayList();
        myHorizontalScrollView = (MyHorizontalScrollView) getActivity().findViewById(R.id.myHorizontalScrollView);

        myHorizontalScrollView.InitView();
        myHorizontalScrollView.setHandler(mhandler);
        myHorizontalScrollView.otherCarlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //UI界面改变
                String IMEI_previous = BindedCarIMEI.getText().toString();
                String IMEI_now = OtherCar.get(position);

                BindedCarIMEI.setText(IMEI_now);

                myHorizontalScrollView.list.remove(position);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("whichcar", IMEI_previous);
                map.put("img", R.drawable.img_1);
                myHorizontalScrollView.list.add(map);
                myHorizontalScrollView.UpdateListview();
                OtherCar.remove(position);
                OtherCar.add(IMEI_previous);

                //实际逻辑改变
                switchManagedCar = new SwitchManagedCar(m_context, m_context, IMEI_now, IMEI_previous);
                reSubscribe();
            }
        });

        refreshBindList();

        ChangeAutobike = (Button) getActivity().findViewById(R.id.ChangeAutobike);
        ChangeAutobike.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //侧滑的代码
                myHorizontalScrollView.toggle();
                if (myHorizontalScrollView.getIsOpen()) {
                    myHorizontalScrollView.UpdateListview();
                }
            }
        });


        TodayWeather = (Button) getActivity().findViewById(R.id.weather1);
        TodayWeather.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
                builder.setMessage(WeatherData.trim());
                builder.create();
                builder.show();
            }
        });


        headImage = (ImageView) getActivity().findViewById(R.id.headImage);
        headImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //补充关于更换头像的代码
            }
        });

        (m_context).receiver.setAlarmHandler(mhandler);
    }



    @Override
    public void onResume() {
        super.onResume();
        if (setManager.getAlarmFlag()) {
            openStateAlarmBtn();
            showNotification("安全宝防盗系统已启动");
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
//        switch_fragment_tvLocation.setText(result.getAddress().trim());
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
                        try {
                            data.errNum = JSONUtils.ParseJSON(originData, "errNum");
                            data.errMsg = JSONUtils.ParseJSON(originData, "errMsg");
                            if ("0".equals(data.errNum) && "success".equals(data.errMsg)) {
                                data.retData = JSONUtils.ParseJSON(originData, "retData");
                                parseRetData(data.retData, data);
                            } else {
                                Log.e(TAG, "fail to get Weather info");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    private void parseRetData(String originData, WeatherBean data) {
                        try {
                            data.city = JSONUtils.ParseJSON(originData, "city");
                            data.time = JSONUtils.ParseJSON(originData, "time");
                            data.weather = JSONUtils.ParseJSON(originData, "weather");
                            data.temp = JSONUtils.ParseJSON(originData, "temp");
                            data.l_tmp = JSONUtils.ParseJSON(originData, "l_tmp");
                            data.h_tmp = JSONUtils.ParseJSON(originData, "h_tmp");
                            data.WD = JSONUtils.ParseJSON(originData, "WD");
                            data.WS = JSONUtils.ParseJSON(originData, "WS");
                            setWeather(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
//                            tvWeather.setText("查询失败");
//                            TodayWeather.setText("查询失败");
                            ToastUtils.showShort(m_context, "天气查询失败");
                        }

                    }

                    private void setWeather(WeatherBean data) {
                        WeatherData = "气温：" + data.temp + "\n" +
                                "天气状况：" + data.weather + "\n" +
                                "城市：" + data.city + "\n" +
                                "风速：" + data.WS + "\n" +
                                "更新时间：" + data.time + "\n" +
                                "最低气温：" + data.l_tmp + "\n" +
                                "最高气温：" + data.h_tmp + "\n" +
                                "风向：" + data.WD + "\n";
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

    //刷新车列表
    public void refreshBindList() {
        AVUser currentUser = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        query.whereEqualTo("user", currentUser);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    Log.e("BINDLISTACT", list.size() + "");
                    if (list.size() > 0) {
                        myHorizontalScrollView.list.clear();
                        OtherCar.clear();
                        HashMap<String, Object> map = null;
                        for (int i = 0; i < list.size(); i++) {
                            //判断这个IMEI是不是正在被监管的车辆
                            if(setManager.getIMEI().equals(list.get(i).get("IMEI")))
                            {
                                BindedCarIMEI.setText((String)list.get(i).get("IMEI"));
                            }
                            else{
                                map = new HashMap<>();
                                map.put("whichcar",list.get(i).get("IMEI"));
                                map.put("img", R.drawable.img_1);
                                myHorizontalScrollView.list.add(map);
                                OtherCar.add((String)list.get(i).get("IMEI"));
                            }

                        }
//                        myHorizontalScrollView.UpdateListview();
                    }
                } else {
                    e.printStackTrace();
//                    ToastUtils.showShort(BindListActivity.this, "查询错误");
                }
            }
        });
    }

    private void reSubscribe() {
        Intent intent;
        intent = new Intent("com.xunce.electrombile.alarmservice");
        m_context.stopService(intent);
//        (m_context).closeStateAlarmBtn();
        closeStateAlarmBtn();
        (m_context).queryIMEI();
    }
}
