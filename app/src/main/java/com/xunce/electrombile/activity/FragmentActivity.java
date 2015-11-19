package com.xunce.electrombile.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil;
import com.baidu.mapapi.model.LatLng;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.fragment.MaptabFragment;
import com.xunce.electrombile.fragment.SettingsFragment;
import com.xunce.electrombile.fragment.SwitchFragment;
import com.xunce.electrombile.fragment.SwitchFragment.LocationTVClickedListener;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.receiver.MyReceiver;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.view.viewpager.CustomViewPager;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by heyukun on 2015/3/24.
 * 修改 by liyanbo
 */

public class FragmentActivity extends android.support.v4.app.FragmentActivity
        implements SwitchFragment.GPSDataChangeListener,
        LocationTVClickedListener {
    private static final String TAG = "FragmentActivity:";

    public MqttAndroidClient mac;
    public CmdCenter mCenter;
    public SwitchFragment switchFragment;
    public MaptabFragment maptabFragment;
    public SettingsFragment settingsFragment;
    public SettingManager setManager;
    //viewpager切换使用
    private CustomViewPager mViewPager;
    private RadioGroup main_radio;
    private int checkId = R.id.rbSwitch;
    //退出使用
    private boolean isExit = false;
    //接收广播
    private MyReceiver receiver;
    /**
     * The handler. to process exit()
     */
    private Handler exitHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            isExit = false;
        }
    };

    public RadioGroup getMain_radio() {
        return main_radio;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        mCenter = CmdCenter.getInstance(this);
        setManager = new SettingManager(this);
        //初始化界面
        initView();
        initData();
        //判断是否绑定设备
        queryIMEI();
//        //注册广播
//        registerBroadCast();
        Historys.put(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!NetworkUtils.isNetworkConnected(this)) {
            NetworkUtils.networkDialog(this, true);
        }
    }

    @Override
    public void onBackPressed() {
        exit();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mac != null && mac.isConnected()) {
            mac.registerResources(this);
            sendMessage(FragmentActivity.this, mCenter.cmdFenceGet(), setManager.getIMEI());
        }
    }

    @Override
    protected void onDestroy() {
        switchFragment.cancelNotification();
        if (mac != null) {
            mac.unregisterResources();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
        if (TracksManager.getTracks() != null) TracksManager.clearTracks();
        super.onDestroy();
    }

    @Override
    public void gpsCallBack(LatLng desLat, TracksManager.TrackPoint trackPoint) {
        //传递数据给地图的Fragment
        //如果正在播放轨迹，则更新位置
        //    Log.i("gpsCallBack","called");
        if (!maptabFragment.isPlaying)
            maptabFragment.locateMobile(trackPoint);
        switchFragment.reverserGeoCedec(desLat);
    }

    @Override
    public void locationTVClicked() {
        checkId = R.id.rbMap;
        main_radio.check(checkId);
        checkId = 1;
    }

    /**
     * 得到mac
     * @return
     */
    public MqttAndroidClient getMac() {
        return mac;
    }

    /**
     * 建立MQTT连接
     */
    private void getMqttConnection() {
        Connection connection = Connection.createConnection(ServiceConstants.clientId,
                ServiceConstants.MQTT_HOST,
                ServiceConstants.PORT,
                FragmentActivity.this,
                false);
        ServiceConstants.handler = connection.handle();
        MqttConnectOptions mcp = new MqttConnectOptions();
        mcp.setCleanSession(true);
        connection.addConnectionOptions(mcp);
        mac = connection.getClient();
        try {
            mac.connect(mcp, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribe(mac);
                    ToastUtils.showShort(FragmentActivity.this, "服务器连接成功");
                    registerBroadCast();
                    startAlarmService();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    ToastUtils.showShort(FragmentActivity.this, "服务器连接失败");
                    Log.d(TAG, exception.toString());
                }
            });
            Connections.getInstance(FragmentActivity.this).addConnection(connection);
        } catch (MqttException e1) {
            e1.printStackTrace();
        }

    }

    /**
     * 开启报警服务
     */
    private void startAlarmService() {
        Intent intent = new Intent();
        intent.setAction("com.xunce.electrombile.alarmservice");
        intent.setPackage(getPackageName());
        FragmentActivity.this.startService(intent);
    }

    /**
     * 发送命令
     *
     * @param context 传递上下文，用于弹吐司
     * @param message 要发送的命令
     * @param IMEI    要发送的设备号
     */
    public void sendMessage(Context context, byte[] message, String IMEI) {
        if (mac == null) {
            ToastUtils.showShort(context, "请先连接设备，或等待连接。");
            return;
        }
        try {
            mac.publish("app2dev/" + IMEI + "/cmd", message, ServiceConstants.MQTT_QUALITY_OF_SERVICE, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询并判断是否该建立MQTT连接
     */
    private void queryIMEI() {
        if (setManager.getIMEI().isEmpty()) {
            AVQuery<AVObject> query = new AVQuery<>("Bindings");
            final AVUser currentUser = AVUser.getCurrentUser();
            query.whereEqualTo("user", currentUser);
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> avObjects, AVException e) {
                    if (e == null && avObjects.size() > 0) {
                        setManager.setIMEI((String) avObjects.get(0).get("IMEI"));
                        //建立连接
                        getMqttConnection();
                        Log.d("成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                        ToastUtils.showShort(FragmentActivity.this, "设备查询成功");
                    } else {
                        Log.d("失败", "查询错误2: ");
                        ToastUtils.showShort(FragmentActivity.this, "请先绑定设备");
                    }
                }
            });

        } else {
            getMqttConnection();
            ToastUtils.showShort(FragmentActivity.this, "登陆成功");
        }
    }

    /**
     * 订阅话题
     * @param mac mqtt的客户端
     */
    private void subscribe(MqttAndroidClient mac) {
        //订阅命令字
        String initTopic = setManager.getIMEI();
        String topic1 = "dev2app/" + initTopic + "/cmd";
        //订阅GPS数据
        String topic2 = "dev2app/" + initTopic + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + initTopic + "/433";
        //订阅报警
        String topic4 = "dev2app/" + initTopic + "/alarm";
        String[] topic = {topic1, topic2, topic3, topic4};
        int[] qos = {ServiceConstants.MQTT_QUALITY_OF_SERVICE, ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE, ServiceConstants.MQTT_QUALITY_OF_SERVICE};
        try {
            mac.subscribe(topic, qos);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic1);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic2);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic3);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic4);
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(this, "订阅失败!请稍后重启再试！");
        }

    }

    /**
     * 注册广播  监听话题为：cmd gps 433
     */
    private void registerBroadCast() {
        receiver = new MyReceiver(FragmentActivity.this);
        IntentFilter filter = new IntentFilter();
        filter.setPriority(800);
        filter.addAction("MqttService.callbackToActivity.v0");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    /**
     * 界面初始化
     */
    private void initView() {
        main_radio = (RadioGroup) findViewById(R.id.main_radio);
        mViewPager = (CustomViewPager) findViewById(R.id.viewpager);
        switchFragment = new SwitchFragment();
        maptabFragment = new MaptabFragment();
        settingsFragment = new SettingsFragment();
    }

    /**
     * 数据初始化
     */
    private void initData() {
        List<Fragment> list = new ArrayList<>();
        list.add(switchFragment);
        list.add(maptabFragment);
        list.add(settingsFragment);
        HomePagerAdapter mAdapter = new HomePagerAdapter(getSupportFragmentManager(), list);
        mViewPager.setAdapter(mAdapter);
        main_radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rbSwitch:
                        mViewPager.setCurrentItem(0, false);
                        checkId = 0;
                        break;
                    case R.id.rbMap:
                        mViewPager.setCurrentItem(1, false);
                        checkId = 1;
                        break;
                    case R.id.rbSettings:
                        mViewPager.setCurrentItem(2, false);
                        checkId = 2;
                        break;
                    default:
                        break;
                }
            }
        });
        main_radio.check(checkId);
    }



    /**
     * 重复按下返回键退出app方法
     */
    public void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(),
                    "退出程序", Toast.LENGTH_SHORT).show();
            exitHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            switchFragment.cancelNotification();
            if (mac != null) {
                mac.unregisterResources();
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            }
            //此方法会不在onDestory中调用，所以放在结束任务之前使用
            if (TracksManager.getTracks() != null) TracksManager.clearTracks();

            Historys.exit();
        }
    }

    /**
     * 界面切换
     */
    class HomePagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> list;

        public HomePagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

}