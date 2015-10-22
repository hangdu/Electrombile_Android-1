package com.xunce.electrombile.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.device.VibratorUtil;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;


/**
 * Created by heyukun on 2015/4/3.
 */
public class AlarmActivity extends BaseActivity {
    ToggleButton btnWarmComfirm = null;
    AudioManager aManager = null;
    MediaPlayer mPlayer;
    private MqttAndroidClient mac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        VibratorUtil.Vibrate(this, 60000);

        //播放警铃
        mPlayer= MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mPlayer.setLooping(true);
        mPlayer.start();
        startMqttClient();
        btnWarmComfirm = (ToggleButton) findViewById(R.id.btn_warning_confirm);
        btnWarmComfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    //stop alarm
                    VibratorUtil.VibrateCancle(AlarmActivity.this);
                    mPlayer.stop();
                    AlarmActivity.this.finish();
                    //AlarmActivity.instance = null;
                    sendMessage(AlarmActivity.this, mCenter.cmdFenceOff(), setManager.getIMEI());
                }
            }
        });

    }

    private void startMqttClient() {
        if (ServiceConstants.handler.isEmpty()) {
            return;
        }
        Connection connection = Connections.getInstance(this).getConnection(ServiceConstants.handler);
        mac = connection.getClient();
    }

    private void sendMessage(Context context, byte[] message, String IMEI) {
        if (mac != null && !mac.isConnected()) {
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
    public void onBackPressed() {
        super.onBackPressed();
        mPlayer.stop();
        VibratorUtil.VibrateCancle(AlarmActivity.this);
        AlarmActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
