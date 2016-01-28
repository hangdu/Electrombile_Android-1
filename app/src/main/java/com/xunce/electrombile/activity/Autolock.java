package com.xunce.electrombile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xunce.electrombile.R;
import com.xunce.electrombile.fragment.SettingsFragment;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lybvinci on 16/1/12.
 */
public class Autolock extends BaseActivity{
    private RadioGroup radioGroup_switch;
    private RadioGroup radioGroup_locktime;
    private RadioButton rb_switchstatus;
    private RadioButton rb_autolockTime;
    private SettingManager settingManager;

    private RadioButton rb_switchOpen;
    private RadioButton rb_switchClose;
    private RadioButton rb_Open5min;
    private RadioButton rb_Open10min;
    private RadioButton rb_Open15min;

    RelativeLayout relative_locktime;
    MqttConnectManager mqttConnectManager;
    public CmdCenter mCenter;
    static public int period;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.autolock);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews(){
        settingManager = new SettingManager(Autolock.this);
        radioGroup_switch = (RadioGroup)findViewById(R.id.RadioGroup_switch);
        radioGroup_locktime = (RadioGroup)findViewById(R.id.RadioGroup_locktime);
        rb_switchOpen = (RadioButton)findViewById(R.id.rb_openautolock);
        rb_switchClose = (RadioButton)findViewById(R.id.rb_closeautolock);
        rb_Open5min = (RadioButton)findViewById(R.id.rb_5min);
        rb_Open10min = (RadioButton)findViewById(R.id.rb_10min);
        rb_Open15min = (RadioButton)findViewById(R.id.rb_15min);
        relative_locktime = (RelativeLayout) findViewById(R.id.relative_locktime);

        initRadioButton();

        mqttConnectManager = MqttConnectManager.getInstance();
        mCenter = CmdCenter.getInstance(Autolock.this);

        radioGroup_switch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                rb_switchstatus = (RadioButton) findViewById(checkedId);
                Toast.makeText(Autolock.this, rb_switchstatus.getText(), Toast.LENGTH_SHORT);
                String s = (String) rb_switchstatus.getText();

                if (s.equals("开启")) {
                    relative_locktime.setVisibility(View.VISIBLE);
                    rb_Open5min.setChecked(true);
                    period = 5;
                    //向服务器发消息
                    if(mqttConnectManager.returnMqttStatus()){
                        mqttConnectManager.sendMessage(Autolock.this,mCenter.cmdAutolockOn(),settingManager.getIMEI());
                    }
                    else{
                        ToastUtils.showShort(Autolock.this,"mqtt连接失败");
                    }
                }
                else if(s.equals("关闭")){
                    mqttConnectManager.sendMessage(Autolock.this,mCenter.cmdAutolockOff(),settingManager.getIMEI());
                    relative_locktime.setVisibility(View.INVISIBLE);
                }
            }
        });

        radioGroup_locktime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                rb_autolockTime = (RadioButton) findViewById(checkedId);
                int Autolockperiod = findLockTime();
                if(mqttConnectManager.returnMqttStatus()){
                    period = Autolockperiod;
                    mqttConnectManager.sendMessage(Autolock.this,mCenter.cmdAutolockTimeSet(Autolockperiod),settingManager.getIMEI());
                }
                else{
                    ToastUtils.showShort(Autolock.this,"mqtt连接失败");
                }
            }
        });
    }

    @Override
    public void initEvents(){

    }

    private int findLockTime(){
        String s = (String)rb_autolockTime.getText();
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(s);
        String result = m.replaceAll("").trim();
        return Integer.parseInt(result);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    //初始化落锁状态
    private void initRadioButton(){
        Boolean AutoLockStatus = settingManager.getAutoLockStatus();
        if(AutoLockStatus == false){
            rb_switchClose.setChecked(true);
            //自动落锁关闭的情况下   落锁时间的设置就隐藏掉
            relative_locktime.setVisibility(View.INVISIBLE);
        }
        else{
            //开启状态
            rb_switchOpen.setChecked(true);
            int LockTime = settingManager.getAutoLockTime();
            switch(LockTime){
                case 5:
                    rb_Open5min.setChecked(true);
                    break;
                case 10:
                    rb_Open10min.setChecked(true);
                    break;
                case 15:
                    rb_Open15min.setChecked(true);
                    break;
            }
        }
    }
}
