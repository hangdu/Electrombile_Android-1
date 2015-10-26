package com.xunce.electrombile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Constants.ActivityConstants;
import com.xunce.electrombile.activity.AlarmActivity;
import com.xunce.electrombile.utils.device.DeviceUtils;

/**
 * Created by lybvinci on 2015/5/1.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private String destinationName;
    private String callbackStatus;
    private String callbackAction;

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "先进入报警Receiver");
        Bundle bundle = intent.getExtras();
        callbackStatus = bundle.get(ActivityConstants.callbackStatus).toString();
        callbackAction = bundle.get(ActivityConstants.callbackAction).toString();
        if (ActivityConstants.OK.equals(callbackStatus)) {
            if (callbackAction.equals(ActivityConstants.messageArrived)) {
                destinationName = bundle.get(ActivityConstants.destinationName).toString();
                if (destinationName.contains("alarm")) {
                    LogUtil.log.i("创建报警界面");
                    // DeviceUtils.showNotifation(context, topic, msg);
                    abortBroadcast();
                    DeviceUtils.wakeUpAndUnlock(context);
                    Intent intentMy = new Intent(context, AlarmActivity.class);
                    intentMy.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentMy);
                }
            }
        }
    }


}