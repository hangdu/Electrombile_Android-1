package com.xunce.electrombile.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.baidu.mapapi.model.LatLng;
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.fragment.MaptabFragment;
import com.xunce.electrombile.fragment.SettingsFragment;
import com.xunce.electrombile.fragment.SwitchFragment;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.receiver.MyReceiver;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.view.viewpager.CustomViewPager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by heyukun on 2015/3/24. 修改 by liyanbo
 */

public class FragmentActivity extends android.support.v4.app.FragmentActivity
        implements SwitchFragment.GPSDataChangeListener{
    private static final String TAG = "FragmentActivity:";
    private static final int DELAYTIME = 500;
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
    public MyReceiver receiver;
    private MqttConnectManager mqttConnectManager;
    private Logger log;

    /**
     * The handler. to process exit()
     */
    private Handler exitHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            isExit = false;
        }
    };
    private Dialog waitDialog;
    public Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 999) {
                settingsFragment.temp = 0;
                return;
            }
            dismissWaitDialog();
            ToastUtils.showShort(FragmentActivity.this, "指令下发失败，请检查网络和设备工作是否正常。");
        }
    };

    public RadioGroup getMain_radio() {
        return main_radio;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogConfigure();
        log.info("onCreate-start");
        com.orhanobut.logger.Logger.i("FragmentActivity-onCreate", "start");
        super.onCreate(savedInstanceState );
        setContentView(R.layout.activity_fragment);

        //初始化界面
        initView();
        initData();
        //判断是否绑定设备
        queryIMEI();
        Historys.put(this);
        registerBroadCast();
        log.info("onCreate-finish");
    }

    @Override
    protected void onStart() {
        log.info("onStart-start");
        com.orhanobut.logger.Logger.i("FragmentActivity-onStart", "start");
        super.onStart();
        if (!NetworkUtils.isNetworkConnected(this)) {
            NetworkUtils.networkDialog(this, true);
        }
        log.info("onStart-finish");
    }

    @Override
    protected void onResume() {
        //这个函数的onResume会被反复执行吗?
        log.info("onResume-start");
        com.orhanobut.logger.Logger.i("FragmentActivity-onResume", "start");
        super.onResume();
        //下面这句话只需要执行一次
        if (mac != null && mac.isConnected()) {
            //这句话干嘛的
            mac.registerResources(this);
        }
        log.info("onResume-finish");
    }

    @Override
    protected void onPause() {
        log.info("onPause-start");
        com.orhanobut.logger.Logger.i("FragmentActivity-onPause", "start");
        super.onPause();
        log.info("onPause-finish");
    }

    @Override
    public void onStop(){
        log.info("onStop-start");
        com.orhanobut.logger.Logger.i("FragmentActivity-onStop", "start");
        super.onStop();
        log.info("onStop-finish");
    }

    @Override
    protected void onDestroy() {
        log.info("onDestroy-start");
        com.orhanobut.logger.Logger.i("FragmentActivity-onDestroy", "start");
        cancelNotification();
        if (mac != null) {
            mac.unregisterResources();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
        if (TracksManager.getTracks() != null) TracksManager.clearTracks();
        super.onDestroy();
        log.info("onDestroy-finish");
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

    //取消显示常驻通知栏
    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(getApplicationContext()
                .NOTIFICATION_SERVICE);
        notificationManager.cancel(R.string.app_name);
    }

    /**
     * 显示等待框
     */
    public void showWaitDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_wait, null);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        view.findViewById(R.id.iv).startAnimation(animation);
        waitDialog = new Dialog(this, R.style.Translucent_NoTitle_trans);
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


    /**
     * 建立MQTT连接
     */
    private void getMqttConnection() {
        mqttConnectManager.setOnMqttConnectListener(new MqttConnectManager.OnMqttConnectListener() {
            @Override
            public void MqttConnectSuccess() {
                mac = mqttConnectManager.getMac();
                mqttConnectManager.subscribe(setManager.getIMEI(), FragmentActivity.this);
                ToastUtils.showShort(FragmentActivity.this, "服务器连接成功");
                log.info("getMqttConnection  服务器连接成功");
                setManager.setMqttStatus(true);
                //开启报警服务
                startAlarmService();
//                GetAlarmStatusFromServer();
//                GetAutoLockStatusFromServer();
            }

            @Override
            public void MqttConnectFail() {
                ToastUtils.showShort(FragmentActivity.this, "连接服务器失败");
            }
        });
        log.info("getMqttConnection 开始连接服务器");
        mqttConnectManager.getMqttConnection();
    }
    /**
     * 延时发送消息
     *
     * @param message 发送的内容
     * @param IMEI    设备IMEI号
     */
    public void DelaySendMessage(final byte[] message, final String IMEI) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                try {
                    mac.publish("app2dev/" + IMEI + "/cmd", message, ServiceConstants.MQTT_QUALITY_OF_SERVICE, false);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }, DELAYTIME);
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
    public void sendMessage(Context context, final byte[] message, final String IMEI) {
        if (mac == null) {
            ToastUtils.showShort(context, "请先连接设备，或等待连接。");
            return;
        } else if (!mac.isConnected()) {
            mqttConnectManager.ReMqttConnect();
            DelaySendMessage(message, IMEI);
            return;
        }
        try {
            //向服务器发送命令
            mac.publish("app2dev/" + IMEI + "/cmd", message, ServiceConstants.MQTT_QUALITY_OF_SERVICE, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询并判断是否该建立MQTT连接
     */
    public void queryIMEI(){
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
                    } else {
                        Log.d("失败", "查询错误2: ");
                        ToastUtils.showShort(FragmentActivity.this, "请先绑定设备");
                    }
                }
            });

        } else {
            getMqttConnection();
            ToastUtils.showShort(FragmentActivity.this, "登陆成功");
            log.info("登录成功");
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
        LocalBroadcastManager.getInstance(FragmentActivity.this).registerReceiver(receiver, filter);
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
        mCenter = CmdCenter.getInstance(this);
        setManager = new SettingManager(this);
        mqttConnectManager = MqttConnectManager.getInstance();
        mqttConnectManager.setContext(FragmentActivity.this);
        mqttConnectManager.initMqtt();

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
                        com.orhanobut.logger.Logger.i("FragmentActivity-buttonMap-clicked", "start");
                        log.info("rbMap-clicked");
                        mViewPager.setCurrentItem(1, false);
                        checkId = 1;

                        //初始化一些东西
                        maptabFragment.HideInfowindow();
                        maptabFragment.setCarname();
                        maptabFragment.InitCarLocation();

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
            cancelNotification();
            if (mac != null) {
                mac.unregisterResources();
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            }
            //此方法会不在onDestory中调用，所以放在结束任务之前使用
            if (TracksManager.getTracks() != null) TracksManager.clearTracks();
            timeHandler.removeMessages(0);
            timeHandler = null;
            Historys.exit();
        }
    }

    private void GetAlarmStatusFromServer(){
        if (mac != null && mac.isConnected())
        {
            sendMessage(FragmentActivity.this, mCenter.cmdFenceGet(), setManager.getIMEI());
        }
        else{
            //绝对不会到这个分支来
            ToastUtils.showShort(FragmentActivity.this, "mqtt连接失败");
        }
    }

    private void GetAutoLockStatusFromServer(){
        if (mac != null && mac.isConnected())
        {
            sendMessage(FragmentActivity.this, mCenter.APP_CMD_AUTOLOCK_GET(), setManager.getIMEI());
        }
        else{
            //绝对不会到这个分支来
            ToastUtils.showShort(FragmentActivity.this, "mqtt连接失败");
        }
    }

    private void LogConfigure(){
        LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(Environment.getExternalStorageDirectory()
                + File.separator + "MyApp" + File.separator + "logs"
                + File.separator + "log4j.txt");
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.configure();
        log = Logger.getLogger(FragmentActivity.class);
        log.info("My Application Created");
    }

    @Override
    public void onBackPressed() {
        exit();
    }
}