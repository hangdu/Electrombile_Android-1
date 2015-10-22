package com.xunce.electrombile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Constants.ActivityConstants;
import com.xunce.electrombile.activity.AlarmActivity;
import com.xunce.electrombile.utils.device.DeviceUtils;

//import io.yunba.android.manager.YunBaManager;

/**
 * Created by lybvinci on 2015/5/1.
 */
public class YunBaReceiver extends BroadcastReceiver {

    private String destinationName;
    public YunBaReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
//        String msg = intent.getStringExtra(YunBaManager.MQTT_MSG);
        //在这里处理从服务器发布下来的消息， 比如显示通知栏， 打开 Activity 等等
//        if(AlarmActivity.instance == null) {
        Bundle bundle = intent.getExtras();
        destinationName = bundle.get(ActivityConstants.destinationName).toString();
        if (destinationName.equals("topic")) {
            LogUtil.log.i("创建报警界面了么？？？");
            // DeviceUtils.showNotifation(context, topic, msg);
            abortBroadcast();
            DeviceUtils.wakeUpAndUnlock(context);
            Intent intentMy = new Intent(context, AlarmActivity.class);
            intentMy.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentMy);
        }
//        }

    }


}