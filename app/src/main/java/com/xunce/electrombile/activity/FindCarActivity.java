package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.receiver.MyReceiver;
import com.xunce.electrombile.utils.system.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FindCarActivity extends Activity {
    private static final String status_start = "START";
    private static final String status_stop = "STOP";
    private String status;
    private int secondleft;
    private Timer timer;
    private TextView tv_second;
    private Button btn_continueGetSignal;
    private TextView tv_firstResult;
    private TextView tv_secondResult;
    private TextView tv_thirdResult;
    private MyReceiver receiver;
    private TextView tv_realtime_tensity;
    private MqttConnectManager mqttConnectManager;
    private CmdCenter mCenter;
    private SettingManager settingManager;
    private ArrayList<Integer> intensityList;
    //需要记录采集的次数
    private int count = 0;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            secondleft--;
            if (secondleft <= 0) {
                timer.cancel();
                tv_second.setText("30");
                btn_continueGetSignal.setEnabled(true);
                if (mqttConnectManager.returnMqttStatus()) {
                    mqttConnectManager.sendMessage(mCenter.cmdSeekOff(), settingManager.getIMEI());
                    status = status_stop;
                    tv_realtime_tensity.setText(0+"");
                    int maxIntensity = findMaxIntensity();
                    setIntentsityResult(maxIntensity);
                }
                else{
                    ToastUtils.showShort(FindCarActivity.this,"mqtt连接失败");
                }
            } else {
                tv_second.setText(secondleft+"");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_car);
        UserGuide();
        initView();
    }

    private void initView(){
        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("找车主界面");
        Button btn_back = (Button)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_second = (TextView)findViewById(R.id.tv_second);
        tv_firstResult = (TextView)findViewById(R.id.tv_firstResult);
        tv_secondResult = (TextView)findViewById(R.id.tv_secondResult);
        tv_thirdResult = (TextView)findViewById(R.id.tv_thirdResult);
        btn_continueGetSignal = (Button)findViewById(R.id.btn_continueGetSignal);
        btn_continueGetSignal.setEnabled(false);
        btn_continueGetSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送seek on的命令
                if (mqttConnectManager.returnMqttStatus()) {
                    mqttConnectManager.sendMessage(mCenter.cmdSeekOn(), settingManager.getIMEI());
                    //开始倒计时
                    TimeCountdown();
                    intensityList.clear();
//                    intensityList = new ArrayList<Integer>();
                    //数据采集次数增加
                    count++;
                    status = status_start;
                    btn_continueGetSignal.setEnabled(false);
                }
                else{
                    ToastUtils.showShort(FindCarActivity.this,"mqtt连接失败");
                }
            }
        });
        tv_realtime_tensity = (TextView)findViewById(R.id.tv_realtime_tensity);

        registerBroadCast();
        settingManager = SettingManager.getInstance();
        mqttConnectManager = MqttConnectManager.getInstance();
        mCenter = CmdCenter.getInstance();
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

    private void TimeCountdown(){
        secondleft = 30;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 1000, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void UserGuide(){
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_findcar_guide3, null);
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle_white);

        Button btn_getSignal = (Button)view.findViewById(R.id.btn_getSignal);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);

        //获取屏幕的宽度
        WindowManager m = this.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75);

        btn_getSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();


                if (mqttConnectManager.returnMqttStatus()) {
                    mqttConnectManager.sendMessage(mCenter.cmdSeekOn(), settingManager.getIMEI());
                    //开始倒计时
                    TimeCountdown();
                    intensityList = new ArrayList<Integer>();
                    //第一次数据采集
                    count = 1;
                    status = status_start;
                }
                else{
                    ToastUtils.showShort(FindCarActivity.this,"mqtt连接失败");
                }
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

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG, "find接收调用");
            Bundle bundle = intent.getExtras();
            int data = bundle.getInt("intensity");
            if(status.equals(status_start)){
                tv_realtime_tensity.setText(data+"");
                intensityList.add(data);
            }
        }
    }

    private int findMaxIntensity(){
        if(intensityList.size() == 0){
            return 0;
        }
        int maxIntensity = intensityList.get(0);
        for(int i=1;i<intensityList.size();i++){
            if(intensityList.get(i)>maxIntensity){
                maxIntensity = intensityList.get(i);
            }
        }
        return maxIntensity;
    }

    private void setIntentsityResult(int maxIntensity){
        switch (count){
            case 1:
                tv_firstResult.setText(maxIntensity+"");
                break;
            case 2:
                tv_secondResult.setText(maxIntensity+"");
                break;
            case 3:
                tv_thirdResult.setText(maxIntensity+"");
                break;
            default:
                break;
        }
    }
}
