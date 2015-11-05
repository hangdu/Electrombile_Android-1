package com.xunce.electrombile.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class FindActivity extends BaseActivity {

    private static final String TAG = "FindActivity";
    private final int TIME_OUT = 0;
    private boolean isFinding = false;
    private RatingBar ratingBar;
    Handler refreshRatingBar = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            float next = (float) msg.obj;
            Log.e("", "next:" + next);
            ratingBar.setRating(next);
        }
    };
    private String IMEI;
    private ImageView scanner;
    private ProgressDialog progressDialog;
    private CmdCenter mCenter;
    private MyReceiver receiver;
    private Animation operatingAnim;
    private Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDialog.dismiss();
            ToastUtils.showShort(FindActivity.this, "指令下发失败，请检查网络和设备工作是否正常。");
        }
    };
    private MqttAndroidClient mac;
    private Button findBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_find);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        scanner = (ImageView) findViewById(R.id.iv_scanner);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        findBtn = (Button) findViewById(R.id.find_button);
        progressDialog = new ProgressDialog(this);
        mCenter = CmdCenter.getInstance(this);
    }

    @Override
    public void initEvents() {
        startMqttClient();
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        progressDialog.setMessage("正在配置...");
        progressDialog.setCancelable(false);
        registerBroadCast();
        IMEI = setManager.getIMEI();
    }

    private void startMqttClient() {
        if (ServiceConstants.handler.isEmpty()) {
            return;
        }
        Connection connection = Connections.getInstance(this).getConnection(ServiceConstants.handler);
        mac = connection.getClient();
    }
    private void registerBroadCast() {
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.xunce.electrombile.find");
        try {
            registerReceiver(receiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startFind(View view) {
        timeHandler.sendEmptyMessageDelayed(TIME_OUT, 5000);
        if (!isFinding) {
            if (mac != null && mac.isConnected()) {
                sendMessage(FindActivity.this, mCenter.cmdSeekOn(), IMEI);
                progressDialog.show();
            } else {
                ToastUtils.showShort(this, "服务未连接...");
                timeHandler.removeMessages(TIME_OUT);
                return;
            }
            isFinding = !isFinding;
        } else {
            if (mac != null && mac.isConnected()) {
                sendMessage(FindActivity.this, mCenter.cmdSeekOff(), IMEI);
                progressDialog.show();
            } else {
                ToastUtils.showShort(this, "服务未连接...");
                timeHandler.removeMessages(TIME_OUT);
                return;
            }
            isFinding = !isFinding;
        }

    }

    private void sendMessage(Context context, byte[] message, String IMEI) {
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //取消等待框，并且刷新界面
    private void cancelDialog(int data) {
        timeHandler.removeMessages(TIME_OUT);
        progressDialog.dismiss();
        float rating = (float) (data / 200.0);
        ratingBar.setRating(rating);
        if (isFinding) {
            if (operatingAnim != null) {
                scanner.startAnimation(operatingAnim);
            }
            findBtn.setText("停止找车");
        } else {
            scanner.clearAnimation();
            findBtn.setText("开始找车");
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "find接收调用");
            Bundle bundle = intent.getExtras();
            int data = bundle.getInt("intensity");
            Log.i(TAG, data + "");
            cancelDialog(data);
        }
    }

}
