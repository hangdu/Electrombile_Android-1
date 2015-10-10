package com.xunce.electrombile.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.fragment.MaptabFragment;
import com.xunce.electrombile.fragment.SettingsFragment;
import com.xunce.electrombile.fragment.SwitchFragment;
import com.xunce.electrombile.fragment.SwitchFragment.LocationTVClickedListener;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.protocol.CmdModeSelect;
import com.xunce.electrombile.protocol.JsonKeys;
import com.xunce.electrombile.protocol.Protocol;
import com.xunce.electrombile.service.PushService;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.view.viewpager.CustomViewPager;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.yunba.android.manager.YunBaManager;


/**
 * Created by heyukun on 2015/3/24.
 */

public class FragmentActivity extends android.support.v4.app.FragmentActivity
        implements SwitchFragment.GPSDataChangeListener,
        LocationTVClickedListener {
    //推送通知用的
    public static PushService pushService;
    //保存自己的实例
    public static FragmentActivity fragmentActivity;
    private static String TAG = "FragmentActivity:";
    protected CmdCenter mCenter;
    //my service
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService实例
            pushService = ((PushService.MsgBinder) service).getService();

        }
    };
    private SwitchFragment switchFragment;
    private MaptabFragment maptabFragment;
    private SettingsFragment settingsFragment;
    //viewpager切换使用
    private CustomViewPager mViewPager;
    private RadioGroup main_radio;
    private int checkId = R.id.rbSwitch;
    //退出使用
    private boolean isExit = false;
    //接收广播
    private MyReceiver receiver;
    private SettingManager setManager;
    /**
     * The handler. to process exit()
     */
    private Handler exitHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            isExit = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        mCenter = CmdCenter.getInstance(this);
        setManager = new SettingManager(this);

        //初始化界面
        initView();
        initData();
        //注册广播
        registerBroadCast();
        //判断是否需要开启服务
        startServer();
        Historys.put(this);

        fragmentActivity = this;
    }

    private void startServer() {
        if (setManager.getIMEI().isEmpty()) {
            AVQuery<AVObject> query = new AVQuery<>("Bindings");
            final AVUser currentUser = AVUser.getCurrentUser();
            query.whereEqualTo("user", currentUser);
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> avObjects, AVException e) {
                    if (e == null && avObjects.size() > 0) {
                        setManager.setIMEI((String) avObjects.get(0).get("IMEI"));
                        Log.i(TAG + "AAAAAA", setManager.getIMEI());
                        final String topic = "simcom_" + setManager.getIMEI();
                        Log.i(TAG + "SSSSSSSSSS", topic);
                        //启动服务
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent("com.xunce.electrombile.service.PushService.MSG_ACTION");
                                bindService(intent, conn, Context.BIND_AUTO_CREATE);
                                //订阅云巴推送
                                YunBaManager.subscribe(getApplicationContext(), topic, new IMqttActionListener() {

                                    @Override
                                    public void onSuccess(IMqttToken arg0) {
                                        Log.d(TAG, "Subscribe topic succeed");
                                    }

                                    @Override
                                    public void onFailure(IMqttToken arg0, Throwable arg1) {
                                        Log.d(TAG, "Subscribe topic failed");
                                    }
                                });
                            }
                        }).start();

                        Log.d("成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                        ToastUtils.showShort(FragmentActivity.this, "设备查询成功");
                    } else {
                        Log.d("失败", "查询错误2: ");
                        ToastUtils.showShort(FragmentActivity.this, "请先绑定设备");
                    }
                }
            });

        } else {
            Log.i(TAG, setManager.getIMEI());
            final String topic = "simcom_" + setManager.getIMEI();
            Log.i(TAG + "SSSSSSSSSS", topic);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("com.xunce.electrombile.service.PushService.MSG_ACTION");
                    bindService(intent, conn, Context.BIND_AUTO_CREATE);
                    YunBaManager.subscribe(getApplicationContext(), topic, new IMqttActionListener() {

                        @Override
                        public void onSuccess(IMqttToken arg0) {
                            Log.d(TAG, "Subscribe topic succeed");
                        }

                        @Override
                        public void onFailure(IMqttToken arg0, Throwable arg1) {
                            if (arg0 != null)
                                Log.i(arg0.toString(), "XXXX");
                            if (arg1 != null)
                                Log.i("AAAA", arg1.toString());
                            Log.d(TAG, "Subscribe topic failed");
                        }
                    });
                }
            }).start();

            ToastUtils.showShort(this, "登陆成功");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!NetworkUtils.isNetworkConnected(this)) {
            NetworkUtils.networkDialog(this, true);
        }
    }

    private void registerBroadCast() {
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.xunce.electrombile.service");
        try {
            FragmentActivity.this.registerReceiver(receiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (pushService != null) {
            pushService.sendMessage1(mCenter.cmdFenceGet());
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        if (!setManager.getIMEI().isEmpty()) {
            pushService.actionStop(this);
            unbindService(conn);
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
            unregisterReceiver(receiver);
            //此方法会不在onDestory中调用，所以放在结束任务之前使用
            if (!setManager.getIMEI().isEmpty()) {
                pushService.actionStop(this);
                unbindService(conn);
            }
            if (TracksManager.getTracks() != null) TracksManager.clearTracks();

            //返回桌面
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
            Historys.exit();
        }
    }

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

    public class MyReceiver extends BroadcastReceiver {
        private Handler timeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ToastUtils.showShort(FragmentActivity.this, "设备超时！");
            }
        };

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "接收调用");
            Bundle bundle = intent.getExtras();
            String MODE = bundle.getString(CmdModeSelect.SELECT_MODE);
            Log.i(TAG, "MODE:" + MODE);
            Protocol protocol;
            protocol = (Protocol) bundle.get("protocol");
            if (CmdModeSelect.SELECT_MODE_GPS.equals(MODE)) {
                Log.i(TAG, "得到GPS");
                maptabFragment.cancelWaitTimeOut();
                onGPSArrived(protocol);
            } else if (CmdModeSelect.SELECT_MODE_CMD.equals(MODE)) {
                Log.i(TAG, "得到命令字");
                onCmdArrived(protocol);
            } else if (CmdModeSelect.SELECT_MODE_433.equals(MODE)) {
                // 找车
                on433Arrived(protocol);
            }

        }

        private void on433Arrived(Protocol protocol) {
            int intensity = protocol.getIntensity();
            caseSeekSendToFindAct(intensity);
        }

        private void onCmdArrived(Protocol protocol) {
            int cmd = protocol.getCmd();
            int result = protocol.getResult();
            timeHandler.removeMessages(JsonKeys.TIME_OUT);
            switch (cmd) {
                //如果是设置围栏的命令
                case JsonKeys.FENCE_ON:
                    switchFragment.cancelWaitTimeOut();
                    caseFence(result, true, "防盗开启成功");
                    break;
                //如果是设置关闭围栏的命令
                case JsonKeys.FENCE_OFF:
                    switchFragment.cancelWaitTimeOut();
                    caseFence(result, false, "防盗关闭成功");
                    break;
                //如果是获取围栏的命令
                case JsonKeys.FENCE_GET:
                    caseFenceGet(protocol, result);
                    break;
                //如果是开始找车的命令
                case JsonKeys.SEEK_ON:
                    caseSeek(result, "开始找车");
                    break;
                //如果是停止找车的命令
                case JsonKeys.SEEK_OFF:
                    caseSeek(result, "停止找车");
                    break;
                case JsonKeys.GET_GPS:
                    caseGetGPS(result);
                default:
                    break;
            }
        }

        private void caseGetGPS(int result) {
            maptabFragment.cancelWaitTimeOut();
            dealErr(result);
        }

        private void caseSeek(int result, String success) {
            if (JsonKeys.ERR_SUCCESS == result) {
                ToastUtils.showShort(FragmentActivity.this, success);
            } else {
                dealErr(result);
            }
            caseSeekSendToFindAct(0);
        }

        private void caseSeekSendToFindAct(int value) {
            Intent intent7 = new Intent();
            intent7.putExtra("intensity", value);
            intent7.setAction("com.xunce.electrombile.find");
            sendBroadcast(intent7);
        }

        private void caseFenceGet(Protocol protocol, int result) {
            if (JsonKeys.ERR_SUCCESS == result) {
                int state = protocol.getState();
                if (JsonKeys.ON == state) {
                    setManager.setAlarmFlag(true);
                    switchFragment.openStateAlarmBtn();
                } else if (JsonKeys.OFF == state) {
                    setManager.setAlarmFlag(false);
                    switchFragment.closeStateAlarmBtn();
                }
                ToastUtils.showShort(FragmentActivity.this, "查询状态成功");
            } else {
                dealErr(result);
            }
        }

        private void caseFence(int result, boolean successAlarmFlag, String success) {
            if (JsonKeys.ERR_SUCCESS == result) {
                setManager.setAlarmFlag(successAlarmFlag);
                switchFragment.msgSuccessArrived();
                ToastUtils.showShort(FragmentActivity.this, success);
            } else {
                dealErr(result);
            }
        }

        private void dealErr(int result) {
            switch (result) {
                case JsonKeys.ERR_WAITING:
                    ToastUtils.showShort(FragmentActivity.this, "正在设置命令，请稍后...");
                    timeHandler.sendEmptyMessageDelayed(JsonKeys.TIME_OUT, JsonKeys.TIME_OUT_VALUE * 2);
                    return;
                case JsonKeys.ERR_OFFLINE:
                    ToastUtils.showShort(FragmentActivity.this, "设备不在线，请检查电源。");
                    break;
                case JsonKeys.ERR_INTERNAL:
                    ToastUtils.showShort(FragmentActivity.this, "服务器内部错误，请稍后再试。");
                    break;
            }
        }

        private void onGPSArrived(Protocol protocol) {
            float Flat = protocol.getLat();
            float Flong = protocol.getLng();
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            TracksManager.TrackPoint trackPoint = null;
            trackPoint = new TracksManager.TrackPoint(curDate, mCenter.convertPoint(new LatLng(Flat, Flong)));
            LogUtil.log.i("保存数据1");
            setManager.setInitLocation(Flat + "", Flong + "");
            if (trackPoint != null) {
                if (!maptabFragment.isPlaying) {
                    maptabFragment.locateMobile(trackPoint);
                }
                switchFragment.reverserGeoCedec(trackPoint.point);
            }
        }
    }

}