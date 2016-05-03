package com.xunce.electrombile.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.utils.device.VibratorUtil;
import com.xunce.electrombile.view.SlidingButton;

import java.text.SimpleDateFormat;


/**
 * Created by heyukun on 2015/4/3.
 * Modify by liyanbo on 2015/10/27
 */
public class AlarmActivity extends BaseActivity {
    MediaPlayer mPlayer;
//    private MqttAndroidClient mac;
    private SlidingButton mSlidingButton;
    private TextView tv_sliding;
    private TextView tv_alarm;
    private Animation operatingAnim;
    private MqttConnectManager mqttConnectManager;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            VibratorUtil.VibrateCancle(AlarmActivity.this);
            mPlayer.stop();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_alarm);
        super.onCreate(savedInstanceState);
        alarm();
//        Intent intent = getIntent();
//        int type = intent.getIntExtra(ProtocolConstants.TYPE, 2);
        mqttConnectManager = MqttConnectManager.getInstance();
        //       int type = savedInstanceState.getInt("type");
    }

    private void alarm() {
        VibratorUtil.Vibrate(this, new long[]{1000, 2000, 2000, 1000}, true);
        //播放警铃
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mPlayer.setLooping(true);
        mPlayer.start();
        Message msg = Message.obtain();
        mHandler.sendMessageDelayed(msg,1000*60);
    }

    @Override
    public void initViews() {
        mSlidingButton = (SlidingButton) this.findViewById(R.id.mainview_answer_1_button);
        tv_sliding = (TextView) findViewById(R.id.tv_sliding);
        tv_alarm = (TextView) findViewById(R.id.tv_alarm);
    }
    @Override
    public void initEvents() {
//        startMqttClient();
//        mac = mqttConnectManager.getMac();
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        tv_alarm.startAnimation(operatingAnim);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mqttConnectManager.sendMessage(mCenter.cmdFenceOff(), setManager.getIMEI());
        mPlayer.stop();
        VibratorUtil.VibrateCancle(AlarmActivity.this);
        AlarmActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 获得mqtt连接
     */
//    private void startMqttClient() {
//        if (ServiceConstants.handler.isEmpty()) {
//            return;
//        }
//        Connection connection = Connections.getInstance(this).getConnection(ServiceConstants.handler);
//        mac = connection.getClient();
//    }

//    /**
//     * 发送命令
//     *
//     * @param context 上下文
//     * @param message 要发送的信息
//     * @param IMEI    设备号
//     */
//    private void sendMessage(Context context, byte[] message, String IMEI) {
//        if (mac != null && !mac.isConnected()) {
//            ToastUtils.showShort(context, "请先连接设备，或等待连接。");
//            return;
//        }
//        try {
//            mac.publish("app2dev/" + IMEI + "/cmd", message, ServiceConstants.MQTT_QUALITY_OF_SERVICE, false);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSlidingButton.handleActivityEvent(event)) {
            //stop alarm
            tv_sliding.setText("停止告警！");
            tv_alarm.clearAnimation();
            VibratorUtil.VibrateCancle(AlarmActivity.this);
            mPlayer.stop();
            AlarmActivity.this.finish();
            mqttConnectManager.sendMessage(mCenter.cmdFenceOff(),setManager.getIMEI());
        } else {
            tv_sliding.setText("滑动关闭报警");
            tv_alarm.startAnimation(operatingAnim);
        }
        return super.onTouchEvent(event);
    }
}
