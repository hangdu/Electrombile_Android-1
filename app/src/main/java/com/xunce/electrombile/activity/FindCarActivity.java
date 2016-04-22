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
import android.widget.RelativeLayout;
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
    private Button btn_continueGetSignal;
    private MyReceiver receiver;
    private MqttConnectManager mqttConnectManager;
    private CmdCenter mCenter;
    private SettingManager settingManager;
    private ArrayList<Integer> intensityList;
    //需要记录采集的次数
    private int count = 0;

    private TextView tv_signalResult1;
    private TextView tv_signalRankNumber1;
    private TextView tv_signalResult2;
    private TextView tv_signalRankNumber2;
    private TextView tv_signalResult3;
    private TextView tv_signalRankNumber3;
    private TextView tv_signalResult4;
    private TextView tv_signalRankNumber4;
    private TextView tv_countdownSecond;
    private TextView tv_intensity;
    private Dialog countdownDialog;
    private Dialog SignalUserguideDialog;
    private TextView tv_signalGetNumber1;
    private TextView tv_signalGetNumber2;
    private TextView tv_signalGetNumber3;
    private TextView tv_signalGetNumber4;

    private TextView tv_AnalyseResult1;
    private TextView tv_AnalyseResult2;
    private TextView tv_AnalyseResult3;

    private Button btn_MoveTenMeter;

    private TextView tv_MoveTenMeter;
    private TextView title;


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            secondleft--;
            if (secondleft <= 0) {
                timer.cancel();
                if (mqttConnectManager.returnMqttStatus()) {
                    mqttConnectManager.sendMessage(mCenter.cmdSeekOff(), settingManager.getIMEI());
                    status = status_stop;
                    int maxIntensity = findMaxIntensity();
                    setIntentsityResult(maxIntensity);

                    countdownDialog.dismiss();
                    tv_countdownSecond.setText(30 + "s");
                    tv_intensity.setText(0+"");
                }
                else{
                    ToastUtils.showShort(FindCarActivity.this, "mqtt连接失败");
                }
            } else {
                tv_countdownSecond.setText(secondleft+"s");
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
        View titleView = findViewById(R.id.ll_button);
        TextView titleTextView = (TextView) titleView.findViewById(R.id.tv_title);
        titleTextView.setText("找车主界面");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View ll_result1 = findViewById(R.id.ll_result1);
        tv_signalGetNumber1 = (TextView)ll_result1.findViewById(R.id.tv_signalGetNumber);
        tv_signalGetNumber1.setText("采集一");
        tv_signalResult1 = (TextView)ll_result1.findViewById(R.id.tv_signalResult);
        tv_signalRankNumber1 = (TextView)ll_result1.findViewById(R.id.tv_signalRankNumber);


        View ll_result2 = findViewById(R.id.ll_result2);
        tv_signalGetNumber2 = (TextView)ll_result2.findViewById(R.id.tv_signalGetNumber);
        tv_signalGetNumber2.setText("采集二");
        tv_signalResult2 = (TextView)ll_result2.findViewById(R.id.tv_signalResult);
        tv_signalRankNumber2 = (TextView)ll_result2.findViewById(R.id.tv_signalRankNumber);

        View ll_result3 = findViewById(R.id.ll_result3);
        tv_signalGetNumber3 = (TextView)ll_result3.findViewById(R.id.tv_signalGetNumber);
        tv_signalGetNumber3.setText("采集三");
        tv_signalResult3 = (TextView)ll_result3.findViewById(R.id.tv_signalResult);
        tv_signalRankNumber3 = (TextView)ll_result3.findViewById(R.id.tv_signalRankNumber);

        View ll_result4 = findViewById(R.id.ll_result4);
        tv_signalGetNumber4 = (TextView)ll_result4.findViewById(R.id.tv_signalGetNumber);
        tv_signalGetNumber4.setText("采集四");
        tv_signalResult4 = (TextView)ll_result4.findViewById(R.id.tv_signalResult);
        tv_signalRankNumber4 = (TextView)ll_result4.findViewById(R.id.tv_signalRankNumber);

        tv_AnalyseResult1 = (TextView)findViewById(R.id.tv_AnalyseResult1);
        tv_AnalyseResult2 = (TextView)findViewById(R.id.tv_AnalyseResult2);
        tv_AnalyseResult3 = (TextView)findViewById(R.id.tv_AnalyseResult3);

        btn_continueGetSignal = (Button)findViewById(R.id.btn_continueGetSignal);
        btn_continueGetSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensityList.clear();

                if(count == 1){
                    getSignalUserguideDialog();
                }else{
                    refreshSignalUserGuide();
                    SignalUserguideDialog.show();
                }
            }
        });

        registerBroadCast();
        settingManager = SettingManager.getInstance();
        mqttConnectManager = MqttConnectManager.getInstance();
        mCenter = CmdCenter.getInstance();
    }

    private void refreshSignalUserGuide(){
        switch (count){
            case 2:
                title.setText("进行第三次数据采集");
                tv_MoveTenMeter.setText("环绕当前点前行10m进行第三次采集!");
                break;
            case 3:
                title.setText("进行第四次数据采集");
                tv_MoveTenMeter.setText("环绕当前点前行10m进行第四次采集!");
                break;

        }
    }


    private void processFindcarResult(){
        //对四次的结果进行排序
        int result1 = Integer.parseInt((String)tv_signalResult1.getText());
        int result2 = Integer.parseInt((String)tv_signalResult2.getText());
        int result3 = Integer.parseInt((String)tv_signalResult3.getText());
        int result4 = Integer.parseInt((String)tv_signalResult4.getText());
        int Result[] = new int[]{result1,result2,result3,result4};

        int maxResult = Result[0];
        int maxPosition = 0;
        for(int i=1;i<4;i++){
            if(Result[i]>maxResult){
                maxResult = Result[i];
                maxPosition = i;
            }
        }
        maxPosition+=1;
        switch (maxPosition){
            case 1:
                tv_signalResult1.setTextColor(Color.parseColor("#1dcf96"));
                tv_signalGetNumber1.setTextColor(Color.parseColor("#1dcf96"));
                break;
            case 2:
                tv_signalResult2.setTextColor(Color.parseColor("#1dcf96"));
                tv_signalGetNumber2.setTextColor(Color.parseColor("#1dcf96"));
                break;
            case 3:
                tv_signalResult3.setTextColor(Color.parseColor("#1dcf96"));
                tv_signalGetNumber3.setTextColor(Color.parseColor("#1dcf96"));
                break;
            case 4:
                tv_signalResult4.setTextColor(Color.parseColor("#1dcf96"));
                tv_signalGetNumber4.setTextColor(Color.parseColor("#1dcf96"));
                break;
        }

        tv_AnalyseResult1.setText("刚刚采集最大数值为"+maxResult);
        tv_AnalyseResult2.setText("您的爱车就在第"+maxPosition+"次采集点附近");
        tv_AnalyseResult3.setText("请到第"+maxPosition+"次采集点附近再更加精确查找");
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
                    CountDownDialog();
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


    private void CountDownDialog(){
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_countdown, null);
        countdownDialog = new Dialog(this, R.style.Translucent_NoTitle_white);
        countdownDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失

        tv_countdownSecond = (TextView)view.findViewById(R.id.tv_countdownSecond);
        tv_intensity = (TextView)view.findViewById(R.id.tv_intensity);

        //获取屏幕的宽度
        WindowManager m = this.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75);

        countdownDialog.addContentView(view, new LinearLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
        countdownDialog.show();
    }

    private void getSignalUserguideDialog(){
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_getsignal_userguide, null);
        SignalUserguideDialog = new Dialog(this, R.style.Translucent_NoTitle_white);
        SignalUserguideDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失

        btn_MoveTenMeter = (Button)view.findViewById(R.id.btn_MoveTenMeter);
        btn_MoveTenMeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignalUserguideDialog.dismiss();

                //发送seek on的命令
                if (mqttConnectManager.returnMqttStatus()) {
                    mqttConnectManager.sendMessage(mCenter.cmdSeekOn(), settingManager.getIMEI());
                    countdownDialog.show();
                    TimeCountdown();
                    //数据采集次数增加
                    count++;
                    status = status_start;
                }
                else{
                    ToastUtils.showShort(FindCarActivity.this,"mqtt连接失败");
                }
            }
        });

        tv_MoveTenMeter = (TextView)view.findViewById(R.id.tv_MoveTenMeter);
        title = (TextView)view.findViewById(R.id.title);

        //获取屏幕的宽度
        WindowManager m = this.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75);

        SignalUserguideDialog.addContentView(view, new LinearLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
        SignalUserguideDialog.show();
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
                tv_signalResult1.setText(maxIntensity+"");
                break;

            case 2:
                tv_signalResult2.setText(maxIntensity+"");
                break;

            case 3:
                tv_signalResult3.setText(maxIntensity+"");
                break;

            case 4:
                tv_signalResult4.setText(maxIntensity+"");
                btn_continueGetSignal.setVisibility(View.INVISIBLE);
                processFindcarResult();
                break;

            default:
                break;
        }
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


    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG, "find接收调用");
            Bundle bundle = intent.getExtras();
            int data = bundle.getInt("intensity");
            if(status.equals(status_start)){
                intensityList.add(data);

                tv_intensity.setText(data+"");
            }
        }
    }
}
