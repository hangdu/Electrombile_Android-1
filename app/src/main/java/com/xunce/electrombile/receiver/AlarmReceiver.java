package com.xunce.electrombile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Constants.ActivityConstants;
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.activity.AlarmActivity;
import com.xunce.electrombile.utils.device.DeviceUtils;
import com.xunce.electrombile.utils.useful.JSONUtils;

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
        Bundle bundle = intent.getExtras();
        callbackStatus = bundle.get(ActivityConstants.callbackStatus).toString();
        callbackAction = bundle.get(ActivityConstants.callbackAction).toString();
        if (ActivityConstants.OK.equals(callbackStatus)) {
            if (callbackAction.equals(ActivityConstants.messageArrived)) {
                destinationName = bundle.get(ActivityConstants.destinationName).toString();
                if (destinationName.contains("alarm")) {
                    String s = bundle.get(ActivityConstants.PARCEL).toString();
                    int type = Integer.parseInt(JSONUtils.ParseJSON(s, "type"));
                    LogUtil.log.i("创建报警界面:" + bundle.toString());
                    // DeviceUtils.showNotifation(context, topic, msg);
                    DeviceUtils.wakeUpAndUnlock(context);
                    Intent intentMy = new Intent(context, AlarmActivity.class);
                    intentMy.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentMy.putExtra(ProtocolConstants.TYPE, type);
                    context.startActivity(intentMy);
                }
            }
        }
    }


}