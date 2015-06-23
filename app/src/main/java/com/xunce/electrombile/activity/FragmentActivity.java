package com.xunce.electrombile.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.xunce.electrombile.Base.sdk.CmdCenter;
import com.xunce.electrombile.Base.sdk.SettingManager;
import com.xunce.electrombile.Base.utils.Historys;
import com.xunce.electrombile.Base.utils.TracksManager;
import com.xunce.electrombile.R;
import com.xunce.electrombile.Updata.UpdateAppService;
import com.xunce.electrombile.fragment.MaptabFragment;
import com.xunce.electrombile.fragment.SettingsFragment;
import com.xunce.electrombile.fragment.SwitchFragment;
import com.xunce.electrombile.fragment.SwitchFragment.LocationTVClickedListener;
import com.xunce.electrombile.service.PushService;
import com.xunce.electrombile.viewpager.CustomViewPager;
import com.xunce.electrombile.xpg.common.useful.NetworkUtils;
import com.xunce.electrombile.xpg.ui.utils.ToastUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.yunba.android.manager.YunBaManager;


/**
 * Created by heyukun on 2015/3/24.
 */

public class FragmentActivity extends android.support.v4.app.FragmentActivity implements SwitchFragment.GPSDataChangeListener, LocationTVClickedListener {
    private static String TAG = "FragmentActivity:";
    private SwitchFragment switchFragment;
    private MaptabFragment maptabFragment;
    private SettingsFragment settingsFragment;

    //viewpager切换使用
    private CustomViewPager mViewPager;
    private RadioGroup main_radio;
    private int checkId = R.id.rbSwitch;

    boolean isupde;
    int a = 0;
    //退出使用
    private boolean isExit = false;

    protected CmdCenter mCenter;

    //接收广播
    private MyReceiver receiver;
    private SettingManager setManager;
    //推送通知用的
    public static PushService pushService;

    //查询电子围栏
    byte firstByteSearch = 0x00;
    byte secondByteSearch = 0x00;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        mCenter = CmdCenter.getInstance(this);
        setManager = new SettingManager(this);

        //检查版本
        //checkVersion();

        //初始化界面
        initView();
        initData();
        //注册广播
        registerBroadCast();
        //判断是否需要开启服务
        startServer();
        Historys.put(this);


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
                        final String topic = "e2link_" + setManager.getIMEI();
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
            final String topic = "e2link_" + setManager.getIMEI();
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


    Handler MyHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            isupde = bundle.getBoolean("isupdate");
            a = bundle.getInt("want");
            if (isupde) {
                upData();
                isupde = false;
            }
        }
    };

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
     * 检查更新
     */
    public void upData() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("软件升级")
                .setMessage("发现新版本,建议立即更新使用.")
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent updateIntent = new Intent(FragmentActivity.this, UpdateAppService.class);
                        startService(updateIntent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alert.create().show();
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
        //IS_STARTED = true;
        super.onResume();
        if (pushService != null) {
            byte[] serial = mCenter.getSerial(firstByteSearch, secondByteSearch);
            pushService.sendMessage1(mCenter.cFenceSearch(serial));
        }
    }

    @Override
    protected void onDestroy() {
        //    IS_STARTED = false;
        unregisterReceiver(receiver);
        if (!setManager.getIMEI().isEmpty()) {
            pushService.actionStop(this);
            unbindService(conn);
        }
        if (TracksManager.getTracks() != null) TracksManager.clearTracks();
        super.onDestroy();
    }


    public void checkVersion() {
            Log.i("updata version","aaaaaaaaaaaaa");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                boolean isupdate;
                String baseUrl = "http://fir.im/api/v2/app/version/%s?token=%s";
                //下面是正式的 版本调整
               // String checkUpdateUrl = String.format(baseUrl, "553ca95096a9fc5c14001802", "39d16f30ebf111e4a2da4efe6522248a4b9d9ed4");
                String checkUpdateUrl = String.format(baseUrl, "556c810d2bb8ac0e5d001a30", "b9d54ba0b12411e4bc2c492c76a46d264a53ba2f");

                HttpClient httpClient = new DefaultHttpClient();
                //请求超时
                httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
                //读取超时
                httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
                try {
                    HttpGet httpGet = new HttpGet(checkUpdateUrl);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        String firResponse = EntityUtils.toString(httpEntity);
                        JSONObject versionJsonObj = new JSONObject(firResponse);
                        //FIR上当前的versionCode
                        int firVersionCode = Integer.parseInt(versionJsonObj.getString("version"));
                        //FIR上当前的versionName
                        String firVersionName = versionJsonObj.getString("versionShort");
                        PackageManager pm = FragmentActivity.this.getPackageManager();
                        PackageInfo pi = pm.getPackageInfo(FragmentActivity.this.getPackageName(),
                                PackageManager.GET_ACTIVITIES);
                        if (pi != null) {
                            int currentVersionCode = pi.versionCode;
                            String currentVersionName = pi.versionName;
//                            Log.i("当前版本",currentVersionCode+"");
//                            Log.i("查看版本",firVersionCode+"");
                            if (firVersionCode > currentVersionCode) {
                                //需要更新
                               Log.i("infox", "need update");
                                bundle.putInt("want",1);
                                bundle.putBoolean("isupdate",true);
                            } else if (firVersionCode == currentVersionCode) {
                                //如果本地app的versionCode与FIR上的app的versionCode一致，则需要判断versionName.
                                if (!currentVersionName.equals(firVersionName)) {
                                          Log.i("infox", "need update");
                                    bundle.putInt("want",1);
                                    bundle.putBoolean("isupdate",true);
                                }
                            } else {
                                //不需要更新,当前版本高于FIR上的app版本.
                                     Log.i("infox", " no need update");
                                bundle.putBoolean("isupdate",false);
                            }
                            msg.setData(bundle);
                            FragmentActivity.this.MyHandler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
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

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "接收调用");

            Bundle bundle = intent.getExtras();
            boolean cmdOrGPS = bundle.getBoolean("CMDORGPS");
            if (!cmdOrGPS) {
                Log.i(TAG, "GPS？");
                float Flat = bundle.getFloat("LAT");
                float Flong = bundle.getFloat("LONG");
                String date = bundle.getString("DATE");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
                TracksManager.TrackPoint trackPoint = null;
                try {
                    trackPoint = new TracksManager.TrackPoint(sdf.parse(date), mCenter.convertPoint(new LatLng(Flat, Flong)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                LogUtil.log.i("保存数据1");
                setManager.setInitLocation(Flat + "", Flong + "");
                if (trackPoint != null) {
                    if (!maptabFragment.isPlaying) {
                        maptabFragment.locateMobile(trackPoint);
                    }
                    switchFragment.reverserGeoCedec(trackPoint.point);
                }
            } else {
                Log.i(TAG, "弹不出来？？？");
                byte[] cmd = bundle.getByteArray("CMD");
                //test用
                // pushService.sendMessage1(mCenter.cTestGPS(new byte[]{0x00,0x12}));
                // pushService.sendMessage1(mCenter.cTestGPS(new byte[]{0x00,0x12}));
                //pushService.sendMessage1(mCenter.cTest(new byte[]{0x00,0x11}));
                //  ToastUtils.showShort(FragmentActivity.this,"命令字是"+cmd[3]);
                if (cmd[3] == 0x01) {
                    switchFragment.cancelDialog();
                    // DeviceUtils.showNotifation(FragmentActivity.this, "安全宝", "设置成功");
                    if (setManager.getAlarmFlag())
                        ToastUtils.showShort(FragmentActivity.this, "防盗开启成功");
                    else
                        ToastUtils.showShort(FragmentActivity.this, "防盗关闭成功");
                } else if (cmd[3] == 0x03) {
                    String cmdString = new String(cmd);
                    Log.i(TAG, cmdString);
                    if (cmdString.contains("NONE")) {
                        setManager.setAlarmFlag(false);
                    } else {
                        setManager.setAlarmFlag(true);
                    }
                    ToastUtils.showShort(FragmentActivity.this, "同步设置成功");
                } else if (cmd[3] == 0x04) {
                    Log.i(TAG, "查询所得到的结果");
                    float Flat = bundle.getFloat("LAT");
                    float Flong = bundle.getFloat("LONG");
                    String date = bundle.getString("DATE");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
                    Date curDate = new Date(System.currentTimeMillis());//获取当前时间

                    TracksManager.TrackPoint trackPoint = null;
                    try {

                        Date mDate = sdf.parse(sdf.format(curDate));
                        trackPoint = new TracksManager.TrackPoint(mDate, mCenter.convertPoint(new LatLng(Flat, Flong)));
                        LogUtil.log.i("保存数据2");
                        setManager.setInitLocation(Flat + "", Flong + "");
                        // trackPoint = new TracksManager.TrackPoint(sdf.parse(date), mCenter.convertPoint(new LatLng(Flat, Flong)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //LatLng point = mCenter.convertPoint(new LatLng(Flat, Flong));
                    if (trackPoint != null) {
                        if (!maptabFragment.isPlaying) {
                            maptabFragment.locateMobile(trackPoint);
                        }
                        maptabFragment.cancelWaitDialog();
                        switchFragment.reverserGeoCedec(trackPoint.point);
                        ToastUtils.showShort(FragmentActivity.this, "查询位置成功");
                    }
                } else if (cmd[3] == 0x05) {
                    addSosActivity.cancelDialog();
                    ToastUtils.showShort(FragmentActivity.this, "设置管理员成功");
                } else if (cmd[3] == 0x06) {
                    String data = new String(cmd);
                    LogUtil.log.i(data);
                    addSosActivity.cancelDialog(data);
                }
            }
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

    /**
     * The handler. to process exit()
     */
    private Handler exitHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            isExit = false;
        }
    };

}