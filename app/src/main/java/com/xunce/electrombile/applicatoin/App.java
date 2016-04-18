package com.xunce.electrombile.applicatoin;

import android.app.Application;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.baidu.mapapi.SDKInitializer;
import com.xunce.electrombile.database.DBManage;
import com.xunce.electrombile.log.MyLog;

import im.fir.sdk.FIR;
//import io.yunba.android.manager.YunBaManager;


/**
 * Created by jk on 2015/3/23.
 */
public class App extends Application {
    private static final String TAG = "App";

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //initial BugHD
        FIR.init(this);
        instance = this;

        //initial the Baidu map SDK
        initBaiduSDK();
        //初始化leacloud
        AVOSCloud.initialize(this,
                "5wk8ccseci7lnss55xfxdgj9xn77hxg3rppsu16o83fydjjn",
                "yovqy5zy16og43zwew8i6qmtkp2y6r9b18zerha0fqi5dqsw");
//        YunBaManager.start(getApplicationContext());


//        YunBaManager.subscribe(getApplicationContext(), new String[]{"e2link/"+setManager.getIMEI()}, new IMqttActionListener() {
//
//            @Override
//            public void onSuccess(IMqttToken arg0) {
//                Log.d(TAG, "Subscribe topic succeed");
//            }
//            @Override
//            public void onFailure(IMqttToken arg0, Throwable arg1) {
//                Log.d(TAG, "Subscribe topic failed");
//            }
//        });

        AVAnalytics.enableCrashReport(this, true);

        MyLog.delFile();
        DBManage.updateDatabase();
    }

    private void initBaiduSDK() {
        SDKInitializer.initialize(this);
    }
}
