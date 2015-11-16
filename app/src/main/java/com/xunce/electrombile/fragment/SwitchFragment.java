package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.bean.WeatherBean;
import com.xunce.electrombile.utils.device.VibratorUtil;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.JSONUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import java.util.ArrayList;

public class SwitchFragment extends BaseFragment implements OnGetGeoCoderResultListener {

    private static final String URL = "http://wap.koudaitong.com/v2/home/a02xn3a3";
    private static String TAG = "SwitchFragment";
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    // 上一个页面的位置
    protected int lastPosition;
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private boolean alarmState = false;
    //缓存view
    private View rootView;
    private Button btnAlarmState;
    //textview 设置当前位置
    private TextView switch_fragment_tvLocation;
    private LocationTVClickedListener locationTVClickedListener;
    private TextView tvWeather;
    //viewpager
    private ViewPager viewPager;
    private LinearLayout pointGroup;
    private TextView imageDesc;
    // 图片资源ID
    private int[] imageIds = {R.drawable.first_fragment_iv, R.drawable.alarm_iv, R.drawable.iv_add_photo_person_act,
            R.drawable.iv_person_head};
    //图片标题集合
    private String[] imageDescriptions = {
            "哈哈",
            "哈哈哈哈",
            "哈哈哈哈哈哈",
            "哈哈哈哈哈哈哈哈",
            "哈哈哈哈哈哈哈哈哈哈"
    };
    private ArrayList<ImageView> imageList;
    //判断是否自动滚动
    private boolean isRunning = false;
    private Handler viewPagerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //让viewPager 滑动到下一页
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            if (isRunning) {
                viewPagerHandler.sendEmptyMessageDelayed(0, 2000);
            }
        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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

    private void initView() {
        //报警按钮
        btnAlarmState = (Button) getActivity().findViewById(R.id.btn_AlarmState);

        //天气按钮
        tvWeather = (TextView) getActivity().findViewById(R.id.weather);
        tvWeather.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
                builder.setMessage(tvWeather.getText().toString().trim());
                builder.create();
                builder.show();
            }
        });

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
        //开关按钮的点击事件
        btnAlarmState.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alarmState) {
                    if (!setManager.getIMEI().isEmpty()) {
                        if (NetworkUtils.isNetworkConnected(m_context)) {
                            //关闭报警
                            //等状态设置成功之后再改变按钮的显示状态，并且再更改标志位等的保存。
                            cancelNotification();
                            ((FragmentActivity) m_context).sendMessage(m_context, mCenter.cmdFenceOff(), setManager.getIMEI());
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
                            VibratorUtil.Vibrate(getActivity(), 700);
                            ((FragmentActivity) m_context).sendMessage(m_context, mCenter.cmdFenceOn(), setManager.getIMEI());
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
        });

//        ImageView title_image = (ImageView) getActivity().findViewById(R.id.viewpager);
//        title_image.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = Uri.parse(URL);
//                Intent it = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(it);
//            }
//        });
        initViewpager();

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

    //初始化viewpager
    private void initViewpager() {
        if (viewPager == null) {
            viewPager = (ViewPager) getActivity().findViewById(R.id.banner_viewpager);
            pointGroup = (LinearLayout) getActivity().findViewById(R.id.point_group);
            imageDesc = (TextView) getActivity().findViewById(R.id.image_desc);
            imageDesc.setText(imageDescriptions[0]);
            imageList = new ArrayList<ImageView>();
            for (int i = 0; i < imageIds.length; i++) {
                //初始化图片资源
                ImageView image = new ImageView(m_context);
                image.setBackgroundResource(imageIds[i]);
                imageList.add(image);

                //添加指示点
                ImageView point = new ImageView(m_context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.rightMargin = 10;
                point.setLayoutParams(params);
                point.setBackgroundResource(R.drawable.point_bg);
                if (i == 0) {
                    point.setEnabled(false);
                } else {
                    point.setEnabled(true);
                }
                pointGroup.addView(point);
            }

            viewPager.setAdapter(new MyPagerAdapter());

            viewPager.setCurrentItem(Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % imageList.size()));

            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                /**
                 * 页面切换后调用
                 * position  新的页面位置
                 */
                public void onPageSelected(int position) {

                    position = position % imageList.size();

                    //设置文字描述内容
                    imageDesc.setText(imageDescriptions[position]);

                    //改变指示点的状态
                    //把当前点enbale 为true
                    pointGroup.getChildAt(position).setEnabled(true);
                    //把上一个点设为false
                    pointGroup.getChildAt(lastPosition).setEnabled(false);
                    lastPosition = position;

                }

                @Override
                /**
                 * 页面正在滑动的时候，回调
                 */
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                }

                @Override
                /**
                 * 当页面状态发生变化的时候，回调
                 */
                public void onPageScrollStateChanged(int state) {

                }
            });

		 /*
          * 自动循环：
		  * 1、定时器：Timer
		  * 2、开子线程 while  true 循环
		  * 3、ColckManager
		  * 4、 用handler 发送延时信息，实现循环
		  */
            isRunning = true;
            viewPagerHandler.sendEmptyMessageDelayed(0, 2000);
        }
    }

    //显示常驻通知栏
    public void showNotification(String text) {
        NotificationManager notificationManager = (NotificationManager) m_context.getSystemService(getActivity().
                getApplicationContext()
                .NOTIFICATION_SERVICE);
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
        isRunning = false;
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
                        String tmp = "气温：" + data.temp + "\n" +
                                "天气状况：" + data.weather + "\n" +
                                "城市：" + data.city + "\n" +
                                "风速：" + data.WS + "\n" +
                                "更新时间：" + data.time + "\n" +
                                "最低气温：" + data.l_tmp + "\n" +
                                "最高气温：" + data.h_tmp + "\n" +
                                "风向：" + data.WD + "\n";
                        tvWeather.setText(tmp);
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
        btnAlarmState.setText("关闭小安宝");
        btnAlarmState.setBackgroundResource(R.drawable.btn_switch_selector_2);
    }

    //点击关闭报警按钮时按钮样式的响应操作
    public void closeStateAlarmBtn() {
        alarmState = false;
        btnAlarmState.setText("开启小安宝");
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

    //viewpager 适配器
    private class MyPagerAdapter extends PagerAdapter {
        @Override
        /**
         * 获得页面的总数
         */
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        /**
         * 获得相应位置上的view
         * container  view的容器，其实就是viewpager自身
         * position 	相应的位置
         */
        public Object instantiateItem(ViewGroup container, int position) {
            // 给 container 添加一个view
            container.addView(imageList.get(position % imageList.size()));
            //返回一个和该view相对的object
            return imageList.get(position % imageList.size());
        }

        @Override
        /**
         * 判断 view和object的对应关系
         */
        public boolean isViewFromObject(View view, Object object) {
            if (view == object) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        /**
         * 销毁对应位置上的object
         */
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            object = null;
        }
    }
}
